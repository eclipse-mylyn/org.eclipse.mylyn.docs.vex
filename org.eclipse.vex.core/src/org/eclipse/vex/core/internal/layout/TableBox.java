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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.dom.IParent;
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * Box that lays out a table.
 */
public class TableBox extends AbstractBlockBox {

	/**
	 * Class constructor.
	 * 
	 * @param node
	 *            Element represented by this box.
	 */
	public TableBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	public TableBox(final LayoutContext context, final BlockBox parent, final int startOffset, final int endOffset) {
		super(context, parent, startOffset, endOffset);
	}

	@Override
	protected List<Box> createChildren(final LayoutContext context) {

		// Walk children:
		// each table-caption gets a BEB
		// each table-column gets a TableColumnBox
		// each table-column-group gets a TableColumnGroupBox
		// runs of others get TableBodyBox

		final List<Box> children = new ArrayList<Box>();

		iterateChildrenByDisplayStyle(context.getStyleSheet(), captionOrColumnStyles, new ElementOrRangeCallback() {
			public void onElement(final IElement child, final String displayStyle) {
				children.add(new BlockElementBox(context, TableBox.this, child));
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				children.add(new TableBodyBox(context, TableBox.this, startOffset, endOffset));
			}
		});

		return children;
	}

	/**
	 * Returns an array of widths of the table columns. These widths do not include column spacing.
	 */
	public int[] getColumnWidths() {
		return columnWidths;
	}

	public int getHorizonalSpacing() {
		return horizonalSpacing;
	}

	@Override
	public Insets getInsets(final LayoutContext context, final int containerWidth) {
		return new Insets(getMarginTop(), 0, getMarginBottom(), 0);
	}

	public int getVerticalSpacing() {
		return verticalSpacing;
	}

	@Override
	public VerticalRange layout(final LayoutContext context, final int top, final int bottom) {

		// TODO Only compute columns widths (a) if re-laying out the whole box
		// or (b) if the invalid child row now has more columns than us
		// or (c) if the invalid child row has < current column count and it
		// used to be the only one with a valid child row.

		final int newColCount = computeColumnCount(context);
		if (columnWidths == null || newColCount != columnWidths.length) {
			setLayoutState(LAYOUT_REDO);
		}

		if (getLayoutState() == LAYOUT_REDO) {
			computeColumnWidths(context, newColCount);
		}

		return super.layout(context, top, bottom);
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		if (skipPaint(context, x, y)) {
			return;
		}

		paintChildren(context, x, y);

		paintSelectionFrame(context, x, y, true);
	}

	// ============================================================ PRIVATE

	private static Set<String> captionOrColumnStyles = new HashSet<String>();

	static {
		captionOrColumnStyles.add(CSS.TABLE_CAPTION);
		captionOrColumnStyles.add(CSS.TABLE_COLUMN);
		captionOrColumnStyles.add(CSS.TABLE_COLUMN_GROUP);
	}

	private int[] columnWidths;
	private int horizonalSpacing;
	private int verticalSpacing;

	private static class CountingCallback implements ElementOrRangeCallback {

		public int getCount() {
			return count;
		}

		public void reset() {
			count = 0;
		}

		public void onElement(final IElement child, final String displayStyle) {
			count++;
		}

		public void onRange(final IParent parent, final int startOffset, final int endOffset) {
			count++;
		}

		private int count;
	}

	/**
	 * Performs a quick count of this table's columns. If the count has changed, we must re-layout the entire table.
	 */
	private int computeColumnCount(final LayoutContext context) {

		final IParent tableElement = findContainingParent();
		final int[] columnCounts = new int[1]; // work around Java's insistence
												// on final
		columnCounts[0] = 0;
		final StyleSheet styleSheet = context.getStyleSheet();
		final CountingCallback callback = new CountingCallback();
		LayoutUtils.iterateTableRows(styleSheet, tableElement, getStartOffset(), getEndOffset(), new ElementOrRangeCallback() {
			public void onElement(final IElement child, final String displayStyle) {
				LayoutUtils.iterateTableCells(styleSheet, child, callback);
				columnCounts[0] = Math.max(columnCounts[0], callback.getCount());
				callback.reset();
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				LayoutUtils.iterateTableCells(styleSheet, parent, startOffset, endOffset, callback);
				columnCounts[0] = Math.max(columnCounts[0], callback.getCount());
				callback.reset();
			}

		});

		return columnCounts[0];
	}

	private void computeColumnWidths(final LayoutContext context, final int columnCount) {

		columnWidths = new int[columnCount];

		if (columnCount == 0) {
			return;
		}

		horizonalSpacing = 0;
		verticalSpacing = 0;
		final int myWidth = getWidth();
		int availableWidth = myWidth;

		if (!isAnonymous()) {
			final Styles styles = context.getStyleSheet().getStyles(getNode());
			horizonalSpacing = styles.getBorderSpacing().getHorizontal();
			verticalSpacing = styles.getBorderSpacing().getVertical();

			// width available for columns
			// Since we apply margins/borders/padding to the TableBodyBox,
			// they're
			// not reflected in the width of this box. Thus, we subtract them
			// here
			availableWidth -= +styles.getMarginLeft().get(myWidth) + styles.getBorderLeftWidth() + styles.getPaddingLeft().get(myWidth) + styles.getPaddingRight().get(myWidth)
					+ styles.getBorderRightWidth() + styles.getMarginRight().get(myWidth);
		}

		int totalColumnWidth = horizonalSpacing;
		final int columnWidth = (availableWidth - horizonalSpacing * (columnCount + 1)) / columnCount;
		for (int i = 0; i < columnWidths.length - 1; i++) {
			System.err.print(" " + columnWidth);
			columnWidths[i] = columnWidth;
			totalColumnWidth += columnWidth + horizonalSpacing;
		}

		// Due to rounding errors in the expression above, we calculate the
		// width of the last column separately, to make it exact.
		columnWidths[columnWidths.length - 1] = availableWidth - totalColumnWidth - horizonalSpacing;

	}
}
