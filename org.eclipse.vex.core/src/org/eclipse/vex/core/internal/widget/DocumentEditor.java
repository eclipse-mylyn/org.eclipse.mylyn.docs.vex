/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toWordEnd;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toWordStart;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.DeleteEdit;
import org.eclipse.vex.core.internal.undo.EditStack;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;

public class DocumentEditor implements IDocumentEditor {

	private final Cursor cursor;
	private final EditStack editStack;

	private IDocument document;
	private boolean readOnly;

	private INode currentNode;
	private ContentPosition caretPosition;
	private final ICursorPositionListener cursorListener = new ICursorPositionListener() {
		@Override
		public void positionChanged(final int offset) {
			cursorPositionChanged(offset);
		}

		@Override
		public void positionAboutToChange() {
			// ignore
		}
	};

	public DocumentEditor(final Cursor cursor) {
		this.cursor = cursor;
		cursor.addPositionListener(cursorListener);

		editStack = new EditStack();
	}

	public void dispose() {
		cursor.removePositionListener(cursorListener);
	}

	/*
	 * Configuration
	 */

	@Override
	public IDocument getDocument() {
		return document;
	}

	@Override
	public void setDocument(final IDocument document) {
		this.document = document;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	/*
	 * Undo/Redo
	 */

	@Override
	public boolean canRedo() {
		return editStack.canRedo();
	}

	@Override
	public void redo() throws CannotApplyException {
		final IUndoableEdit edit = editStack.redo();
		cursor.move(toOffset(edit.getOffsetAfter()));
	}

	@Override
	public boolean canUndo() {
		return editStack.canUndo();
	}

	@Override
	public void undo() throws CannotUndoException {
		final IUndoableEdit edit = editStack.undo();
		cursor.move(toOffset(edit.getOffsetBefore()));
	}

	/*
	 * Transaction Handling
	 */

	@Override
	public void beginWork() {
		editStack.beginWork();
	}

	@Override
	public void doWork(final Runnable runnable) {
		doWork(runnable, false);
	}

	@Override
	public void doWork(final Runnable runnable, final boolean savePosition) {
		final IPosition position = document.createPosition(cursor.getOffset());
		editStack.beginWork();
		try {
			runnable.run();
			final IUndoableEdit work = editStack.commitWork();
			cursor.move(toOffset(work.getOffsetAfter()));
		} catch (final Throwable t) {
			final IUndoableEdit work = editStack.rollbackWork();
			cursor.move(toOffset(work.getOffsetBefore()));
			// TODO throw exception? at least log error?
		} finally {
			if (savePosition) {
				cursor.move(toOffset(position.getOffset()));
			}
			document.removePosition(position);
		}
	}

	@Override
	public void endWork(final boolean success) {
		// TODO split in two different methods (commitWork and rollbackWork)
		if (success) {
			final IUndoableEdit work = editStack.commitWork();
			cursor.move(toOffset(work.getOffsetAfter()));
		} else {
			final IUndoableEdit work = editStack.rollbackWork();
			cursor.move(toOffset(work.getOffsetBefore()));
		}
	}

	@Override
	public void savePosition(final Runnable runnable) {
		final IPosition position = document.createPosition(cursor.getOffset());
		try {
			runnable.run();
		} finally {
			cursor.move(toOffset(position.getOffset()));
			document.removePosition(position);
		}
	}

	/*
	 * Clipboard cut/copy/paste
	 */

	@Override
	public void cutSelection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void copySelection() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canPaste() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void paste() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canPasteText() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pasteText() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	/*
	 * Caret and Selection
	 */

	private void cursorPositionChanged(final int offset) {
		currentNode = document.getNodeForInsertionAt(cursor.getOffset());
		caretPosition = new ContentPosition(currentNode.getDocument(), cursor.getOffset());
	}

	@Override
	public ContentPosition getCaretPosition() {
		return caretPosition;
	}

	@Override
	public IElement getCurrentElement() {
		return currentNode.accept(new BaseNodeVisitorWithResult<IElement>(null) {
			@Override
			public IElement visit(final IElement element) {
				return element;
			}

			@Override
			public IElement visit(final IComment comment) {
				return comment.getParent().accept(this);
			}

			@Override
			public IElement visit(final IText text) {
				return text.getParent().accept(this);
			}

			@Override
			public IElement visit(final IProcessingInstruction pi) {
				return pi.getParent().accept(this);
			}
		});
	}

	@Override
	public INode getCurrentNode() {
		return currentNode;
	}

	@Override
	public boolean hasSelection() {
		return cursor.hasSelection();
	}

	@Override
	public ContentRange getSelectedRange() {
		return cursor.getSelectedRange();
	}

	@Override
	public ContentPositionRange getSelectedPositionRange() {
		final ContentRange selectedRange = getSelectedRange();
		return new ContentPositionRange(new ContentPosition(document, selectedRange.getStartOffset()), new ContentPosition(document, selectedRange.getEndOffset()));
	}

	@Override
	public IDocumentFragment getSelectedFragment() {
		if (hasSelection()) {
			return document.getFragment(getSelectedRange());
		} else {
			return null;
		}
	}

	@Override
	public String getSelectedText() {
		if (hasSelection()) {
			return document.getText(getSelectedRange());
		} else {
			return "";
		}
	}

	@Override
	public void selectAll() {
		cursor.move(toOffset(document.getStartOffset()));
		cursor.select(toOffset(document.getEndOffset()));
	}

	@Override
	public void selectWord() {
		cursor.move(toWordStart());
		cursor.select(toWordEnd());
	}

	@Override
	public void selectContentOf(final INode node) {
		if (node.isEmpty()) {
			cursor.move(toOffset(node.getEndOffset()));
		}

		cursor.move(toOffset(node.getStartOffset() + 1));
		cursor.select(toOffset(node.getEndOffset()));
	}

	@Override
	public void select(final INode node) {
		cursor.move(toOffset(node.getStartOffset()));
		cursor.select(toOffset(node.getEndOffset()));
	}

	@Override
	public boolean canDeleteSelection() {
		if (isReadOnly()) {
			return false;
		}
		if (!hasSelection()) {
			return false;
		}
		return document.canDelete(getSelectedRange());
	}

	@Override
	public void deleteSelection() {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}
		if (!hasSelection()) {
			return;
		}

		final DeleteEdit deleteEdit = editStack.apply(new DeleteEdit(document, getSelectedRange(), cursor.getOffset()));
		cursor.move(toOffset(deleteEdit.getOffsetAfter()));
	}

