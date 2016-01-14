/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.swt.ProcessingInstrDialog;

public class SetProcessingInstructionTargetHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		if (editor.getCurrentNode() instanceof IProcessingInstruction) {
			final IProcessingInstruction pi = (IProcessingInstruction) editor.getCurrentNode();
			final Shell shell = VexPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			final ProcessingInstrDialog dialog = new ProcessingInstrDialog(shell, pi.getTarget());
			dialog.create();

			if (dialog.open() == Window.OK) {
				try {
					editor.editProcessingInstruction(dialog.getTarget(), null);
				} catch (final DocumentValidationException e) {

				}
			}
		}
	}
}
