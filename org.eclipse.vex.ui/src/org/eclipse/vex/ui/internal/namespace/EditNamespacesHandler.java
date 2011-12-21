/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.namespace;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.vex.ui.internal.handlers.AbstractVexWidgetHandler;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * @author Florian Thienel
 */
public class EditNamespacesHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		final EditNamespacesDialog dialog = new EditNamespacesDialog(widget.getShell(), widget.getCurrentElement());
		if (dialog.open() == Window.OK) {
			; // TODO maybe we have to refresh something in the widget... 
		}
	}

}
