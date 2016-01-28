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
package org.eclipse.vex.core.internal.widget;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

public interface IDocumentEditor {

	/*
	 * Configuration
	 */

	/**
	 * Returns the document associated with this editor.
	 */
	IDocument getDocument();

	/**
	 * Sets a new document for editing.
	 *
	 * @param document
	 *            new Document to edit
	 */
	void setDocument(IDocument document);

	IWhitespacePolicy getWhitespacePolicy();

	void setWhitespacePolicy(IWhitespacePolicy policy);

	ITableModel getTableModel();

	void setTableModel(ITableModel tableModel);

	/**
	 * @return true if this editor is read-only
	 */
	boolean isReadOnly();

	/**
	 * Make this editor read-only.
	 *
	 * @param readOnly
	 *            set to true if this editor should be read-only
	 */
	void setReadOnly(boolean readOnly);

	/*
	 * Undo/Redo
	 */

	/**
	 * Returns true if a redo can be performed.
	 */
	boolean canRedo();

	/**
	 * Redoes the last action on the redo stack.
	 *
	 * @throws CannotApplyException
	 *             if the last action cannot be re-done, or if there is nothing to redo.
	 */
	void redo() throws CannotApplyException;

	/**
	 * Returns true if an undo can be performed.
	 */
	boolean canUndo();

	/**
	 * Undoes the last action on the undo stack.
	 *
	 * @throws CannotUndoException
	 *             if the last action cannot be undone, or if there's nothing left to undo.
	 */
	void undo() throws CannotUndoException;

	boolean isDirty();

	void markClean();

	/*
	 * Transaction Handling
	 */

	/**
	 * Perform the runnable's run method within a transaction. All operations in the runnable are treated as a single
	 * unit of work, and can be undone in one operation by the user. Also, if a later operation fails, all earlier
	 * operations are also undone.
	 *
	 * @param runnable
	 *            Runnable implementing the work to be done.
	 */
	void doWork(Runnable runnable) throws CannotApplyException;

	/**
	 * Perform the runnable's run method within a transaction. All operations in the runnable are treated as a single
	 * unit of work, and can be undone in one operation by the user. Also, if a later operation fails, all earlier
	 * operations are also undone.
	 *
	 * @param runnable
	 *            Runnable implementing the work to be done.
	 * @param savePosition
	 *            If true, the current caret position is saved and restored once the operation is complete.
	 */
	void doWork(Runnable runnable, boolean savePosition) throws CannotApplyException;

	/**
	 * Execute a Runnable, restoring the caret position to its original position afterward.
	 *
	 * @param runnable
	 *            Runnable to be invoked.
	 */
	void savePosition(Runnable runnable);

	/*
	 * Clipboard cut/copy/paste
	 */

	/**
	 * Cuts the current selection to the clipboard.
	 */
	void cutSelection();

	/**
	 * Copy the current selection to the clipboard.
	 */
	void copySelection();

	/**
	 * Returns true if the clipboard has content that can be pasted. Used to enable/disable the paste action of a
	 * containing application.
	 */
	boolean canPaste();

	/**
	 * Paste the current clipboard contents into the document at the current caret position.
	 */
	void paste() throws DocumentValidationException;

	/**
	 * Returns true if the clipboard has plain text content that can be pasted. Used to enable/disable the "paste text"
	 * action of a containing application.
	 */
	boolean canPasteText();

	/**
	 * Paste the current clipboard contents as plain text into the document at the current caret position.
	 */
	void pasteText() throws DocumentValidationException;

	/*
	 * Caret and Selection
	 */

	/**
	 * Return the offset into the document represented by the caret.
	 */
	ContentPosition getCaretPosition();

	/**
	 * Returns the element at the current caret offset.
	 */
	IElement getCurrentElement();

	/**
	 * Returns the node a the current caret offset.
	 */
	INode getCurrentNode();

	/**
	 * Returns the offset range in the content which is selected.
	 */
	ContentRange getSelectedRange();

	/**
	 * Returns the {@link ContentPositionRange} which is selected.
	 */
	ContentPositionRange getSelectedPositionRange();

	/**
	 * Returns the currently selected document fragment, or null if there is no current selection.
	 */
	IDocumentFragment getSelectedFragment();

	/**
	 * Returns the currently selected string, or an empty string if there is no current selection.
	 */
	String getSelectedText();

	/**
	 * Returns true if the user currently has some text selected.
	 */
	boolean hasSelection();

	/**
	 * Selects all content in the document.
	 */
	void selectAll();

	/**
	 * Selects the word at the current caret offset.
	 */
	void selectWord();

	/**
	 * Selects the content of the given node.
	 *
	 * @param node
	 *            the node
	 */
	void selectContentOf(INode node);

