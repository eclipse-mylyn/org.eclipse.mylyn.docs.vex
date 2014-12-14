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

import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Drawable;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * An inline box that draws a Drawable object. The drawable is drawn relative to the text baseline, therefore it should
 * draw using mostly negative y-coordinates.
 */
public class DrawableBox extends AbstractInlineBox {

	public static final byte NO_MARKER = 0;
	public static final byte START_MARKER = 1;
	public static final byte END_MARKER = 2;

	private final Drawable drawable;
	private final INode node;
	private final byte marker;

	/**
	 * Class constructor.
	 *
	 * @param drawable
	 *            Drawable to draw.
	 * @param node
	 *            Node whose styles determine the color of the drawable.
	 */
	public DrawableBox(final Drawable drawable, final INode node) {
		this(drawable, node, NO_MARKER);
	}

	/**
	 * Class constructor. This constructor is called when creating a DrawableBox that represents the start or end marker
	 * of an inline element.
	 *
	 * @param drawable
	 *            Drawable to draw.
	 * @param node
	 *            Node whose styles determine the color of the drawable.
	 * @param marker
	 *            which marker should be drawn. Must be one of NO_MARKER, START_MARKER, or END_MARKER.
	 */
	public DrawableBox(final Drawable drawable, final INode node, final byte marker) {
		this.drawable = drawable;
		this.node = node;
		this.marker = marker;
		final Rectangle bounds = drawable.getBounds();
		setWidth(bounds.getWidth());
		setHeight(bounds.getHeight());
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	@Override
	public int getBaseline() {
		return 0;
	}

	/**
	 * Returns the node that controls the styling for this text box.
	 */
	@Override
	public INode getNode() {
		return node;
	}

	@Override
	public boolean isEOL() {
		return false;
	}

	/**
	 * Draw the drawable. The foreground color of the context's Graphics is set before calling the drawable's draw
	 * method.
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);

		boolean drawSelected = false;
		if (marker == START_MARKER) {
			drawSelected = getNode().getStartOffset() >= context.getSelectionStart() && getNode().getStartOffset() + 1 <= context.getSelectionEnd();
		} else if (marker == END_MARKER) {
			drawSelected = getNode().getEndOffset() >= context.getSelectionStart() && getNode().getEndOffset() + 1 <= context.getSelectionEnd();
		}

		final FontResource font = g.getFont(styles.getFont());
		final ColorResource color = g.getColor(styles.getColor());

		final FontResource oldFont = g.setCurrentFont(font);
		final ColorResource oldColor = g.setColor(color);

		final FontMetrics fm = g.getFontMetrics();

		if (drawSelected) {
			final Rectangle bounds = drawable.getBounds();
			g.setColor(g.getSystemColor(ColorResource.SELECTION_BACKGROUND));
			g.fillRect(x + bounds.getX(), y - fm.getAscent(), bounds.getWidth(), styles.getLineHeight());
			g.setColor(g.getSystemColor(ColorResource.SELECTION_FOREGROUND));
		}

		drawable.draw(g, x, y);

		g.setCurrentFont(oldFont);
		g.setColor(oldColor);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[shape]";
	}

}
