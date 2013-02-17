/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * Box representing a row in a table.
 */
public class TableRowBox extends AbstractBlockBox {

	public TableRowBox(final LayoutContext context, final TableRowGroupBox parent, final IElement element) {
		super(context, parent, element);
	}

	public TableRowBox(final LayoutContext context, final BlockBox parent, final int startOffset, final int endOffset) {
		super(context, parent, startOffset, endOffset);
	}

	@Override
	protected List<Box> createChildren(final LayoutContext context) {

		final List<Box> children = new ArrayList<Box>();

		final IParent parent = findContainingParent();
		final int[] widths = getTableBox().getColumnWidths();

		LayoutUtils.iterateTableCells(context.getStyleSheet(), parent, getStartOffset(), getEndOffset(), new ElementOrRangeCallback() {
			private int column = 0;

			public void onElement(final IElement child, final String displayStyle) {
				children.add(new TableCellBox(context, TableRowBox.this, child, widths[column++]));
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				children.add(new TableCellBox(context, TableRowBox.this, startOffset, endOffset, widths[column++]));
			}
		});

		return children;
	}

	/**
	 * Override drawBox to do nothing. Table rows have no borders in border-collapse:separate mode.
	 */
	@Override
	public void drawBox(final LayoutContext context, final int x, final int y, final int containerWidth, final boolean drawBorders) {
	}

	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {

		final int hSpacing = getTableBox().getHorizonalSpacing();

		final Box[] children = getChildren();

		// If we haven't yet laid out this block, estimate the caret.
		if (children == null) {
			final int relative = offset - getStartOffset();
			final int size = getEndOffset() - getStartOffset();
			int y = 0;
			if (size > 0) {
				y = getHeight() * relative / size;
			}
			return new HCaret(0, y, getWidth());
		}

		int x = hSpacing / 2;

		final int[] widths = getTableBox().getColumnWidths();

		for (int i = 0; i < children.length; i++) {

			final Box child = children[i];

			if (!child.hasContent()) {
				continue; // TODO can we really have generated table cells?
			}

			if (offset < child.getStartOffset()) {
				return new TextCaret(x, 0, getHeight());
			}

			if (offset >= child.getStartOffset() && offset <= child.getEndOffset()) {

				final Caret caret = child.getCaret(context, offset);
				caret.translate(child.getX(), child.getY());
				return caret;
			}

			x += widths[i] + hSpacing;
		}

		return new TextCaret(x, 0, getHeight());
	}

	/**
	 * Override to return zero insets. Table rows have no insets in border-collapse:separate mode.
	 */
	@Override
	public Insets getInsets(final LayoutContext context, final int containerWidth) {
		return Insets.ZERO_INSETS;
	}

	@Override
	public int getMarginBottom() {
		return 0;
	}

	@Override
	public int getMarginTop() {
		return 0;
	}

	@Override
	public int getNextLineOffset(final LayoutContext context, final int offset, final int x) {

		final BlockBox[] children = getContentChildren();
		final int[] widths = getTableBox().getColumnWidths();
		int leftEdge = 0;

		for (int i = 0; i < children.length; i++) {
			if (leftEdge + widths[i] > x) {
				final int newOffset = children[i].getNextLineOffset(context, offset, x - leftEdge);
				if (newOffset == children[i].getEndOffset() + 1) {
					return -1;
				} else {
					return newOffset;
				}
			}
			leftEdge += widths[i];
		}

		return -1;
	}

	@Override
	public int getPreviousLineOffset(final LayoutContext context, final int offset, final int x) {

		final BlockBox[] children = getContentChildren();
		final int[] widths = getTableBox().getColumnWidths();
		int leftEdge = 0;

		for (int i = 0; i < children.length; i++) {
			if (leftEdge + widths[i] > x) {
				final int newOffset = children[i].getPreviousLineOffset(context, offset, x - leftEdge);
				if (newOffset == children[i].getStartOffset() - 1) {
					return -1;
				} else {
					return newOffset;
				}
			}
			leftEdge += widths[i];
		}

		return -1;
	}

	/**
	 * Returns the TableBox associated with this row.
	 */
	public TableBox getTableBox() {
		return (TableBox) getParent().getParent().getParent();
	}

	@Override
	protected int positionChildren(final LayoutContext context) {

		final int hSpacing = getTableBox().getHorizonalSpacing();

		int childX = hSpacing;
		int topInset = 0;
		int height = 0;
		int bottomInset = 0;
		for (int i = 0; i < getChildren().length; i++) {
			final Box child = getChildren()[i];
			final Insets insets = child.getInsets(context, getWidth());

			childX += insets.getLeft();

			child.setX(childX);

			childX += child.getWidth() + insets.getRight() + hSpacing;

			topInset = Math.max(topInset, insets.getTop());
			height = Math.max(height, child.getHeight());
			bottomInset = Math.max(bottomInset, insets.getBottom());
		}

		setHeight(topInset + height + bottomInset);

		for (int i = 0; i < getChildren().length; i++) {
			final Box child = getChildren()[i];
			child.setY(topInset);
			child.setHeight(height);
		}

		return -1; // TODO revisit
	}

	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {

		final Box[] children = getChildren();
		if (children == null) {
			return super.viewToModel(context, x, y);
		}

		for (final Box child : children) {
			if (!child.hasContent()) {
				continue;
			}

			if (x < child.getX()) {
				return child.getStartOffset() - 1;
			}

			if (x < child.getX() + child.getWidth()) {
				return child.viewToModel(context, x - child.getX(), y - child.getY());
			}
		}

		return getEndOffset();
	}

}
