package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.Range;

public class DeleteEdit implements IUndoableEdit {

	private final Document document;
	private final Range range;
	private DocumentFragment fragment = null;

	public DeleteEdit(final Document document, final Range range) {
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
