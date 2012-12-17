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

import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.dom.Element;

/**
 * A wrapper for the top level <code>BlockElementBox</code> that applies its margins.
 */
public class RootBox extends AbstractBox implements BlockBox {

	private final Element element;
	private final BlockElementBox childBox;
	private final Box[] children = new Box[1];

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext used to create children.
	 * @param element
	 *            Element associated with this box.
	 * @param width
	 *            width of this box
	 */
	public RootBox(final LayoutContext context, final Element element, final int width) {
		this.element = element;
		setWidth(width);

		childBox = new BlockElementBox(context, this, this.element);

		final Insets insets = this.getInsets(context, getWidth());
		childBox.setX(insets.getLeft());
		childBox.setY(insets.getTop());
		childBox.setInitialSize(context);
		children[0] = childBox;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {
		final Caret caret = childBox.getCaret(context, offset);
		caret.translate(childBox.getX(), childBox.getY());
		return caret;
	}

	@Override
	public Box[] getChildren() {
		return children;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getNode()
	 */
	@Override
	public Element getNode() {
		return element;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return childBox.getEndOffset();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getFirstLine()
	 */
	public LineBox getFirstLine() {
		return childBox.getFirstLine();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getLastLine()
	 */
	public LineBox getLastLine() {
		return childBox.getLastLine();
	}

	public int getLineEndOffset(final int offset) {
		return childBox.getLineEndOffset(offset);
	}

	public int getLineStartOffset(final int offset) {
		return childBox.getLineStartOffset(offset);
	}

	public int getMarginBottom() {
		return 0;
	}

	public int getMarginTop() {
		return 0;
	}

	public int getNextLineOffset(final LayoutContext context, final int offset, final int x) {
		return childBox.getNextLineOffset(context, offset, x - childBox.getX());
	}

	public BlockBox getParent() {
		throw new IllegalStateException("RootBox does not have a parent");
	}

	public int getPreviousLineOffset(final LayoutContext context, final int offset, final int x) {
		return childBox.getPreviousLineOffset(context, offset, x - childBox.getX());
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		return childBox.getStartOffset();
	}

	public void invalidate(final boolean direct) {
		// do nothing. layout is always propagated to our child box.
	}

	public VerticalRange layout(final LayoutContext context, final int top, final int bottom) {

		final Insets insets = this.getInsets(context, getWidth());

		long start = 0;
		if (VEXCorePlugin.getInstance().isDebugging()) {
			start = System.currentTimeMillis();
		}

		final VerticalRange repaintRange = childBox.layout(context, top - insets.getTop(), bottom - insets.getBottom());

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 50) {
				System.out.println("RootBox.layout took " + (end - start) + "ms");
			}
		}

		setHeight(childBox.getHeight() + insets.getTop() + insets.getBottom());

		if (repaintRange != null) {
			return new VerticalRange(repaintRange.getStart() + childBox.getY(), repaintRange.getEnd() + childBox.getY());
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.vex.core.internal.layout.AbstractBox#viewToModel(
	 * org.eclipse.vex.core.internal.layout.LayoutContext, int, int)
	 */
	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {
		return childBox.viewToModel(context, x - childBox.getX(), y - childBox.getY());
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		final Rectangle r = context.getGraphics().getClipBounds();
		final long start = System.currentTimeMillis();
		super.paint(context, x, y);
		final long end = System.currentTimeMillis();
		if (end - start > 50) {
			System.out.println("RootBox.paint " + r.getHeight() + " pixel rows in " + (end - start) + "ms");
		}
	}

	public void setInitialSize(final LayoutContext context) {
		throw new IllegalStateException();
	}

}
