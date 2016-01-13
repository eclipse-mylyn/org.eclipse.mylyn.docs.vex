/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import java.util.LinkedList;

/**
 * @author Florian Thienel
 */
public class EditStack {

	private final LinkedList<IUndoableEdit> doneEdits = new LinkedList<IUndoableEdit>();
	private final LinkedList<IUndoableEdit> undoneEdits = new LinkedList<IUndoableEdit>();

	private final LinkedList<CompoundEdit> pendingEdits = new LinkedList<CompoundEdit>();

	public <T extends IUndoableEdit> T apply(final T edit) throws CannotApplyException {
		edit.redo();

		if (pendingEdits.isEmpty()) {
			if (doneEdits.isEmpty() || !doneEdits.peek().combine(edit)) {
				doneEdits.push(edit);
			}
			undoneEdits.clear();
		} else {
			pendingEdits.peek().addEdit(edit);
		}

		return edit;
	}

	public IUndoableEdit undo() throws CannotUndoException {
		if (doneEdits.isEmpty()) {
			throw new CannotUndoException("EditStack is empty, nothing to undo!");
		}

		final IUndoableEdit undoneEdit = doneEdits.peek();
		undoneEdit.undo();

		undoneEdits.push(doneEdits.pop());

		return undoneEdit;
	}

	public IUndoableEdit redo() throws CannotApplyException {
		if (undoneEdits.isEmpty()) {
			throw new CannotApplyException("Nothing available to redo!");
		}

		final IUndoableEdit redoneEdit = undoneEdits.peek();
		redoneEdit.redo();

		doneEdits.push(undoneEdits.pop());

		return redoneEdit;
	}

	public boolean canUndo() {
		return !doneEdits.isEmpty() && doneEdits.peek().canUndo();
	}

	public boolean canRedo() {
		return !undoneEdits.isEmpty() && undoneEdits.peek().canRedo();
	}

	public void beginWork() {
		pendingEdits.push(new CompoundEdit());
	}

	public IUndoableEdit commitWork() {
		if (pendingEdits.isEmpty()) {
			throw new CannotApplyException("No edit pending, cannot commit!");
		}

		return apply(pendingEdits.pop());
	}

	public IUndoableEdit rollbackWork() {
		if (pendingEdits.isEmpty()) {
			throw new CannotUndoException("No edit pending, cannot rollback!");
		}

		final CompoundEdit work = pendingEdits.pop();
		work.undo();
		return work;
	}

	public boolean inTransaction() {
		return !pendingEdits.isEmpty();
	}
}
