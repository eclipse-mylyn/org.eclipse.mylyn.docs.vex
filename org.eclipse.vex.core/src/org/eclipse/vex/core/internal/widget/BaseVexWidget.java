/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Holger Voormann - bug 315914: content assist should only show elements
 *			valid in the current context
 *     Carsten Hiesserich - handling of elements within comments (bug 407801)
 *     Carsten Hiesserich - allow insertion of newline into pre elements (bug 407827)
 *     Carsten Hiesserich - handling of preformatted elements, XML insertion(bug 407827, bug 408501 )
 *     Carsten Hiesserich - added dispose()
 *     Carsten Hiesserich - flushing StyleSheet when content structure is changed
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.QualifiedNameComparator;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.layout.BlockBox;
import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.layout.BoxFactory;
import org.eclipse.vex.core.internal.layout.CssBoxFactory;
import org.eclipse.vex.core.internal.layout.LayoutContext;
import org.eclipse.vex.core.internal.layout.RootBox;
import org.eclipse.vex.core.internal.layout.VerticalRange;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.ChangeAttributeEdit;
import org.eclipse.vex.core.internal.undo.ChangeNamespaceEdit;
import org.eclipse.vex.core.internal.undo.CompoundEdit;
import org.eclipse.vex.core.internal.undo.DeleteEdit;
import org.eclipse.vex.core.internal.undo.EditProcessingInstructionEdit;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;
import org.eclipse.vex.core.internal.undo.InsertCommentEdit;
import org.eclipse.vex.core.internal.undo.InsertElementEdit;
import org.eclipse.vex.core.internal.undo.InsertFragmentEdit;
import org.eclipse.vex.core.internal.undo.InsertProcessingInstructionEdit;
import org.eclipse.vex.core.internal.undo.InsertTextEdit;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;

/**
 * A component that allows the display and edit of an XML document with an associated CSS stylesheet.
 */
public class BaseVexWidget implements IVexWidget {

	/**
	 * Number of pixel rows above and below the caret that are rendered at a time.
	 */
	private static final int LAYOUT_WINDOW = 5000;

	/**
	 * Because the height of each BlockElementBox is initially estimated, we sometimes have to try several times before
	 * the band being laid out is properly positioned about the offset. When the position of the offset changes by less
	 * than this amount between subsequent layout calls, the layout is considered stable.
	 */
	private static final int LAYOUT_TOLERANCE = 500;

	/**
	 * Minimum layout width, in pixels. Prevents performance problems when width is very small.
	 */
	private static final int MIN_LAYOUT_WIDTH = 200;

	private static final IWhitespacePolicy DEFAULT_POLICY = IWhitespacePolicy.ALL_BLOCKS;

	private boolean debugging;
	private boolean readOnly;

	private final IHostComponent hostComponent;
	private int layoutWidth = 500; // something reasonable to handle a document
	// being set before the widget is sized

	private IDocument document;
	private StyleSheet styleSheet;
	private IWhitespacePolicy whitespacePolicy = DEFAULT_POLICY;

	private final BoxFactory boxFactory = new CssBoxFactory();

	private RootBox rootBox;

	/** Stacks of UndoableEditEvents; items added and removed from end of list */
	private LinkedList<UndoableAndOffset> undoList = new LinkedList<UndoableAndOffset>();
	private LinkedList<UndoableAndOffset> redoList = new LinkedList<UndoableAndOffset>();
	private static final int MAX_UNDO_STACK_SIZE = 100;

	/** Support for beginWork/endWork */
	private int beginWorkCount = 0;
	private ContentPosition beginWorkCaretPosition;
	private CompoundEdit compoundEdit;

	private ContentPosition caretPosition;
	private ContentPosition mark;
	private ContentPosition selectionStart;
	private ContentPosition selectionEnd;

	private INode currentNode;

	private boolean caretVisible = true;
	private Caret caret;
	private Color caretColor;

	// x offset to be maintained when moving vertically
	private int magicX = -1;

	private boolean antiAliased = false;

	private final IDocumentListener documentListener = new IDocumentListener() {

		@Override
		public void attributeChanged(final AttributeChangeEvent e) {
			invalidateElementBox(e.getParent());

			/*
			 * Flush cached styles, since they might depend attribute values via conditional selectors.
			 *
			 * This cast is save because this event is only fired due to the attribute changes of elements.
			 */
			getStyleSheet().flushStyles(e.getParent());

			BaseVexWidget.this.relayout();

			fireSelectionChanged();
		}

		@Override
		public void beforeContentDeleted(final ContentChangeEvent e) {
			// Clean-up stylesheet cache
			if (e.isStructuralChange()) {
				final Iterator<? extends INode> childrenToDelete = e.getParent().children().withoutText().in(e.getRange()).iterator();
				while (childrenToDelete.hasNext()) {
					getStyleSheet().flushStyles(childrenToDelete.next());
				}
			}
		}

		@Override
		public void beforeContentInserted(final ContentChangeEvent e) {
		}

		@Override
		public void contentDeleted(final ContentChangeEvent e) {
			flushStyles(e);
			invalidateElementBox(e.getParent());

			BaseVexWidget.this.relayout();
		}

		@Override
		public void contentInserted(final ContentChangeEvent e) {
			flushStyles(e);
			invalidateElementBox(e.getParent());

			BaseVexWidget.this.relayout();
		}

		@Override
		public void namespaceChanged(final NamespaceDeclarationChangeEvent e) {
			invalidateElementBox(e.getParent());

			BaseVexWidget.this.relayout();

			fireSelectionChanged();
		}

		private void flushStyles(final ContentChangeEvent e) {
			if (e.isStructuralChange()) {
				final StyleSheet styleSheet = getStyleSheet();
				styleSheet.flushStyles(e.getParent());

				// Flush styles of children before and after the changed content
				final Iterator<? extends INode> childs = e.getParent().children().withoutText().in(e.getRange().resizeBy(-2, 2)).iterator();
				while (childs.hasNext()) {
					styleSheet.flushStyles(childs.next());
				}
			}
		}

	};

