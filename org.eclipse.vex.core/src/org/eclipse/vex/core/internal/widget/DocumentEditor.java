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

import static org.eclipse.vex.core.internal.cursor.CursorMoves.by;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.down;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toNextWord;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toPreviousWord;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toWordEnd;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toWordStart;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.up;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.core.QualifiedNameComparator;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.ChangeAttributeEdit;
import org.eclipse.vex.core.internal.undo.ChangeNamespaceEdit;
import org.eclipse.vex.core.internal.undo.CompoundEdit;
import org.eclipse.vex.core.internal.undo.DefineOffsetEdit;
import org.eclipse.vex.core.internal.undo.DeleteEdit;
import org.eclipse.vex.core.internal.undo.DeleteNextCharEdit;
import org.eclipse.vex.core.internal.undo.DeletePreviousCharEdit;
import org.eclipse.vex.core.internal.undo.EditProcessingInstructionEdit;
import org.eclipse.vex.core.internal.undo.EditStack;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;
import org.eclipse.vex.core.internal.undo.InsertCommentEdit;
import org.eclipse.vex.core.internal.undo.InsertElementEdit;
import org.eclipse.vex.core.internal.undo.InsertFragmentEdit;
import org.eclipse.vex.core.internal.undo.InsertLineBreakEdit;
import org.eclipse.vex.core.internal.undo.InsertProcessingInstructionEdit;
import org.eclipse.vex.core.internal.undo.InsertTextEdit;
import org.eclipse.vex.core.internal.undo.JoinElementsAtOffsetEdit;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IAxis;
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

	private <T extends IUndoableEdit> T apply(final T edit) throws CannotApplyException {
		final T result = editStack.apply(edit);
		cursor.move(toOffset(result.getOffsetAfter()));
		return result;
	}

	/*
	 * Transaction Handling
	 */

	@Override
	public void doWork(final Runnable runnable) throws DocumentValidationException {
		doWork(runnable, false);
	}

	@Override
	public void doWork(final Runnable runnable, final boolean savePosition) throws DocumentValidationException {
		final IPosition position = document.createPosition(cursor.getOffset());
		editStack.beginWork();
		try {
			runnable.run();
			final IUndoableEdit work = editStack.commitWork();
			cursor.move(toOffset(work.getOffsetAfter()));
		} catch (final DocumentValidationException e) {
			final IUndoableEdit work = editStack.rollbackWork();
			cursor.move(toOffset(work.getOffsetBefore()));
			throw e;
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
		caretPosition = new ContentPosition(currentNode, cursor.getOffset());
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

		apply(new DeleteEdit(document, getSelectedRange(), cursor.getOffset()));
	}

	/*
	 * Caret Movement
	 */

	@Override
	public void moveBy(final int distance) {
		moveBy(distance, false);
	}

	@Override
	public void moveBy(final int distance, final boolean select) {
		if (select) {
			cursor.select(by(distance));
		} else {
			cursor.move(by(distance));
		}
	}

	@Override
	public void moveTo(final ContentPosition position) {
		moveTo(position, false);
	}

	@Override
	public void moveTo(final ContentPosition position, final boolean select) {
		if (select) {
			cursor.select(toOffset(position.getOffset()));
		} else {
			cursor.move(toOffset(position.getOffset()));
		}
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
		if (select) {
			cursor.select(down());
		} else {
			cursor.move(down());
		}
	}

	@Override
	public void moveToNextPage(final boolean select) {
	}

	@Override
	public void moveToNextWord(final boolean select) {
		if (select) {
			cursor.select(toNextWord());
		} else {
			cursor.move(toNextWord());
		}
	}

	@Override
	public void moveToPreviousLine(final boolean select) {
		if (select) {
			cursor.select(up());
		} else {
			cursor.move(up());
		}
	}

	@Override
	public void moveToPreviousPage(final boolean select) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToPreviousWord(final boolean select) {
		if (select) {
			cursor.select(toPreviousWord());
		} else {
			cursor.move(toPreviousWord());
		}
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
		apply(new ChangeNamespaceEdit(document, cursor.getOffset(), namespacePrefix, currentNamespaceURI, namespaceURI));
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
		apply(new ChangeNamespaceEdit(document, cursor.getOffset(), namespacePrefix, currentNamespaceURI, null));
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
		apply(new ChangeNamespaceEdit(document, cursor.getOffset(), null, currentNamespaceURI, namespaceURI));
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
		apply(new ChangeNamespaceEdit(document, cursor.getOffset(), null, currentNamespaceURI, null));
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
			apply(new ChangeAttributeEdit(document, cursor.getOffset(), qualifiedAttributeName, currentAttributeValue, value));
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
			apply(new ChangeAttributeEdit(document, cursor.getOffset(), qualifiedAttributeName, currentAttributeValue, null));
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

		apply(new InsertTextEdit(document, cursor.getOffset(), Character.toString(c)));
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

		apply(edit);
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

		apply(edit);
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
						apply(new InsertTextEdit(document, cursor.getOffset(), toInsert.substring(i, nextLineBreak)));
					}

					if (isPreformatted) {
						apply(new InsertLineBreakEdit(document, cursor.getOffset()));
					} else {
						split();
					}
					i = nextLineBreak + 1;
				}

				if (i < toInsert.length()) {
					apply(new InsertTextEdit(document, cursor.getOffset(), toInsert.substring(i)));
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
		if (isReadOnly()) {
			return new ElementName[0];
		}
		if (document == null) {
			return new ElementName[0];
		}
		final IValidator validator = document.getValidator();
		if (validator == null) {
			return new ElementName[0];
		}

		final ContentRange selectedRange = cursor.getSelectedRange();

		final INode parentNode = document.getNodeForInsertionAt(cursor.getOffset());
		final boolean parentNodeIsElement = Filters.elements().matches(parentNode);
		if (!parentNodeIsElement) {
			return new ElementName[0];
		}

		final IElement parent = (IElement) parentNode;

		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(selectedRange.getStartOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(selectedRange.getEndOffset()));
		final List<QualifiedName> selectedNodes = Node.getNodeNames(parent.children().in(selectedRange));
		final List<QualifiedName> candidates = createCandidatesList(validator, parent, IValidator.PCDATA);

		filterInvalidSequences(validator, parent, nodesBefore, nodesAfter, candidates);

		// If there's a selection, root out those candidates that can't contain the selection.
		if (hasSelection()) {
			filterInvalidSelectionParents(validator, selectedNodes, candidates);
		}

		Collections.sort(candidates, new QualifiedNameComparator());

		final ElementName[] result = toElementNames(parent, candidates);
		return result;
	}

	private static List<QualifiedName> createCandidatesList(final IValidator validator, final IElement parent, final QualifiedName... exceptions) {
		final Set<QualifiedName> validItems = validator.getValidItems(parent);
		final List<QualifiedName> exceptionItems = Arrays.asList(exceptions);
		final List<QualifiedName> result = new ArrayList<QualifiedName>();
		for (final QualifiedName validItem : validItems) {
			if (!exceptionItems.contains(validItem)) {
				result.add(validItem);
			}
		}
		return result;
	}

	private static void filterInvalidSequences(final IValidator validator, final IElement parent, final List<QualifiedName> nodesBefore, final List<QualifiedName> nodesAfter, final List<QualifiedName> candidates) {
		final int sequenceLength = nodesBefore.size() + 1 + nodesAfter.size();
		for (final Iterator<QualifiedName> iterator = candidates.iterator(); iterator.hasNext();) {
			final QualifiedName candidate = iterator.next();
			final List<QualifiedName> sequence = new ArrayList<QualifiedName>(sequenceLength);
			sequence.addAll(nodesBefore);
			sequence.add(candidate);
			sequence.addAll(nodesAfter);
			if (!canContainContent(validator, parent.getQualifiedName(), sequence)) {
				iterator.remove();
			}
		}
	}

	private static void filterInvalidSelectionParents(final IValidator validator, final List<QualifiedName> selectedNodes, final List<QualifiedName> candidates) {
		for (final Iterator<QualifiedName> iter = candidates.iterator(); iter.hasNext();) {
			final QualifiedName candidate = iter.next();
			if (!canContainContent(validator, candidate, selectedNodes)) {
				iter.remove();
			}
		}
	}

	private static boolean canContainContent(final IValidator validator, final QualifiedName elementName, final List<QualifiedName> content) {
		return validator.isValidSequence(elementName, content, true);
	}

	private static ElementName[] toElementNames(final IElement parent, final List<QualifiedName> candidates) {
		final ElementName[] result = new ElementName[candidates.size()];
		int i = 0;
		for (final QualifiedName candidate : candidates) {
			result[i++] = new ElementName(candidate, parent.getNamespacePrefix(candidate.getQualifier()));
		}
		return result;
	}

	@Override
	public ElementName[] getValidMorphElements() {
		final IElement currentElement = document.getElementForInsertionAt(cursor.getOffset());
		if (!canMorphElement(currentElement)) {
			return new ElementName[0];
		}

		final IValidator validator = document.getValidator();
		final IElement parent = currentElement.getParentElement();
		final List<QualifiedName> candidates = createCandidatesList(validator, parent, IValidator.PCDATA, currentElement.getQualifiedName());
		if (candidates.isEmpty()) {
			return new ElementName[0];
		}

		final List<QualifiedName> content = Node.getNodeNames(currentElement.children());
		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(currentElement.getStartOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(currentElement.getEndOffset()));

		for (final Iterator<QualifiedName> iter = candidates.iterator(); iter.hasNext();) {
			final QualifiedName candidate = iter.next();
			if (!canContainContent(validator, candidate, content)) {
				iter.remove();
			} else if (!isValidChild(validator, parent.getQualifiedName(), candidate, nodesBefore, nodesAfter)) {
				iter.remove();
			}
		}

		Collections.sort(candidates, new QualifiedNameComparator());
		return toElementNames(parent, candidates);
	}

	private boolean canMorphElement(final IElement element) {
		if (isReadOnly()) {
			return false;
		}
		if (document == null) {
			return false;
		}
		if (document.getValidator() == null) {
			return false;
		}
		if (element.getParentElement() == null) {
			return false;
		}
		if (element == document.getRootElement()) {
			return false;
		}

		return true;
	}

	private static boolean isValidChild(final IValidator validator, final QualifiedName parentName, final QualifiedName elementName, final List<QualifiedName> nodesBefore, final List<QualifiedName> nodesAfter) {
		return validator.isValidSequence(parentName, nodesBefore, Arrays.asList(elementName), nodesAfter, true);
	}

	@Override
	public boolean canInsertElement(final QualifiedName elementName) {
		return canReplaceCurrentSelectionWith(elementName);
	}

	@Override
	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot insert element {0}, because the editor is read-only.", elementName));
		}

		final ContentRange selectedRange = getSelectedRange();
		final IDocumentFragment selectedFragment;
		if (hasSelection()) {
			selectedFragment = getSelectedFragment();
		} else {
			selectedFragment = null;
		}

		final IElement[] result = new IElement[1];
		doWork(new Runnable() {
			@Override
			public void run() {
				if (hasSelection()) {
					apply(new DeleteEdit(document, selectedRange, cursor.getOffset()));
				}

				result[0] = apply(new InsertElementEdit(document, cursor.getOffset(), elementName)).getElement();

				if (selectedFragment != null) {
					insertFragment(selectedFragment);
				}
			}
		});

		return result[0];
	}

	@Override
	public boolean canInsertComment() {
		if (isReadOnly()) {
			return false;
		}
		if (document == null) {
			return false;
		}
		return document.canInsertComment(cursor.getOffset());
	}

	@Override
	public IComment insertComment() throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert comment, because the editor is read-only.");
		}
		Assert.isTrue(canInsertComment());

		if (hasSelection()) {
			deleteSelection();
		}

		return apply(new InsertCommentEdit(document, cursor.getOffset())).getComment();
	}

	@Override
	public boolean canInsertProcessingInstruction() {
		if (isReadOnly()) {
			return false;
		}
		if (document == null) {
			return false;
		}
		return document.canInsertProcessingInstruction(cursor.getOffset(), null);
	}

	@Override
	public IProcessingInstruction insertProcessingInstruction(final String target) throws CannotApplyException, ReadOnlyException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert processing instruction, because the editor is read-only.");
		}
		Assert.isTrue(canInsertProcessingInstruction());

		if (hasSelection()) {
			deleteSelection();
		}

		return apply(new InsertProcessingInstructionEdit(document, cursor.getOffset(), target)).getProcessingInstruction();
	}

	@Override
	public void editProcessingInstruction(final String target, final String data) throws CannotApplyException, ReadOnlyException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot change processing instruction, because the editor is read-only.");
		}
		final INode node = getCurrentNode();
		if (!(node instanceof IProcessingInstruction)) {
			throw new CannotApplyException("Current node is not a processing instruction");
		}

		apply(new EditProcessingInstructionEdit(document, cursor.getOffset(), target, data));
	}

	@Override
	public boolean canInsertFragment(final IDocumentFragment fragment) {
		return canInsertAtCurrentSelection(fragment.getNodeNames());
	}

	@Override
	public void insertFragment(final IDocumentFragment fragment) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot insert fragment, because the editor is read-only");
		}

		if (hasSelection()) {
			deleteSelection();
		}

		final IElement surroundingElement = document.getElementForInsertionAt(cursor.getOffset());

		doWork(new Runnable() {
			@Override
			public void run() {
				final InsertFragmentEdit insertFragment = editStack.apply(new InsertFragmentEdit(document, cursor.getOffset(), fragment));
				final IPosition finalOffset = document.createPosition(insertFragment.getOffsetAfter());

				applyWhitespacePolicy(surroundingElement);

				editStack.apply(new DefineOffsetEdit(insertFragment.getOffsetAfter(), finalOffset.getOffset()));
				document.removePosition(finalOffset);
			}
		});
	}

	private void applyWhitespacePolicy(final INode node) {
		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final IDocument document) {
				document.children().accept(this);
			}

			@Override
			public void visit(final IDocumentFragment fragment) {
				fragment.children().accept(this);
			}

			@Override
			public void visit(final IElement element) {
				element.children().accept(this);
			}

			@Override
			public void visit(final IText text) {
				final IParent parentElement = text.ancestors().matching(Filters.elements()).first();
				if (!whitespacePolicy.isPre(parentElement)) {
					final String compressedContent = XML.compressWhitespace(text.getText(), false, false, false);
					final ContentRange originalTextRange = text.getRange();
					final CompoundEdit compoundEdit = new CompoundEdit();
					compoundEdit.addEdit(new DeleteEdit(document, originalTextRange, originalTextRange.getStartOffset()));
					compoundEdit.addEdit(new InsertTextEdit(document, originalTextRange.getStartOffset(), compressedContent));
					editStack.apply(compoundEdit);
				}
			}
		});
	}

	@Override
	public boolean canUnwrap() {
		if (isReadOnly()) {
			return false;
		}
		if (document == null) {
			return false;
		}
		final IValidator validator = document.getValidator();
		if (validator == null) {
			return false;
		}

		final IElement element = document.getElementForInsertionAt(cursor.getOffset());
		final IElement parent = element.getParentElement();
		if (parent == null) {
			// can't unwrap the root
			return false;
		}

		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(element.getStartOffset()));
		final List<QualifiedName> newNodes = Node.getNodeNames(element.children());
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(element.getEndOffset()));

		return validator.isValidSequence(parent.getQualifiedName(), nodesBefore, newNodes, nodesAfter, true);
	}

	@Override
	public void unwrap() throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot unwrap the element, because the editor is read-only.");
		}

		final IElement currentElement = document.getElementForInsertionAt(cursor.getOffset());
		if (currentElement == document.getRootElement()) {
			throw new DocumentValidationException("Cannot unwrap the root element.");
		}

		final ContentRange elementRange = currentElement.getRange();
		final IDocumentFragment elementContent = document.getFragment(elementRange.resizeBy(1, -1));

		doWork(new Runnable() {
			@Override
			public void run() {
				editStack.apply(new DeleteEdit(document, currentElement.getRange(), cursor.getOffset()));
				if (elementContent != null) {
					editStack.apply(new InsertFragmentEdit(document, elementRange.getStartOffset(), elementContent));
				}
			}
		});
	}

	@Override
	public boolean canMorph(final QualifiedName elementName) {
		final IElement currentElement = document.getElementForInsertionAt(cursor.getOffset());
		if (!canMorphElement(currentElement)) {
			return false;
		}

		final IValidator validator = document.getValidator();

		if (!canContainContent(validator, elementName, Node.getNodeNames(currentElement.children()))) {
			return false;
		}

		final IElement parent = currentElement.getParentElement();
		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(currentElement.getStartOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(currentElement.getEndOffset()));

		return isValidChild(validator, parent.getQualifiedName(), elementName, nodesBefore, nodesAfter);
	}

	@Override
	public void morph(final QualifiedName elementName) throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException(MessageFormat.format("Cannot morph to element {0}, because the editor is read-only.", elementName));
		}

		final IElement currentElement = document.getElementForInsertionAt(cursor.getOffset());
		if (currentElement == document.getRootElement()) {
			throw new DocumentValidationException("Cannot morph the root element.");
		}

		final ContentRange elementRange = currentElement.getRange();
		final IDocumentFragment elementContent = document.getFragment(elementRange.resizeBy(1, -1));

		doWork(new Runnable() {
			@Override
			public void run() {
				editStack.apply(new DeleteEdit(document, currentElement.getRange(), cursor.getOffset()));
				final InsertElementEdit insertElement = editStack.apply(new InsertElementEdit(document, elementRange.getStartOffset(), elementName));
				if (elementContent != null) {
					editStack.apply(new InsertFragmentEdit(document, insertElement.getElement().getEndOffset(), elementContent));
				}
			}
		});
	}

	@Override
	public boolean canJoin() {
		if (isReadOnly()) {
			return false;
		}
		if (!hasSelection()) {
			return false;
		}

		final IElement parent = document.getElementForInsertionAt(cursor.getOffset());
		final IAxis<? extends INode> selectedNodes = parent.children().in(getSelectedRange());
		if (selectedNodes.isEmpty()) {
			return false;
		}

		final IValidator validator = document.getValidator();
		final INode firstNode = selectedNodes.first();
		final List<QualifiedName> childNodeNames = new ArrayList<QualifiedName>();
		int count = 0;
		for (final INode selectedNode : selectedNodes) {
			if (!selectedNode.isKindOf(firstNode)) {
				return false;
			}
			childNodeNames.addAll(selectedNode.accept(new BaseNodeVisitorWithResult<List<QualifiedName>>(Collections.<QualifiedName> emptyList()) {
				@Override
				public List<QualifiedName> visit(final IElement element) {
					return Node.getNodeNames(element.children());
				}
			}));
			count++;
		}

		if (count <= 1) {
			return false;
		}

		final boolean joinedChildrenValid = firstNode.accept(new BaseNodeVisitorWithResult<Boolean>(true) {
			@Override
			public Boolean visit(final IElement element) {
				return validator.isValidSequence(element.getQualifiedName(), childNodeNames, true);
			}
		});
		if (!joinedChildrenValid) {
			return false;
		}

		return true;
	}

	@Override
	public void join() throws DocumentValidationException {
		if (isReadOnly()) {
			return;
		}
		if (!hasSelection()) {
			return;
		}

		final IElement parent = document.getElementForInsertionAt(cursor.getOffset());
		final ContentRange selectedRange = getSelectedRange();
		final IAxis<? extends INode> selectedNodes = parent.children().in(selectedRange);
		if (selectedNodes.isEmpty()) {
			return;
		}

		final INode firstNode = selectedNodes.first();
		final ArrayList<IDocumentFragment> contentToJoin = new ArrayList<IDocumentFragment>();
		for (final INode selectedNode : selectedNodes) {
			if (!selectedNode.isKindOf(firstNode) && !contentToJoin.isEmpty()) {
				throw new DocumentValidationException("Cannot join nodes of different kind.");
			}
			if (!selectedNode.isEmpty()) {
				contentToJoin.add(document.getFragment(selectedNode.getRange().resizeBy(1, -1)));
			}
		}

		if (contentToJoin.size() <= 1) {
			return;
		}

		doWork(new Runnable() {
			@Override
			public void run() {
				final DeleteEdit deletePreservedContent = editStack.apply(new DeleteEdit(document, new ContentRange(firstNode.getEndOffset() + 1, selectedRange.getEndOffset()), cursor.getOffset()));
				editStack.apply(new DeleteEdit(document, firstNode.getRange().resizeBy(1, -1), deletePreservedContent.getOffsetAfter()));
				for (final IDocumentFragment contentPart : contentToJoin) {
					editStack.apply(new InsertFragmentEdit(document, firstNode.getEndOffset(), contentPart));
				}
			}
		});
	}

	@Override
	public boolean canSplit() {
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

		if (!Filters.elements().matches(currentNode)) {
			return false;
		}

		final IElement element = (IElement) currentNode;
		final IElement parent = element.getParentElement();
		if (parent == null) {
			return false;
		}

		final int startOffset = element.getStartOffset();
		final int endOffset = element.getEndOffset();

		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(startOffset));
		final List<QualifiedName> newNodes = Arrays.asList(element.getQualifiedName(), element.getQualifiedName());
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(endOffset));

		return validator.isValidSequence(parent.getQualifiedName(), nodesBefore, newNodes, nodesAfter, true);
	}

	@Override
	public void split() throws DocumentValidationException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot split, because the editor is read-only.");
		}

		if (!Filters.elements().matches(currentNode)) {
			throw new DocumentValidationException("Can only split elements.");
		}
		final IElement element = (IElement) currentNode;

		if (hasSelection()) {
			deleteSelection();
		}

		final boolean splitAtEnd = cursor.getOffset() == element.getEndOffset();
		final ContentRange splittingRange;
		final IDocumentFragment splittedFragment;
		if (!splitAtEnd) {
			splittingRange = new ContentRange(cursor.getOffset(), element.getEndOffset() - 1);
			splittedFragment = document.getFragment(splittingRange);
		} else {
			splittingRange = null;
			splittedFragment = null;
		}

		doWork(new Runnable() {
			@Override
			public void run() {
				if (!splitAtEnd) {
					editStack.apply(new DeleteEdit(document, splittingRange, cursor.getOffset()));
				}
				final IElement newElement = editStack.apply(new InsertElementEdit(document, element.getEndOffset() + 1, element.getQualifiedName())).getElement();
				if (!splitAtEnd) {
					editStack.apply(new InsertFragmentEdit(document, newElement.getEndOffset(), splittedFragment));
				}
				editStack.apply(new DefineOffsetEdit(cursor.getOffset(), newElement.getStartOffset() + 1));
			}
		});
	}

}
