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

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * @author Florian Thienel
 */
public class InsertLineBreakEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;

	public InsertLineBreakEdit(final IDocument document, final int offset) {
		this.document = document;
		this.offset = offset;
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			document.insertLineBreak(offset);
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(new ContentRange(offset, offset));
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	public int getOffsetBefore() {
		return offset;
	}

	public int getOffsetAfter() {
		return offset + 1;
	}
}
