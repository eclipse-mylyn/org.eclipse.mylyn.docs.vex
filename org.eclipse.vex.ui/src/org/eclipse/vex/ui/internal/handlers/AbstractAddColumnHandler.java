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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.dom.CopyOfElement;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Inserts a single table column before (left of) or after (right of) the current one.
 * 
 * @see AddColumnLeftHandler
 * @see AddColumnRightHandler
 */
public abstract class AbstractAddColumnHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final VexWidget widget = VexHandlerUtil.computeWidget(event);
		widget.doWork(new Runnable() {
			public void run() {
				try {
					addColumn(widget);
				} catch (final ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return null;
	}

	private void addColumn(final VexWidget widget) throws ExecutionException {
		final int indexToDup = VexHandlerUtil.getCurrentColumnIndex(widget);

		// adding possible?
		if (indexToDup == -1) {
			return;
		}

		final List<IElement> cellsToDup = new ArrayList<IElement>();
		VexHandlerUtil.iterateTableCells(widget, new TableCellCallbackAdapter() {
			@Override
			public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {
				if (cellIndex == indexToDup && cell instanceof IElement) {
					cellsToDup.add((IElement) cell);
				}
			}
		});

		int finalOffset = -1;
		for (final IElement element : cellsToDup) {
			if (finalOffset == -1) {
				finalOffset = element.getStartOffset() + 1;
			}
			widget.moveTo(addBefore() ? element.getStartOffset() : element.getEndOffset() + 1);
			widget.insertElement(element.getQualifiedName()).accept(new CopyOfElement(element));
		}

		if (finalOffset != -1) {
			widget.moveTo(finalOffset);
		}
	}

	/**
	 * @return {@code true} to add new column before (left of) current column or {@code false} to add new column after
	 *         (right of) current column
	 */
	protected abstract boolean addBefore();

}