	/**
	 * Selects the given node.
	 *
	 * @param node
	 *            the node to select
	 */
	void select(INode node);

	/**
	 * @return true if the current selection can be deleted. Returns false if there is no selection.
	 */
	boolean canDeleteSelection();

	/**
	 * Delete the current selection. Does nothing if there is no current selection.
	 */
	void deleteSelection();

	/*
	 * Caret Movement
	 */

	/**
	 * Moves the caret a given distance relative to the current caret offset.
	 *
	 * @param distance
	 *            Amount by which to alter the caret offset. Positive values increase the caret offset.
	 */
	void moveBy(int distance);

	/**
	 * Moves the caret a given distance relative to the current caret offset.
	 *
	 * @param distance
	 *            Amount by which to alter the caret offset. Positive values increase the caret offset.
	 * @param select
	 *            if true, the current selection is extended to match the new caret offset
	 */
	void moveBy(int distance, boolean select);

	/**
	 * Moves the caret to a new offset. The selection is not extended. This is equivalent to
	 * <code>moveTo(offset, false)</code>.
	 *
	 * @param int
	 *            new offset for the caret. The offset must be >= 1 and less than the document size; if not, it is
	 *            silently ignored.
	 */
	void moveTo(final ContentPosition position);

	/**
	 * Moves the caret to the new offset, possibly changing the selection.
	 *
	 * @param int
	 *            new offset for the caret. The offset must be >= 1 and less than the document size; if not, it is
	 *            silently ignored.
	 * @param select
	 *            if true, the current selection is extended to match the new caret offset.
	 */
	void moveTo(final ContentPosition position, boolean select);

	/**
	 * Move the caret to the end of the current line.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToLineEnd(boolean select);

	/**
	 * Move the caret to the start of the current line.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToLineStart(boolean select);

	/**
	 * Move the caret down to the next line. Attempts to preserve the same distance from the left edge of the control.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToNextLine(boolean select);

	/**
	 * Move the caret down to the next page. Attempts to preserve the same distance from the left edge of the control.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToNextPage(boolean select);

	/**
	 * Moves the caret to the end of the current or next word.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToNextWord(boolean select);

	/**
	 * Moves the caret up to the previous line.
	 *
	 * @param select
	 *            If true, the selection is extended
	 */
	void moveToPreviousLine(boolean select);

	/**
	 * Moves the caret up to the previous page.
	 *
	 * @param select
	 *            If true, the selection is extended
	 */
	void moveToPreviousPage(boolean select);

	/**
	 * Moves the caret to the start of the current or previous word.
	 *
	 * @param select
	 *            If true, the selection is extended.
	 */
	void moveToPreviousWord(boolean select);

	/*
	 * Namespaces
	 */

	void declareNamespace(final String namespacePrefix, final String namespaceURI);

	void removeNamespace(final String namespacePrefix);

	void declareDefaultNamespace(final String namespaceURI);

	void removeDefaultNamespace();

	/*
	 * Attributes
	 */

	/**
	 * @param attributeName
	 *            local name of the attribute being changed.
	 * @param value
	 *            New value for the attribute. If null, the attribute is removed from the element.
	 * @return true if the given value is valid for the attribute with the given name
	 */
	boolean canSetAttribute(String attributeName, String value);

	/**
	 * Sets the value of an attribute in the current element. Attributes set in this manner (as opposed to calling
	 * Element.setAttribute directly) will be subject to undo/redo.
	 *
	 * @param attributeName
	 *            local name of the attribute being changed.
	 * @param value
	 *            New value for the attribute. If null, the attribute is removed from the element.
	 */
	void setAttribute(String attributeName, String value);

	/**
	 * @param attributeName
	 *            the local name of the attribute to remove
	 * @return true if it is valid to remove the attribute with the given name
	 */
	boolean canRemoveAttribute(String attributeName);

	/**
	 * Removes an attribute from the current element. Attributes removed in this manner (as opposed to calling
	 * Element.setAttribute directly) will be subject to undo/redo.
	 *
	 * @param attributeName
	 *            local name of the attribute to remove.
	 */
	void removeAttribute(String attributeName);

	/*
	 * Content
	 */

	/**
	 * Returns true if text can be inserted at the current position.
	 */
	boolean canInsertText();

	/**
	 * Inserts the given character at the current caret position. Any selected content is deleted. The main difference
	 * between this method and insertText is that this method does not use beginWork/endWork, so consecutive calls to
	 * insertChar are collapsed into a single IUndoableEdit. This method should normally only be called in response to a
	 * user typing a key.
	 *
	 * @param c
	 *            Character to insert.
	 */
	void insertChar(char c) throws DocumentValidationException;

