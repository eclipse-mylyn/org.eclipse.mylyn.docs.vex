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

import org.eclipse.ui.IActionBars;

/**
 * Implemented by outline providers that use ToolBar actions.
 */
public interface IToolBarContributor {

	/**
	 * Handler for toggle states.
	 * 
	 * @param commandId
	 *            The commandId that defines the state
	 * @param state
	 *            The states new values
	 */
	public void setState(String commandId, boolean state);

	/**
	 * Register the ToolBarActions supoorted by this OutlineProvider
	 * 
	 * @param page
	 *            The DocumentOutlinePage
	 * @param actionBars
	 *            The page sites ActionBars
	 */
	public void registerToolBarActions(DocumentOutlinePage page, IActionBars actionBars);
}
