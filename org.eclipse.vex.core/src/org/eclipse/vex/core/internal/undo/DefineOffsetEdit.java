/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

public class DefineOffsetEdit extends AbstractUndoableEdit {

	private final int offsetBefore;
	private final int offsetAfter;

	public DefineOffsetEdit(final int offsetBefore, final int offsetAfter) {
		this.offsetBefore = offsetBefore;
		this.offsetAfter = offsetAfter;
	}

	@Override
	public int getOffsetBefore() {
		return offsetBefore;
	}

	@Override
	public int getOffsetAfter() {
		return offsetAfter;
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		// ignore
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		// ignore
	}
}
