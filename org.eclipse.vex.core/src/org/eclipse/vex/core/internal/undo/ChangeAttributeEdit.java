package org.eclipse.vex.core.internal.undo;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.dom.DocumentValidationException;
import org.eclipse.vex.core.dom.IElement;

public class ChangeAttributeEdit implements IUndoableEdit {

	private final IElement element;
	private final QualifiedName attributeName;
	private final String oldValue;
	private final String newValue;

	public ChangeAttributeEdit(final IElement element, final QualifiedName attributeName, final String oldValue, final String newValue) {
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			element.setAttribute(attributeName, oldValue);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			element.setAttribute(attributeName, newValue);
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}
}
