package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class InsertFragmentEdit implements IUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final IDocumentFragment fragment;

	public InsertFragmentEdit(final IDocument document, final int offset, final IDocumentFragment fragment) {
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
