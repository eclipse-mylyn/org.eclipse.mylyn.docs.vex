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
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReferenceWithText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.paragraph;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.rootBox;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.frame;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.staticText;
import static org.eclipse.vex.core.internal.visualization.CssBoxFactory.textContent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.RootBox;
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
					return new VisualizeResult(element, childrenResults, visualizeAsBlock(element, childrenResults));
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

	private IStructuralBox visualizeAsBlock(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<IStructuralBox>() {
			@Override
			public IStructuralBox visit(final IElement element) {
				final boolean mayContainText = mayContainText(element);
				final IStructuralBox content;
				if (mayContainText || containsInlineContent(childrenResults)) {
					content = visualizeInlineElementContent(styles, childrenResults, paragraph());
				} else {
					content = visualizeChildrenAsStructure(childrenResults, verticalBlock());
				}

				if (mayContainText) {
					return nodeReferenceWithText(element, frame(content, styles));
				} else {
					return nodeReference(element, frame(content, styles));
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

	/*
	 * Render inline elements
	 */

	private IInlineBox visualizeInline(final INode node, final Collection<VisualizeResult> childrenResults) {
		final Styles styles = styleSheet.getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<IInlineBox>() {
			@Override
			public IInlineBox visit(final IElement element) {
				if (mayContainText(element)) {
					return nodeReferenceWithText(element, frame(visualizeInlineElementContent(styles, childrenResults, inlineContainer()), styles));
				} else {
					return nodeReference(element, frame(visualizeInlineElementContent(styles, childrenResults, inlineContainer()), styles));
				}
			}

			@Override
			public IInlineBox visit(final IText text) {
				return textContent(text.getContent(), text.getRange(), styles);
			}
		});
	}

	private <P extends IParentBox<IInlineBox>> P visualizeInlineElementContent(final Styles styles, final Collection<VisualizeResult> childrenResults, final P parent) {
		if (!childrenResults.isEmpty()) {
			return visualizeChildrenInline(childrenResults, parent);
		} else {
			return placeholderForEmptyElement(styles, parent);
		}
	}

	private <P extends IParentBox<IInlineBox>> P placeholderForEmptyElement(final Styles styles, final P parent) {
		parent.appendChild(staticText(" ", styles));
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
