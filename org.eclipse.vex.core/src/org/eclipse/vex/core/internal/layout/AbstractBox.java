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

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Element;

/**
 * Base implementation of the <code>Box</code> interface, implementing some common methods.
 */
public abstract class AbstractBox implements Box {

	private static final Box[] EMPTY_BOX_ARRAY = new Box[0];

	private int x;
	private int y;
	private int width = -1;
	private int height = -1;

	/**
	 * Class constructor.
	 */
	public AbstractBox() {
	}

	/**
	 * Returns true if the given offset is between startOffset and endOffset, inclusive.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#containsOffset(int)
	 */
	public boolean containsOffset(final int offset) {
		return offset >= getStartOffset() && offset <= getEndOffset();
	}

	/**
	 * Throws <code>IllegalStateException</code>. Boxes with content must provide an implementation of this method.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	public Caret getCaret(final LayoutContext context, final int offset) {
		throw new IllegalStateException();
	}

	/**
	 * Returns an empty array of children.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getChildren()
	 */
	public Box[] getChildren() {
		return EMPTY_BOX_ARRAY;
	}

	/**
	 * Returns null. Boxes associated with elements must provide an implementation of this method.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getElement()
	 */
	public Element getElement() {
		return null;
	}

	/**
	 * Throws <code>IllegalStateException</code>. Boxes with content must provide an implementation of this method.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	public int getEndOffset() {
		throw new IllegalStateException();
	}

	/**
	 * Returns the height set with <code>setHeight</code>.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Throws <code>IllegalStateException</code>. Boxes with content must provide an implementation of this method.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	public int getStartOffset() {
		throw new IllegalStateException();
	}

	/**
	 * Returns the insets of this box, which is the sum of the margin, border, and padding on each side. If no element
	 * is associated with this box returns all zeros.
	 */
	public Insets getInsets(final LayoutContext context, final int containerWidth) {
		final Element element = getElement();
		if (element == null) {
			return Insets.ZERO_INSETS;
		} else {
			return getInsets(context.getStyleSheet().getStyles(element), containerWidth);
		}
	}

	/**
	 * Returns false. Boxes with content must override this method and return true, and must provide implementations for
	 * the following methods.
	 * 
	 * <ul>
	 * <li>{@link Box#getCaretShapes}</li>
	 * <li>{@link Box#getStartOffset}</li>
	 * <li>{@link Box#getEndOffset}</li>
	 * <li>{@link Box#viewToModel}</li>
	 * </ul>
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#hasContent()
	 */
	public boolean hasContent() {
		return false;
	}

	public boolean isAnonymous() {
		return true;
	}

	/**
	 * Returns the width set with <code>setWidth</code>.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the value set with <code>setX</code>.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getX()
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the value set with <code>setY</code>.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getY()
	 */
	public int getY() {
		return y;
	}

	/**
	 * Paint all children of this box.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#paint(org.eclipse.vex.core.internal.layout.LayoutContext, int, int)
	 */
	public void paint(final LayoutContext context, final int x, final int y) {
		if (skipPaint(context, x, y)) {
			return;
		}

		paintChildren(context, x, y);
	}

