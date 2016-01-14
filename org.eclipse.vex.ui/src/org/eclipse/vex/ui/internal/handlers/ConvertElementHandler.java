/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.ui.internal.swt.ContentAssist;

/**
 * Shows the content assist to convert current element ({@link MorphAssistant}).
 */
public class ConvertElementHandler extends AbstractVexWidgetHandler implements IElementUpdater {

	/** ID of the corresponding convert element command. */
	public static final String COMMAND_ID = "org.eclipse.vex.ui.ConvertElementCommand"; //$NON-NLS-1$

	private static final String LABEL_ID = "command.convertElement.dynamicName"; //$NON-NLS-1$

	@Override
	public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final Rectangle caretArea = VexHandlerUtil.getCaretArea(event);
		final Point location = new Point(caretArea.getX(), caretArea.getY());
		ContentAssist.openQuickFixContentAssist(shell, editor, location);
	}

	@Override
	public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
		updateElement(element, parameters, LABEL_ID, LABEL_ID);
	}

}
