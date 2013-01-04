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
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.dom.CopyVisitor;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Inserts one or more table rows either above or below the currently selected one(s). If more than one row is selected
 * the same number of new rows will be created.
 * 
 * @see AddRowBelowHandler
 * @see AddRowAboveHandler
 */
public abstract class AbstractAddRowHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		widget.doWork(new Runnable() {
			public void run() {
				addRow(widget);
			}
		});
	}

	/**
	 * @return {@code true} to add new table row above current row or {@code false} to add new row below current row
	 */
	protected abstract boolean addAbove();

	private void addRow(final VexWidget widget) {
		final List<RowCells> rowCellsToInsert = new ArrayList<RowCells>();

		VexHandlerUtil.iterateTableCells(widget, new ITableCellCallback() {

			private boolean selectedRow;
			private List<Object> cellsToInsert;

			public void startRow(final Object row, final int rowIndex) {
				selectedRow = VexHandlerUtil.elementOrRangeIsPartiallySelected(widget, row);

				if (selectedRow) {
					cellsToInsert = new ArrayList<Object>();
				}
			}

			public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {
				if (selectedRow) {
					cellsToInsert.add(cell);
				}
			}

			public void endRow(final Object row, final int rowIndex) {
				if (selectedRow) {
					rowCellsToInsert.add(new RowCells(row, cellsToInsert));
				}
			}

		});

		// something to do?
		if (rowCellsToInsert.isEmpty()) {
			return;
		}

		// save the caret offset to return inside the first table cell after
		// row has been added
		final RowCells firstRow = rowCellsToInsert.get(0);
		final int outerOffset = VexHandlerUtil.getOuterRange(firstRow.row).getStart();
		final Object firstInner = firstRow.cells.isEmpty() ? firstRow.row : firstRow.cells.get(0);
		final int innerOffset = VexHandlerUtil.getInnerRange(firstInner).getStart();
		final int insertOffset = addAbove() ? VexHandlerUtil.getOuterRange(firstRow.row).getStart() : VexHandlerUtil.getOuterRange(rowCellsToInsert.get(rowCellsToInsert.size() - 1).row).getEnd();

		// (innerOffset - outerOffset) represents the final offset of
		// the caret, relative to the insertion point of the new rows
		final int finalOffset = insertOffset + innerOffset - outerOffset;
		widget.moveTo(insertOffset);

		final CopyVisitor copyVisitor = new CopyVisitor();
		for (final RowCells rowCells : rowCellsToInsert) {
			if (rowCells.row instanceof Element) {
				widget.insertElement((Element) ((Element) rowCells.row).accept(copyVisitor));
			}

			//cells that are to be inserted.
			for (final Object cell : rowCells.cells) {
				if (cell instanceof Element) {
					widget.insertElement((Element) ((Element) cell).accept(copyVisitor));
					widget.moveBy(+1);
				} else {
					widget.insertText(" ");
				}
			}

			if (rowCells.row instanceof Element) {
				widget.moveBy(+1);
			}
		}

		// move inside first inserted table cell
		widget.moveTo(finalOffset);
	}

	/** Represents a row and its cells. */
	private static class RowCells {

		/** The row. */
		private final Object row;

		/** All cell objects that belong to this row. */
		private final List<Object> cells;

		private RowCells(final Object row, final List<Object> cells) {
			this.row = row;
			this.cells = cells;
		}

	}

}
