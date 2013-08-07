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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.editor.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * The Action used by all outline toggle buttons.
 */
public class ToolBarToggleAction extends Action {

	private final DocumentOutlinePage outlinePage;
	private final String commandId;

	/**
	 * @param outlinePage
	 *            The DocumentOutlinePage that uses this action.
	 * @param stateName
	 *            The name of the toggle state. Constant defined in {@link IToolBarContributor}.
	 * @param imageDescriptor
	 *            The image to be displayed
	 */
	public ToolBarToggleAction(final DocumentOutlinePage outlinePage, final String stateName, final ImageDescriptor imageDescriptor) {
		super(Messages.getString("DocumentOutlinePage.action." + stateName + ".title"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
		this.outlinePage = outlinePage;
		commandId = stateName;
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		final boolean isChecked = preferences.getBoolean(stateName, true);
		setChecked(isChecked);
		setToolTipText(Messages.getString("DocumentOutlinePage.action." + stateName + ".tooltip")); //$NON-NLS-1$
		setDescription(Messages.getString("DocumentOutlinePage.action." + stateName + ".tooltip")); //$NON-NLS-1$
		setImageDescriptor(imageDescriptor);
	}

	@Override
	public void run() {
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		preferences.putBoolean(commandId, isChecked());
		outlinePage.setViewState(commandId, isChecked());
	}

}