	/**
	 * Class constructor.
	 */
	public BaseVexWidget(final IHostComponent hostComponent) {
		this.hostComponent = hostComponent;
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 *
	 */
	public void dispose() {
		if (document != null) {
			// Flushing the styles is not absolutely necessary, but without doing so,
			// the entries in the StyleSheets cache map would only collected when the
			// map is accessed agein by another instance.
			getStyleSheet().flushAllStyles(document);
			final IDocument doc = document;
			doc.removeDocumentListener(documentListener);
		}
		styleSheet = null;
	}

	public void beginWork() {
		if (beginWorkCount == 0) {
			beginWorkCaretPosition = getCaretPosition();
			compoundEdit = new CompoundEdit();
		}
		beginWorkCount++;
	}

	public void endWork(final boolean success) {
		beginWorkCount--;
		if (beginWorkCount == 0) {
			// this.compoundEdit.end();
			if (success) {
				undoList.add(new UndoableAndOffset(compoundEdit, beginWorkCaretPosition.getOffset()));
				if (undoList.size() > MAX_UNDO_STACK_SIZE) {
					undoList.removeFirst();
				}
				redoList.clear();
				relayout();
				fireSelectionChanged();
			} else {
				try {
					compoundEdit.undo();
					this.moveTo(beginWorkCaretPosition);
				} catch (final CannotUndoException e) {
					// TODO: handle exception
				}
			}
			compoundEdit = null;
		}
	}

	private void beginSelection() {
		beginWorkCount++;
	}

	private void endSelection() {
		beginWorkCount--;
	}

	private boolean isInWorkBlock() {
		return beginWorkCount > 0;
	}

	@Override
	public boolean canInsertComment() {
		if (readOnly) {
			return false;
		}
		if (document == null) {
			return false;
		}
		return document.canInsertComment(getCaretPosition().getOffset());
	}

	@Override
	public boolean canInsertProcessingInstruction() {
		if (readOnly) {
			return false;
		}
		if (document == null) {
			return false;
		}
		return document.canInsertProcessingInstruction(getCaretPosition().getOffset(), null);
	}

	@Override
	public boolean canInsertFragment(final IDocumentFragment fragment) {
		return canInsertAtCurrentSelection(fragment.getNodeNames());
	}

	@Override
	public boolean canInsertText() {
		return canReplaceCurrentSelectionWith(IValidator.PCDATA);
	}

	private boolean canReplaceCurrentSelectionWith(final QualifiedName... nodeNames) {
		return canInsertAtCurrentSelection(Arrays.asList(nodeNames));
	}

	private boolean canInsertAtCurrentSelection(final List<QualifiedName> nodeNames) {
		if (readOnly) {
			return false;
		}

		if (document == null) {
			return false;
		}

		final IValidator validator = document.getValidator();
		if (validator == null) {
			return true;
		}

		ContentPosition startPosition = getCaretPosition();
		ContentPosition endPosition = getCaretPosition();
		if (hasSelection()) {
			startPosition = getSelectionStart();
			endPosition = getSelectionEnd();
		}

		final IElement parent = document.getElementForInsertionAt(startPosition.getOffset());
		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(startPosition.getOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(endPosition.getOffset()));

		return validator.isValidSequence(parent.getQualifiedName(), nodesBefore, nodeNames, nodesAfter, true);
	}

	@Override
	public boolean canPaste() {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public boolean canPasteText() {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public boolean canRedo() {
		if (readOnly) {
			return false;
		}
		return !redoList.isEmpty();
	}

	@Override
	public boolean canUndo() {
		if (readOnly) {
			return false;
		}
		return !undoList.isEmpty();
	}

	@Override
	public void copySelection() {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public void cutSelection() {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public void deleteForward() throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		} else {
			final ContentPosition position = getCaretPosition();
			final int n = document.getLength() - 1;
			if (position.getOffset() == n) {
				// nop
			} else if (isBetweenMatchingElements(position.getOffset())) {
				joinElementsAt(position);
			} else if (isBetweenMatchingElements(position.getOffset() + 1)) {
				joinElementsAt(position.moveBy(1));
			} else if (document.getNodeForInsertionAt(position).isEmpty()) {
				// deleting the right sentinel of an empty element
				// so just delete the whole element an move on
				moveBy(1);
				moveBy(-2, true);
				deleteSelection();
			} else if (document.getNodeForInsertionAt(position.moveBy(1)).isEmpty()) {
				// deleting the left sentinel of an empty element
				// so just delete the whole element an move on
				moveBy(2, true);
				deleteSelection();
			} else if (!document.isTagAt(position.getOffset())) {
				deleteNextToCaret();
			}
		}
	}

	@Override
	public void deleteBackward() throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		} else {
			final ContentPosition position = getCaretPosition();
			if (position.getOffset() == 1) {
				// nop
			} else if (isBetweenMatchingElements(position.getOffset())) {
				joinElementsAt(position);
			} else if (isBetweenMatchingElements(position.getOffset() - 1)) {
				joinElementsAt(position.moveBy(-1));
			} else if (document.getNodeForInsertionAt(position).isEmpty()) {
				// deleting the left sentinel of an empty element
				// so just delete the whole element an move on
				moveBy(1);
				moveBy(-2, true);
				deleteSelection();
			} else if (document.getNodeForInsertionAt(position.moveBy(-1)).isEmpty()) {
				// deleting the right sentinel of an empty element
				// so just delete the whole element an move on
				moveBy(-2, true);
				deleteSelection();
			} else {
				if (!document.isTagAt(position.moveBy(-1).getOffset())) {
					deleteBeforeCaret();
				}
			}
		}
	}

	@Override
	public boolean canDeleteSelection() {
		if (readOnly) {
			return false;
		}
		if (!hasSelection()) {
			return false;
		}
		return document.canDelete(getSelectedRange());
	}

	@Override
	public void deleteSelection() throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot delete, because the editor is read-only.");
		}

