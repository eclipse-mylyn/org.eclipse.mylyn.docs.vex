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

import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.layout.TableRowBox;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Navigates to the next table cell (usual shortcut: {@code Tab}).
 * 
 * @see PreviousTableCellHandler
 */
public class NextTableCellHandler extends AbstractNavigateTableCellHandler {

	@Override
	protected void navigate(final VexWidget widget, final TableRowBox row, final int offset) {
		final Box[] cells = row.getChildren();

		// in this row
		for (final Box cell : cells) {
			if (cell.getStartOffset() > offset) {
				widget.moveTo(cell.getStartOffset());
				widget.moveTo(cell.getEndOffset(), true);
				return;
			}
		}

		// in other row
		final Box[] rows = row.getParent().getChildren();
		for (final Box boxRow : rows) {
			if (boxRow.getStartOffset() > offset) {
				final Box[] rowCells = boxRow.getChildren();
				if (rowCells.length > 0) {
					final Box cell = rowCells[0];
					widget.moveTo(cell.getStartOffset());
					widget.moveTo(cell.getEndOffset(), true);
				} else {
					System.out.println("TODO - dup row into new empty row");
				}
				return;
			}
		}

		// We didn't find a "next row", so let's dup the current one
		VexHandlerUtil.duplicateTableRow(widget, row);

	}

}