	/*
	 * Caret Movement
	 */

	@Override
	public void moveBy(final int distance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBy(final int distance, final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveTo(final ContentPosition position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveTo(final ContentPosition position, final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToLineEnd(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToLineStart(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToNextLine(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToNextPage(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToNextWord(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToPreviousLine(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToPreviousPage(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToPreviousWord(final boolean select) {
		// TODO Auto-generated method stub

	}

	/*
	 * Namespaces
	 */

	@Override
	public void declareNamespace(final String namespacePrefix, final String namespaceURI) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNamespace(final String namespacePrefix) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareDefaultNamespace(final String namespaceURI) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDefaultNamespace() {
		// TODO Auto-generated method stub

	}

	/*
	 * Attributes
	 */

	@Override
	public boolean canSetAttribute(final String attributeName, final String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAttribute(final String attributeName, final String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canRemoveAttribute(final String attributeName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(final String attributeName) {
		// TODO Auto-generated method stub

	}

	/*
	 * Content
	 */

	@Override
	public boolean canInsertText() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void insertChar(final char c) throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteNextChar() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deletePreviousChar() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertText(final String text) throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	/*
	 * Structure
	 */

	@Override
	public ElementName[] getValidInsertElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementName[] getValidMorphElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertElement(final QualifiedName elementName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertComment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IComment insertComment() throws DocumentValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertProcessingInstruction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IProcessingInstruction insertProcessingInstruction(final String target) throws CannotApplyException, ReadOnlyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void editProcessingInstruction(final String target, final String data) throws CannotApplyException, ReadOnlyException {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertXML(final String xml) throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canInsertFragment(final IDocumentFragment fragment) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void insertFragment(final IDocumentFragment frag) throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canUnwrap() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unwrap() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canMorph(final QualifiedName elementName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void morph(final QualifiedName elementName) throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canJoin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void join() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canSplit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void split() throws DocumentValidationException {
		// TODO Auto-generated method stub

	}

}