	/**
	 * Paint the children of this box.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param x
	 *            x-coordinate at which to paint
	 * @param y
	 *            y-coordinate at which to paint
	 */
	protected void paintChildren(final LayoutContext context, final int x, final int y) {
		final Box[] children = getChildren();
		for (int i = 0; children != null && i < children.length; i++) {
			final Box child = children[i];
			child.paint(context, x + child.getX(), y + child.getY());
		}
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public void setY(final int y) {
		this.y = y;
	}

	/**
	 * Returns true if this box is outside the clip region. Implementations of <code>paint</code> should use this to
	 * avoid unnecessary painting.
	 * 
	 * @param context
	 *            <code>LayoutContext</code> in effect.
	 * @param x
	 *            the x-coordinate at which the box is being painted
	 * @param y
	 *            the y-coordinate at which the box is being painted
	 */
	protected boolean skipPaint(final LayoutContext context, final int x, final int y) {
		final Rectangle clipBounds = context.getGraphics().getClipBounds();

		return clipBounds.getY() + clipBounds.getHeight() <= y || clipBounds.getY() >= y + getHeight();

	}

	/**
	 * Throws <code>IllegalStateException</code>. Boxes with content must provide an implementation of this method.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#viewToModel(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	public int viewToModel(final LayoutContext context, final int x, final int y) {
		throw new IllegalStateException();
	}

	/**
	 * Draws the background and borders of a CSS-styled box.
	 * 
	 * @param context
	 *            LayoutContext used for drawing.
	 * @param x
	 *            x-coordinate of the left side of the box
	 * @param y
	 *            y-coordinate of the top of the box
	 * @param containerWidth
	 *            width of the containing client area. Used for calculating padding expressed as a percentage.
	 * @param drawBorders
	 *            If true, the background is filled and the borders are drawn; otherwise, just the background is filled.
	 *            This is handy when removing the borders when drawing the selection frame.
	 */
	protected void drawBox(final LayoutContext context, final int x, final int y, final int containerWidth, final boolean drawBorders) {
		this.drawBox(context, getElement(), x, y, containerWidth, drawBorders);
	}

	/**
	 * Draws the background and borders of a CSS-styled box.
	 * 
	 * @param context
	 *            LayoutContext used for drawing.
	 * @param element
	 *            Element to use when determining styles. This is used by TableBodyBox to specify the corresponding
	 *            table element.
	 * @param x
	 *            x-coordinate of the left side of the box
	 * @param y
	 *            y-coordinate of the top of the box
	 * @param containerWidth
	 *            width of the containing client area. Used for calculating padding expressed as a percentage.
	 * @param drawBorders
	 *            If true, the background is filled and the borders are drawn; otherwise, just the background is filled.
	 *            This is handy when removing the borders when drawing the selection frame.
	 */
	protected void drawBox(final LayoutContext context, final Element element, final int x, final int y, final int containerWidth, final boolean drawBorders) {

		if (element == null) {
			return;
		}

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(element);

		boolean hasLeft = true;
		boolean hasRight = true;
		final int left = x - styles.getPaddingLeft().get(containerWidth) - styles.getBorderLeftWidth();
		final int top = y - styles.getPaddingTop().get(containerWidth) - styles.getBorderTopWidth();
		final int right = x + getWidth() + styles.getPaddingRight().get(containerWidth) + styles.getBorderRightWidth();
		final int bottom = y + getHeight() + styles.getPaddingBottom().get(containerWidth) + styles.getBorderBottomWidth();

		if (this instanceof InlineElementBox) {
			// TODO fix boxes for inline elements
			hasLeft = getStartOffset() == element.getStartOffset() + 1;
			hasRight = getEndOffset() == element.getEndOffset();
			if (hasLeft) {
				// left += styles.getMarginLeft().get(0);
			}
			if (hasRight) {
				// right -= styles.getMarginRight().get(0);
			}
			// top = y - styles.getPaddingTop().get(0) -
			// styles.getBorderTopWidth();
			// bottom = y + box.getHeight() + styles.getPaddingBottom().get(0) +
			// styles.getBorderBottomWidth();
		}

		final Color backgroundColor = styles.getBackgroundColor();

		if (backgroundColor != null) {
			final ColorResource color = g.createColor(backgroundColor);
			final ColorResource oldColor = g.setColor(color);
			g.fillRect(left, top, right - left, bottom - top);
			g.setColor(oldColor);
			color.dispose();
		}

		if (drawBorders) {
			// Object oldAntiAlias =
			// g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			//
			// g.setRenderingHint(
			// RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_OFF);
			final boolean oldAntiAlias = g.isAntiAliased();
			g.setAntiAliased(false);

			final int bw2 = styles.getBorderBottomWidth() / 2;
			final int lw2 = styles.getBorderLeftWidth() / 2;
			final int rw2 = styles.getBorderRightWidth() / 2;
			final int tw2 = styles.getBorderTopWidth() / 2;

			// Bottom border
			if (styles.getBorderBottomWidth() > 0) {
				final ColorResource color = g.createColor(styles.getBorderBottomColor());
				final ColorResource oldColor = g.setColor(color);
				g.setLineStyle(lineStyle(styles.getBorderBottomStyle()));
				g.setLineWidth(styles.getBorderBottomWidth());
				g.drawLine(left + bw2, bottom - bw2 - 1, right - bw2, bottom - bw2 - 1);
				g.setColor(oldColor);
				color.dispose();
			}

			// Left border
			if (hasLeft && styles.getBorderLeftWidth() > 0) {
				final ColorResource color = g.createColor(styles.getBorderLeftColor());
				final ColorResource oldColor = g.setColor(color);
				g.setLineStyle(lineStyle(styles.getBorderLeftStyle()));
				g.setLineWidth(styles.getBorderLeftWidth());
				g.drawLine(left + lw2, top + lw2, left + lw2, bottom - lw2 - 1);
				g.setColor(oldColor);
				color.dispose();
			}

			// Right border
			if (hasRight && styles.getBorderRightWidth() > 0) {
				final ColorResource color = g.createColor(styles.getBorderRightColor());
				final ColorResource oldColor = g.setColor(color);
				g.setLineStyle(lineStyle(styles.getBorderRightStyle()));
				g.setLineWidth(styles.getBorderRightWidth());
				g.drawLine(right - rw2 - 1, top + rw2, right - rw2 - 1, bottom - rw2 - 1);
				g.setColor(oldColor);
				color.dispose();
			}

			// Top border
			if (styles.getBorderTopWidth() > 0) {
				final ColorResource color = g.createColor(styles.getBorderTopColor());
				final ColorResource oldColor = g.setColor(color);
				g.setLineStyle(lineStyle(styles.getBorderTopStyle()));
				g.setLineWidth(styles.getBorderTopWidth());
				g.drawLine(left + tw2, top + tw2, right - tw2, top + tw2);
				g.setColor(oldColor);
				color.dispose();
			}

			// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// oldAntiAlias);
			g.setAntiAliased(oldAntiAlias);

		}
	}

	/**
	 * Convert a CSS line style string (e.g. "dotted") to the corresponding Graphics.LINE_XXX style.
	 */
	private static int lineStyle(final String style) {
		if (style.equals(CSS.DOTTED)) {
			return Graphics.LINE_DOT;
		} else if (style.equals(CSS.DASHED)) {
			return Graphics.LINE_DASH;
		} else {
			return Graphics.LINE_SOLID;
		}

	}

	/**
	 * Returns the insets for a CSS box with the given styles.
	 * 
	 * @param styles
	 *            Styles for the box.
	 * @param containerWidth
	 *            Content area of the containing box.
	 */
	public static Insets getInsets(final Styles styles, final int containerWidth) {

		final int top = styles.getMarginTop().get(containerWidth) + styles.getBorderTopWidth() + styles.getPaddingTop().get(containerWidth);

		final int left = styles.getMarginLeft().get(containerWidth) + styles.getBorderLeftWidth() + styles.getPaddingLeft().get(containerWidth);

		final int bottom = styles.getMarginBottom().get(containerWidth) + styles.getBorderBottomWidth() + styles.getPaddingBottom().get(containerWidth);

		final int right = styles.getMarginRight().get(containerWidth) + styles.getBorderRightWidth() + styles.getPaddingRight().get(containerWidth);

		return new Insets(top, left, bottom, right);
	}

}
