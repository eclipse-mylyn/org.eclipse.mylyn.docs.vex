package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.dom.DocumentValidationException;
import org.eclipse.vex.core.dom.IComment;
import org.eclipse.vex.core.dom.IDocument;

public class InsertCommentEdit implements IUndoableEdit {

	private final IDocument document;
	private final int offset;
	private IComment comment;

	public InsertCommentEdit(final IDocument document, final int offset) {
		this.document = document;
		this.offset = offset;
		comment = null;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			document.delete(comment.getRange());
			comment = null;
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			comment = document.insertComment(offset);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public IComment getComment() {
		return comment;
	}

}
