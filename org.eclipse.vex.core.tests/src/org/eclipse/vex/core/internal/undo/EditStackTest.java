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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class EditStackTest {

	@Test
	public void apply_shouldIndicateUndoPossible() throws Exception {
		final EditStack stack = new EditStack();

		stack.apply(new MockEdit());

		assertTrue("can undo", stack.canUndo());
	}

	@Test
	public void apply_shouldPerformRedo() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit = new MockEdit();

		stack.apply(edit);

		assertTrue("perform redo", edit.redoCalled);
	}

	@Test
	public void apply_whenEditCannotBeUndone_shouldIndicateUndoNotPossible() throws Exception {
		final EditStack stack = new EditStack();

		stack.apply(new MockEdit(false, false));

		assertFalse("cannot undo", stack.canUndo());
	}

	@Test
	public void apply_when3EditsWhereUndone_shouldDiscardUndoneEdits() throws Exception {
		final EditStack stack = new EditStack();

		stack.apply(new MockEdit());
		stack.apply(new MockEdit());
		stack.apply(new MockEdit());
		stack.undo();
		stack.undo();
		stack.undo();
		stack.apply(new MockEdit());

		assertFalse("discard undone edits", stack.canRedo());
	}

	@Test
	public void apply_shouldCombineEditsIfPossible() throws Exception {
		final EditStack stack = new EditStack();

		final MockEdit edit1 = stack.apply(new MockEdit());
		edit1.canCombine = true;

		stack.apply(new MockEdit());

		assertTrue("combine", edit1.combineCalled);
	}

	@Test
	public void undo_shouldPerformUndo() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit = new MockEdit();

		stack.apply(edit);
		stack.undo();

		assertTrue("perform undo", edit.undoCalled);
	}

	@Test(expected = CannotUndoException.class)
	public void undo_whenStackIsEmpty_shouldThrowCannotUndoException() throws Exception {
		new EditStack().undo();
	}

	@Test
	public void undo_whenGiven3EditsAndCallingUndo3Times_shouldPerformUndoOnAllEdits() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();

		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);
		stack.undo();
		stack.undo();
		stack.undo();

		assertTrue("undo edit1", edit1.undoCalled);
		assertTrue("undo edit2", edit2.undoCalled);
		assertTrue("undo edit3", edit3.undoCalled);
	}

	@Test
	public void undo_whenGiven3EditsAndCallingUndo3Times_shouldPerformUndoInReverseOrder() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();

		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);

		stack.undo();
		assertTrue("undo edit3", edit3.undoCalled);

		stack.undo();
		assertTrue("undo edit2", edit2.undoCalled);

		stack.undo();
		assertTrue("undo edit1", edit1.undoCalled);
	}

	@Test
	public void redo_shouldPerformRedoOnLastUndoneEdit() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit = new MockEdit();

		stack.apply(edit);
		stack.undo();

		edit.redoCalled = false;
		stack.redo();

		assertTrue("perform redo", edit.redoCalled);
	}

	@Test(expected = CannotApplyException.class)
	public void redo_whenNothingWasUndone_shouldThrowCannotApplyException() throws Exception {
		new EditStack().redo();
	}

	@Test
	public void commit_shouldApplyAllPendingEdits() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();

		stack.beginWork();
		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);
		stack.commitWork();

		assertTrue("apply edit1", edit1.redoCalled);
		assertTrue("apply edit2", edit2.redoCalled);
		assertTrue("apply edit3", edit3.redoCalled);
	}

	@Test
	public void rollback_shouldUndoAllPendingEdits() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();

		stack.beginWork();
		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);
		stack.rollbackWork();

		assertTrue("undo edit1", edit1.undoCalled);
		assertTrue("undo edit2", edit2.undoCalled);
		assertTrue("undo edit3", edit3.undoCalled);
	}

	@Test
	public void inTransaction_whenOpendTwiceButCommittedOnlyOnce_shouldIndicateTrue() throws Exception {
		final EditStack stack = new EditStack();

		stack.beginWork();
		stack.beginWork();
		stack.commitWork();

		assertTrue("in transaction", stack.inTransaction());
	}

	@Test
	public void nestedTransactions_whenNestedTransactionIsRolledBackAndOuterTransactionIsCommitted_shouldApplyEditsInOuterTransaction() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();
		final MockEdit edit4 = new MockEdit();
		final MockEdit edit5 = new MockEdit();
		final MockEdit edit6 = new MockEdit();

		stack.beginWork();
		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);
		stack.beginWork();
		stack.apply(edit4);
		stack.apply(edit5);
		stack.apply(edit6);
		stack.rollbackWork();
		stack.commitWork();

		assertFalse("apply edit1", edit1.undoCalled);
		assertFalse("apply edit2", edit2.undoCalled);
		assertFalse("apply edit3", edit3.undoCalled);
		assertTrue("undo edit4", edit4.undoCalled);
		assertTrue("undo edit5", edit5.undoCalled);
		assertTrue("undo edit6", edit6.undoCalled);
	}

	@Test
	public void threeEditsAppliedInOneTransaction_shouldBeUndoneWithOneCallToUndo() throws Exception {
		final EditStack stack = new EditStack();
		final MockEdit edit1 = new MockEdit();
		final MockEdit edit2 = new MockEdit();
		final MockEdit edit3 = new MockEdit();

		stack.beginWork();
		stack.apply(edit1);
		stack.apply(edit2);
		stack.apply(edit3);
		stack.commitWork();
		stack.undo();

		assertTrue("undo edit1", edit1.undoCalled);
		assertTrue("undo edit2", edit2.undoCalled);
		assertTrue("undo edit3", edit3.undoCalled);
	}

	private static class MockEdit implements IUndoableEdit {

		public boolean redoCalled;
		public boolean undoCalled;
		public boolean combineCalled;
		private final boolean canUndo;
		private final boolean canRedo;
		public boolean canCombine;

		public MockEdit() {
			this(true, true);
		}

		public MockEdit(final boolean canUndo, final boolean canRedo) {
			this.canUndo = canUndo;
			this.canRedo = canRedo;
		}

		@Override
		public boolean combine(final IUndoableEdit edit) {
			combineCalled = true;
			return canCombine;
		}

		@Override
		public void redo() throws CannotApplyException {
			redoCalled = true;
		}

		@Override
		public void undo() throws CannotUndoException {
			undoCalled = true;
		}

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		public int getOffsetBefore() {
			return 0;
		}

		public int getOffsetAfter() {
			return 0;
		}
	}
}
