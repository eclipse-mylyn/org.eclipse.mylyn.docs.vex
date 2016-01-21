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

import static org.eclipse.vex.core.internal.boxes.BoxFactory.inlineContainer;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReference;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReferenceWithInlineContent;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReferenceWithText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.rootBox;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.endOffsetPlaceholder;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.endTag;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.frame;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.nodeTag;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.paragraph;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.startTag;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.staticText;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.textContent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.Image;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.boxes.LineWrappingRule;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.IPropertyContent;
import org.eclipse.vex.core.internal.css.IPropertyContentVisitor;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.css.Styles.PseudoElement;
import org.eclipse.vex.core.internal.css.TextualContent;
import org.eclipse.vex.core.internal.css.URIContent;
import org.eclipse.vex.core.internal.dom.CollectingNodeTraversal;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.core.provisional.dom.MultilineText;

/**
 * @author Florian Thienel
 */
public class CssBasedBoxModelBuilder implements IBoxModelBuilder {

	private final StyleSheet styleSheet;

	public CssBasedBoxModelBuilder(final StyleSheet styleSheet) {
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

	private IStructuralBox asStructuralBox(final VisualizeResult visualizeResult) {
		if (visualizeResult.inline) {
			return visualizeAsBlock(visualizeResult.node, visualizeResult.childrenResults);
		} else {
			return visualizeResult.structuralBox;
		}
	}

	@Override
	public IInlineBox visualizeInline(final INode node) {
		return asInlineBox(visualize(node));
	}

	private <P extends IParentBox<IInlineBox>> P visualizeChildrenInline(final Iterable<VisualizeResult> childrenResults, final P parentBox) {
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
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new CollectingNodeTraversal<VisualizeResult>() {
			@Override
			public VisualizeResult visit(final IDocument document) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(document);
				return new VisualizeResult(document, childrenResults, nodeReference(document, visualizeChildrenAsStructure(document, styles, childrenResults, verticalBlock())));
			}

			@Override
			public VisualizeResult visit(final IDocumentFragment documentFragment) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(documentFragment);
				return new VisualizeResult(documentFragment, childrenResults,
						nodeReference(documentFragment, visualizeChildrenAsStructure(documentFragment, styles, childrenResults, verticalBlock())));
			}

			@Override
			public VisualizeResult visit(final IElement element) {
				final Collection<VisualizeResult> childrenResults = traverseChildren(element);
				final Styles styles = styleSheet.getStyles(element);
				if (isDisplayedAsBlock(styles)) {
					return new VisualizeResult(element, childrenResults, visualizeAsBlock(element, childrenResults));
				} else {
					return new VisualizeResult(element, childrenResults, visualizeInline(element, childrenResults));
				}
			}

			@Override
			public VisualizeResult visit(final IComment comment) {
				final List<VisualizeResult> childrenResults = Collections.<VisualizeResult> emptyList();
				final Styles styles = styleSheet.getStyles(comment);
				if (isDisplayedAsBlock(styles)) {
					return new VisualizeResult(comment, childrenResults, visualizeAsBlock(comment, childrenResults));
				} else {
					return new VisualizeResult(comment, childrenResults, visualizeInline(comment, childrenResults));
				}
			}

			@Override
			public VisualizeResult visit(final IProcessingInstruction pi) {
				final List<VisualizeResult> childrenResults = Collections.<VisualizeResult> emptyList();
				final Styles styles = styleSheet.getStyles(pi);
				if (isDisplayedAsBlock(styles)) {
					return new VisualizeResult(pi, childrenResults, visualizeAsBlock(pi, childrenResults));
				} else {
					return new VisualizeResult(pi, childrenResults, visualizeInline(pi, childrenResults));
				}
			}

			@Override
			public VisualizeResult visit(final IText text) {
				final List<VisualizeResult> childrenResults = Collections.<VisualizeResult> emptyList();
				return new VisualizeResult(text, childrenResults, visualizeInline(text, childrenResults));
			}
		});
	}

	private static boolean isDisplayedAsBlock(final Styles styles) {
		// currently we can only render blocks or inline, hence everything that is not inline must be a block
		return !isDisplayedInline(styles);
	}

	private static boolean isDisplayedInline(final Styles styles) {
		return CSS.INLINE.equals(styles.getDisplay());
	}

	private static boolean isPreservingWhitespace(final Styles styles) {
		return CSS.PRE.equals(styles.getWhiteSpace());
	}

	private static boolean isWrappedInInlineMarkers(final Styles styles) {
		return CSS.NORMAL.equals(styles.getInlineMarker());
	}

	/*
	 * Render as Block
	 */

	private IStructuralBox visualizeAsBlock(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<IStructuralBox>() {
			@Override
			public IStructuralBox visit(final IElement element) {
				final boolean mayContainText = mayContainText(element);
				final boolean containsInlineContent = containsInlineContent(childrenResults);

				final IStructuralBox content;
				if (isElementWithNoContentAllowed(element)) {
					content = visualizeStructuralElementWithNoContentAllowed(styles, element);
				} else if (element.isEmpty()) {
					content = placeholderForEmptyNode(node, styles, paragraph(styles));
				} else {
					content = visualizeChildrenAsStructure(element, styles, childrenResults, verticalBlock());
				}

				if (mayContainText) {
					return nodeReferenceWithText(element, surroundWithPseudoElements(frame(content, styles), element, styles));
				} else if (containsInlineContent) {
					return nodeReferenceWithInlineContent(element, surroundWithPseudoElements(frame(content, styles), element, styles));
				} else {
					return nodeReference(element, surroundWithPseudoElements(frame(content, styles), element, styles));
				}
			}

			@Override
			public IStructuralBox visit(final IComment comment) {
				final Paragraph inlineElementContent;
				if (comment.isEmpty()) {
					inlineElementContent = placeholderForEmptyNode(node, styles, paragraph(styles));
				} else {
					inlineElementContent = paragraph(styles, visualizeText(comment.getContent(), comment.getRange().resizeBy(1, -1), comment, styles));
				}

				return nodeReferenceWithText(comment, surroundWithPseudoElements(frame(surroundWithInlinePseudoElements(inlineElementContent, comment, styles), styles), comment, styles));
			}

			@Override
			public IStructuralBox visit(final IProcessingInstruction pi) {
				final Paragraph inlineElementContent;
				if (pi.isEmpty()) {
					inlineElementContent = placeholderForEmptyNode(node, styles, paragraph(styles));
				} else {
					inlineElementContent = paragraph(styles, visualizeText(pi.getContent(), pi.getRange().resizeBy(1, -1), pi, styles));
				}

				return nodeReferenceWithText(pi, surroundWithPseudoElements(frame(surroundWithInlinePseudoElements(inlineElementContent, pi, styles), styles), pi, styles));
			}
		});
	}

	private IStructuralBox visualizeStructuralElementWithNoContentAllowed(final Styles styles, final IElement element) {
		return paragraph(styles, visualizeInlineElementWithNoContentAllowed(element, styles));
	}

	private <P extends IParentBox<IStructuralBox>> P visualizeChildrenAsStructure(final INode node, final Styles styles, final Iterable<VisualizeResult> childrenResults, final P parentBox) {
		final LinkedList<VisualizeResult> pendingInline = new LinkedList<VisualizeResult>();
		for (final VisualizeResult visualizeResult : childrenResults) {
			if (visualizeResult.inline) {
				pendingInline.add(visualizeResult);
			} else {
				if (!pendingInline.isEmpty()) {
					parentBox.appendChild(visualizeInlineNodeContent(node, styles, pendingInline, paragraph(styles)));
				}
				pendingInline.clear();
				parentBox.appendChild(asStructuralBox(visualizeResult));
			}
		}
		if (!pendingInline.isEmpty()) {
			parentBox.appendChild(visualizeInlineNodeContent(node, styles, pendingInline, paragraph(styles)));
		}
		return parentBox;
	}

	private static IStructuralBox surroundWithPseudoElements(final IStructuralBox content, final INode node, final Styles styles) {
		final IStructuralBox pseudoElementBefore = visualizePseudoElementAsBlock(styles, node, PseudoElement.BEFORE);
		final IStructuralBox pseudoElementAfter = visualizePseudoElementAsBlock(styles, node, PseudoElement.AFTER);

		if (pseudoElementBefore == null && pseudoElementAfter == null) {
			return content;
		}

		return verticalBlock(pseudoElementBefore, content, pseudoElementAfter);
	}

	private static IStructuralBox visualizePseudoElementAsBlock(final Styles styles, final INode node, final PseudoElement pseudoElement) {
		if (!styles.hasPseudoElement(pseudoElement)) {
			return null;
		}

		final Styles pseudoElementStyles = styles.getPseudoElementStyles(pseudoElement);
		if (!isDisplayedAsBlock(pseudoElementStyles)) {
			return null;
		}

		return frame(visualizeContentProperty(node, pseudoElementStyles, paragraph(pseudoElementStyles)), pseudoElementStyles);
	}

	private static IInlineBox visualizePseudoElementInline(final Styles styles, final INode node, final PseudoElement pseudoElement) {
		if (!styles.hasPseudoElement(pseudoElement)) {
			return null;
		}

		final Styles pseudoElementStyles = styles.getPseudoElementStyles(pseudoElement);
		if (!isDisplayedInline(pseudoElementStyles)) {
			return null;
		}

		return frame(visualizeContentProperty(node, pseudoElementStyles, inlineContainer()), pseudoElementStyles);
	}

	private static <P extends IParentBox<IInlineBox>> P visualizeContentProperty(final INode node, final Styles styles, final P parent) {
		for (final IPropertyContent part : styles.getAllContent(node)) {
			final IInlineBox box = part.accept(new IPropertyContentVisitor<IInlineBox>() {
				@Override
				public IInlineBox visit(final TextualContent content) {
					return staticText(content.toString(), styles);
				}

				@Override
				public IInlineBox visit(final URIContent content) {
					final String imageUri = content.uri.toString();
					final Image image = new Image();
					image.setImageUrl(styles.resolveUrl(imageUri));
					return image;
				}
			});
			if (box != null) {
				parent.appendChild(box);
			}
		}
		return parent;
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

	private static boolean isElementWithNoContentAllowed(final IElement element) {
		final Set<QualifiedName> validItems = element.getDocument().getValidator().getValidItems(element);
		return validItems.isEmpty() && !element.hasChildren();
	}

	/*
	 * Render inline elements
	 */

	private IInlineBox visualizeInline(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		final IContent content = node.getContent();
		return node.accept(new BaseNodeVisitorWithResult<IInlineBox>() {
			@Override
			public IInlineBox visit(final IElement element) {
				if (isElementWithNoContentAllowed(element)) {
					return nodeReference(element,
							frame(surroundWithInlinePseudoElements(inlineContainer(visualizeInlineElementWithNoContentAllowed(element, styles)), element, styles), styles));
				}

				final InlineContainer inlineElementContent = surroundWithInlineMarkers(element, styles,
						visualizeInlineNodeContent(element, styles, childrenResults,
								inlineContainer()));

				if (mayContainText(element)) {
					return nodeReferenceWithText(element, frame(inlineElementContent, styles));
				} else {
					return nodeReference(element, frame(inlineElementContent, styles));
				}
			}

			@Override
			public IInlineBox visit(final IComment comment) {
				final InlineContainer inlineElementContent;
				if (comment.isEmpty()) {
					inlineElementContent = placeholderForEmptyNode(node, styles, inlineContainer());
				} else {
					inlineElementContent = inlineContainer(visualizeText(content, comment.getRange().resizeBy(1, -1), comment, styles));
				}

				return nodeReferenceWithText(comment, frame(surroundWithInlinePseudoElements(inlineElementContent, comment, styles), styles));
			}

			@Override
			public IInlineBox visit(final IProcessingInstruction pi) {
				final InlineContainer inlineElementContent;
				if (pi.isEmpty()) {
					inlineElementContent = placeholderForEmptyNode(node, styles, inlineContainer());
				} else {
					inlineElementContent = inlineContainer(visualizeText(content, pi.getRange().resizeBy(1, -1), pi, styles));
				}

				return nodeReferenceWithText(pi, frame(surroundWithInlinePseudoElements(inlineElementContent, pi, styles), styles));
			}

			@Override
			public IInlineBox visit(final IText text) {
				return visualizeText(content, text.getRange(), text.getParent(), styles);
			}

		});
	}

	private static <P extends IParentBox<IInlineBox>> P surroundWithInlineMarkers(final INode node, final Styles styles, final P parent) {
		if (isWrappedInInlineMarkers(styles)) {
			parent.prependChild(startTag(node, styles));
			parent.appendChild(endTag(node, styles));
		}
		return parent;
	}

	private <P extends IParentBox<IInlineBox>> P visualizeInlineNodeContent(final INode node, final Styles styles, final Collection<VisualizeResult> childrenResults, final P parent) {
		if (!childrenResults.isEmpty()) {
			return surroundWithInlinePseudoElements(visualizeChildrenInline(childrenResults, parent), node, styles);
		} else {
			return surroundWithInlinePseudoElements(placeholderForEmptyNode(node, styles, parent), node, styles);
		}
	}

	private static IInlineBox visualizeInlineElementWithNoContentAllowed(final INode node, final Styles styles) {
		if (!styles.isContentDefined()) {
			return nodeTag(node, styles);
		}
		return visualizeContentProperty(node, styles, inlineContainer());
	}

	private static IInlineBox visualizeText(final IContent content, final ContentRange textRange, final INode parentNode, final Styles styles) {
		if (isPreservingWhitespace(styles)) {
			return visualizeAsMultilineText(content, textRange, parentNode, styles);
		} else {
			return textContent(content, textRange, styles);
		}
	}

	private static IInlineBox visualizeAsMultilineText(final IContent content, final ContentRange textRange, final INode parentNode, final Styles styles) {
		final InlineContainer lineContainer = inlineContainer();
		final MultilineText lines = content.getMultilineText(textRange);
		for (int i = 0; i < lines.size(); i += 1) {
			final TextContent textLine = textContent(content, lines.getRange(i), styles);
			if (content.isLineBreak(lines.getRange(i).getEndOffset())) {
				textLine.setLineWrappingAtEnd(LineWrappingRule.REQUIRED);
			}
			lineContainer.appendChild(textLine);
		}

		if (textRange.getEndOffset() == parentNode.getEndOffset() - 1 && content.isLineBreak(textRange.getEndOffset())) {
			lineContainer.appendChild(endOffsetPlaceholder(parentNode, styles));
		}

		return lineContainer;
	}

	private static <P extends IParentBox<IInlineBox>> P surroundWithInlinePseudoElements(final P parent, final INode node, final Styles styles) {
		final IInlineBox pseudoElementBefore = visualizePseudoElementInline(styles, node, PseudoElement.BEFORE);
		final IInlineBox pseudoElementAfter = visualizePseudoElementInline(styles, node, PseudoElement.AFTER);

		if (pseudoElementBefore != null) {
			parent.prependChild(pseudoElementBefore);
		}
		if (pseudoElementAfter != null) {
			parent.appendChild(pseudoElementAfter);
		}

		return parent;
	}

	private static <P extends IParentBox<IInlineBox>> P placeholderForEmptyNode(final INode node, final Styles styles, final P parent) {
		parent.appendChild(endOffsetPlaceholder(node, styles));
		//		if (false) { // TODO allow to provide a placeholder text in the CSS
		//			parent.appendChild(staticText(MessageFormat.format("[placeholder for empty <{0}> element]", element.getLocalName()), styles));
		//		}
		return parent;
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
