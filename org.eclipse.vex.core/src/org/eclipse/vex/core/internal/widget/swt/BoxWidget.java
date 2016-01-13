/*******************************************************************************
 * Copyright (c) 2014, 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import static org.eclipse.vex.core.internal.cursor.CursorMoves.down;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.left;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.right;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toAbsoluteCoordinates;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.up;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.DeleteEdit;
import org.eclipse.vex.core.internal.undo.DeleteNextCharEdit;
import org.eclipse.vex.core.internal.undo.DeletePreviousCharEdit;
import org.eclipse.vex.core.internal.undo.EditStack;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;
import org.eclipse.vex.core.internal.undo.InsertCommentEdit;
import org.eclipse.vex.core.internal.undo.InsertElementEdit;
import org.eclipse.vex.core.internal.undo.InsertLineBreakEdit;
import org.eclipse.vex.core.internal.undo.InsertProcessingInstructionEdit;
import org.eclipse.vex.core.internal.undo.InsertTextEdit;
import org.eclipse.vex.core.internal.undo.JoinElementsAtOffsetEdit;
import org.eclipse.vex.core.internal.visualization.IBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.BalancingSelector;
import org.eclipse.vex.core.internal.widget.IViewPort;
import org.eclipse.vex.core.internal.widget.ReadOnlyException;
import org.eclipse.vex.core.internal.widget.VisualizationController;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas implements ISelectionProvider {

	private final org.eclipse.swt.graphics.Cursor mouseCursor;

	private IDocument document;

	private final Cursor cursor;
	private final BalancingSelector selector;
	private final VisualizationController controller;
	private final EditStack editStack;

	private final ListenerList selectionChangedListeners = new ListenerList();

	private INode currentNode;
	private ContentPosition caretPosition;

	public BoxWidget(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND);

		mouseCursor = new org.eclipse.swt.graphics.Cursor(parent.getDisplay(), SWT.CURSOR_IBEAM);
		setCursor(mouseCursor);

		connectDispose();
		connectResize();
		if ((style & SWT.V_SCROLL) == SWT.V_SCROLL) {
			connectScrollVertically();
		}
		connectKeyboard();
		connectMouse();

		selector = new BalancingSelector();
		cursor = new Cursor(selector);
		connectCursor();

		controller = new VisualizationController(new DoubleBufferedRenderer(this), new ViewPort(), cursor);

		editStack = new EditStack();
	}

	public void setDocument(final IDocument document) {
		this.document = document;
		controller.setDocument(document);
		selector.setDocument(document);
	}

	public IDocument getDocument() {
		return document;
	}

	public void setBoxModelBuilder(final IBoxModelBuilder boxModelBuilder) {
		controller.setBoxModelBuilder(boxModelBuilder);
	}

	private void connectDispose() {
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				BoxWidget.this.widgetDisposed();
			}
		});
	}

	private void connectResize() {
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				resize(e);
			}
		});
	}

	private void connectScrollVertically() {
		getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				scrollVertically(e);
			}
		});
	}

	private void connectKeyboard() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				BoxWidget.this.keyPressed(e);
			}
		});
	}

	private void connectMouse() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				BoxWidget.this.mouseDown(e);
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(final MouseEvent e) {
				BoxWidget.this.mouseMove(e);
			}
		});
	}

	private void connectCursor() {
		cursor.addPositionListener(new ICursorPositionListener() {
			@Override
			public void positionAboutToChange() {
				// ignore
			}

			@Override
			public void positionChanged(final int offset) {
				cursorPositionChanged(offset);
			}
		});
	}

	private void widgetDisposed() {
		controller.dispose();
		mouseCursor.dispose();
	}

	private void resize(final ControlEvent event) {
		controller.resize(getClientArea().width);
	}

	private void scrollVertically(final SelectionEvent event) {
		controller.refreshViewport();
	}

	private void keyPressed(final KeyEvent event) {
		switch (event.keyCode) {
		case SWT.ARROW_LEFT:
			moveOrSelect(event.stateMask, left());
			break;
		case SWT.ARROW_RIGHT:
			moveOrSelect(event.stateMask, right());
			break;
		case SWT.ARROW_UP:
			moveOrSelect(event.stateMask, up());
			break;
		case SWT.ARROW_DOWN:
			moveOrSelect(event.stateMask, down());
			break;
		case SWT.HOME:
			moveOrSelect(event.stateMask, toOffset(0));
			break;
		case SWT.CR:
			insertLineBreak();
			break;
		case SWT.DEL:
			deleteForward();
			break;
		case SWT.BS:
			deleteBackward();
			break;
		case 0x79:
			if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
				if (canRedo()) {
					redo();
				}
			} else {
				insertChar(event.character);
			}
			break;
		case 0x7A:
			if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
				if (canUndo()) {
					undo();
				}
			} else {
				insertChar(event.character);
			}
			break;
		default:
			if (event.character > 0 && Character.isDefined(event.character)) {
				insertChar(event.character);
			}
			break;
		}
	}

	private void moveOrSelect(final int stateMask, final ICursorMove move) {
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
			cursor.select(move);
		} else {
			cursor.move(move);
		}
	}

	private void mouseDown(final MouseEvent event) {
		final int absoluteY = event.y + getVerticalBar().getSelection();
		moveOrSelect(event.stateMask, toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void mouseMove(final MouseEvent event) {
		if ((event.stateMask & SWT.BUTTON_MASK) != SWT.BUTTON1) {
			return;
		}
		final int absoluteY = event.y + getVerticalBar().getSelection();
		cursor.select(toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void cursorPositionChanged(final int offset) {
		currentNode = document.getNodeForInsertionAt(cursor.getOffset());
		caretPosition = new ContentPosition(currentNode.getDocument(), cursor.getOffset());
		fireSelectionChanged(createSelectionForOffset(offset));
	}

	public ContentPosition getCaretPosition() {
		return caretPosition;
	}

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

	public INode getCurrentNode() {
		return currentNode;
	}

	@Override
	public IVexSelection getSelection() {
		return createSelectionForOffset(cursor.getOffset());
	}

	@Override
	public void setSelection(final ISelection selection) {
		Assert.isLegal(selection instanceof IVexSelection, "BoxWidget can only handle instances of IVexSelection");
		final IVexSelection vexSelection = (IVexSelection) selection;

		cursor.move(toOffset(vexSelection.getCaretOffset()));
	};

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);

	}

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	private void fireSelectionChanged(final IVexSelection selection) {
		for (final Object listener : selectionChangedListeners.getListeners()) {
			try {
				((ISelectionChangedListener) listener).selectionChanged(new SelectionChangedEvent(this, selection));
			} catch (final Throwable t) {
				t.printStackTrace();
				// TODO remove listener?
			}
		}
	}

	private IVexSelection createSelectionForOffset(final int offset) {
		return new IVexSelection() {
			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public int getCaretOffset() {
				return offset;
			}
		};
	}

	public void refresh() {
		controller.refreshAll();
	}

	public void undo() {
		final IUndoableEdit edit = editStack.undo();
		cursor.move(toOffset(edit.getOffsetBefore()));
	}

	public void redo() {
		final IUndoableEdit edit = editStack.redo();
		cursor.move(toOffset(edit.getOffsetAfter()));
	}

	public boolean canUndo() {
		return editStack.canUndo();
	}

	public boolean canRedo() {
		return editStack.canRedo();
	}

	public void insertChar(final char c) {
		final InsertTextEdit insertText = editStack.apply(new InsertTextEdit(document, cursor.getOffset(), Character.toString(c)));
		cursor.move(toOffset(insertText.getOffsetAfter()));
	}

	public void insertLineBreak() {
		final InsertLineBreakEdit insertLineBreak = editStack.apply(new InsertLineBreakEdit(document, cursor.getOffset()));
		cursor.move(toOffset(insertLineBreak.getOffsetAfter()));
	}

	public void deleteForward() {
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

	public void deleteBackward() {
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

	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException {
		final InsertElementEdit insertElement = editStack.apply(new InsertElementEdit(document, cursor.getOffset(), elementName));
		final IElement element = insertElement.getElement();
		cursor.move(toOffset(insertElement.getOffsetAfter()));
		return element;
	}

	public IComment insertComment() throws DocumentValidationException {
		final InsertCommentEdit insertComment = editStack.apply(new InsertCommentEdit(document, cursor.getOffset()));
		final IComment comment = insertComment.getComment();
		cursor.move(toOffset(insertComment.getOffsetAfter()));
		return comment;
	}

	public IProcessingInstruction insertProcessingInstruction(final String target) throws CannotApplyException, ReadOnlyException {
		final InsertProcessingInstructionEdit insertPI = editStack.apply(new InsertProcessingInstructionEdit(document, cursor.getOffset(), target));
		final IProcessingInstruction pi = insertPI.getProcessingInstruction();
		cursor.move(toOffset(insertPI.getOffsetAfter()));
		return pi;
	}

	private final class ViewPort implements IViewPort {
		@Override
		public void reconcile(final int maximumHeight) {
			final int pageSize = getClientArea().height;
			final int selection = getVerticalBar().getSelection();
			getVerticalBar().setValues(selection, 0, maximumHeight, pageSize, pageSize / 4, pageSize);
		}

		@Override
		public void moveRelative(final int delta) {
			if (delta == 0) {
				return;
			}

			final int selection = getVerticalBar().getSelection() + delta;
			getVerticalBar().setSelection(selection);
		}

		@Override
		public Rectangle getVisibleArea() {
			return new Rectangle(0, getVerticalBar().getSelection(), getSize().x, getSize().y);
		}
	}

}
