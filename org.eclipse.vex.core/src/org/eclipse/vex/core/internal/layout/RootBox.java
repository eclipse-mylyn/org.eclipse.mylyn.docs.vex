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
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A wrapper for the top level <code>BlockElementBox</code> that applies its margins.
 */
public class RootBox extends AbstractBox implements BlockBox {

	private final IDocument document;
	private final BlockElementBox childBox;
	private final Box[] children = new Box[1];

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext used to create children.
	 * @param document
	 *            Element associated with this box.
	 * @param width
	 *            width of this box
	 */
	public RootBox(final LayoutContext context, final IDocument document, final int width) {
		this.document = document;
		setWidth(width);

		childBox = new BlockElementBox(context, this, this.document);

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
	public Caret getCaret(final LayoutContext context, final ContentPosition position) {
		final Caret caret = childBox.getCaret(context, position);
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
	public INode getNode() {
		return document;
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

	public ContentPosition getLineEndPosition(final ContentPosition linePosition) {
		return childBox.getLineEndPosition(linePosition);
	}

	public ContentPosition getLineStartPosition(final ContentPosition linePosition) {
		return childBox.getLineStartPosition(linePosition);
	}

	public int getMarginBottom() {
		return 0;
	}

	public int getMarginTop() {
		return 0;
	}

	public ContentPosition getNextLinePosition(final LayoutContext context, final ContentPosition linePosition, final int x) {
		final ContentPosition position = childBox.getNextLinePosition(context, linePosition, x - childBox.getX());
		return position != null ? position : new ContentPosition(getNode(), getEndOffset());
	}

	public BlockBox getParent() {
		throw new IllegalStateException("RootBox does not have a parent");
	}

	public ContentPosition getPreviousLinePosition(final LayoutContext context, final ContentPosition linePosition, final int x) {
		final ContentPosition position = childBox.getPreviousLinePosition(context, linePosition, x - childBox.getX());
		return position != null ? position : new ContentPosition(getNode(), getStartOffset() + 1);
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

		final VerticalRange repaintRange = childBox.layout(context, top - insets.getTop(), bottom - insets.getBottom());

		setHeight(childBox.getHeight() + insets.getTop() + insets.getBottom());

		if (repaintRange != null) {
			return repaintRange.moveBy(childBox.getY());
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
	public ContentPosition viewToModel(final LayoutContext context, final int x, final int y) {
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
