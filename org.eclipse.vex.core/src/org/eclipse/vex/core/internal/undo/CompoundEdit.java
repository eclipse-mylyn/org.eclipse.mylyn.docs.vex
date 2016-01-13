/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *	   Carsten Hiesserich - Refactored to use AbstractUndoableEdit
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import java.util.ArrayList;
import java.util.List;

/**
 * An undoable edit that is a composite of others.
 */
public class CompoundEdit extends AbstractUndoableEdit {

	private final List<IUndoableEdit> edits = new ArrayList<IUndoableEdit>();

	/**
	 * Adds an edit to the list.
	 *
	 * @param edit
	 *            Edit to be undone/redone as part of the compound group.
	 */
	public void addEdit(final IUndoableEdit edit) {
		edits.add(edit);
	}

	/**
	 * Calls redo() on each contained edit, in the order that they were added.
	 */
	@Override
	protected void performRedo() {
		for (int i = 0; i < edits.size(); i++) {
			final IUndoableEdit edit = edits.get(i);
			if (edit.canRedo()) {
				edit.redo();
			}
		}
	}

	/**
	 * Calls undo() on each contained edit, in reverse order from which they were added.
	 */
	@Override
	protected void performUndo() {
		for (int i = edits.size() - 1; i >= 0; i--) {
			final IUndoableEdit edit = edits.get(i);
			if (edit.canUndo()) {
				edit.undo();
			}
		}
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	public int getOffsetBefore() {
		if (edits.isEmpty()) {
			return 0;
		}
		return edits.get(0).getOffsetBefore();
	}

	public int getOffsetAfter() {
		if (edits.isEmpty()) {
			return 0;
		}
		return edits.get(edits.size() - 1).getOffsetAfter();
	}
}
