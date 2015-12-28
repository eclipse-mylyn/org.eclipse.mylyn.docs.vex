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
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.TextAlign;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * This factory allows the conventient creation of box structures using nested method calls to factory methods.
 *
 * @author Florian Thienel
 */
public class BoxFactory {

	public static RootBox rootBox(final IStructuralBox... children) {
		final RootBox rootBox = new RootBox();
		for (final IStructuralBox child : children) {
			rootBox.appendChild(child);
		}
		return rootBox;
	}

	public static VerticalBlock verticalBlock(final IStructuralBox... children) {
		final VerticalBlock verticalBlock = new VerticalBlock();
		for (final IStructuralBox child : children) {
			verticalBlock.appendChild(child);
		}
		return verticalBlock;
	}

	public static StructuralFrame frame(final IStructuralBox component) {
		final StructuralFrame frame = new StructuralFrame();
		frame.setComponent(component);
		return frame;
	}

	public static StructuralFrame frame(final IStructuralBox component, final Margin margin, final Border border, final Padding padding, final Color backgroundColor) {
		final StructuralFrame frame = new StructuralFrame();
		frame.setComponent(component);
		frame.setMargin(margin);
		frame.setBorder(border);
		frame.setPadding(padding);
		frame.setBackgroundColor(backgroundColor);
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component, final Margin margin, final Border border, final Padding padding, final Color backgroundColor) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		frame.setMargin(margin);
		frame.setBorder(border);
		frame.setPadding(padding);
		frame.setBackgroundColor(backgroundColor);
		return frame;
	}

	public static StructuralNodeReference nodeReference(final INode node, final IStructuralBox component) {
		final StructuralNodeReference structuralNodeReference = new StructuralNodeReference();
		structuralNodeReference.setNode(node);
		structuralNodeReference.setComponent(component);
		return structuralNodeReference;
	}

	public static StructuralNodeReference nodeReferenceWithText(final INode node, final IStructuralBox component) {
		final StructuralNodeReference structuralNodeReference = new StructuralNodeReference();
		structuralNodeReference.setNode(node);
		structuralNodeReference.setCanContainText(true);
		structuralNodeReference.setComponent(component);
		return structuralNodeReference;
	}

	public static InlineNodeReference nodeReference(final INode node, final IInlineBox component) {
		final InlineNodeReference inlineNodeReference = new InlineNodeReference();
		inlineNodeReference.setNode(node);
		inlineNodeReference.setComponent(component);
		return inlineNodeReference;
	}

	public static InlineNodeReference nodeReferenceWithText(final INode node, final IInlineBox component) {
		final InlineNodeReference inlineNodeReference = new InlineNodeReference();
		inlineNodeReference.setNode(node);
		inlineNodeReference.setCanContainText(true);
		inlineNodeReference.setComponent(component);
		return inlineNodeReference;
	}

	public static HorizontalBar horizontalBar(final int height) {
		final HorizontalBar horizontalBar = new HorizontalBar();
		horizontalBar.setHeight(height);
		return horizontalBar;
	}

	public static HorizontalBar horizontalBar(final int height, final Color color) {
		final HorizontalBar horizontalBar = new HorizontalBar();
		horizontalBar.setHeight(height);
		horizontalBar.setColor(color);
		return horizontalBar;
	}

	public static Paragraph paragraph(final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		return paragraph;
	}

	public static Paragraph paragraph(final TextAlign textAlign, final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		paragraph.setTextAlign(textAlign);
		return paragraph;
	}

	public static InlineContainer inlineContainer(final IInlineBox... children) {
		final InlineContainer inlineContainer = new InlineContainer();
		for (final IInlineBox child : children) {
			inlineContainer.appendChild(child);
		}
		return inlineContainer;
	}

	public static TextContent textContent(final IContent content, final ContentRange range, final FontSpec font, final Color color) {
		final TextContent textContent = new TextContent();
		textContent.setContent(content, range);
		textContent.setFont(font);
		textContent.setColor(color);
		return textContent;
	}

	public static NodeEndOffsetPlaceholder endOffsetPlaceholder(final INode node, final FontSpec font) {
		final NodeEndOffsetPlaceholder contentPlaceholder = new NodeEndOffsetPlaceholder();
		contentPlaceholder.setNode(node);
		contentPlaceholder.setFont(font);
		return contentPlaceholder;
	}

	public static StaticText staticText(final String text, final FontSpec font, final Color color) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(font);
		staticText.setColor(color);
		return staticText;
	}

	public static Square square(final int size) {
		final Square square = new Square();
		square.setSize(size);
		return square;
	}

	public static NodeTag nodeTag(final INode node, final Color foreground) {
		final NodeTag nodeTag = new NodeTag();
		nodeTag.setNode(node);
		nodeTag.setColor(foreground);
		return nodeTag;
	}
}
