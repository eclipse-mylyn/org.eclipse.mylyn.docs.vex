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
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;

public class ChangeAttributeEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final QualifiedName attributeName;
	private final String oldValue;
	private final String newValue;

	public ChangeAttributeEdit(final IDocument document, final int offset, final QualifiedName attributeName, final String oldValue, final String newValue) {
		super();
		this.document = document;
		this.offset = offset;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			final IElement element = document.getElementForInsertionAt(offset);
			element.setAttribute(attributeName, oldValue);
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			final IElement element = document.getElementForInsertionAt(offset);
			element.setAttribute(attributeName, newValue);
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	public int getOffsetBefore() {
		return offset;
	}

	public int getOffsetAfter() {
		return offset;
	}
}
