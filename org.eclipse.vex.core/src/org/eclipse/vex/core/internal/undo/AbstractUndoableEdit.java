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
package org.eclipse.vex.core.internal.undo;

/**
 * A default implementation of <code>IUndoableEdit</code> that can be used as a base for implementing editing
 * operations.
 * 
 */
public abstract class AbstractUndoableEdit implements IUndoableEdit {

	/**
	 * Indicates whether this editing action has been executed. A value of <code>true</code> means that the action was
	 * performed, or that a redo operation was successful.
	 */
	private boolean hasBeenDone = false;

	public boolean combine(final IUndoableEdit edit) {
		if (hasBeenDone) {
			return performCombine(edit);
		}
		return false;
	}

	/**
	 * To be implemented by subclasses to perform the actual Combine action. The default implementation always returns
	 * <code>false</code>.
	 * 
	 * @param edit
	 *            The edit to combine with this one
	 * @return <code>true</code> if the given edit has been combined with this one.
	 */
	protected boolean performCombine(final IUndoableEdit edit) {
		return false;
	}

	public void redo() throws CannotRedoException {
		if (!canRedo()) {
			throw new CannotRedoException();
		}

		try {
			performRedo();
			hasBeenDone = true;
		} catch (final CannotRedoException ex) {
			hasBeenDone = false;
			throw ex;
		}
	}

	/**
	 * To be implemented by subclasses to perform the actual Redo action.
	 * 
	 * @throws CannotRedoException
	 */
	protected abstract void performRedo() throws CannotRedoException;

	public void undo() throws CannotUndoException {
		if (!canUndo()) {
			throw new CannotUndoException();
		}

		try {
			performUndo();
			hasBeenDone = false;
		} catch (final CannotUndoException ex) {
			throw ex;
		}
	}

	/**
	 * To be implemented by subclasses to perform the actual Undo action.
	 * 
	 * @throws CannotUndoException
	 */
	protected abstract void performUndo() throws CannotUndoException;

	public boolean canUndo() {
		return hasBeenDone;
	}

	public boolean canRedo() {
		return !hasBeenDone;
	}

}