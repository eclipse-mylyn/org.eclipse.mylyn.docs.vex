package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.Element;

public class ChangeNamespaceEdit implements IUndoableEdit {

	private final Element element;
	private final String prefix;
	private final String oldUri;
	private final String newUri;

	public ChangeNamespaceEdit(final Element element, final String prefix, final String oldUri, final String newUri) {
		this.element = element;
		this.prefix = prefix;
		this.oldUri = oldUri;
		this.newUri = newUri;
	}

	public boolean combine(final IUndoableEdit edit) {
		return false;
	}

	public void undo() throws CannotUndoException {
		try {
			if (oldUri == null) {
				element.removeNamespace(prefix);
			} else {
				element.declareNamespace(prefix, oldUri);
			}
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	public void redo() throws CannotRedoException {
		try {
			if (newUri == null) {
				element.removeNamespace(prefix);
			} else {
				element.declareNamespace(prefix, newUri);
			}
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}
}
