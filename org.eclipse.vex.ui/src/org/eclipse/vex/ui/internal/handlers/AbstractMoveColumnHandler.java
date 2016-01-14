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
import org.eclipse.vex.core.internal.layout.LayoutUtils.ElementOrRange;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
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
		final IDocumentEditor editor = VexHandlerUtil.getDocumentEditor(event);
		final VexHandlerUtil.RowColumnInfo rcInfo = VexHandlerUtil.getRowColumnInfo(editor);

		if (rcInfo == null || !movingPossible(rcInfo)) {
			return null;
		}

		editor.doWork(new Runnable() {
			@Override
			public void run() {
				final List<Object> sourceCells = new ArrayList<Object>();
				final List<Object> targetCells = new ArrayList<Object>();
				computeCells(editor, rcInfo, sourceCells, targetCells);
				swapCells(editor, sourceCells, targetCells);
			}
		}, true);
		return null;
	}

	/**
	 * @return {@code true} to move column to the right or {@code false} to move column to the left
	 */
	protected abstract boolean moveRight();

	private void computeCells(final IDocumentEditor editor, final VexHandlerUtil.RowColumnInfo rcInfo, final List<Object> sourceCells, final List<Object> targetCells) {

		VexHandlerUtil.iterateTableCells(editor, new TableCellCallbackAdapter() {

			private Object leftCell;

			@Override
			public void onCell(final ElementOrRange row, final ElementOrRange cell, final int rowIndex, final int cellIndex) {

				if (leftCell(cellIndex, rcInfo.cellIndex)) {
					leftCell = cell;
				} else if (rightCell(cellIndex, rcInfo.cellIndex)) {
					sourceCells.add(moveRight() ? cell : leftCell);
					targetCells.add(moveRight() ? leftCell : cell);
				}
			}
		});
	}

	private void swapCells(final IDocumentEditor editor, final List<Object> sourceCells, final List<Object> targetCells) {

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
			editor.moveTo(moveRight() ? targetRange.getStartPosition() : targetRange.getEndPosition());
			editor.savePosition(new Runnable() {
				@Override
				public void run() {
					editor.moveTo(sourceRange.getStartPosition());
					editor.moveTo(sourceRange.getEndPosition(), true);
					editor.cutSelection();
				}
			});
			editor.paste();

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
