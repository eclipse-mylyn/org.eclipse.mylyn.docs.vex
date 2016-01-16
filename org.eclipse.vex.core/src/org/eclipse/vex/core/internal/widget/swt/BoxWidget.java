/*******************************************************************************
 * Copyright (c) 2014, 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import static org.eclipse.vex.core.internal.cursor.CursorMoves.toAbsoluteCoordinates;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.visualization.IBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.BalancingSelector;
import org.eclipse.vex.core.internal.widget.DocumentEditor;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.internal.widget.ITableModel;
import org.eclipse.vex.core.internal.widget.IViewPort;
import org.eclipse.vex.core.internal.widget.ReadOnlyException;
import org.eclipse.vex.core.internal.widget.VisualizationController;
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

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas implements ISelectionProvider, IDocumentEditor {

	private static final char CHAR_NONE = 0;
	private static final Map<KeyStroke, IVexWidgetHandler> KEY_MAP = buildKeyMap();

	private IDocument document;

	private final org.eclipse.swt.graphics.Cursor mouseCursor;

	private final Cursor cursor;
	private final BalancingSelector selector;
	private final VisualizationController controller;
	private final SwtClipboard clipboard;
	private final DocumentEditor editor;

	private final ListenerList selectionChangedListeners = new ListenerList();

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

		final ViewPort viewPort = new ViewPort();

		selector = new BalancingSelector();
		cursor = new Cursor(selector, viewPort);
		connectCursor();

		controller = new VisualizationController(new DoubleBufferedRenderer(this), viewPort, cursor);
		clipboard = new SwtClipboard(parent.getDisplay());
		editor = new DocumentEditor(cursor, IWhitespacePolicy.NULL, clipboard);
	}

	public void setDocument(final IDocument document) {
		this.document = document;
		selector.setDocument(document);
		controller.setDocument(document);
		editor.setDocument(document);
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
			public void mouseDoubleClick(final MouseEvent e) {
				BoxWidget.this.mouseDoubleClick(e);
			}

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
		clipboard.dispose();
	}

	private void resize(final ControlEvent event) {
		controller.resize(getClientArea().width);
	}

	private void scrollVertically(final SelectionEvent event) {
		controller.refreshViewport();
	}

	private void keyPressed(final KeyEvent event) {
		final KeyStroke keyStroke = new KeyStroke(event);
		final IVexWidgetHandler handler = KEY_MAP.get(keyStroke);
		if (handler != null) {
			try {
				handler.execute(new ExecutionEvent(null, Collections.emptyMap(), event, null), BoxWidget.this);
			} catch (final ReadOnlyException e) {
				// TODO give feedback: the editor is read-only
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		} else if (!Character.isISOControl(event.character)) {
			try {
				insertChar(event.character);
			} catch (final DocumentValidationException e) {
				// TODO give feedback: at this document position no character can be entered
			} catch (final ReadOnlyException e) {
				// TODO give feedback: the editor is read-only
			}
		}
	}

	private void moveOrSelect(final int stateMask, final ICursorMove move) {
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
			cursor.select(move);
		} else {
			cursor.move(move);
		}
	}

	private void mouseDoubleClick(final MouseEvent event) {
		if (event.button == 1) {
			selectWord();
		}
	}

	private void mouseDown(final MouseEvent event) {
		if (event.button == 1) {
			final int absoluteY = event.y + getVerticalBar().getSelection();
			moveOrSelect(event.stateMask, toAbsoluteCoordinates(event.x, absoluteY));
		}
	}

	private void mouseMove(final MouseEvent event) {
		if ((event.stateMask & SWT.BUTTON_MASK) != SWT.BUTTON1) {
			return;
		}
		final int absoluteY = event.y + getVerticalBar().getSelection();
		cursor.select(toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void cursorPositionChanged(final int offset) {
		fireSelectionChanged(createSelectionForCursor(cursor));
	}

	public void refresh() {
		controller.refreshAll();
	}

	public Rectangle getCaretArea() {
		return cursor.getCaretArea();
	}

	/*
	 * ISelectionProvider
	 */

	@Override
	public IVexSelection getSelection() {
		return createSelectionForCursor(cursor);
	}

	@Override
	public void setSelection(final ISelection selection) {
		Assert.isLegal(selection instanceof IVexSelection, "BoxWidget can only handle instances of IVexSelection");
		final IVexSelection vexSelection = (IVexSelection) selection;

		// TODO use DocumentEditor
		if (vexSelection.isEmpty()) {
			cursor.move(toOffset(vexSelection.getCaretOffset()));
		} else {
			final ContentRange selectedRange = vexSelection.getSelectedRange();
			cursor.move(toOffset(selectedRange.getStartOffset()));
			cursor.select(toOffset(selectedRange.getEndOffset()));
		}
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

	private static IVexSelection createSelectionForCursor(final ICursor cursor) {
		return new IVexSelection() {
			@Override
			public boolean isEmpty() {
				return cursor.hasSelection();
			}

			@Override
			public int getCaretOffset() {
				return cursor.getOffset();
			}

			@Override
			public ContentRange getSelectedRange() {
				return cursor.getSelectedRange();
			}
		};
	}

	/*
	 * IDocumentEditor
	 */

	@Override
	public IWhitespacePolicy getWhitespacePolicy() {
		return editor.getWhitespacePolicy();
	}

	@Override
	public void setWhitespacePolicy(final IWhitespacePolicy policy) {
		editor.setWhitespacePolicy(policy);
	}

	@Override
	public ITableModel getTableModel() {
		return editor.getTableModel();
	}

	@Override
	public void setTableModel(final ITableModel tableModel) {
		editor.setTableModel(tableModel);
	}

	public boolean isReadOnly() {
		return editor.isReadOnly();
	}

	public void setReadOnly(final boolean readOnly) {
		editor.setReadOnly(readOnly);
	}

	public boolean canRedo() {
		return editor.canRedo();
	}

	public void redo() throws CannotApplyException {
		editor.redo();
	}

	public boolean canUndo() {
		return editor.canUndo();
	}

	public void undo() throws CannotUndoException {
		editor.undo();
	}

	public void doWork(final Runnable runnable) throws CannotApplyException {
		editor.doWork(runnable);
	}

	public void doWork(final Runnable runnable, final boolean savePosition) throws CannotApplyException {
		editor.doWork(runnable, savePosition);
	}

	public void savePosition(final Runnable runnable) {
		editor.savePosition(runnable);
	}

	public void cutSelection() {
		editor.cutSelection();
	}

	public void copySelection() {
		editor.copySelection();
	}

	public boolean canPaste() {
		return editor.canPaste();
	}

	public void paste() throws DocumentValidationException {
		editor.paste();
	}

	public boolean canPasteText() {
		return editor.canPasteText();
	}

	public void pasteText() throws DocumentValidationException {
		editor.pasteText();
	}

	public ContentPosition getCaretPosition() {
		return editor.getCaretPosition();
	}

	public IElement getCurrentElement() {
		return editor.getCurrentElement();
	}

	public INode getCurrentNode() {
		return editor.getCurrentNode();
	}

	@Override
	public String toString() {
		return editor.toString();
	}

	public boolean hasSelection() {
		return editor.hasSelection();
	}

	public ContentRange getSelectedRange() {
		return editor.getSelectedRange();
	}

	public ContentPositionRange getSelectedPositionRange() {
		return editor.getSelectedPositionRange();
	}

	public IDocumentFragment getSelectedFragment() {
		return editor.getSelectedFragment();
	}

	public String getSelectedText() {
		return editor.getSelectedText();
	}

	public void selectAll() {
		editor.selectAll();
	}

	public void selectWord() {
		editor.selectWord();
	}

	public void selectContentOf(final INode node) {
		editor.selectContentOf(node);
	}

	public void select(final INode node) {
		editor.select(node);
	}

	public boolean canDeleteSelection() {
		return editor.canDeleteSelection();
	}

	public void deleteSelection() {
		editor.deleteSelection();
	}

	public void moveBy(final int distance) {
		editor.moveBy(distance);
	}

	public void moveBy(final int distance, final boolean select) {
		editor.moveBy(distance, select);
	}

	public void moveTo(final ContentPosition position) {
		editor.moveTo(position);
	}

	public void moveTo(final ContentPosition position, final boolean select) {
		editor.moveTo(position, select);
	}

	public void moveToLineEnd(final boolean select) {
		editor.moveToLineEnd(select);
	}

	public void moveToLineStart(final boolean select) {
		editor.moveToLineStart(select);
	}

	public void moveToNextLine(final boolean select) {
		editor.moveToNextLine(select);
	}

	public void moveToNextPage(final boolean select) {
		editor.moveToNextPage(select);
	}

	public void moveToNextWord(final boolean select) {
		editor.moveToNextWord(select);
	}

	public void moveToPreviousLine(final boolean select) {
		editor.moveToPreviousLine(select);
	}

	public void moveToPreviousPage(final boolean select) {
		editor.moveToPreviousPage(select);
	}

	public void moveToPreviousWord(final boolean select) {
		editor.moveToPreviousWord(select);
	}

	public void declareNamespace(final String namespacePrefix, final String namespaceURI) {
		editor.declareNamespace(namespacePrefix, namespaceURI);
	}

	public void removeNamespace(final String namespacePrefix) {
		editor.removeNamespace(namespacePrefix);
	}

	public void declareDefaultNamespace(final String namespaceURI) {
		editor.declareDefaultNamespace(namespaceURI);
	}

	public void removeDefaultNamespace() {
		editor.removeDefaultNamespace();
	}

	public boolean canSetAttribute(final String attributeName, final String value) {
		return editor.canSetAttribute(attributeName, value);
	}

	public void setAttribute(final String attributeName, final String value) {
		editor.setAttribute(attributeName, value);
	}

	public boolean canRemoveAttribute(final String attributeName) {
		return editor.canRemoveAttribute(attributeName);
	}

	public void removeAttribute(final String attributeName) {
		editor.removeAttribute(attributeName);
	}

	public boolean canInsertText() {
		return editor.canInsertText();
	}

	public void insertChar(final char c) throws DocumentValidationException {
		editor.insertChar(c);
	}

	@Override
	public void insertLineBreak() throws DocumentValidationException {
		editor.insertLineBreak();
	}

	public void deleteForward() throws DocumentValidationException {
		editor.deleteForward();
	}

	public void deleteBackward() throws DocumentValidationException {
		editor.deleteBackward();
	}

	public void insertText(final String text) throws DocumentValidationException {
		editor.insertText(text);
	}

	public void insertXML(final String xml) throws DocumentValidationException {
		editor.insertXML(xml);
	}

	public ElementName[] getValidInsertElements() {
		return editor.getValidInsertElements();
	}

	public ElementName[] getValidMorphElements() {
		return editor.getValidMorphElements();
	}

	public boolean canInsertElement(final QualifiedName elementName) {
		return editor.canInsertElement(elementName);
	}

	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException {
		return editor.insertElement(elementName);
	}

	public boolean canInsertComment() {
		return editor.canInsertComment();
	}

	public IComment insertComment() throws DocumentValidationException {
		return editor.insertComment();
	}

	public boolean canInsertProcessingInstruction() {
		return editor.canInsertProcessingInstruction();
	}

	public IProcessingInstruction insertProcessingInstruction(final String target) throws CannotApplyException, ReadOnlyException {
		return editor.insertProcessingInstruction(target);
	}

	public void editProcessingInstruction(final String target, final String data) throws CannotApplyException, ReadOnlyException {
		editor.editProcessingInstruction(target, data);
	}

	public boolean canInsertFragment(final IDocumentFragment fragment) {
		return editor.canInsertFragment(fragment);
	}

	public void insertFragment(final IDocumentFragment fragment) throws DocumentValidationException {
		editor.insertFragment(fragment);
	}

	public boolean canUnwrap() {
		return editor.canUnwrap();
	}

	public void unwrap() throws DocumentValidationException {
		editor.unwrap();
	}

	public boolean canMorph(final QualifiedName elementName) {
		return editor.canMorph(elementName);
	}

	public void morph(final QualifiedName elementName) throws DocumentValidationException {
		editor.morph(elementName);
	}

	public boolean canJoin() {
		return editor.canJoin();
	}

	public void join() throws DocumentValidationException {
		editor.join();
	}

	public boolean canSplit() {
		return editor.canSplit();
	}

	public void split() throws DocumentValidationException {
		editor.split();
	}

	/*
	 * Key Map
	 */

	private static void addKey(final Map<KeyStroke, IVexWidgetHandler> keyMap, final char character, final int keyCode, final int stateMask, final IVexWidgetHandler action) {
		keyMap.put(new KeyStroke(character, keyCode, stateMask), action);
	}

	private static Map<KeyStroke, IVexWidgetHandler> buildKeyMap() {
		final Map<KeyStroke, IVexWidgetHandler> keyMap = new HashMap<KeyStroke, IVexWidgetHandler>();

		// arrows: (Shift) Up/Down, {-, Shift, Ctrl, Shift+Ctrl} + Left/Right
		addKey(keyMap, CHAR_NONE, SWT.ARROW_DOWN, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextLine(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_DOWN, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextLine(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_UP, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousLine(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_UP, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousLine(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_LEFT, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveBy(-1);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_LEFT, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveBy(-1, true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_LEFT, SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousWord(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_LEFT, SWT.SHIFT | SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousWord(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_RIGHT, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveBy(+1);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_RIGHT, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveBy(+1, true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_RIGHT, SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextWord(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.ARROW_RIGHT, SWT.SHIFT | SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextWord(true);
			}
		});

		// Delete/Backspace
		addKey(keyMap, SWT.BS, SWT.BS, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				try {
					editor.deleteBackward();
				} catch (final DocumentValidationException e) {
					throw new ExecutionException(e.getMessage(), e);
				}
			}
		});
		addKey(keyMap, SWT.DEL, SWT.DEL, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				try {
					editor.deleteForward();
				} catch (final DocumentValidationException e) {
					throw new ExecutionException(e.getMessage(), e);
				}
			}
		});

		// Tab
		addKey(keyMap, SWT.TAB, SWT.TAB, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				try {
					editor.insertChar('\t');
				} catch (final DocumentValidationException e) {
					throw new ExecutionException(e.getMessage(), e);
				}
			}
		});

		// {-, Shift, Ctrl, Shift+Ctrl} + Home/End
		addKey(keyMap, CHAR_NONE, SWT.END, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToLineEnd(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.END, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToLineEnd(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.END, SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1));
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.END, SWT.SHIFT | SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1));
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.HOME, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToLineStart(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.HOME, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToLineStart(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.HOME, SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveTo(editor.getDocument().getStartPosition().moveBy(1));
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.HOME, SWT.SHIFT | SWT.CONTROL, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveTo(editor.getDocument().getStartPosition().moveBy(1), true);
			}
		});

		// (Shift) Page Up/Down
		addKey(keyMap, CHAR_NONE, SWT.PAGE_DOWN, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextPage(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.PAGE_DOWN, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToNextPage(true);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.PAGE_UP, SWT.NONE, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousPage(false);
			}
		});
		addKey(keyMap, CHAR_NONE, SWT.PAGE_UP, SWT.SHIFT, new IVexWidgetHandler() {
			@Override
			public void execute(final ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
				editor.moveToPreviousPage(true);
			}
		});

		return keyMap;
	}

	/*
	 * Inner Classes
	 */

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
