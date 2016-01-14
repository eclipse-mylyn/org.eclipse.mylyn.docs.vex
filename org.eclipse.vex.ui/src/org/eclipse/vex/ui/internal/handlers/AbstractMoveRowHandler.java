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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.ui.internal.handlers.VexHandlerUtil.SelectedRows;

/**
 * Moves the current table row either down below its next sibling or up above its previous sibling.
 *
 * @see MoveRowUpHandler
 * @see MoveRowDownHandler
 */
public abstract class AbstractMoveRowHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		final VexHandlerUtil.SelectedRows selected = VexHandlerUtil.getSelectedTableRows(editor);

		if (selected.getRows() == null || targetRow(selected) == null) {
			return;
		}

		editor.doWork(new Runnable() {
			@Override
			public void run() {
				final ContentPositionRange range = VexHandlerUtil.getOuterRange(targetRow(selected));
				editor.moveTo(range.getStartPosition());
				editor.moveTo(range.getEndPosition(), true);
				editor.cutSelection();

				editor.moveTo(target(selected));
				editor.paste();
			}

		}, true);
	}

	/**
	 * @param selected
	 *            current selected row
	 * @return the row with which to switch current row
	 */
	protected abstract Object targetRow(SelectedRows selected);

	/**
	 * @param selected
	 *            current selected row
	 * @return position where to move to
	 */
	protected abstract ContentPosition target(SelectedRows selected);

}
