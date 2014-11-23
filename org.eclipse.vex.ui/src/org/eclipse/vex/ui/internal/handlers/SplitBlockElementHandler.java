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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
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
	public void execute(final VexWidget widget) throws ExecutionException {
		if (widget.isReadOnly()) {
			return;
		}

		final INode currentNode = widget.getCurrentNode();
		if (widget.canSplit()) {
			splitElement(widget, currentNode);
		} else {
			final ContentPosition targetPosition = currentNode.getEndPosition().moveBy(1);
			if (widget.getDocument().getRootElement().containsPosition(targetPosition)) {
				widget.moveTo(targetPosition);
				ContentAssist.openAddElementsContentAssist(widget);
			}
		}
	}

	/**
	 * Splits the given element.
	 *
	 * @param vexWidget
	 *            IVexWidget containing the document.
	 * @param node
	 *            Node to be split.
	 */
	protected void splitElement(final IVexWidget vexWidget, final INode node) {
		vexWidget.doWork(new Runnable() {
			@Override
			public void run() {
				final boolean isPreformatted = vexWidget.getWhitespacePolicy().isPre(node);
				if (isPreformatted) {
					vexWidget.insertText("\n");
				} else {
					vexWidget.split();
				}
			}
		});
	}
}
