package org.eclipse.vex.core.internal.undo;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.dom.DocumentValidationException;
import org.eclipse.vex.core.dom.IDocument;
import org.eclipse.vex.core.dom.IElement;

public class InsertElementEdit implements IUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final QualifiedName elementName;
	private IElement element;

	public InsertElementEdit(final IDocument document, final int offset, final QualifiedName elementName) {
		this.document = document;
		this.offset = offset;
		this.elementName = elementName;
		element = null;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			document.delete(element.getRange());
			element = null;
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			element = document.insertElement(offset, elementName);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public IElement getElement() {
		return element;
	}

}
