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
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;

import java.util.TreeSet;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxView extends ViewPart {

	private static final FontSpec TIMES_NEW_ROMAN = new FontSpec(new String[] { "Times New Roman" }, 0, 20.0f);

	private static final String LOREM_IPSUM_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.";

	private Composite parent;
	private BoxWidget boxWidget;

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
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
		boxWidget.setContent(createTestModel());
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
		for (int i = 0; i < 25000; i += 1) {
			insertParagraph(document);
		}
		return document;
	}

	private static void insertParagraph(final Document document) {
		final Element textElement = document.insertElement(document.getRootElement().getEndOffset(), new QualifiedName(null, "para"));
		document.insertText(textElement.getEndOffset(), LOREM_IPSUM_LONG);
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
			final RootBox result = rootBox();
			visualizeChildrenStructure(document.children(), result);
			return result;
		}
	}

	private static final class StructureElementVisualization extends NodeVisualization<IChildBox> {
		@Override
		public IChildBox visit(final IElement element) {
			final VerticalBlock component = verticalBlock();
			visualizeChildrenStructure(element.children(), component);
			return frame(component);
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
			visualizeChildrenInline(element.children(), paragraph);
			return frame(paragraph);
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
