/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - Added methods to support AbstractUndoableEdit
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

/**
 * Represents a change to a document (an edit) that can be undone and redone. Typically, the edit source (i.e. the
 * document) will have a flag that is set by the edit to indicate that the edits being performed are part of an undo or
 * redo. The document can use this to supress events to any IUndoableEventListeners during undo/redo.
 */
public interface IUndoableEdit {

	/**
	 * Try to combine the given edit event with this one. The common use-case involves a user typing sequential
	 * characters into the document: all such insertions should be undone in one go.
	 *
	 * @param edit
	 *            IUndoableEdit to be combined with this one.
	 * @return True if the given edit was successfully combined into this one.
	 */
	public boolean combine(IUndoableEdit edit);

	/**
	 * Redo the edit.
	 */
	public void redo() throws CannotRedoException;

	/**
	 * Undo the edit.
	 */
	public void undo() throws CannotUndoException;

	/**
	 * Determines whether it would be possible to undo this editing action.
	 *
	 * @return <code>true</code> to indicate that this edit can be undone, <code>false</code> otherwise.
	 */
	public boolean canUndo();

	/**
	 * Determines whether it would be possible to redo this editing action.
	 *
	 * @return <code>true</code> to indicate that this edit can be redone, <code>false</code> otherwise.
	 */
	public boolean canRedo();
}
