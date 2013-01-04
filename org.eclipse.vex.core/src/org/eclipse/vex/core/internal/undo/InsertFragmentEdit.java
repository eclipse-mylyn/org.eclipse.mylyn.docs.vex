package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;

public class InsertFragmentEdit implements IUndoableEdit {

	private final Document document;
	private final int offset;
	private final DocumentFragment fragment;

	public InsertFragmentEdit(final Document document, final int offset, final DocumentFragment fragment) {
		this.document = document;
		this.offset = offset;
		this.fragment = fragment;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			document.delete(fragment.getContent().getRange().moveBy(offset));
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			document.insertFragment(offset, fragment);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

}
