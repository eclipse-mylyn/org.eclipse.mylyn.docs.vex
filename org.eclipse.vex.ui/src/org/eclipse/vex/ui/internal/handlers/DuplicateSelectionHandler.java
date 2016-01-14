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
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * Duplicates current element or current selection.
 */
public class DuplicateSelectionHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		editor.doWork(new Runnable() {
			@Override
			public void run() {
				if (!editor.hasSelection()) {
					final INode node = editor.getCurrentNode();

					// Can't duplicate the document
					if (node.getParent() == null) {
						return;
					}

					editor.moveTo(node.getStartPosition());
					editor.moveTo(node.getEndPosition().moveBy(+1), true);
				}

				editor.copySelection();
				final ContentPosition startPosition = editor.getSelectedPositionRange().getEndPosition().moveBy(1);
				editor.moveTo(startPosition);
				editor.paste();
				final ContentPosition endPosition = editor.getCaretPosition();
				editor.moveTo(startPosition);
				editor.moveTo(endPosition, true);
			}
		});
	}

}
