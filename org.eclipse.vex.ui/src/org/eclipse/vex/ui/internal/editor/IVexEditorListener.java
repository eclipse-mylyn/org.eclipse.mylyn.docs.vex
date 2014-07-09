/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - added styleChanged event
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

/**
 * Event interface through which VexEditor events are published.
 */
public interface IVexEditorListener {

	/**
	 * Called after the editor has successfully loaded a document.
	 */
	public void documentLoaded(VexEditorEvent event);

	/**
	 * Called before the editor unloads a document. Note that the editor may be disposing of the corresponding
	 * VexWidget, so any registered listeners on the widget should unregister in this event.
	 */
	public void documentUnloaded(VexEditorEvent event);

	/**
	 * Called after the editor switched to another stylesheet.
	 */
	public void styleChanged(VexEditorEvent event);
}
