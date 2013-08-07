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
import org.eclipse.ui.PlatformUI;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.editor.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * The Action used by all outline toggle buttons.
 */
public class OutlineFilterAction extends Action {

	private final OutlineFilterActionGroup actionGroup;
	private final int filterId;
	private final String messageKey;

	/**
	 * @param outlinePage
	 *            The ActionGroup that contians this action.
	 * @param filter
	 *            The binary ID of this filter. See {@link OutlineFilter}.
	 * @param messageKey
	 *            The key used to get message texts and preference values.
	 * @param contextHelpId
	 *            The id to be registered with the help system. May be null.
	 */
	public OutlineFilterAction(final OutlineFilterActionGroup actionGroup, final int filterId, final String messageKey, final String contextHelpId) {
		super(Messages.getString("DocumentOutlinePage.action." + messageKey + ".title"), IAction.AS_CHECK_BOX);
		this.actionGroup = actionGroup;
		this.filterId = filterId;
		this.messageKey = messageKey;
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		setChecked(preferences.getBoolean(messageKey, false));
		setToolTipText(Messages.getString("DocumentOutlinePage.action." + messageKey + ".tooltip")); //$NON-NLS-1$
		setDescription(Messages.getString("DocumentOutlinePage.action." + messageKey + ".tooltip")); //$NON-NLS-1$
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, contextHelpId);
		}
	}

	@Override
	public void run() {
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		preferences.putBoolean(messageKey, isChecked());
		actionGroup.setFilter(filterId, isChecked());
	}

}
