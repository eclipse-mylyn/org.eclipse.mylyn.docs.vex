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
import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * Moves the current selection or block element above the previous sibling.
 * 
 * TODO WORK IN PROGRESS.
 */
public class MoveSelectionUpHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		final ContentRange selectedRange = widget.getSelectedRange();
		final IAxis<? extends IParent> parentsContainingSelection = widget.getCurrentElement().ancestors().matching(containingRange(selectedRange));

		// expand the selection until a parent has other block-children
		final StyleSheet stylesheet = widget.getStyleSheet();
		for (final IParent parent : parentsContainingSelection) {
			final IAxis<? extends INode> blockChildren = parent.children().matching(displayedAsBlock(stylesheet));
			if (blockChildren.isEmpty()) {
				widget.moveTo(parent.getStartOffset(), false);
				widget.moveTo(parent.getEndOffset(), true);
			} else {
				break;
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

	private static IFilter<INode> containingRange(final ContentRange range) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return node.getRange().contains(range);
			}
		};
	}

	private static IFilter<INode> displayedAsBlock(final StyleSheet stylesheet) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return stylesheet.getStyles(node).isBlock();
			}
		};
	}
}
