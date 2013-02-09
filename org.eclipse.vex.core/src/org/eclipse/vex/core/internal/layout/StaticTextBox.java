/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * A TextBox representing a static string. Represents text which is not editable within the VexWidget, such as
 * enumerated list markers.
 */
public class StaticTextBox extends TextBox {

	public static final byte NO_MARKER = 0;
	public static final byte START_MARKER = 1;
	public static final byte END_MARKER = 2;

	private final String text;
	private final byte marker;

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext used to calculate the box's size.
	 * @param node
	 *            Node used to style the text.
	 * @param text
	 *            Static text to display
	 */
	public StaticTextBox(final LayoutContext context, final INode node, final String text) {
		this(context, node, text, NO_MARKER);
		if (text.length() == 0) {
			throw new IllegalArgumentException("StaticTextBox cannot have an empty text string.");
		}
	}

	/**
	 * Class constructor. This constructor is used when generating a static text box representing a marker for the start
	 * or end of an inline element. If the selection spans the related marker, the text is drawn in the platform's text
	 * selection colours.
	 * 
	 * @param context
	 *            LayoutContext used to calculate the box's size
	 * @param node
	 *            Node used to style the text
	 * @param text
	 *            Static text to display
	 * @param marker
	 *            START_MARKER or END_MARKER, depending on whether the text represents the start sentinel or the end
	 *            sentinel of the element
	 */
	public StaticTextBox(final LayoutContext context, final INode node, final String text, final byte marker) {
		super(node);
		this.text = text;
		this.marker = marker;
		calculateSize(context);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.TextBox#getText()
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return false;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#paint(org.eclipse.vex.core.internal.layout.LayoutContext, int, int)
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final Graphics g = context.getGraphics();

		boolean drawSelected = false;
		if (marker == START_MARKER) {
			drawSelected = getNode().getStartOffset() >= context.getSelectionStart() && getNode().getStartOffset() + 1 <= context.getSelectionEnd();
		} else if (marker == END_MARKER) {
			drawSelected = getNode().getEndOffset() >= context.getSelectionStart() && getNode().getEndOffset() + 1 <= context.getSelectionEnd();
		}

		final FontResource font = g.createFont(styles.getFont());
		final ColorResource color = g.createColor(styles.getColor());

		final FontResource oldFont = g.setFont(font);
		final ColorResource oldColor = g.setColor(color);

		if (drawSelected) {
			paintSelectedText(context, getText(), x, y);
		} else {
			g.drawString(getText(), x, y);
		}
		paintTextDecoration(context, styles, getText(), x, y);

		g.setFont(oldFont);
		g.setColor(oldColor);
		font.dispose();
		color.dispose();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.TextBox#splitAt(int)
	 */
	@Override
	public Pair splitAt(final LayoutContext context, final int offset) {

		StaticTextBox left;
		if (offset == 0) {
			left = null;
		} else {
			left = new StaticTextBox(context, getNode(), getText().substring(0, offset), marker);
		}

		StaticTextBox right;
		if (offset == getText().length()) {
			right = null;
		} else {
			right = new StaticTextBox(context, getNode(), getText().substring(offset), marker);
		}
		return new Pair(left, right);
	}

}
