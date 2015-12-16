/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.visualization;

import static org.eclipse.vex.core.internal.boxes.BoxFactory.frame;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.inlineContainer;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReference;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReferenceWithText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.paragraph;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.rootBox;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.staticText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.textContent;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.CollectingNodeTraversal;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * @author Florian Thienel
 */
public class CSSBasedBoxModelBuilder implements IBoxModelBuilder {

	private final StyleSheet styleSheet;

	public CSSBasedBoxModelBuilder(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public RootBox visualizeRoot(final INode node) {
		final IDocument document = node.getDocument();
		return rootBox(asStructuralBox(visualize(document)));
	}

	@Override
	public IStructuralBox visualizeStructure(final INode node) {
		return asStructuralBox(visualize(node));
	}

	private <P extends IParentBox<IStructuralBox>> P visualizeChildrenAsStructure(final Iterable<VisualizeResult> childrenResults, final P parentBox) {
		for (final VisualizeResult visualizeResult : childrenResults) {
			parentBox.appendChild(asStructuralBox(visualizeResult));
		}
		return parentBox;
	}

	private IStructuralBox asStructuralBox(final VisualizeResult visualizeResult) {
		if (visualizeResult.inline) {
			return visualizeStructure(visualizeResult.node, visualizeResult.childrenResults);
		} else {
			return visualizeResult.structuralBox;
		}
	}

	@Override
	public IInlineBox visualizeInline(final INode node) {
		return asInlineBox(visualize(node));
	}

	private <P extends IParentBox<IInlineBox>> P visualizeChildrenAsInline(final Iterable<VisualizeResult> childrenResults, final P parentBox) {
		for (final VisualizeResult visualizeResult : childrenResults) {
			parentBox.appendChild(asInlineBox(visualizeResult));
		}
		return parentBox;
	}

	private IInlineBox asInlineBox(final VisualizeResult visualizeResult) {
		if (visualizeResult.inline) {
			return visualizeResult.inlineBox;
		} else {
			return visualizeInline(visualizeResult.node, visualizeResult.childrenResults);
		}
	}

	/*
	 * Traverse, coarse decision depending on "display" property, collect
	 */

	private VisualizeResult visualize(final INode node) {
		return node.accept(new CollectingNodeTraversal<VisualizeResult>() {
			@Override
			public VisualizeResult visit(final IDocument document) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(document);
				return new VisualizeResult(document, childrenResults, nodeReference(document, visualizeChildrenAsStructure(childrenResults, verticalBlock())));
			}

			@Override
			public VisualizeResult visit(final IDocumentFragment documentFragment) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(documentFragment);
				return new VisualizeResult(documentFragment, childrenResults, nodeReference(documentFragment, visualizeChildrenAsStructure(childrenResults, verticalBlock())));
			}

			@Override
			public VisualizeResult visit(final IElement element) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(element);
				final Styles styles = styleSheet.getStyles(element);
				if (isDisplayedAsBlock(element, styles)) {
					return new VisualizeResult(element, childrenResults, visualizeStructure(element, childrenResults));
				} else {
					return new VisualizeResult(element, childrenResults, visualizeInline(element, childrenResults));
				}
			}

			@Override
			public VisualizeResult visit(final IText text) {
				final List<VisualizeResult> childrenResults = Collections.<VisualizeResult> emptyList();
				return new VisualizeResult(text, childrenResults, visualizeInline(text, childrenResults));
			}
		});
	}

	private static boolean isDisplayedAsBlock(final IElement element, final Styles styles) {
		return CSS.BLOCK.equals(styles.getDisplay()); // TODO provide real implementation
	}

	/*
	 * Render as Block
	 */

	private IStructuralBox visualizeStructure(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<IStructuralBox>() {
			@Override
			public IStructuralBox visit(final IElement element) {
				if (containsInlineContent(childrenResults)) {
					final StructuralFrame framedContent = frame(visualizeParagraphElementContent(styles, childrenResults), Margin.NULL, Border.NULL, getPadding(styles));
					if (mayContainText(element)) {
						return nodeReferenceWithText(element, framedContent);
					} else {
						return nodeReference(element, framedContent);
					}
				} else {
					if (mayContainText(element)) {
						final StructuralFrame framedContent = frame(visualizeParagraphElementContent(styles, childrenResults), Margin.NULL, Border.NULL, getPadding(styles));
						return nodeReferenceWithText(element, framedContent);
					} else {
						return nodeReference(element, frame(visualizeChildrenAsStructure(childrenResults, verticalBlock()), Margin.NULL, Border.NULL, getPadding(styles)));
					}
				}
			}
		});
	}

	private static boolean containsInlineContent(final Collection<VisualizeResult> visualizeResults) {
		for (final VisualizeResult visualizeResult : visualizeResults) {
			if (visualizeResult.inline) {
				return true;
			}
		}
		return false;
	}

	private static boolean mayContainText(final IElement element) {
		final Set<QualifiedName> validItems = element.getDocument().getValidator().getValidItems(element);
		return validItems.contains(IValidator.PCDATA);
	}

	// "Paragraph" is a special case of "Block"

	private Paragraph visualizeParagraphElementContent(final Styles styles, final Collection<VisualizeResult> childrenResults) {
		if (!childrenResults.isEmpty()) {
			return visualizeParagraphWithChildren(childrenResults);
		} else {
			return visualizeEmptyParagraph(styles);
		}
	}

	private Paragraph visualizeParagraphWithChildren(final Collection<VisualizeResult> childrenResults) {
		final Paragraph paragraph = paragraph();
		visualizeChildrenAsInline(childrenResults, paragraph);
		return paragraph;
	}

	private Paragraph visualizeEmptyParagraph(final Styles styles) {
		final Paragraph paragraph = paragraph();
		paragraph.appendChild(placeholderForEmptyElement(styles));
		return paragraph;
	}

	/*
	 * Render inline elements
	 */

	private IInlineBox visualizeInline(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<IInlineBox>() {
			@Override
			public IInlineBox visit(final IElement element) {
				return nodeReferenceWithText(element, frame(visualizeInlineElementContent(styles, childrenResults), Margin.NULL, Border.NULL, getPadding(styles)));
			}

			@Override
			public IInlineBox visit(final IText text) {
				return textContent(text.getContent(), text.getRange(), styles.getFont());
			}
		});
	}

	private InlineContainer visualizeInlineElementContent(final Styles styles, final Collection<VisualizeResult> childrenResults) {
		final InlineContainer container = inlineContainer();
		if (!childrenResults.isEmpty()) {
			visualizeChildrenAsInline(childrenResults, container);
		} else {
			container.appendChild(placeholderForEmptyElement(styles));
		}
		return container;
	}

	private StaticText placeholderForEmptyElement(final Styles styles) {
		return staticText(" ", styles.getFont());
	}

	private static Padding getPadding(final Styles styles) {
		final int top = styles.getPaddingTop().get(1);
		final int left = styles.getPaddingLeft().get(1);
		final int bottom = styles.getPaddingBottom().get(1);
		final int right = styles.getPaddingRight().get(1);

		return new Padding(top, left, bottom, right);
	}

	private static class VisualizeResult {
		public final INode node;
		public final Collection<VisualizeResult> childrenResults;
		public final boolean inline;
		public final IInlineBox inlineBox;
		public final IStructuralBox structuralBox;

		public VisualizeResult(final INode node, final Collection<VisualizeResult> childrenResults, final IStructuralBox box) {
			this.node = node;
			this.childrenResults = childrenResults;
			inline = false;
			inlineBox = null;
			structuralBox = box;
		}

		public VisualizeResult(final INode node, final Collection<VisualizeResult> childrenResults, final IInlineBox box) {
			this.node = node;
			this.childrenResults = childrenResults;
			inline = true;
			inlineBox = box;
			structuralBox = null;
		}
	}

}
