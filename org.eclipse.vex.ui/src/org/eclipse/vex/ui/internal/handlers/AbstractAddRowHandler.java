/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - use common method from VexHandlerUtil
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.IElement;

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
			@Override
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
		// Find the parent table row
		final IElement currentRow = VexHandlerUtil.getCurrentTableRow(widget);

		// Do nothing is the caret is not inside a table row
		if (currentRow == widget.getDocument().getRootElement()) {
			return;
		}

		VexHandlerUtil.duplicateTableRow(widget, currentRow, addAbove());

	}
}
