/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		John Krasnay - initial API and implementation
 *		Carsten Hiesserich - Refactored to use AbstractUndoableEdit
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;

public class InsertCommentEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private IComment comment;

	public InsertCommentEdit(final IDocument document, final int offset) {
		super();
		this.document = document;
		this.offset = offset;
		comment = null;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(comment.getRange());
			comment = null;
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	@Override
	protected void performRedo() throws CannotRedoException {
		try {
			comment = document.insertComment(offset);
		} catch (final DocumentValidationException ex) {
			throw new CannotRedoException();
		}
	}

	public IComment getComment() {
		return comment;
	}

}
