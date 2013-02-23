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

import java.util.NoSuchElementException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Navigates either to the next or previous table cell (usual shortcut: {@code Tab} or {@code Shift+Tab}).
 * 
 * @see PreviousTableCellHandler
 */
public abstract class AbstractNavigateTableCellHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		final IAxis<? extends IParent> parentTableRows = widget.getCurrentElement().ancestors().matching(displayedAsTableRow(widget.getStyleSheet()));
		final IElement tableRow;
		try {
			tableRow = (IElement) parentTableRows.first();
		} catch (final NoSuchElementException e) {
			return;
		}

		final int offset = widget.getCaretOffset();
		navigate(widget, tableRow, offset);
	}

	/**
	 * Navigates either to the next or previous table cell.
	 * 
	 * @param widget
	 *            the Vex widget containing the document
	 * @param tableRow
	 *            the current row
	 * @param offset
	 *            the current offset
	 */
	protected abstract void navigate(VexWidget widget, IElement tableRow, int offset);

	private static IFilter<INode> displayedAsTableRow(final StyleSheet stylesheet) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return stylesheet.getStyles(node).getDisplay().equals(CSS.TABLE_ROW);
			}
		};
	}

}
