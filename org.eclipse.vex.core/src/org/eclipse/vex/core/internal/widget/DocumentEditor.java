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

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.cursor.Cursor;
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
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

public class DocumentEditor implements IDocumentEditor {

	private final Cursor cursor;

	private IDocument document;
	private boolean readOnly;

	public DocumentEditor(final Cursor cursor) {
		this.cursor = cursor;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void redo() throws CannotApplyException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canUndo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void undo() throws CannotUndoException {
		// TODO Auto-generated method stub

	}

	/*
	 * Transaction Handling
	 */

	@Override
	public void beginWork() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doWork(final Runnable runnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doWork(final Runnable runnable, final boolean savePosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endWork(final boolean success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePosition(final Runnable runnable) {
		// TODO Auto-generated method stub

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

	@Override
	public ContentPosition getCaretPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IElement getCurrentElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INode getCurrentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentRange getSelectedRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentPositionRange getSelectedPositionRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDocumentFragment getSelectedFragment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSelectedText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void selectAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectWord() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectContentOf(final INode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void select(final INode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canDeleteSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteSelection() {
		// TODO Auto-generated method stub

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
