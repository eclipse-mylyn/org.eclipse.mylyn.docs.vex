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

import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * @author Florian Thienel
 */
public class CSSBasedBoxModelBuilder implements IBoxModelBuilder {

	private final StyleSheet styleSheet;

	private final CascadingProperties properties = new CascadingProperties() {
		{
			push(CSS.FONT, new FontSpec("Times New Roman", FontSpec.PLAIN, 20.0f));
		}
	};

	public CSSBasedBoxModelBuilder(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public RootBox visualizeRoot(final INode node) {
		final IDocument document = node.getDocument();
		return rootBox(nodeReference(document, visualizeChildrenStructure(document.children(), verticalBlock())));
	}

	@Override
	public IStructuralBox visualizeStructure(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<IStructuralBox>() {
			@Override
			public IStructuralBox visit(final IElement element) {
				if ("para".equals(element.getLocalName())) {
					return nodeReferenceWithText(element, frame(visualizeParagraphElementContent(element), Margin.NULL, Border.NULL, new Padding(5, 4)));
				}
				return nodeReference(element, frame(visualizeChildrenStructure(element.children(), verticalBlock()), Margin.NULL, Border.NULL, new Padding(3, 3)));
			}
		});
	}

	@Override
	public IInlineBox visualizeInline(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<IInlineBox>() {
			@Override
			public IInlineBox visit(final IElement element) {
				if ("b".equals(element.getLocalName())) {
					properties.push(CSS.FONT, currentFont().bold());
					final InlineNodeReference box = nodeReferenceWithText(element, frame(visualizeInlineElementContent(element), Margin.NULL, Border.NULL, Padding.NULL));
					properties.pop(CSS.FONT);
					return box;
				}
				if ("i".equals(element.getLocalName())) {
					properties.push(CSS.FONT, currentFont().italic());
					final InlineNodeReference box = nodeReferenceWithText(element, frame(visualizeInlineElementContent(element), Margin.NULL, Border.NULL, Padding.NULL));
					properties.pop(CSS.FONT);
					return box;
				}
				return nodeReferenceWithText(element, frame(visualizeInlineElementContent(element), Margin.NULL, Border.NULL, Padding.NULL));
			}

			@Override
			public IInlineBox visit(final IText text) {
				return textContent(text.getContent(), text.getRange(), currentFont());
			}
		});
	}

	private Paragraph visualizeParagraphElementContent(final IElement element) {
		if (element.hasChildren()) {
			return visualizeParagraphWithChildren(element);
		} else {
			return visualizeEmptyParagraph(element);
		}
	}

	private Paragraph visualizeParagraphWithChildren(final IElement element) {
		final Paragraph paragraph = paragraph();
		visualizeChildrenInline(element.children(), paragraph);
		return paragraph;
	}

	private Paragraph visualizeEmptyParagraph(final IElement element) {
		final Paragraph paragraph = paragraph();
		paragraph.appendChild(placeholderForEmptyElement());
		return paragraph;
	}

	private InlineContainer visualizeInlineElementContent(final IElement element) {
		final InlineContainer container = inlineContainer();
		if (element.hasChildren()) {
			visualizeChildrenInline(element.children(), container);
		} else {
			container.appendChild(placeholderForEmptyElement());
		}
		return container;
	}

	private StaticText placeholderForEmptyElement() {
		return staticText(" ", currentFont());
	}

	private <P extends IParentBox<IStructuralBox>> P visualizeChildrenStructure(final Iterable<INode> children, final P parentBox) {
		for (final INode child : children) {
			final IStructuralBox childBox = visualizeStructure(child);
			if (childBox != null) {
				parentBox.appendChild(childBox);
			}
		}
		return parentBox;
	}

	private <P extends IParentBox<IInlineBox>> P visualizeChildrenInline(final Iterable<INode> children, final P parentBox) {
		for (final INode child : children) {
			final IInlineBox childBox = visualizeInline(child);
			if (childBox != null) {
				parentBox.appendChild(childBox);
			}
		}
		return parentBox;
	}

	private FontSpec currentFont() {
		return properties.<FontSpec> peek(CSS.FONT);
	}

}
