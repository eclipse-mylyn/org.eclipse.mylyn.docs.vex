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
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;

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

	public static TextContent textContent(final IContent content, final ContentRange range, final Styles styles) {
		final TextContent textContent = new TextContent();
		textContent.setContent(content, range);
		textContent.setFont(font(styles));
		return textContent;
	}

	public static StaticText staticText(final String text, final Styles styles) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(font(styles));
		return staticText;
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

	public static int borderStyle(final String borderStyleName) {
		if (CSS.HIDDEN.equals(borderStyleName)) {
			return Graphics.LINE_SOLID;
		} else if (CSS.DOTTED.equals(borderStyleName)) {
			return Graphics.LINE_DOT;
		} else if (CSS.DASHED.equals(borderStyleName)) {
			return Graphics.LINE_DASH;
		} else if (CSS.SOLID.equals(borderStyleName)) {
			return Graphics.LINE_SOLID;
		} else {
			return Graphics.LINE_SOLID;
		}
	}

	public static Padding padding(final Styles styles) {
		return new Padding(styles.getPaddingTop(), styles.getPaddingLeft(), styles.getPaddingBottom(), styles.getPaddingRight());
	}

	public static FontSpec font(final Styles styles) {
		return styles.getFont();
	}
}
