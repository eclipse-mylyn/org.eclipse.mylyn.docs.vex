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
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;

/**
 * @author Florian Thienel
 */
public class JoinElementsAtOffsetEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private ContentRange rangeToRestore = null;
	private IDocumentFragment fragmentToRestore = null;
	private int offsetAfter;

	public JoinElementsAtOffsetEdit(final IDocument document, final int offset) {
		this.document = document;
		this.offset = offset;
		offsetAfter = offset;
	}

	@Override
	protected void performRedo() throws CannotApplyException {
		final IElement headElement;
		final IElement tailElement;
		if (isBetweenMatchingElements(document, offset - 1)) {
			headElement = document.getElementForInsertionAt(offset - 2);
			tailElement = document.getElementForInsertionAt(offset);
		} else if (isBetweenMatchingElements(document, offset)) {
			headElement = document.getElementForInsertionAt(offset - 1);
			tailElement = document.getElementForInsertionAt(offset + 1);
		} else if (isBetweenMatchingElements(document, offset + 1)) {
			headElement = document.getElementForInsertionAt(offset);
			tailElement = document.getElementForInsertionAt(offset + 2);
		} else {
			throw new CannotApplyException("The given offset " + offset + " is not between matching elements!");
		}

		final IDocumentFragment tailElementContent;
		if (!tailElement.isEmpty()) {
			tailElementContent = document.getFragment(tailElement.getRange().resizeBy(1, -1));
		} else {
			tailElementContent = null;
		}

		offsetAfter = headElement.getEndOffset();
		fragmentToRestore = document.getFragment(headElement.getRange().union(tailElement.getRange()));

		try {
			document.delete(tailElement.getRange());
			if (tailElementContent != null) {
				document.insertFragment(headElement.getEndOffset(), tailElementContent);
			}
			rangeToRestore = headElement.getRange();
		} catch (final DocumentValidationException e) {
			throw new CannotApplyException(e);
		}
	}

	public static boolean isBetweenMatchingElements(final IDocument document, final int offset) {
		if (offset <= 1 || offset >= document.getLength() - 1) {
			return false;
		}
		final IElement e1 = document.getElementForInsertionAt(offset - 1);
		final IElement e2 = document.getElementForInsertionAt(offset + 1);
		return e1 != e2 && e1 != null && e2 != null && e1.getParent() == e2.getParent() && e1.isKindOf(e2);
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			document.delete(rangeToRestore);
			document.insertFragment(rangeToRestore.getStartOffset(), fragmentToRestore);
			rangeToRestore = null;
			fragmentToRestore = null;
		} catch (final DocumentValidationException e) {
			throw new CannotUndoException(e);
		}
	}

	@Override
	public int getOffsetBefore() {
		return offset;
	}

	@Override
	public int getOffsetAfter() {
		return offsetAfter;
	}

}
