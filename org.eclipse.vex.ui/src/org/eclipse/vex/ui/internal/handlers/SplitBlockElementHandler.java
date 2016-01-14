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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.ui.internal.swt.ContentAssist;

/**
 * Splits the current block element, for instance to create new block/paragraph or table cell (usually by hitting the
 * {@code Return} key).
 *
 * @see SplitItemHandler
 */
public class SplitBlockElementHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		if (editor.isReadOnly()) {
			return;
		}

		final INode currentNode = editor.getCurrentNode();
		if (editor.canSplit()) {
			splitElement(editor, currentNode);
		} else {
			final ContentPosition targetPosition = currentNode.getEndPosition().moveBy(1);
			if (editor.getDocument().getRootElement().containsPosition(targetPosition)) {
				editor.moveTo(targetPosition);
				final Shell shell = HandlerUtil.getActiveShell(event);
				final Rectangle caretArea = VexHandlerUtil.getCaretArea(event);
				final Point location = new Point(caretArea.getX(), caretArea.getY());
				ContentAssist.openAddElementsContentAssist(shell, editor, location);
			}
		}
	}

	/**
	 * Splits the given element.
	 *
	 * @param editor
	 *            IVexWidget containing the document.
	 * @param node
	 *            Node to be split.
	 */
	protected void splitElement(final IDocumentEditor editor, final INode node) {
		editor.doWork(new Runnable() {
			@Override
			public void run() {
				final boolean isPreformatted = editor.getWhitespacePolicy().isPre(node);
				if (isPreformatted) {
					editor.insertText("\n");
				} else {
					editor.split();
				}
			}
		});
	}
}
