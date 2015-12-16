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
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.InlineFrame;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.FontSpec;
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
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component, final Styles styles) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		frame.setMargin(margin(styles));
		frame.setBorder(border(styles));
		frame.setPadding(padding(styles));
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
		final int top = styles.getMarginTop().get(1);
		final int left = styles.getMarginLeft().get(1);
		final int bottom = styles.getMarginBottom().get(1);
		final int right = styles.getMarginRight().get(1);

		return new Margin(top, left, bottom, right);
	}

	public static Border border(final Styles styles) {
		final int top = styles.getBorderTopWidth();
		final int left = styles.getBorderLeftWidth();
		final int bottom = styles.getBorderBottomWidth();
		final int right = styles.getBorderRightWidth();
		return new Border(top, left, bottom, right);
	}

	public static Padding padding(final Styles styles) {
		final int top = styles.getPaddingTop().get(1);
		final int left = styles.getPaddingLeft().get(1);
		final int bottom = styles.getPaddingBottom().get(1);
		final int right = styles.getPaddingRight().get(1);

		return new Padding(top, left, bottom, right);
	}

	public static FontSpec font(final Styles styles) {
		return styles.getFont();
	}
}
