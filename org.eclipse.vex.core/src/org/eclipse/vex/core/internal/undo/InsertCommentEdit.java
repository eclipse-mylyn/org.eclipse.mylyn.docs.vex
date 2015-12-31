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

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;

public class InsertCommentEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private IComment comment;
	private ContentRange commentRange;

	public InsertCommentEdit(final IDocument document, final int offset) {
		super();
		this.document = document;
		this.offset = offset;
		comment = null;
		commentRange = ContentRange.NULL;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(commentRange);
			comment = null;
			commentRange = ContentRange.NULL;
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			comment = document.insertComment(offset);
			commentRange = comment.getRange();
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	public IComment getComment() {
		return comment;
	}

	public int getOffsetBefore() {
		return offset;
	}

	public int getOffsetAfter() {
		return comment.getEndOffset();
	}
}
