/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.outline;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;

public class OutlineFilter extends ViewerFilter {

	// Binary coded - use 2-4-8... for next constants
	public static final int FILTER_ID_INLINE_ELEMENTS = 1;

	private int activeFilters = 0;
	private StyleSheet styleSheet;

	public OutlineFilter(final StyleSheet styleSheet) {
		super();
		this.styleSheet = styleSheet;
	}

	public void setStyleSheet(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (!(element instanceof INode)) {
			return true;
		}

		final IElement domElement = (IElement) element;
		if (hasFilter(FILTER_ID_INLINE_ELEMENTS)) {
			if (styleSheet.getStyles(domElement).getDisplay().equals(CSS.INLINE)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param filterId
	 *            The filter ID to add as active filter.
	 */
	public final void addFilter(final int filterId) {
		activeFilters |= filterId;
	}

	/**
	 * @param filterId
	 *            The filter ID to remove from active filters.
	 */
	public final void removeFilter(final int filterId) {
		activeFilters &= -1 ^ filterId;
	}

	/**
	 * Tests if a filter is active
	 * 
	 * @param filter
	 *            The filter id to test.
	 * @return <code>true</true> if the given filter is active.
	 */
	public final boolean hasFilter(final int filter) {
		return (activeFilters & filter) != 0;
	}

}
