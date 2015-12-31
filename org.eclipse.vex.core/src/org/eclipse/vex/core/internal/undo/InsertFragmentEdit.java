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

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class InsertFragmentEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final IDocumentFragment fragment;

	public InsertFragmentEdit(final IDocument document, final int offset, final IDocumentFragment fragment) {
		super();
		this.document = document;
		this.offset = offset;
		this.fragment = fragment;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(fragment.getContent().getRange().moveBy(offset));
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		try {
			document.insertFragment(offset, fragment);
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	public int getOffsetBefore() {
		return offset;
	}

	public int getOffsetAfter() {
		return fragment.getContent().getRange().moveBy(offset).getEndOffset();
	}
}
