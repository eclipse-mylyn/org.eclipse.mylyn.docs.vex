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
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * Splits the nearest enclosing table row or list item (usually by hitting {@code Shift+Return}). If a table row is
 * being split, empty versions of the current row's cells are created.
 * 
 * @see SplitBlockElementHandler
 */
public class SplitItemHandler extends SplitBlockElementHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		final StyleSheet stylesheet = widget.getStyleSheet();
		final IAxis<? extends IParent> parentTableRowOrListItems = widget.getCurrentElement().ancestors().matching(displayedAsTableRowOrListItem(stylesheet));

		final IParent firstTableRowOrListItem;
		try {
			firstTableRowOrListItem = parentTableRowOrListItems.first();
		} catch (final NoSuchElementException e) {
			return;
		}

		final String displayStyle = stylesheet.getStyles(firstTableRowOrListItem).getDisplay();
		if (displayStyle.equals(CSS.TABLE_ROW)) {
			new AddRowBelowHandler().execute(widget);
		} else if (displayStyle.equals(CSS.LIST_ITEM)) {
			splitElement(widget, firstTableRowOrListItem);
		}
	}

	private final IFilter<INode> displayedAsTableRowOrListItem(final StyleSheet stylesheet) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				final String displayStyle = stylesheet.getStyles(node).getDisplay();
				if (displayStyle.equals(CSS.TABLE_ROW)) {
					return true;
				}
				if (displayStyle.equals(CSS.LIST_ITEM)) {
					return true;
				}
				return false;
			}
		};
	}
}