		try {
			if (hasSelection()) {
				// The position has to be moved here, because selectionStart may be invalid after the deletion.
				final ContentPosition positionAfterDelete = getSelectionStart().moveBy(-1);
				applyEdit(new DeleteEdit(document, getSelectedRange(), getSelectionEnd().getOffset()), getSelectionEnd().getOffset());
				this.moveTo(positionAfterDelete.moveBy(1));
			}
		} catch (final DocumentValidationException e) {
			e.printStackTrace(); // This should never happen, because we constrain the selection
		}
	}

	private void deleteNextToCaret() {
		try {
			final ContentPosition nextToCaret = getCaretPosition();
			applyEdit(new DeleteEdit(document, new ContentRange(nextToCaret, nextToCaret), nextToCaret.getOffset()), nextToCaret.getOffset());
			this.moveTo(nextToCaret);
		} catch (final DocumentValidationException e) {
			e.printStackTrace(); // This should never happen, because we constrain the selection
		}
	}

	private void deleteBeforeCaret() {
		try {
			final ContentPosition beforeCaret = getCaretPosition().moveBy(-1);
			applyEdit(new DeleteEdit(document, new ContentRange(beforeCaret, beforeCaret), beforeCaret.getOffset() + 1), beforeCaret.getOffset() + 1);
			this.moveTo(beforeCaret);
		} catch (final DocumentValidationException e) {
			e.printStackTrace(); // This should never happen, because we constrain the selection
		}
	}

	@Override
	public void doWork(final Runnable runnable) {
		this.doWork(runnable, false);
	}

	@Override
	public void doWork(final Runnable runnable, final boolean savePosition) {
		IPosition position = null;

		if (savePosition) {
			position = document.createPosition(getCaretPosition().getOffset());
		}

		boolean success = false;
		try {
			beginWork();
			runnable.run();
			success = true;
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			endWork(success);
			if (position != null) {
				this.moveTo(new ContentPosition(document, position.getOffset()));
			}
		}
	}

	private Box findInnermostBox(final IBoxFilter filter) {
		return this.findInnermostBox(filter, getCaretPosition().getOffset());
	}

	/**
	 * Returns the innermost box containing the given offset that matches the given filter.
	 *
	 * @param filter
	 *            IBoxFilter that determines which box to return
	 * @param offset
	 *            Document offset around which to search.
	 */
	private Box findInnermostBox(final IBoxFilter filter, final int offset) {

		Box box = rootBox.getChildren()[0];
		Box matchingBox = null;

		for (;;) {
			if (filter.matches(box)) {
				matchingBox = box;
			}

			final Box original = box;
			final Box[] children = box.getChildren();
			for (final Box child : children) {
				if (child.hasContent() && offset >= child.getStartOffset() && offset <= child.getEndOffset()) {
					box = child;
					break;
				}
			}

			if (box == original) {
				// No child found containing offset,
				// so just return the latest match.
				return matchingBox;
			}
		}

	}

	/**
	 * Returns the background color for the control, which is the same as the background color of the root element.
	 */
	public Color getBackgroundColor() {
		return styleSheet.getStyles(document.getRootElement()).getBackgroundColor();
	}

	/**
	 * Returns the current caret.
	 */
	public Caret getCaret() {
		return caret;
	}

	@Override
	public ContentPosition getCaretPosition() {
		return caretPosition;
	}

	private int getCaretOffset() {
		return caretPosition.getOffset();
	}

	private ContentPosition getStartPosition() {
		if (hasSelection()) {
			return getSelectionStart();
		}
		return getCaretPosition();
	}

	private ContentPosition getEndPosition() {
		if (hasSelection()) {
			return getSelectionEnd();
		}
		return getCaretPosition();
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
	public IDocument getDocument() {
		return document;
	}

	/**
	 * Returns the natural height of the widget based on the current layout width.
	 */
	public int getHeight() {
		return rootBox.getHeight();
	}

	@Override
	public ElementName[] getValidInsertElements() {
		if (readOnly) {
			return new ElementName[0];
		}

		if (document == null) {
			return new ElementName[0];
		}

		final IValidator validator = document.getValidator();
		if (validator == null) {
			return new ElementName[0];
		}

		final ContentPosition startPosition = getStartPosition();
		final ContentPosition endPosition = getEndPosition();

		final INode parentNode = document.getNodeForInsertionAt(startPosition);
		final boolean parentNodeIsElement = Filters.elements().matches(parentNode);
		if (!parentNodeIsElement) {
			return new ElementName[0];
		}

		final IElement parent = (IElement) parentNode;

		final List<QualifiedName> nodesBefore = Node.getNodeNames(parent.children().before(startPosition.getOffset()));
		final List<QualifiedName> nodesAfter = Node.getNodeNames(parent.children().after(endPosition.getOffset()));
		final List<QualifiedName> selectedNodes = Node.getNodeNames(parent.children().in(new ContentRange(startPosition, endPosition)));
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

	public boolean isAntiAliased() {
		return antiAliased;
	}

	@Override
	public boolean isDebugging() {
		return debugging;
	}

	private ContentPosition getSelectionEnd() {
		return selectionEnd;
	}

	private ContentPosition getSelectionStart() {
		return selectionStart;
	}

	@Override
	public ContentRange getSelectedRange() {
		if (!hasSelection()) {
			return new ContentRange(getCaretPosition(), getCaretPosition());
		}
		return new ContentRange(getSelectionStart(), getSelectionEnd().moveBy(-1));
	}

	@Override
	public ContentPositionRange getSelectedPositionRange() {
		return new ContentPositionRange(getSelectionStart(), getSelectionEnd());
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
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

	@Override
	public int getLayoutWidth() {
		return layoutWidth;
	}

	public RootBox getRootBox() {
		return rootBox;
	}

	@Override
	public boolean hasSelection() {
		return !getSelectionStart().equals(getSelectionEnd());
	}

	@Override
	public boolean canInsertElement(final QualifiedName elementName) {
		return canReplaceCurrentSelectionWith(elementName);
	}

	@Override
	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException(MessageFormat.format("Cannot insert element {0}, because the editor is read-only.", elementName));
		}

		boolean success = false;
		final IElement result;
		try {
			beginWork();

			IDocumentFragment selectedFragment = null;
			if (hasSelection()) {
				selectedFragment = getSelectedFragment();
				deleteSelection();
			}

			result = applyEdit(new InsertElementEdit(document, getCaretPosition().getOffset(), elementName), getCaretPosition().getOffset()).getElement();

			this.moveTo(getCaretPosition().moveBy(1));
			if (selectedFragment != null) {
				insertFragment(selectedFragment);
			}
			scrollCaretVisible();
			success = true;
			return result;
		} finally {
			endWork(success);
		}
	}

	@Override
	public void insertFragment(final IDocumentFragment fragment) throws DocumentValidationException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert fragment, because the editor is read-only");
		}

		if (hasSelection()) {
			deleteSelection();
		}

		final IElement surroundingElement = document.getElementForInsertionAt(getCaretOffset());

		boolean success = false;
		try {
			beginWork();
			applyEdit(new InsertFragmentEdit(document, getCaretOffset(), fragment), getCaretOffset());
			final IPosition finalCaretPosition = document.createPosition(getCaretOffset() + fragment.getLength());

			applyWhitespacePolicy(surroundingElement);

			this.moveTo(new ContentPosition(caretPosition.getNodeAtOffset(), finalCaretPosition.getOffset()));
			document.removePosition(finalCaretPosition);
			success = true;
		} finally {
			endWork(success);
		}
	}

	@Override
	public void insertText(final String text) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert text, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		}

		final IElement element = document.getElementForInsertionAt(getCaretOffset());
		final boolean isPreformatted = whitespacePolicy.isPre(element);

		final String toInsert;
		if (!isPreformatted) {
			toInsert = XML.compressWhitespace(XML.normalizeNewlines(text), true, true, true);
		} else {
			toInsert = text;
		}

		boolean success = false;
		try {
			beginWork();
			int i = 0;
			for (;;) {
				final int nextLineBreak = toInsert.indexOf('\n', i);
				if (nextLineBreak == -1) {
					break;
				}
				if (nextLineBreak - i > 0) {
					applyEdit(new InsertTextEdit(document, getCaretOffset(), toInsert.substring(i, nextLineBreak)), getCaretOffset());
				}
				this.moveTo(getCaretPosition().moveBy(nextLineBreak - i));
				if (isPreformatted) {
					applyEdit(new InsertTextEdit(document, getCaretOffset(), "\n"), getCaretOffset());
					moveBy(1);
				} else {
					split();
				}
				i = nextLineBreak + 1;
			}

			if (i < toInsert.length()) {
				applyEdit(new InsertTextEdit(document, getCaretOffset(), toInsert.substring(i)), getCaretOffset());
				this.moveTo(getCaretPosition().moveBy(toInsert.length() - i));
			}
			success = true;
		} finally {
			endWork(success);
		}
	}

	@Override
	public void insertXML(final String xml) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert text, because the editor is read-only.");
		}

		final XMLFragment wrappedFragment = new XMLFragment(xml);

		// If fragment contains only simple Text, use insertText to ensure consistent behavior
		if (wrappedFragment.isTextOnly()) {
			insertText(wrappedFragment.getXML());
			return;
		}

		final IElement element = getBlockForInsertionAt(getCaretOffset());
		final boolean isPreformatted = whitespacePolicy.isPre(element);

		try {
			final IDocumentFragment fragment = wrappedFragment.getDocumentFragment();

			if (document.canInsertFragment(getCaretOffset(), fragment)) {
				insertFragment(fragment);
			} else if (document.canInsertText(getCaretOffset())) {
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
					applyEdit(compoundEdit, originalTextRange.getStartOffset());
				}

			}
		});
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

	@Override
	public void insertChar(final char c) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert a character, because the editor is read-only.");
		}

		if (hasSelection()) {
			deleteSelection();
		}
		applyEdit(new InsertTextEdit(document, getCaretOffset(), Character.toString(c)), getCaretOffset());
		this.moveBy(+1);
	}

	@Override
	public IComment insertComment() throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert comment, because the editor is read-only.");
		}
		Assert.isTrue(canInsertComment());

		if (hasSelection()) {
			deleteSelection();
		}

		boolean success = false;
		try {
			beginWork();

			final InsertCommentEdit edit = applyEdit(new InsertCommentEdit(document, getCaretOffset()), getCaretOffset());
			final IComment result = edit.getComment();
			this.moveTo(getCaretPosition().moveBy(1));
			scrollCaretVisible();
			success = true;
			return result;
		} finally {
			endWork(success);
		}
	}

	@Override
	public IProcessingInstruction insertProcessingInstruction(final String target) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot insert processing instruction, because the editor is read-only.");
		}
		Assert.isTrue(canInsertProcessingInstruction());

		boolean success = false;
		try {
			beginWork();

			final InsertProcessingInstructionEdit edit = applyEdit(new InsertProcessingInstructionEdit(document, getCaretOffset(), target), getCaretOffset());
			final IProcessingInstruction result = edit.getProcessingInstruction();
			this.moveTo(getCaretPosition().moveBy(1));
			scrollCaretVisible();
			success = true;
			return result;
		} finally {
			endWork(success);
		}
	}

	@Override
	public void editProcessingInstruction(final String target, final String data) throws CannotApplyException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot change processing instruction, because the editor is read-only.");
		}

		final INode node = getCurrentNode();
		if (!(node instanceof IProcessingInstruction)) {
			throw new CannotApplyException("Current node is not a processing instruction");
		}

		boolean success = false;
		try {
			beginWork();
			applyEdit(new EditProcessingInstructionEdit(document, getCaretOffset(), target, data), getCaretOffset());
			this.moveTo(node.getStartPosition().moveBy(1));
			scrollCaretVisible();
			success = true;
		} finally {
			endWork(success);
		}
	}

	@Override
	public boolean canUnwrap() {
		if (readOnly) {
			return false;
		}

		if (document == null) {
			return false;
		}

		final IValidator validator = document.getValidator();
		if (validator == null) {
			return false;
		}

		final IElement element = document.getElementForInsertionAt(getCaretOffset());
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
	public void unwrap() throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot unwrap the element, because the editor is read-only.");
		}

		final ContentPosition caretPosition = getCaretPosition();
		final IElement currentElement = document.getElementForInsertionAt(caretPosition.getOffset());

		if (currentElement == document.getRootElement()) {
			throw new DocumentValidationException("Cannot unwrap the root element.");
		}

		boolean success = false;
		try {
			beginWork();
			this.moveTo(currentElement.getStartPosition().moveBy(1), false);
			this.moveTo(currentElement.getEndPosition(), true);
			final IDocumentFragment frag = getSelectedFragment();
			deleteSelection();
			this.moveBy(-1, false);
			this.moveBy(2, true);
			deleteSelection();
			if (frag != null) {
				insertFragment(frag);
			}
			this.moveTo(caretPosition.moveBy(-1), false);
			success = true;
		} finally {
			endWork(success);
		}
	}

	@Override
	public ElementName[] getValidMorphElements() {
		final IElement currentElement = document.getElementForInsertionAt(getCaretOffset());
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

	private static ElementName[] toElementNames(final IElement parent, final List<QualifiedName> candidates) {
		final ElementName[] result = new ElementName[candidates.size()];
		int i = 0;
		for (final QualifiedName candidate : candidates) {
			result[i++] = new ElementName(candidate, parent.getNamespacePrefix(candidate.getQualifier()));
		}
		return result;
	}

	private boolean canMorphElement(final IElement element) {
		if (readOnly) {
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

	private static boolean canContainContent(final IValidator validator, final QualifiedName elementName, final List<QualifiedName> content) {
		return validator.isValidSequence(elementName, content, true);
	}

	private static boolean isValidChild(final IValidator validator, final QualifiedName parentName, final QualifiedName elementName, final List<QualifiedName> nodesBefore, final List<QualifiedName> nodesAfter) {
		return validator.isValidSequence(parentName, nodesBefore, Arrays.asList(elementName), nodesAfter, true);
	}

	@Override
	public boolean canMorph(final QualifiedName elementName) {
		final IElement currentElement = document.getElementForInsertionAt(getCaretOffset());
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
	public void morph(final QualifiedName elementName) throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException(MessageFormat.format("Cannot morph to element {0}, because the editor is read-only.", elementName));
		}

		final ContentPosition caretPosition = getCaretPosition();
		final IElement currentElement = document.getElementForInsertionAt(caretPosition.getOffset());

		if (currentElement == document.getRootElement()) {
			throw new DocumentValidationException("Cannot morph the root element.");
		}

		boolean success = false;
		try {
			beginWork();
			this.moveTo(currentElement.getStartPosition().moveBy(1), false);
			this.moveTo(currentElement.getEndPosition(), true);
			final IDocumentFragment frag = getSelectedFragment();
			deleteSelection();
			this.moveBy(-1, false);
			this.moveBy(2, true);
			deleteSelection();
			insertElement(elementName);
			if (frag != null) {
				insertFragment(frag);
			}
			this.moveTo(caretPosition, false);
			success = true;
		} finally {
			endWork(success);
		}

	}

	@Override
	public void moveBy(final int distance) {
		this.moveTo(getCaretPosition().moveBy(distance), false);
	}

	@Override
	public void moveBy(final int distance, final boolean select) {
		this.moveTo(getCaretPosition().moveBy(distance), select);
	}

	@Override
	public void moveTo(final ContentPosition position) {
		this.moveTo(position, false);
	}

	@Override
	public void moveTo(final ContentPosition position, final boolean select) {
		if (!Document.isInsertionPointIn(document, position.getOffset())) {
			return;
		}

		repaintCaret();
		repaintSelectedRange();

		if (select) {
			moveSelectionTo(position);
		} else {
			moveCaretTo(position);
		}

		final INode oldNode = currentNode;
		currentNode = document.getNodeForInsertionAt(position);

		relayout();

		final Graphics g = hostComponent.createDefaultGraphics();
		final LayoutContext context = createLayoutContext(g);
		caret = rootBox.getCaret(context, caretPosition);

		INode node = currentNode;
		if (node != oldNode) {
			caretColor = Color.BLACK;
			while (node != null) {
				final Color bgColor = styleSheet.getStyles(node).getBackgroundColor();
				if (bgColor != null) {
					final int red = ~bgColor.getRed() & 0xff;
					final int green = ~bgColor.getGreen() & 0xff;
					final int blue = ~bgColor.getBlue() & 0xff;
					caretColor = new Color(red, green, blue);
					break;
				}
				node = node.getParent();
			}
		}

		g.dispose();

		magicX = -1;
		scrollCaretVisible();
		fireSelectionChanged();
		caretVisible = true;

		repaintSelectedRange();
	}

	private void moveSelectionTo(final ContentPosition position) {
		final boolean movingForward = position.isAfter(caretPosition);
		final boolean movingBackward = position.isBefore(caretPosition);
		final boolean movingTowardMark = movingForward && mark.isAfterOrEquals(position) || movingBackward && mark.isBeforeOrEquals(position);
		final boolean movingAwayFromMark = !movingTowardMark;

		// expand or shrink the selection to make sure the selection is balanced
		final ContentPosition balancedStart = ContentPosition.smallest(mark, position);
		final ContentPosition balancedEnd = ContentPosition.greatest(mark, position);
		final INode balancedNode = document.findCommonNode(balancedStart.getOffset(), balancedEnd.getOffset());
		if (movingForward && movingTowardMark) {
			selectionStart = ContentPosition.balanceForward(balancedStart, balancedNode);
			selectionEnd = ContentPosition.balanceForward(balancedEnd, balancedNode);
			caretPosition = selectionStart;
		} else if (movingBackward && movingTowardMark) {
			selectionStart = ContentPosition.balanceBackward(balancedStart, balancedNode);
			selectionEnd = ContentPosition.balanceBackward(balancedEnd, balancedNode);
			caretPosition = selectionEnd;
		} else if (movingForward && movingAwayFromMark) {
			selectionStart = ContentPosition.balanceBackward(balancedStart, balancedNode);
			selectionEnd = ContentPosition.balanceForward(balancedEnd, balancedNode);
			caretPosition = selectionEnd;
		} else if (movingBackward && movingAwayFromMark) {
			selectionStart = ContentPosition.balanceBackward(balancedStart, balancedNode);
			selectionEnd = ContentPosition.balanceForward(balancedEnd, balancedNode);
			caretPosition = selectionStart;
		}
	}

	private void moveCaretTo(final ContentPosition position) {
		selectionStart = position;
		selectionEnd = position;
		caretPosition = position;
		mark = position;
	}

	@Override
	public void moveToLineEnd(final boolean select) {
		final ContentPosition position = rootBox.getLineEndPosition(caretPosition.copy());
		this.moveTo(position, select);
	}

	@Override
	public void moveToLineStart(final boolean select) {
		final ContentPosition position = rootBox.getLineStartPosition(caretPosition.copy());
		this.moveTo(position, select);
	}

	@Override
	public void moveToNextLine(final boolean select) {
		final int x = magicX == -1 ? caret.getBounds().getX() : magicX;

		final Graphics g = hostComponent.createDefaultGraphics();
		final ContentPosition position = rootBox.getNextLinePosition(createLayoutContext(g), caretPosition.copy(), x);
		g.dispose();

		this.moveTo(position, select);
		magicX = x;
	}

	@Override
	public void moveToNextPage(final boolean select) {
		final int x = magicX == -1 ? caret.getBounds().getX() : magicX;
		final int y = caret.getY() + Math.round(hostComponent.getViewport().getHeight() * 0.9f);
		this.moveTo(viewToModel(x, y), select);
		magicX = x;
	}

	@Override
	public void moveToNextWord(final boolean select) {
		final int n = document.getLength() - 1;
		int offset = getCaretOffset();
		while (offset < n && !Character.isLetterOrDigit(document.getCharacterAt(offset))) {
			offset++;
		}

		while (offset < n && Character.isLetterOrDigit(document.getCharacterAt(offset))) {
			offset++;
		}

		this.moveTo(new ContentPosition(document, offset), select);
	}

	@Override
	public void moveToPreviousLine(final boolean select) {
		final int x = magicX == -1 ? caret.getBounds().getX() : magicX;

		final Graphics g = hostComponent.createDefaultGraphics();
		final ContentPosition position = rootBox.getPreviousLinePosition(createLayoutContext(g), caretPosition.copy(), x);
		g.dispose();

		this.moveTo(position, select);
		magicX = x;
	}

	@Override
	public void moveToPreviousPage(final boolean select) {
		final int x = magicX == -1 ? caret.getBounds().getX() : magicX;
		final int y = caret.getY() - Math.round(hostComponent.getViewport().getHeight() * 0.9f);
		this.moveTo(viewToModel(x, y), select);
		magicX = x;
	}

	@Override
	public void moveToPreviousWord(final boolean select) {
		int offset = getCaretOffset();
		while (offset > 1 && !Character.isLetterOrDigit(document.getCharacterAt(offset - 1))) {
			offset--;
		}

		while (offset > 1 && Character.isLetterOrDigit(document.getCharacterAt(offset - 1))) {
			offset--;
		}

		this.moveTo(new ContentPosition(document, offset), select);
	}

	private void select(final ContentPositionRange range) {
		if (!range.isInsertionPointIn(document)) {
			return;
		}

		beginSelection();
		moveTo(range.getStartPosition());
		moveTo(range.getEndPosition(), true);
		endSelection();
	}

	private void fireSelectionChanged() {
		if (isInWorkBlock()) {
			return;
		}
		hostComponent.fireSelectionChanged();
	}

	@Override
	public void selectAll() {
		select(document.getPositionRange().resizeBy(1, -1));
	}

	@Override
	public void selectWord() {
		int startOffset = getCaretOffset();
		int endOffset = getCaretOffset();
		while (startOffset > 1 && Character.isLetterOrDigit(document.getCharacterAt(startOffset - 1))) {
			startOffset--;
		}
		final int n = document.getLength() - 1;
		while (endOffset < n && Character.isLetterOrDigit(document.getCharacterAt(endOffset))) {
			endOffset++;
		}

		final INode nodeAtCaret = caretPosition.getNodeAtOffset();

		if (startOffset < endOffset) {
			select(new ContentPositionRange(new ContentPosition(nodeAtCaret, startOffset), new ContentPosition(nodeAtCaret, endOffset)));
		}
	}

	@Override
	public void selectContentOf(final INode node) {
		if (node.isEmpty()) {
			moveTo(node.getEndPosition());
			return;
		}

		select(node.getPositionRange().resizeBy(1, 0));
	}

	@Override
	public void select(final INode node) {
		select(node.getPositionRange());
	}

	/**
	 * Paints the contents of the widget in the given Graphics at the given point.
	 *
	 * @param g
	 *            Graphics in which to draw the widget contents
	 * @param x
	 *            x-coordinate at which to draw the widget
	 * @param y
	 *            y-coordinate at which to draw the widget
	 */
	public void paint(final Graphics g, final int x, final int y) {

		if (rootBox == null) {
			return;
		}

		final LayoutContext context = createLayoutContext(g);

		// Since we may be scrolling to sections of the document that have
		// yet to be layed out, lay out any exposed area.
		//
		// TODO: this will probably be inaccurate, since we should really
		// iterate the layout, but we don't have an offset around which
		// to iterate...what to do, what to do....
		final Rectangle rect = g.getClipBounds();
		final int oldHeight = rootBox.getHeight();
		rootBox.layout(context, rect.getY(), rect.getY() + rect.getHeight());
		if (rootBox.getHeight() != oldHeight) {
			hostComponent.setPreferredSize(rootBox.getWidth(), rootBox.getHeight());
		}

		rootBox.paint(context, 0, 0);
		if (caretVisible) {
			caret.draw(g, caretColor);
		}

		// Debug hash marks
		/*
		 * ColorResource grey = g.createColor(new Color(160, 160, 160)); ColorResource oldColor = g.setColor(grey); for
		 * (int y2 = rect.getY() - rect.getY() % 50; y2 < rect.getY() + rect.getHeight(); y2 += 50) { g.drawLine(x, y +
		 * y2, x+10, y + y2); g.drawString(Integer.toString(y2), x + 15, y + y2 - 10); } g.setColor(oldColor);
		 * grey.dispose();
		 */
	}

	@Override
	public void paste() throws DocumentValidationException {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public void pasteText() throws DocumentValidationException {
		throw new UnsupportedOperationException("Must be implemented in tookit-specific widget.");
	}

	@Override
	public void redo() throws CannotApplyException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot redo, because the editor is read-only.");
		}
		if (redoList.isEmpty()) {
			throw new CannotApplyException();
		}
		final UndoableAndOffset event = redoList.removeLast();
		this.moveTo(new ContentPosition(document, event.caretOffset), false);
		event.edit.redo();
		undoList.add(event);
	}

	@Override
	public void savePosition(final Runnable runnable) {
		final IPosition pos = document.createPosition(getCaretOffset());
		try {
			runnable.run();
		} finally {
			this.moveTo(new ContentPosition(document, pos.getOffset()));
		}
	}

	/**
	 * Sets the value of the antiAliased flag.
	 *
	 * @param antiAliased
	 *            if true, text is rendered using antialiasing.
	 */
	public void setAntiAliased(final boolean antiAliased) {
		this.antiAliased = antiAliased;
	}

	@Override
	public boolean canSetAttribute(final String attributeName, final String value) {
		if (readOnly) {
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
	public void setAttribute(final String attributeName, final String value) throws ReadOnlyException {
		if (readOnly) {
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
			applyEdit(new ChangeAttributeEdit(document, getCaretOffset(), qualifiedAttributeName, currentAttributeValue, value), getCaretOffset());
		}
	}

	@Override
	public boolean canRemoveAttribute(final String attributeName) {
		if (readOnly) {
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
	public void removeAttribute(final String attributeName) throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException(MessageFormat.format("Cannot remove attribute {0}, because the editor is read-only.", attributeName));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			return;
		}

		final QualifiedName qualifiedAttributeName = element.qualify(attributeName);
		final String currentAttributeValue = element.getAttributeValue(qualifiedAttributeName);
		if (currentAttributeValue != null) {
			applyEdit(new ChangeAttributeEdit(document, getCaretOffset(), qualifiedAttributeName, currentAttributeValue, null), getCaretOffset());
		}
	}

	@Override
	public void setDebugging(final boolean debugging) {
		this.debugging = debugging;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void setDocument(final IDocument document, final StyleSheet styleSheet) {
		if (this.document != null) {
			final IDocument doc = document;
			doc.removeDocumentListener(documentListener);
		}

		this.document = document;
		this.styleSheet = styleSheet;

		undoList = new LinkedList<UndoableAndOffset>();
		redoList = new LinkedList<UndoableAndOffset>();
		beginWorkCount = 0;
		compoundEdit = null;

		caretPosition = new ContentPosition(document, document.getRootElement().getStartPosition().moveBy(1).getOffset());
		selectionStart = selectionEnd = caretPosition;
		createRootBox();

		this.moveTo(caretPosition);
		this.document.addDocumentListener(documentListener);
	}

	@Override
	public void setDocument(final IDocument document) {
		setDocument(document, StyleSheet.NULL);
	}

	/**
	 * Called by the host component when it gains or loses focus.
	 *
	 * @param focus
	 *            true if the host component has focus
	 */
	public void setFocus(final boolean focus) {
		caretVisible = true;
		repaintCaret();
	}

	@Override
	public void setLayoutWidth(int width) {
		width = Math.max(width, MIN_LAYOUT_WIDTH);
		if (document != null && width != getLayoutWidth()) {
			// this.layoutWidth is set by relayoutAll
			relayoutAll(width, styleSheet);
		} else {
			// maybe doc is null. Let's store layoutWidth so it's right
			// when we set a doc
			layoutWidth = width;
		}
	}

	@Override
	public void setStyleSheet(final StyleSheet styleSheet) {
		if (document != null) {
			relayoutAll(layoutWidth, styleSheet);
		}
	}

	public void setStyleSheet(final URL ssUrl) throws IOException {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(ssUrl);
		this.setStyleSheet(ss);
	}

	@Override
	public void setWhitespacePolicy(final IWhitespacePolicy whitespacePolicy) {
		if (whitespacePolicy == null) {
			this.whitespacePolicy = DEFAULT_POLICY;
		} else {
			this.whitespacePolicy = whitespacePolicy;
		}
	}

	@Override
	public IWhitespacePolicy getWhitespacePolicy() {
		return whitespacePolicy;
	}

	@Override
	public boolean canSplit() {
		if (readOnly) {
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
	public void split() throws DocumentValidationException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot split, because the editor is read-only.");
		}

		if (!Filters.elements().matches(currentNode)) {
			throw new DocumentValidationException("Can only split elements.");
		}
		final IElement element = (IElement) currentNode;

		final long start = System.currentTimeMillis();
		boolean success = false;
		try {
			beginWork();

			if (hasSelection()) {
				deleteSelection();
			}

			final IDocumentFragment splittedFragment;
			final boolean splitAtEnd = getCaretOffset() == element.getEndOffset();
			if (!splitAtEnd) {
				this.moveTo(element.getEndPosition(), true);
				splittedFragment = getSelectedFragment();
				deleteSelection();
			} else {
				splittedFragment = null;
			}

			// either way, we are now at the end offset for the element, let's move just outside
			this.moveBy(1);

			insertElement(element.getQualifiedName());
			// TODO: clone attributes

			if (splittedFragment != null) {
				final ContentPosition finalPosition = getCaretPosition();
				insertFragment(splittedFragment);
				this.moveTo(finalPosition, false);
			}
			success = true;
		} finally {
			endWork(success);
		}

		if (isDebugging()) {
			final long end = System.currentTimeMillis();
			System.out.println("split() took " + (end - start) + "ms");
		}
	}

	/**
	 * Toggles the caret to produce a flashing caret effect. This method should be called from the GUI event thread at
	 * regular intervals.
	 */
	public void toggleCaret() {
		caretVisible = !caretVisible;
		repaintCaret();
	}

	@Override
	public void undo() throws CannotUndoException, ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot undo, because the editor is read-only.");
		}

		if (undoList.isEmpty()) {
			throw new CannotUndoException();
		}
		final UndoableAndOffset event = undoList.removeLast();
		event.edit.undo();
		this.moveTo(new ContentPosition(document, event.caretOffset), false);
		redoList.add(event);
	}

	@Override
	public ContentPosition viewToModel(final int x, final int y) {
		final Graphics g = hostComponent.createDefaultGraphics();
		final LayoutContext context = createLayoutContext(g);
		final ContentPosition position = rootBox.viewToModel(context, x, y);
		g.dispose();
		return position;
	}

	@Override
	public void declareNamespace(final String namespacePrefix, final String namespaceURI) throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException(MessageFormat.format("Cannot declare namespace {0}, because the editor is read-only.", namespacePrefix));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			// TODO throw IllegalStateException("Not in element");
			return;
		}
		final String currentNamespaceURI = element.getNamespaceURI(namespacePrefix);
		applyEdit(new ChangeNamespaceEdit(document, getCaretOffset(), namespacePrefix, currentNamespaceURI, namespaceURI), getCaretOffset());
	}

	@Override
	public void removeNamespace(final String namespacePrefix) throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException(MessageFormat.format("Cannot remove namespace {0}, because the editor is read-only.", namespacePrefix));
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			// TODO throw IllegalStateException("Not in element");
			return;
		}
		final String currentNamespaceURI = element.getNamespaceURI(namespacePrefix);
		applyEdit(new ChangeNamespaceEdit(document, getCaretOffset(), namespacePrefix, currentNamespaceURI, null), getCaretOffset());
	}

	@Override
	public void declareDefaultNamespace(final String namespaceURI) throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot declare default namespace, because the editor is read-only.");
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			// TODO throw IllegalStateException("Not in element");
			return;
		}
		final String currentNamespaceURI = element.getDefaultNamespaceURI();
		applyEdit(new ChangeNamespaceEdit(document, getCaretOffset(), null, currentNamespaceURI, namespaceURI), getCaretOffset());
	}

	@Override
	public void removeDefaultNamespace() throws ReadOnlyException {
		if (readOnly) {
			throw new ReadOnlyException("Cannot remove default namespace, because the editor is read-only.");
		}

		final IElement element = getCurrentElement();
		if (element == null) {
			// TODO throw IllegalStateException("Not in element");
			return;
		}
		final String currentNamespaceURI = element.getDefaultNamespaceURI();
		applyEdit(new ChangeNamespaceEdit(document, getCaretOffset(), null, currentNamespaceURI, null), getCaretOffset());
	}

	// ================================================== PRIVATE

	/**
	 * Captures an UndoableAction and the offset at which it occurred.
	 */
	private static class UndoableAndOffset {
		public IUndoableEdit edit;
		public int caretOffset;

		public UndoableAndOffset(final IUndoableEdit edit, final int caretOffset) {
			this.edit = edit;
			this.caretOffset = caretOffset;
		}
	}

	private <T extends IUndoableEdit> T applyEdit(final T edit, final int caretOffset) {
		addEdit(edit, caretOffset);
		edit.redo();
		return edit;
	}

	/**
	 * Processes the given edit, adding it to the undo stack.
	 *
	 * @param edit
	 *            The edit to process.
	 * @param caretOffset
	 *            Offset of the caret before the edit occurred. If the edit is undone, the caret is returned to this
	 *            offset.
	 */
	private void addEdit(final IUndoableEdit edit, final int caretOffset) {

		if (edit == null) {
			return;
		}

		if (compoundEdit != null) {
			compoundEdit.addEdit(edit);
		} else if (!undoList.isEmpty() && undoList.getLast().edit.combine(edit)) {
			return;
		} else {
			undoList.add(new UndoableAndOffset(edit, caretOffset));
			if (undoList.size() > MAX_UNDO_STACK_SIZE) {
				undoList.removeFirst();
			}
			redoList.clear();
		}
	}

	/**
	 * Creates a layout context given a particular graphics context.
	 *
	 * @param g
	 *            The graphics context to use for the layout context.
	 * @return the new layout context
	 */
	private LayoutContext createLayoutContext(final Graphics g) {
		final LayoutContext context = new LayoutContext();
		context.setBoxFactory(boxFactory);
		context.setDocument(document);
		context.setGraphics(g);
		context.setStyleSheet(styleSheet);
		context.setWhitespacePolicy(whitespacePolicy);

		if (hasSelection()) {
			context.setSelectionStart(getSelectionStart().getOffset());
			context.setSelectionEnd(getSelectionEnd().getOffset());
		} else {
			context.setSelectionStart(getCaretOffset());
			context.setSelectionEnd(getCaretOffset());
		}

		return context;
	}

	private void createRootBox() {
		final Graphics g = hostComponent.createDefaultGraphics();
		final LayoutContext context = createLayoutContext(g);
		rootBox = new RootBox(context, document, getLayoutWidth());
		g.dispose();
	}

	/**
	 * Invalidates the box tree due to document changes. The lowest box that completely encloses the changed node is
	 * invalidated.
	 *
	 * @param node
	 *            Node for which to search.
	 */
	private void invalidateElementBox(final INode node) {

		final BlockBox elementBox = (BlockBox) this.findInnermostBox(new IBoxFilter() {
			@Override
			public boolean matches(final Box box) {
				return box instanceof BlockBox && !box.isAnonymous() && box.getStartOffset() <= node.getStartOffset() + 1 && box.getEndOffset() >= node.getEndOffset();
			}
		});

		if (elementBox != null) {
			elementBox.invalidate(true);
		}
	}

	/**
	 * Returns true if the given offset represents the boundary between two different elements with the same name and
	 * parent. This is used to determine if the elements can be joined via joinElementsAt.
	 *
	 * @param int
	 *            offset The offset to check.
	 */
	private boolean isBetweenMatchingElements(final int offset) {
		if (offset <= 1 || offset >= document.getLength() - 1) {
			return false;
		}
		final IElement e1 = document.getElementForInsertionAt(offset - 1);
		final IElement e2 = document.getElementForInsertionAt(offset + 1);
		return e1 != e2 && e1 != null && e2 != null && e1.getParent() == e2.getParent() && e1.isKindOf(e2);
	}

	/**
	 * Calls layout() on the rootBox until the y-coordinate of a caret at the given offset converges, i.e. is less than
	 * LAYOUT_TOLERANCE pixels from the last call.
	 *
	 * @param offset
	 *            Offset around which we should lay out boxes.
	 */
	private void iterateLayout(final ContentPosition position) {

		VerticalRange repaintRange = null;
		final Graphics g = hostComponent.createDefaultGraphics();
		final LayoutContext context = createLayoutContext(g);
		int layoutY = rootBox.getCaret(context, caretPosition).getY();

		while (true) {
			final int oldLayoutY = layoutY;
			final VerticalRange layoutRange = rootBox.layout(context, layoutY - LAYOUT_WINDOW / 2, layoutY + LAYOUT_WINDOW / 2);
			if (layoutRange != null) {
				if (repaintRange == null) {
					repaintRange = layoutRange;
				} else {
					repaintRange = repaintRange.union(layoutRange);
				}
			}

			layoutY = rootBox.getCaret(context, position).getY();
			if (Math.abs(layoutY - oldLayoutY) < LAYOUT_TOLERANCE) {
				break;
			}
		}
		g.dispose();

		if (repaintRange == null || repaintRange.isEmpty()) {
			return;
		}

		final Rectangle viewport = hostComponent.getViewport();
		final VerticalRange viewportRange = new VerticalRange(viewport.getY(), viewport.getY() + viewport.getHeight());
		if (repaintRange.intersects(viewportRange)) {
			final VerticalRange intersection = repaintRange.intersection(viewportRange);
			hostComponent.repaint(viewport.getX(), intersection.getTop(), viewport.getWidth(), intersection.getHeight());
		}
	}

	@Override
	public boolean canJoin() {
		if (!hasSelection()) {
			return false;
		}

		final IElement parent = document.getElementForInsertionAt(getCaretOffset());
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
		if (!hasSelection()) {
			return;
		}

		final IElement parent = document.getElementForInsertionAt(getCaretOffset());
		final IAxis<? extends INode> selectedNodes = parent.children().in(getSelectedRange());
		if (selectedNodes.isEmpty()) {
			return;
		}

		final INode firstNode = selectedNodes.first();
		final ContentPosition selectionEnd = getSelectionEnd();

		boolean success = false;
		try {
			beginWork();

			final ArrayList<IDocumentFragment> contentToJoin = new ArrayList<IDocumentFragment>();
			int count = 0;
			for (final INode selectedNode : selectedNodes) {
				if (!selectedNode.isKindOf(firstNode) && count > 0) {
					throw new DocumentValidationException("Cannot join nodes of different kind.");
				}
				if (!selectedNode.isEmpty()) {
					contentToJoin.add(document.getFragment(selectedNode.getRange().resizeBy(1, -1)));
				}
				count++;
			}

			if (count <= 1) {
				return;
			}

			moveTo(firstNode.getEndPosition().moveBy(1));
			moveTo(selectionEnd, true);
			deleteSelection();

			moveTo(firstNode.getStartPosition().moveBy(1));
			moveTo(firstNode.getEndPosition(), true);
			deleteSelection();

			for (final IDocumentFragment preservedContent : contentToJoin) {
				moveTo(firstNode.getEndPosition());
				insertFragment(preservedContent);
			}

			success = true;
		} finally {
			endWork(success);
		}

	}

	private void joinElementsAt(final ContentPosition position) throws DocumentValidationException {
		boolean success = false;
		try {
			beginWork();

			// get the second element
			moveTo(position.moveBy(1));
			final IElement secondElement = getCurrentElement();

			// preserve the second element's content
			final boolean shouldMoveContent = !secondElement.isEmpty();
			final IDocumentFragment preservedContent;
			if (shouldMoveContent) {
				moveTo(secondElement.getEndPosition(), true);
				preservedContent = getSelectedFragment();
				deleteSelection();
			} else {
				preservedContent = null;
			}

			// delete the empty element
			moveBy(1);
			moveBy(-2, true);
			deleteSelection();

			// insert the preserved content into the first element
			moveBy(-1);
			if (shouldMoveContent) {
				final ContentPosition savedPosition = getCaretPosition();
				insertFragment(preservedContent);
				moveTo(savedPosition, false);
			}
			success = true;
		} finally {
			endWork(success);
		}
	}

	/**
	 * Lay out the area around the caret.
	 */
	private void relayout() {
		if (isInWorkBlock()) {
			return;
		}

		final long start = System.currentTimeMillis();

		final int oldHeight = rootBox.getHeight();

		iterateLayout(getCaretPosition());

		if (rootBox.getHeight() != oldHeight) {
			hostComponent.setPreferredSize(rootBox.getWidth(), rootBox.getHeight());
		}

		final Graphics g = hostComponent.createDefaultGraphics();
		final LayoutContext context = createLayoutContext(g);
		caret = rootBox.getCaret(context, getCaretPosition());
		g.dispose();

		if (isDebugging()) {
			final long end = System.currentTimeMillis();
			System.out.println("VexWidget layout took " + (end - start) + "ms");
		}
	}

	/**
	 * Re-layout the entire widget, due to either a layout width change or a stylesheet range. This method does the
	 * actual setting of the width and stylesheet, since it needs to know where the caret is <i>before</i> the change,
	 * so that it can do a reasonable job of restoring the position of the viewport after the change.
	 *
	 * @param newWidth
	 *            New width for the widget.
	 * @param newStyleSheet
	 *            New stylesheet for the widget.
	 */
	private void relayoutAll(final int newWidth, final StyleSheet newStyleSheet) {

		final Graphics g = hostComponent.createDefaultGraphics();
		LayoutContext context = createLayoutContext(g);

		final Rectangle viewport = hostComponent.getViewport();

		// true if the caret is within the viewport
		//
		// TODO: incorrect if caret near the bottom and the viewport is
		// shrinking
		// To fix, we probably need to save the viewport height, just like
		// we now store viewport width (as layout width).
		final boolean caretVisible = viewport.intersects(caret.getBounds());

		// distance from the top of the viewport to the top of the caret
		// use this if the caret is visible in the viewport
		int relCaretY = 0;

		// position around which we are laying out
		// this is also where we put the top of the viewport if the caret
		// isn't visible
		ContentPosition position;

		if (caretVisible) {
			relCaretY = caret.getY() - viewport.getY();
			position = getCaretPosition();
		} else {
			position = rootBox.viewToModel(context, 0, viewport.getY());
		}

		layoutWidth = newWidth;
		styleSheet = newStyleSheet;

		// Re-create the context, since it holds the old stylesheet
		context = createLayoutContext(g);

		createRootBox();

		iterateLayout(position);

		hostComponent.setPreferredSize(rootBox.getWidth(), rootBox.getHeight());

		caret = rootBox.getCaret(context, getCaretPosition());

		if (caretVisible) {
			int viewportY = caret.getY() - Math.min(relCaretY, viewport.getHeight());
			viewportY = Math.min(rootBox.getHeight() - viewport.getHeight(), viewportY);
			viewportY = Math.max(0, viewportY); // this must appear after the
			// above line, since
			// that line might set viewportY negative
			hostComponent.scrollTo(viewport.getX(), viewportY);
			scrollCaretVisible();
		} else {
			final int viewportY = rootBox.getCaret(context, position).getY();
			hostComponent.scrollTo(viewport.getX(), viewportY);
		}

		hostComponent.repaint();

		g.dispose();

	}

	/**
	 * Repaints the area of the caret.
	 */
	private void repaintCaret() {
		if (caret != null) {
			// caret may be null when document is first set
			final Rectangle bounds = caret.getBounds();
			hostComponent.repaint(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		}
	}

	/**
	 * Repaints area of the control corresponding to a range of offsets in the document.
	 */
	private void repaintSelectedRange() {
		if (isInWorkBlock()) {
			return;
		}

		final Graphics g = hostComponent.createDefaultGraphics();

		final LayoutContext context = createLayoutContext(g);

		final Rectangle startBounds = rootBox.getCaret(context, getSelectionStart()).getBounds();
		final int top1 = startBounds.getY();
		final int bottom1 = top1 + startBounds.getHeight();

		final Rectangle endBounds = rootBox.getCaret(context, getSelectionEnd()).getBounds();
		final int top2 = endBounds.getY();
		final int bottom2 = top2 + endBounds.getHeight();

		final int top = Math.min(top1, top2);
		final int bottom = Math.max(bottom1, bottom2);
		if (top == bottom) {
			// Account for zero-height horizontal carets
			hostComponent.repaint(0, top - 1, getLayoutWidth(), bottom - top + 1);
		} else {
			hostComponent.repaint(0, top, getLayoutWidth(), bottom - top);
		}

		g.dispose();
	}

	private void scrollCaretVisible() {

		final Rectangle caretBounds = caret.getBounds();
		final Rectangle viewport = hostComponent.getViewport();

		final int x = viewport.getX();
		int y = 0;
		final int offset = getCaretOffset();
		if (offset == 1) {
			y = 0;
		} else if (offset == document.getLength() - 1) {
			if (rootBox.getHeight() < viewport.getHeight()) {
				y = 0;
			} else {
				y = rootBox.getHeight() - viewport.getHeight() + caret.getBounds().getHeight();
			}
		} else if (caretBounds.getY() < viewport.getY()) {
			y = caretBounds.getY();
		} else if (caretBounds.getY() + caretBounds.getHeight() > viewport.getY() + viewport.getHeight()) {
			y = caretBounds.getY() + caretBounds.getHeight() - viewport.getHeight();
		} else {
			// no scrolling required
			return;
		}
		hostComponent.scrollTo(x, y);
	}

}
