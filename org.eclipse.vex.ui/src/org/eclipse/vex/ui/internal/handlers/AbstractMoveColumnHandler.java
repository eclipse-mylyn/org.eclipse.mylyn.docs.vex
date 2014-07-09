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
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;

/**
 * Moves the current table column either to the left or to the right.
 *
 * @see MoveColumnLeftHandler
 * @see MoveColumnRightHandler
 */
public abstract class AbstractMoveColumnHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final VexWidget widget = VexHandlerUtil.computeWidget(event);
		final VexHandlerUtil.RowColumnInfo rcInfo = VexHandlerUtil.getRowColumnInfo(widget);

		if (rcInfo == null || !movingPossible(rcInfo)) {
			return null;
		}

		widget.doWork(new Runnable() {
			@Override
			public void run() {
				final List<Object> sourceCells = new ArrayList<Object>();
				final List<Object> targetCells = new ArrayList<Object>();
				computeCells(widget, rcInfo, sourceCells, targetCells);
				swapCells(widget, sourceCells, targetCells);
			}
		}, true);
		return null;
	}

	/**
	 * @return {@code true} to move column to the right or {@code false} to move column to the left
	 */
	protected abstract boolean moveRight();

	private void computeCells(final VexWidget widget, final VexHandlerUtil.RowColumnInfo rcInfo, final List<Object> sourceCells, final List<Object> targetCells) {

		VexHandlerUtil.iterateTableCells(widget, new TableCellCallbackAdapter() {

			private Object leftCell;

			@Override
			public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {

				if (leftCell(cellIndex, rcInfo.cellIndex)) {
					leftCell = cell;
				} else if (rightCell(cellIndex, rcInfo.cellIndex)) {
					sourceCells.add(moveRight() ? cell : leftCell);
					targetCells.add(moveRight() ? leftCell : cell);
				}
			}
		});
	}

	private void swapCells(final VexWidget widget, final List<Object> sourceCells, final List<Object> targetCells) {

		// Iterate the deletions in reverse, so that we don't mess up offsets
		// that are in anonymous cells, which are not stored as positions.
		for (int i = sourceCells.size() - 1; i >= 0; i--) {

			// Also, to preserve the current caret position, we don't cut and
			// paste the current column. Instead, we cut the target column and
			// paste it to the source column.
			final Object source = sourceCells.get(i);
			final Object target = targetCells.get(i);
			final ContentPositionRange sourceRange = VexHandlerUtil.getOuterRange(source);
			final ContentPositionRange targetRange = VexHandlerUtil.getOuterRange(target);
			widget.moveTo(moveRight() ? targetRange.getStartPosition() : targetRange.getEndPosition());
			widget.savePosition(new Runnable() {
				@Override
				public void run() {
					widget.moveTo(sourceRange.getStartPosition());
					widget.moveTo(sourceRange.getEndPosition(), true);
					widget.cutSelection();
				}
			});
			widget.paste();

		}
	}

	/**
	 * @param rcInfo
	 *            row/column info of the current selected element
	 * @return {@code true} if moving is possible (there must be a column in moving direction to swap with), otherwise
	 *         {@code false}
	 */
	protected abstract boolean movingPossible(VexHandlerUtil.RowColumnInfo rcInfo);

	private boolean leftCell(final int currentIndex, final int sourceIndex) {
		return moveRight() ? currentIndex == sourceIndex : currentIndex == sourceIndex - 1;
	}

	private boolean rightCell(final int currentIndex, final int sourceIndex) {
		return moveRight() ? currentIndex == sourceIndex + 1 : currentIndex == sourceIndex;
	}

}
