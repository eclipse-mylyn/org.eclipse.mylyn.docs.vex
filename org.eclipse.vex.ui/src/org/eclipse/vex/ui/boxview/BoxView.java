/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.boxview;

import static org.eclipse.vex.core.internal.boxes.BoxFactory.frame;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.paragraph;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.rootBox;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.staticText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;

import java.util.TreeSet;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.ContentMap;
import org.eclipse.vex.core.internal.boxes.Cursor;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget.ILayoutListener;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget.IPaintingListener;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxView extends ViewPart {

	private static final FontSpec TIMES_NEW_ROMAN = new FontSpec("Times New Roman", FontSpec.PLAIN, 20.0f);

	private static final String LOREM_IPSUM_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.";

	private Composite parent;
	private BoxWidget boxWidget;
	private ContentMap contentMap;
	private Cursor cursor;

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		contentMap = new ContentMap();
		cursor = new Cursor(contentMap);
		refresh();
	}

	@Override
	public void dispose() {
		super.dispose();
		boxWidget = null;
	}

	@Override
	public void setFocus() {
		boxWidget.setFocus();
	}

	public void refresh() {
		if (boxWidget != null) {
			boxWidget.dispose();
			boxWidget = null;
			cleanStaleReferenceInShell();
		}
		boxWidget = new BoxWidget(parent, SWT.V_SCROLL);
		boxWidget.addLayoutListener(new ILayoutListener() {
			private long startTime;

			@Override
			public void layoutStarting(final Graphics graphics) {
				System.out.print("Layout ");
				startTime = System.currentTimeMillis();
			}

			@Override
			public void layoutFinished(final Graphics graphics) {
				final long duration = System.currentTimeMillis() - startTime;
				System.out.println("took " + duration + "ms");
			}

		});
		boxWidget.addPaintingListener(new IPaintingListener() {
			private long startTime;

			@Override
			public void paintingStarting(final Graphics graphics) {
				System.out.print("Painting ");
				startTime = System.currentTimeMillis();
			}

			@Override
			public void paintingFinished(final Graphics graphics) {
				final long duration = System.currentTimeMillis() - startTime;
				System.out.println("took " + duration + "ms");

				cursor.paint(graphics);
			}

		});
		boxWidget.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent e) {
				// ignore
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.keyCode) {
				case SWT.ARROW_LEFT:
					cursor.setPosition(Math.max(0, cursor.getPosition() - 1));
					boxWidget.invalidate();
					break;
				case SWT.ARROW_RIGHT:
					cursor.setPosition(cursor.getPosition() + 1);
					boxWidget.invalidate();
					break;
				default:
					break;
				}
			}
		});

		final RootBox testModel = createTestModel();
		boxWidget.setContent(testModel);
		contentMap.setRootBox(testModel);
		// cursor.setPosition(0);
		parent.layout();
	}

	private void cleanStaleReferenceInShell() {
		/*
		 * Shell keeps a reference to the boxWidget in Shell.savedFocus. parent.setFocus() forces Shell to store a
		 * reference to parent instead.
		 */
		parent.setFocus();
	}

	private RootBox createTestModel() {
		final Document document = createTestDocument();

		final VisualizationChain visualizationChain = buildVisualizationChain();

		return visualizationChain.visualizeRoot(document);
	}

	private static Document createTestDocument() {
		final Document document = new Document(new QualifiedName(null, "doc"));
		for (int i = 0; i < 25; i += 1) {
			insertSection(document.getRootElement());
		}
		return document;
	}

	private static void insertSection(final IParent parent) {
		final IDocument document = parent.getDocument();
		final IElement section = document.insertElement(parent.getEndOffset(), new QualifiedName(null, "section"));
		insertParagraph(section);
		insertEmptyParagraph(section);
	}

	private static void insertParagraph(final IParent parent) {
		final IDocument document = parent.getDocument();
		final IElement textElement = document.insertElement(parent.getEndOffset(), new QualifiedName(null, "para"));
		document.insertText(textElement.getEndOffset(), LOREM_IPSUM_LONG);
	}

	private static void insertEmptyParagraph(final IParent parent) {
		final IDocument document = parent.getDocument();
		document.insertElement(parent.getEndOffset(), new QualifiedName(null, "para"));
	}

	private static VisualizationChain buildVisualizationChain() {
		final VisualizationChain visualizationChain = new VisualizationChain();
		visualizationChain.addForRoot(new DocumentRootVisualization());
		visualizationChain.addForStructure(new ParagraphVisualization());
		visualizationChain.addForStructure(new StructureElementVisualization());
		visualizationChain.addForInline(new TextVisualization());
		return visualizationChain;
	}

	private static final class DocumentRootVisualization extends NodeVisualization<RootBox> {
		@Override
		public RootBox visit(final IDocument document) {
			final RootBox rootBox = rootBox();
			final NodeReference rootReference = new NodeReference();
			rootReference.setNode(document);
			rootBox.appendChild(rootReference);

			final VerticalBlock rootChildren = verticalBlock();
			visualizeChildrenStructure(document.children(), rootChildren);

			rootReference.setComponent(rootChildren);
			return rootBox;
		}
	}

	private static final class StructureElementVisualization extends NodeVisualization<IChildBox> {
		@Override
		public IChildBox visit(final IElement element) {
			final NodeReference elementReference = new NodeReference();
			elementReference.setNode(element);

			final VerticalBlock component = verticalBlock();
			visualizeChildrenStructure(element.children(), component);

			elementReference.setComponent(frame(component, Margin.NULL, Border.NULL, new Padding(3, 3)));
			return elementReference;
		}
	}

	private static final class ParagraphVisualization extends NodeVisualization<IChildBox> {
		public ParagraphVisualization() {
			super(1);
		}

		@Override
		public IChildBox visit(final IElement element) {
			if (!"para".equals(element.getLocalName())) {
				return super.visit(element);
			}

			final Paragraph paragraph = paragraph();
			if (element.hasChildren()) {
				visualizeChildrenInline(element.children(), paragraph);
			} else {
				visualizeEmptyParagraph(element, paragraph);
			}
			final NodeReference nodeReference = new NodeReference();
			nodeReference.setComponent(frame(paragraph, Margin.NULL, Border.NULL, new Padding(5, 4)));
			nodeReference.setNode(element);
			nodeReference.setCanContainText(true);
			return nodeReference;
		}

		private void visualizeEmptyParagraph(final IElement element, final Paragraph paragraph) {
			paragraph.appendChild(staticText(" ", TIMES_NEW_ROMAN));
		}
	}

	private static final class TextVisualization extends NodeVisualization<IInlineBox> {
		@Override
		public IInlineBox visit(final IText text) {
			final TextContent result = new TextContent();
			result.setContent(text.getContent(), text.getRange());
			result.setFont(TIMES_NEW_ROMAN);
			return result;
		}
	}

	private static class NodeVisualization<T extends IBox> extends BaseNodeVisitorWithResult<T> implements Comparable<NodeVisualization<?>> {
		private final int priority;
		private VisualizationChain chain;

		public NodeVisualization() {
			this(0);
		}

		public NodeVisualization(final int priority) {
			this.priority = priority;
		}

		public final T visualize(final INode node) {
			return node.accept(this);
		}

		@Override
		public final int compareTo(final NodeVisualization<?> other) {
			return other.priority - priority;
		}

		public final void setChain(final VisualizationChain chain) {
			this.chain = chain;
		}

		protected final void visualizeChildrenStructure(final Iterable<INode> children, final IParentBox<IChildBox> parentBox) {
			for (final INode child : children) {
				final IChildBox childBox = chain.visualizeStructure(child);
				if (childBox != null) {
					parentBox.appendChild(childBox);
				}
			}
		}

		protected final void visualizeChildrenInline(final Iterable<INode> children, final IParentBox<IInlineBox> parentBox) {
			for (final INode child : children) {
				final IInlineBox childBox = chain.visualizeInline(child);
				if (childBox != null) {
					parentBox.appendChild(childBox);
				}
			}
		}
	}

	private static final class VisualizationChain {
		private final TreeSet<NodeVisualization<RootBox>> rootChain = new TreeSet<NodeVisualization<RootBox>>();
		private final TreeSet<NodeVisualization<IChildBox>> structureChain = new TreeSet<NodeVisualization<IChildBox>>();
		private final TreeSet<NodeVisualization<IInlineBox>> inlineChain = new TreeSet<NodeVisualization<IInlineBox>>();

		public RootBox visualizeRoot(final INode node) {
			return visualize(node, rootChain);
		}

		public IChildBox visualizeStructure(final INode node) {
			return visualize(node, structureChain);
		}

		public IInlineBox visualizeInline(final INode node) {
			return visualize(node, inlineChain);
		}

		private static <T extends IBox> T visualize(final INode node, final TreeSet<NodeVisualization<T>> chain) {
			for (final NodeVisualization<T> visualization : chain) {
				final T box = visualization.visualize(node);
				if (box != null) {
					return box;
				}
			}
			return null;
		}

		public void addForRoot(final NodeVisualization<RootBox> visualization) {
			add(visualization, rootChain);
		}

		public void addForStructure(final NodeVisualization<IChildBox> visualization) {
			add(visualization, structureChain);
		}

		public void addForInline(final NodeVisualization<IInlineBox> visualization) {
			add(visualization, inlineChain);
		}

		private <T extends IBox> void add(final NodeVisualization<T> visualization, final TreeSet<NodeVisualization<T>> chain) {
			chain.add(visualization);
			visualization.setChain(this);
		}
	}
}
