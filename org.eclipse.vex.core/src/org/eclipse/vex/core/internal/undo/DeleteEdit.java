package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class DeleteEdit implements IUndoableEdit {

	private final IDocument document;
	private final ContentRange range;
	private IDocumentFragment fragment = null;

	public DeleteEdit(final IDocument document, final ContentRange range) {
		this.document = document;
		this.range = range;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			document.insertFragment(range.getStartOffset(), fragment);
			fragment = null;
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			fragment = document.getFragment(range);
			document.delete(range);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

}
