/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

public class InsertProcessingInstructionEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final String target;
	private IProcessingInstruction pi;
	private ContentRange contentRange;

	public InsertProcessingInstructionEdit(final IDocument document, final int offset, final String target) {
		super();
		this.document = document;
		this.offset = offset;
		this.target = target;
		pi = null;
		contentRange = ContentRange.NULL;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(contentRange);
			pi = null;
			contentRange = ContentRange.NULL;
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			pi = document.insertProcessingInstruction(offset, target);
			contentRange = pi.getRange();
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	public IProcessingInstruction getProcessingInstruction() {
		return pi;
	}

	public int getOffsetBefore() {
		return offset;
	}

	public int getOffsetAfter() {
		return pi.getEndOffset();
	}
}
