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
import org.eclipse.vex.core.internal.layout.BlockBox;
import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.widget.IBoxFilter;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Moves the current selection or block element above the previous sibling.
 * 
 * TODO WORK IN PROGRESS.
 */
public class MoveSelectionUpHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		// First we determine whether we should expand the selection
		// to contain an entire block box.

		// Find the lowest block box that completely contains the selection
		final Box box = widget.findInnermostBox(new IBoxFilter() {
			public boolean matches(final Box box) {
				final ContentRange selectedRange = widget.getSelectedRange();
				return box instanceof BlockBox && box.getNode() != null && new ContentRange(box.getStartOffset(), box.getEndOffset()).contains(selectedRange);
			}
		});

		final Box[] children = box.getChildren();
		if (children.length > 0 && children[0] instanceof BlockBox) {
			// The found box contains other block children, so we do NOT have to
			// expand the selection
		} else {
			// Expand selection to the containing box

			// (Note: This "if" is caused by the fact that getStartOffset is
			// treated differently between elements and boxes. Boxes own their
			// startOffset, while elements don't own theirs. Perhaps we should
			// fix this by having box.getStartOffset() return
			// box.getStartPosition() + 1, but this would be a VERY large
			// change.)
			System.out.println("Box is " + box);
			final INode node = box.getNode();
			if (node != null) {
				widget.moveTo(node.getEndOffset() + 1);
				widget.moveTo(node.getStartOffset(), true);

			} else {
				widget.moveTo(box.getEndOffset() + 1);
				widget.moveTo(box.getStartOffset(), true);
			}
		}

		// final int previousSiblingStart =
		// ActionUtils.getPreviousSiblingStart(vexWidget);
		//
		// vexWidget.doWork(new IRunnable() {
		// public void run() throws Exception {
		// vexWidget.cutSelection();
		// vexWidget.moveTo(previousSiblingStart);
		// vexWidget.paste();
		// vexWidget.moveTo(previousSiblingStart, true);
		// }
		// });
	}

}
