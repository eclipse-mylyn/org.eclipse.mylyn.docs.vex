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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.ChangeAttributeEdit;
import org.eclipse.vex.core.internal.undo.ChangeNamespaceEdit;
import org.eclipse.vex.core.internal.undo.DeleteEdit;
import org.eclipse.vex.core.internal.undo.DeleteNextCharEdit;
import org.eclipse.vex.core.internal.undo.DeletePreviousCharEdit;
import org.eclipse.vex.core.internal.undo.EditStack;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;
import org.eclipse.vex.core.internal.undo.InsertLineBreakEdit;
import org.eclipse.vex.core.internal.undo.InsertTextEdit;
import org.eclipse.vex.core.internal.undo.JoinElementsAtOffsetEdit;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

public class DocumentEditor implements IDocumentEditor {

	private final Cursor cursor;
	private final IWhitespacePolicy whitespacePolicy;
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
		this(cursor, IWhitespacePolicy.NULL);
	}

	public DocumentEditor(final Cursor cursor, final IWhitespacePolicy whitespacePolicy) {
		this.cursor = cursor;
		cursor.addPositionListener(cursorListener);
		this.whitespacePolicy = whitespacePolicy;

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
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot declare namespace {0}, because the editor is read-only.", namespacePrefix));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}
		final String currentNamespaceURI = element.getNamespaceURI(namespacePrefix);
		final ChangeNamespaceEdit changeNamespace = editStack.apply(new ChangeNamespaceEdit(document, cursor.getOffset(), namespacePrefix, currentNamespaceURI, namespaceURI));
		cursor.move(toOffset(changeNamespace.getOffsetAfter()));
	}

	@Override
	public void removeNamespace(final String namespacePrefix) {
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot remove namespace {0}, because the editor is read-only.", namespacePrefix));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}
		final String currentNamespaceURI = element.getNamespaceURI(namespacePrefix);
		final ChangeNamespaceEdit changeNamespace = editStack.apply(new ChangeNamespaceEdit(document, cursor.getOffset(), namespacePrefix, currentNamespaceURI, null));
		cursor.move(toOffset(changeNamespace.getOffsetAfter()));
	}

	@Override
	public void declareDefaultNamespace(final String namespaceURI) {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot declare default namespace, because the editor is read-only.");
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}
		final String currentNamespaceURI = element.getDefaultNamespaceURI();
		final ChangeNamespaceEdit changeNamespace = editStack.apply(new ChangeNamespaceEdit(document, cursor.getOffset(), null, currentNamespaceURI, namespaceURI));
		cursor.move(toOffset(changeNamespace.getOffsetAfter()));
	}

	@Override
	public void removeDefaultNamespace() {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot remove default namespace, because the editor is read-only.");
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}
		final String currentNamespaceURI = element.getDefaultNamespaceURI();
		final ChangeNamespaceEdit changeNamespace = editStack.apply(new ChangeNamespaceEdit(document, cursor.getOffset(), null, currentNamespaceURI, null));
		cursor.move(toOffset(changeNamespace.getOffsetAfter()));
	}

	/*
	 * Attributes
	 */

	@Override
	public boolean canSetAttribute(final String attributeName, final String value) {
		if (isReadOnly()) {
			return false;
		}
		final IElement element = getCurrentElement();
		if (element == null) {
			return false;
		}
		final QualifiedName qualifiedAttributeName = element.qualify(attributeName);
		return element.canSetAttribute(qualifiedAttributeName, value);
	}

	@Override
	public void setAttribute(final String attributeName, final String value) {
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot set attribute {0}, because the editor is read-only.", attributeName));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}

		final QualifiedName qualifiedAttributeName = element.qualify(attributeName);
		final String currentAttributeValue = element.getAttributeValue(qualifiedAttributeName);
		if (value == null) {
			removeAttribute(attributeName);
		} else if (!value.equals(currentAttributeValue)) {
			final ChangeAttributeEdit changeAttribute = editStack.apply(new ChangeAttributeEdit(document, cursor.getOffset(), qualifiedAttributeName, currentAttributeValue, value));
			cursor.move(toOffset(changeAttribute.getOffsetAfter()));
		}
	}

	@Override
	public boolean canRemoveAttribute(final String attributeName) {
		if (isReadOnly()) {
			return false;
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return false;
		}

		final QualifiedName qualifiedAttributeName = element.qualify(attributeName);
		return element.canRemoveAttribute(qualifiedAttributeName);
	}

	@Override
	public void removeAttribute(final String attributeName) {
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot remove attribute {0}, because the editor is read-only.", attributeName));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}

		final QualifiedName qualifiedAttributeName = element.qualify(attributeName);
		final String currentAttributeValue = element.getAttributeValue(qualifiedAttributeName);
		if (currentAttributeValue != null) {
			final ChangeAttributeEdit changeAttribute = editStack.apply(new ChangeAttributeEdit(document, cursor.getOffset(), qualifiedAttributeName, currentAttributeValue, null));
			cursor.move(toOffset(changeAttribute.getOffsetAfter()));
		}
	}

	/*
	 * Content
	 */

	@Override
	public boolean canInsertText() {
		return canReplaceCurrentSelectionWith(IValidator.PCDATA);
	}

	private boolean canReplaceCurrentSelectionWith(final QualifiedName... nodeNames) {
		return canInsertAtCurrentSelection(Arrays.asList(nodeNames));
	}

	private boolean canInsertAtCurrentSelection(final List<QualifiedName> nodeNames) {
		if (isReadOnly()) {
			return false;
		}
		if (document == null) {
			return false;
		}

		final IValidator validator = document.getValidator();
		if (validator == null) {
			return true;
		}

		final ContentRange selectedRange = getSelectedRange();

		final IElement parent = document.getElementForInsertionAt(selectedRange.getStartOffset());
		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(selectedRange.getStartOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(selectedRange.getEndOffset()));

		return validator.isValidSequence(parent.getQualifiedName(), nodesBefore, nodeNames, nodesAfter, true);
	}

	@Override
	public void insertChar(final char c) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert a character, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		}

		final InsertTextEdit insertText = editStack.apply(new InsertTextEdit(document, cursor.getOffset(), Character.toString(c)));
		cursor.move(toOffset(insertText.getOffsetAfter()));
	}

	@Override
	public void deleteForward() throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}

		final IUndoableEdit edit;
		final int offset = cursor.getOffset();
		if (offset == document.getLength()) {
			// ignore
			edit = null;
		} else if (JoinElementsAtOffsetEdit.isBetweenMatchingElements(document, offset)) {
			edit = new JoinElementsAtOffsetEdit(document, offset);
		} else if (JoinElementsAtOffsetEdit.isBetweenMatchingElements(document, offset + 1)) {
			edit = new JoinElementsAtOffsetEdit(document, offset);
		} else if (document.getNodeForInsertionAt(offset).isEmpty()) {
			final ContentRange range = document.getNodeForInsertionAt(offset).getRange();
			edit = new DeleteEdit(document, range, offset);
		} else if (document.getNodeForInsertionAt(offset + 1).isEmpty()) {
			final ContentRange range = document.getNodeForInsertionAt(offset + 1).getRange();
			edit = new DeleteEdit(document, range, offset);
		} else if (!document.isTagAt(offset)) {
			edit = new DeleteNextCharEdit(document, offset);
		} else {
			edit = null;
		}

		if (edit == null) {
			return;
		}

		editStack.apply(edit);
		cursor.move(toOffset(edit.getOffsetAfter()));
	}

	@Override
	public void deleteBackward() throws DocumentValidationException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}

		final IUndoableEdit edit;
		final int offset = cursor.getOffset();

		if (offset == 1) {
			//ignore
			edit = null;
		} else if (JoinElementsAtOffsetEdit.isBetweenMatchingElements(document, offset)) {
			edit = new JoinElementsAtOffsetEdit(document, offset);
		} else if (JoinElementsAtOffsetEdit.isBetweenMatchingElements(document, offset - 1)) {
			edit = new JoinElementsAtOffsetEdit(document, offset);
		} else if (document.getNodeForInsertionAt(offset).isEmpty()) {
			final ContentRange range = document.getNodeForInsertionAt(offset).getRange();
			edit = new DeleteEdit(document, range, offset);
		} else if (document.getNodeForInsertionAt(offset - 1).isEmpty()) {
			final ContentRange range = document.getNodeForInsertionAt(offset + 1).getRange();
			edit = new DeleteEdit(document, range, offset);
		} else if (!document.isTagAt(offset - 1)) {
			edit = new DeletePreviousCharEdit(document, offset);
		} else {
			edit = null;
		}

		if (edit == null) {
			return;
		}

		editStack.apply(edit);
		cursor.move(toOffset(edit.getOffsetAfter()));
	}

	@Override
	public void insertText(final String text) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert text, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		}

		final IElement element = document.getElementForInsertionAt(cursor.getOffset());
		final boolean isPreformatted = whitespacePolicy.isPre(element);

		final String toInsert;
		if (!isPreformatted) {
			toInsert = XML.compressWhitespace(XML.normalizeNewlines(text), true, true, true);
		} else {
			toInsert = text;
		}

		doWork(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				for (;;) {
					final int nextLineBreak = toInsert.indexOf('\n', i);
					if (nextLineBreak == -1) {
						break;
					}
					if (nextLineBreak - i > 0) {
						final InsertTextEdit insertText = editStack.apply(new InsertTextEdit(document, cursor.getOffset(), toInsert.substring(i, nextLineBreak)));
						cursor.move(toOffset(insertText.getOffsetAfter()));
					}

					if (isPreformatted) {
						final InsertLineBreakEdit insertLineBreak = editStack.apply(new InsertLineBreakEdit(document, cursor.getOffset()));
						cursor.move(toOffset(insertLineBreak.getOffsetAfter()));
					} else {
						split();
					}
					i = nextLineBreak + 1;
				}

				if (i < toInsert.length()) {
					final InsertTextEdit insertText = editStack.apply(new InsertTextEdit(document, cursor.getOffset(), toInsert.substring(i)));
					cursor.move(toOffset(insertText.getOffsetAfter()));
				}
			}
		});
	}

	@Override
	public void insertXML(final String xml) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert text, because the editor is read-only.");
		}

		final XMLFragment wrappedFragment = new XMLFragment(xml);

		// If fragment contains only simple Text, use insertText to ensure consistent behavior
		if (wrappedFragment.isTextOnly()) {
			insertText(wrappedFragment.getXML());
			return;
		}

		final IElement element = getBlockForInsertionAt(cursor.getOffset());
		final boolean isPreformatted = whitespacePolicy.isPre(element);

		try {
			final IDocumentFragment fragment = wrappedFragment.getDocumentFragment();

			if (document.canInsertFragment(cursor.getOffset(), fragment)) {
				insertFragment(fragment);
			} else if (document.canInsertText(cursor.getOffset())) {
				insertText(fragment.getText());
			}
		} catch (final DocumentValidationException e) {
			// given XML is not valid - Insert text instead if target is preformatted
			if (isPreformatted) {
				insertText(wrappedFragment.getXML());
			} else {
				throw e;
			}
		}
	}

	private IElement getBlockForInsertionAt(final int offset) {
		final IElement element = document.getElementForInsertionAt(offset);

		if (whitespacePolicy.isBlock(element)) {
			return element;
		}

		for (final IParent parent : element.ancestors().matching(Filters.elements())) {
			if (whitespacePolicy.isBlock(parent)) {
				return (IElement) parent;
			}
		}

		return null;
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
