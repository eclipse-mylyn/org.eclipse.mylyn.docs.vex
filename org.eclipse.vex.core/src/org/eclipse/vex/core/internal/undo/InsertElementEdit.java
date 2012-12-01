package org.eclipse.vex.core.internal.undo;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.Element;

public class InsertElementEdit implements IUndoableEdit {

	private final Document document;
	private final int offset;
	private final QualifiedName elementName;
	private Element element;

	public InsertElementEdit(final Document document, final int offset, final QualifiedName elementName) {
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

	public Element getElement() {
		return element;
	}

}
