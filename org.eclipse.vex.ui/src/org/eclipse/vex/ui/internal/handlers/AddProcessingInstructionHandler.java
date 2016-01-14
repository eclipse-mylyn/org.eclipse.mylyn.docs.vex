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
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.swt.ProcessingInstrDialog;

public class AddProcessingInstructionHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		if (editor.canInsertProcessingInstruction()) {
			final Shell shell = VexPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			final ProcessingInstrDialog dialog = new ProcessingInstrDialog(shell, "");
			dialog.create();

			if (dialog.open() == Window.OK && editor.canInsertProcessingInstruction()) {
				try {
					editor.insertProcessingInstruction(dialog.getTarget());
				} catch (final DocumentValidationException e) {

				}
			}
		}
	}
}
