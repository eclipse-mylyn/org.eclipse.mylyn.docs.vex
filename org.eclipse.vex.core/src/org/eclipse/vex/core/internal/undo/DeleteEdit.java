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
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class DeleteEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final ContentRange range;
	private IDocumentFragment fragment = null;

	public DeleteEdit(final IDocument document, final ContentRange range) {
		super();
		this.document = document;
		this.range = range;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.insertFragment(range.getStartOffset(), fragment);
			fragment = null;
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotRedoException {
		try {
			fragment = document.getFragment(range);
			document.delete(range);
		} catch (final DocumentValidationException e) {
			throw new CannotRedoException(e);
		}
	}

}
