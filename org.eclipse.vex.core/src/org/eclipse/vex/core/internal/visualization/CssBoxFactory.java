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

import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.BorderLine;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.InlineFrame;
import org.eclipse.vex.core.internal.boxes.LineWrappingRule;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.NodeTag;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.Square;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.LineStyle;
import org.eclipse.vex.core.internal.core.TextAlign;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.INode;

public class CssBoxFactory {

	public static StructuralFrame frame(final IStructuralBox component, final Styles styles) {
		final StructuralFrame frame = new StructuralFrame();
		frame.setComponent(component);
		frame.setMargin(margin(styles));
		frame.setBorder(border(styles));
		frame.setPadding(padding(styles));
		frame.setBackgroundColor(styles.getBackgroundColor());
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component, final Styles styles) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		frame.setMargin(margin(styles));
		frame.setBorder(border(styles));
		frame.setPadding(padding(styles));
		frame.setBackgroundColor(styles.getBackgroundColor());
		return frame;
	}

	public static Paragraph paragraph(final Styles styles, final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		paragraph.setTextAlign(textAlign(styles));
		return paragraph;
	}

	public static TextContent textContent(final IContent content, final ContentRange range, final Styles styles) {
		final TextContent textContent = new TextContent();
		textContent.setContent(content, range);
		textContent.setFont(font(styles));
		textContent.setColor(styles.getColor());
		return textContent;
	}

	public static TextContent textContentWithLineBreak(final IContent content, final ContentRange range, final Styles styles) {
		final TextContent textContent = textContent(content, range, styles);
		textContent.setLineWrappingAtEnd(LineWrappingRule.REQUIRED);
		return textContent;
	}

	public static NodeEndOffsetPlaceholder endOffsetPlaceholder(final INode node, final Styles styles) {
		final NodeEndOffsetPlaceholder contentPlaceholder = new NodeEndOffsetPlaceholder();
		contentPlaceholder.setNode(node);
		contentPlaceholder.setFont(font(styles));
		return contentPlaceholder;
	}

	public static StaticText staticText(final String text, final Styles styles) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(font(styles));
		staticText.setColor(styles.getColor());
		return staticText;
	}

	public static StaticText staticTextWithLineBreak(final String text, final Styles styles) {
		final StaticText staticText = staticText(text, styles);
		staticText.setLineWrappingAtEnd(LineWrappingRule.REQUIRED);
		return staticText;
	}

	public static Square square(final int size, final Styles styles) {
		final Square square = new Square();
		square.setSize(size);
		square.setColor(styles.getColor());
		return square;
	}

	public static NodeTag nodeTag(final INode node, final Styles styles) {
		final NodeTag nodeTag = new NodeTag();
		nodeTag.setKind(NodeTag.Kind.NODE);
		nodeTag.setNode(node);
		nodeTag.setColor(styles.getColor());
		nodeTag.setShowText(true);
		return nodeTag;
	}

	public static NodeTag startTag(final INode node, final Styles styles) {
		final NodeTag nodeTag = new NodeTag();
		nodeTag.setKind(NodeTag.Kind.START);
		nodeTag.setNode(node);
		nodeTag.setColor(styles.getColor());
		nodeTag.setShowText(false);
		return nodeTag;
	}

	public static NodeTag endTag(final INode node, final Styles styles) {
		final NodeTag nodeTag = new NodeTag();
		nodeTag.setKind(NodeTag.Kind.END);
		nodeTag.setNode(node);
		nodeTag.setColor(styles.getColor());
		nodeTag.setShowText(false);
		return nodeTag;
	}

	public static Margin margin(final Styles styles) {
		return new Margin(styles.getMarginTop(), styles.getMarginLeft(), styles.getMarginBottom(), styles.getMarginRight());
	}

	public static Border border(final Styles styles) {
		final BorderLine top = new BorderLine(styles.getBorderTopWidth(), borderStyle(styles.getBorderTopStyle()), styles.getBorderTopColor());
		final BorderLine left = new BorderLine(styles.getBorderLeftWidth(), borderStyle(styles.getBorderLeftStyle()), styles.getBorderLeftColor());
		final BorderLine bottom = new BorderLine(styles.getBorderBottomWidth(), borderStyle(styles.getBorderBottomStyle()), styles.getBorderBottomColor());
		final BorderLine right = new BorderLine(styles.getBorderRightWidth(), borderStyle(styles.getBorderRightStyle()), styles.getBorderRightColor());
		return new Border(top, left, bottom, right);
	}

	public static LineStyle borderStyle(final String borderStyleName) {
		if (CSS.DOTTED.equals(borderStyleName)) {
			return LineStyle.DOTTED;
		} else if (CSS.DASHED.equals(borderStyleName)) {
			return LineStyle.DASHED;
		} else {
			return LineStyle.SOLID;
		}
	}

	public static Padding padding(final Styles styles) {
		return new Padding(styles.getPaddingTop(), styles.getPaddingLeft(), styles.getPaddingBottom(), styles.getPaddingRight());
	}

	public static FontSpec font(final Styles styles) {
		return styles.getFont();
	}

	public static TextAlign textAlign(final Styles styles) {
		final String textAlign = styles.getTextAlign();
		if (CSS.CENTER.equals(textAlign)) {
			return TextAlign.CENTER;
		} else if (CSS.RIGHT.equals(textAlign)) {
			return TextAlign.RIGHT;
		} else {
			return TextAlign.LEFT;
		}
	}
}
