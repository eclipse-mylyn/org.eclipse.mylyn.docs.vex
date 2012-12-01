package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;

public class InsertCommentEdit implements IUndoableEdit {

	private final Document document;
	private final int offset;
	private Comment comment;

	public InsertCommentEdit(final Document document, final int offset) {
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

	public Comment getElement() {
		return comment;
	}

}
