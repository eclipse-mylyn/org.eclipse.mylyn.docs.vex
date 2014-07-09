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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * Duplicates current element or current selection.
 */
public class DuplicateSelectionHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		widget.doWork(new Runnable() {
			@Override
			public void run() {
				if (!widget.hasSelection()) {
					final INode node = widget.getCurrentNode();

					// Can't duplicate the document
					if (node.getParent() == null) {
						return;
					}

					widget.moveTo(node.getStartPosition());
					widget.moveTo(node.getEndPosition().moveBy(+1), true);
				}

				widget.copySelection();
				final ContentPosition startPosition = widget.getSelectedPositionRange().getEndPosition().moveBy(1);
				widget.moveTo(startPosition);
				widget.paste();
				final ContentPosition endPosition = widget.getCaretPosition();
				widget.moveTo(startPosition);
				widget.moveTo(endPosition, true);
			}
		});
	}

}
