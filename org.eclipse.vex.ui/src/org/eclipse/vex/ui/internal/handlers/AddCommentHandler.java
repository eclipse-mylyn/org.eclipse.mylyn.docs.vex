/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.dom.CommentElement;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * @author Florian Thienel
 */
public class AddCommentHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(VexWidget widget) throws ExecutionException {
		widget.insertElement(new CommentElement());
	}

}