	void insertLineBreak() throws DocumentValidationException;

	/**
	 * Deletes the character to the right of the caret.
	 */
	void deleteForward() throws DocumentValidationException;

	/**
	 * Deletes the character to the left of the caret.
	 */
	void deleteBackward() throws DocumentValidationException;

	/**
	 * Inserts the given text at the current caret position. Any selected content is first deleted.
	 *
	 * @param text
	 *            String to insert.
	 */
	void insertText(String text) throws DocumentValidationException;

	/**
	 * Inserts the given XML fragment at the current caret position. Any selected content is first deleted.
	 *
	 * @param xml
	 *            XML to insert
	 * @throws DocumentValidationException
	 */
	public void insertXML(String xml) throws DocumentValidationException;

	/*
	 * Structure
	 */

	/**
	 * Returns an array of names of elements that are valid to insert at the given caret offset and selection
	 */
	ElementName[] getValidInsertElements();

	/**
	 * Returns an array of names of elements to which the element at the current caret location can be morphed.
	 */
	ElementName[] getValidMorphElements();

	/**
	 * @param elementName
	 *            the qualified name of the element to insert
	 * @return true if an element with the given name can be inserted at the current caret position/instead of the
	 *         current selection
	 */
	boolean canInsertElement(QualifiedName elementName);

	/**
	 * Inserts the given element at the current caret position. Any selected content becomes the new contents of the
	 * element.
	 *
	 * @param elementName
	 *            Qualified name of the element to insert.
	 * @return the newly inserted element
	 */
	IElement insertElement(QualifiedName elementName) throws DocumentValidationException;

	/**
	 * @return true if a comment can be inserted at the current caret position/instead of the current selection
	 */
	boolean canInsertComment();

	/**
	 * Inserts a comment a the current caret position. Any selected content is first deleted.
	 *
	 * @return the new comment
	 */
	IComment insertComment() throws DocumentValidationException;

	/**
	 * @return true if a processing instruction can be inserted at the current caret position/instead of the current
	 *         selection
	 */
	boolean canInsertProcessingInstruction();

	/**
	 * Inserts a processing instruction at the current caret position. Any selected content is first deleted.
	 *
	 * @return the new comment
	 */
	IProcessingInstruction insertProcessingInstruction(final String target) throws CannotApplyException, ReadOnlyException;

	/**
	 * Edits the processing instruction at the current caret position. Updates target and data with the given Strings.
	 *
	 * @param target
	 *            The target to set. may be null to keep the old target.
	 * @param data
	 *            The data to set. May be null to keep the old value.
	 */
	void editProcessingInstruction(final String target, final String data) throws CannotApplyException, ReadOnlyException;

	/**
	 * Returns true if the given fragment can be inserted at the current caret position.
	 *
	 * @param fragment
	 *            DocumentFragment to be inserted.
	 */
	boolean canInsertFragment(final IDocumentFragment fragment);

	/**
	 * Inserts the given document fragment at the current caret position. Any selected content is deleted.
	 *
	 * @param frag
	 *            DocumentFragment to insert.
	 */
	void insertFragment(IDocumentFragment fragment) throws DocumentValidationException;

	/**
	 * Returns true if the current element can be unwrapped, i.e. replaced with its content.
	 */
	boolean canUnwrap();

	void unwrap() throws DocumentValidationException;

	/**
	 * Indicates whether the current element can be morphed into the given element.
	 *
	 * @param elementName
	 *            Qualified name of the element to morph the current element into.
	 * @return true if the current element can be morphed
	 */
	boolean canMorph(QualifiedName elementName);

	/**
	 * Replaces the current element with an element with the given name. The content of the element is preserved.
	 *
	 * @param elementName
	 *            Qualified name of the element to replace the current element with.
	 * @throws DocumentValidationException
	 *             if the given element is not valid at this place in the document, or if the current element's content
	 *             is not compatible with the given element.
	 */
	void morph(QualifiedName elementName) throws DocumentValidationException;

	/**
	 * Indiciates whether the caret is at a position between adjacent nodes that can be joined.
	 *
	 * @return true if the nodes adjacent to the caret can be joined
	 */
	boolean canJoin();

	/**
	 * Joins the nodes adjacent to the caret.
	 *
	 * @throws DocumentValidationException
	 */
	void join() throws DocumentValidationException;

	/**
	 * Indicates whether the current element can be splitted into two elements at the current caret position.
	 *
	 * @return true if the current element can be splitted
	 */
	boolean canSplit();

	/**
	 * Splits the element at the current caret offset.
	 */
	void split() throws DocumentValidationException;

}
