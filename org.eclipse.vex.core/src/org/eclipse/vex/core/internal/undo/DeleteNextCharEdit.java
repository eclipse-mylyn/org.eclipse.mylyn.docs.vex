/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * @author Florian Thienel
 */
public class DeleteNextCharEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;

	private int count;
	private String textToRestore = null;

	public DeleteNextCharEdit(final IDocument document, final int offset) {
		this.document = document;
		this.offset = offset;
		count = 1;
	}

	@Override
	protected boolean performCombine(final IUndoableEdit other) {
		if (other instanceof DeleteNextCharEdit) {
			final DeleteNextCharEdit otherEdit = (DeleteNextCharEdit) other;
			if (otherEdit.offset == offset) {
				count += otherEdit.count;
				textToRestore += otherEdit.textToRestore;
				return true;
			}
		}
		return false;
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		if (document.isTagAt(offset)) {
			throw new CannotApplyException("Cannot delete a tag!");
		}

		try {
			final ContentRange range = new ContentRange(offset, offset + count - 1);
			textToRestore = document.getText(range);
			document.delete(range);
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.insertText(offset, textToRestore);
			textToRestore = null;
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	@Override
	public int getOffsetBefore() {
		return offset;
	}

	@Override
	public int getOffsetAfter() {
		return offset;
	}

}
