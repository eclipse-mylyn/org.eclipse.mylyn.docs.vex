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
package org.eclipse.vex.ui.internal.editor;

/**
 * An abstract adapter class for receiving VexEditor events. The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 */
public abstract class EditorEventAdapter implements IVexEditorListener {

	public void documentLoaded(final VexEditorEvent event) {
	}

	public void documentUnloaded(final VexEditorEvent event) {
	}

	public void styleChanged(final VexEditorEvent event) {
	}

}
