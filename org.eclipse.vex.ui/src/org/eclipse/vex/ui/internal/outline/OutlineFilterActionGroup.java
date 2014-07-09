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

import java.util.ArrayList;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.ui.internal.PluginImages;

public class OutlineFilterActionGroup extends ActionGroup {
	private static final String KEY_HIDEINLINEELEMENTS = "hideInlineElements"; //$NON-NLS-1$
	private static final String KEY_SHOWCOMMENTS = "showComments"; //$NON-NLS-1$
	private static final String KEY_SHOWPROCINSTR = "showProcInstr"; //$NON-NLS-1$

	private final OutlineFilterAction[] filterActions;
	private final OutlineFilter filter;

	private StructuredViewer viewer;

	public OutlineFilterActionGroup(final StyleSheet styleSheet) {

		final ArrayList<OutlineFilterAction> actions = new ArrayList<OutlineFilterAction>(4);

		final OutlineFilterAction hideInlineElements = new OutlineFilterAction(this, OutlineFilter.FILTER_ID_INCLUDE_INLINE_ELEMENTS, KEY_HIDEINLINEELEMENTS, null);
		hideInlineElements.setImageDescriptor(PluginImages.DESC_SHOW_INLINE_ELEMENTS);
		actions.add(hideInlineElements);

		final OutlineFilterAction showComments = new OutlineFilterAction(this, OutlineFilter.FILTER_ID_INCLUDE_COMMENTS, KEY_SHOWCOMMENTS, null);
		showComments.setImageDescriptor(PluginImages.DESC_XML_COMMENT);
		actions.add(showComments);

		final OutlineFilterAction showProcInstr = new OutlineFilterAction(this, OutlineFilter.FILTER_ID_INCLUDE_PROC_INSTR, KEY_SHOWPROCINSTR, null);
		showProcInstr.setImageDescriptor(PluginImages.DESC_XML_PROC_INSTR);
		actions.add(showProcInstr);

		filter = new OutlineFilter(styleSheet);
		filterActions = actions.toArray(new OutlineFilterAction[actions.size()]);

		// Init filters from preferences
		for (final OutlineFilterAction action : filterActions) {
			setFilter(action.getFilterId(), action.isChecked());
		}
	}

	public void setFilter(final int filterId, final boolean isSet) {
		if (isSet) {
			filter.addFilter(filterId);
		} else {
			filter.removeFilter(filterId);
		}

		if (viewer != null) {
			final ISelection currentSelection = viewer.getSelection();
			viewer.getControl().setRedraw(false);
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
					viewer.setSelection(currentSelection, true);
				}
			});
			viewer.getControl().setRedraw(true);
		}
	}

	public void setViewer(final StructuredViewer viewer) {
		this.viewer = viewer;
		viewer.addFilter(filter);
	}

	public void setStyleSheet(final StyleSheet styleSheet) {
		filter.setStyleSheet(styleSheet);
	}

	public boolean isElementFiltered(final Object element) {
		return !filter.select(viewer, null, element);
	}

	@Override
	public void fillActionBars(final IActionBars actionBars) {
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		for (final OutlineFilterAction action : filterActions) {
			toolBarManager.add(action);
		}
	}

}
