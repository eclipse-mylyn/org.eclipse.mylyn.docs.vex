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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * Navigates either to the next or previous table cell (usual shortcut: {@code Tab} or {@code Shift+Tab}).
 *
 * @see PreviousTableCellHandler
 */
public abstract class AbstractNavigateTableCellHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		final IAxis<? extends IParent> parentTableRows = editor.getCurrentElement().ancestors().matching(displayedAsTableRow(editor.getTableModel().getStyleSheet()));
		final IElement tableRow;
		try {
			tableRow = (IElement) parentTableRows.first();
		} catch (final NoSuchElementException e) {
			return;
		}

		final ContentPosition position = editor.getCaretPosition();
		navigate(editor, tableRow, position);
	}

	/**
	 * Navigates either to the next or previous table cell.
	 *
	 * @param editor
	 *            the Vex widget containing the document
	 * @param tableRow
	 *            the current row
	 * @param offset
	 *            the current offset
	 */
	protected abstract void navigate(IDocumentEditor editor, IElement tableRow, ContentPosition position);

	private static IFilter<INode> displayedAsTableRow(final StyleSheet stylesheet) {
		return new IFilter<INode>() {
			@Override
			public boolean matches(final INode node) {
				return stylesheet.getStyles(node).getDisplay().equals(CSS.TABLE_ROW);
			}
		};
	}

}
