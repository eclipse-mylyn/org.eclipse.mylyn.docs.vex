/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		John Krasnay - initial API and implementation
 *		Carsten Hiesserich - Refactored to use AbstractUndoableEdit
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;

public class InsertElementEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final QualifiedName elementName;
	private ContentRange elementRange;
	private IElement element;

	public InsertElementEdit(final IDocument document, final int offset, final QualifiedName elementName) {
		this.document = document;
		this.offset = offset;
		this.elementName = elementName;
		elementRange = ContentRange.NULL;
		element = null;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(elementRange);
			elementRange = ContentRange.NULL;
			element = null;
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotRedoException {
		try {
			element = document.insertElement(offset, elementName);
			elementRange = element.getRange();
		} catch (final DocumentValidationException e) {
			throw new CannotRedoException(e);
		}
	}

	public IElement getElement() {
		return element;
	}

}
