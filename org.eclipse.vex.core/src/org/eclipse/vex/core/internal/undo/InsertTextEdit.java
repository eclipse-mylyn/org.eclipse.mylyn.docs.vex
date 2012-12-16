package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.ContentRange;

public class InsertTextEdit implements IUndoableEdit {

	private final Document document;
	private final int offset;
	private String text;

	public InsertTextEdit(final Document document, final int offset, final String text) {
		this.document = document;
		this.offset = offset;
		this.text = text;
	}

	public boolean combine(final IUndoableEdit edit) {
		if (edit instanceof InsertTextEdit) {
			final InsertTextEdit ite = (InsertTextEdit) edit;
			if (ite.offset == offset + text.length()) {
				text = text + ite.text;
				return true;
			}
		}
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			document.delete(new ContentRange(offset, offset + text.length()));
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			document.insertText(offset, text);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

}
