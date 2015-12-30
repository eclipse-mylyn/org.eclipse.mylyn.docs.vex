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

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;

public class InsertTextEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private String text;

	public InsertTextEdit(final IDocument document, final int offset, final String text) {
		super();
		this.document = document;
		this.offset = offset;
		this.text = text;
	}

	@Override
	protected boolean performCombine(final IUndoableEdit edit) {
		if (edit instanceof InsertTextEdit) {
			final InsertTextEdit ite = (InsertTextEdit) edit;
			if (ite.offset == offset + text.length()) {
				text = text + ite.text;
				return true;
			}
		}
		return false;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(new ContentRange(offset, offset + text.length() - 1));
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			document.insertText(offset, text);
		} catch (final DocumentValidationException ex) {
			throw new CannotApplyException();
		}
	}

}
