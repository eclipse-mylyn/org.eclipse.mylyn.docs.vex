package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;

public class InsertTextEdit implements IUndoableEdit {

	private final IDocument document;
	private final int offset;
	private String text;

	public InsertTextEdit(final IDocument document, final int offset, final String text) {
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
			document.delete(new ContentRange(offset, offset + text.length() - 1));
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
