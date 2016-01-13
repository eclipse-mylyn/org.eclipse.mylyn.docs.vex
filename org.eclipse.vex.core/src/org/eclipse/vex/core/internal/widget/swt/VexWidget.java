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
 *     Carsten Hiesserich - changed fragment pasting to allow XML content
 *     Carsten Hiesserich - replaced dispose override by disposeListener
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.core.ElementName;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.widget.BaseVexWidget;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.internal.widget.IHostComponent;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.core.internal.widget.ReadOnlyException;
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
 * An implementation of the Vex widget based on SWT.
 */
public class VexWidget extends Canvas implements IVexWidget, ISelectionProvider {

	public VexWidget(final Composite parent, final int style) {
		super(parent, style);

		if (DisplayDevice.getCurrent() == null) {
			DisplayDevice.setCurrent(new SwtDisplayDevice());
		}

		impl = new BaseVexWidget(hostComponent);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		final ScrollBar vbar = getVerticalBar();
		if (vbar != null) {
			vbar.setIncrement(20);
			vbar.addSelectionListener(selectionListener);
		}

		addControlListener(controlListener);
		addFocusListener(focusListener);
		addKeyListener(keyListener);
		addMouseListener(mouseListener);
		addMouseMoveListener(mouseMoveListener);
		addPaintListener(painter);
		addDisposeListener(disposeListener);
	}

	public Object getInput() {
		return impl.getDocument();
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public void setSelection(final ISelection selection) {
		throw new RuntimeException("Unexpected call to setSelection");
	}

	@Override
	public void beginWork() {
		impl.beginWork();
	}

	@Override
	public boolean canInsertComment() {
		return impl.canInsertComment();
	}

	@Override
	public boolean canInsertProcessingInstruction() {
		return impl.canInsertProcessingInstruction();
	}

	@Override
	public boolean canPaste() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canPasteText() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canRedo() {
		return impl.canRedo();
	}

	@Override
	public boolean canUndo() {
		return impl.canUndo();
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final org.eclipse.swt.graphics.Rectangle r = getClientArea();
		int height = r.height;

		final ScrollBar vbar = getVerticalBar();
		if (vbar != null) {
			height = vbar.getMaximum();
		}
		return new Point(r.width, height);
	}

	@Override
	public void copySelection() {
		final Clipboard clipboard = new Clipboard(getDisplay());
		final String text = getSelectedText();
		if (text.isEmpty()) {
			// Some elements (like XInclude) may not contain textual content.
			final Object[] data = { getSelectedFragment() };
			final Transfer[] transfers = { DocumentFragmentTransfer.getInstance() };
			clipboard.setContents(data, transfers);
		} else {
			final Object[] data = { getSelectedFragment(), getSelectedText() };
			final Transfer[] transfers = { DocumentFragmentTransfer.getInstance(), TextTransfer.getInstance() };
			clipboard.setContents(data, transfers);
		}

	}

	@Override
	public void cutSelection() throws ReadOnlyException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot cut selection, because the editor is read-only.");
		}

		copySelection();
		deleteSelection();
	}

	@Override
	public void deleteForward() throws DocumentValidationException {
		impl.deleteForward();
	}

	@Override
	public void deleteBackward() throws DocumentValidationException {
		impl.deleteBackward();
	}

	@Override
	public boolean canDeleteSelection() {
		return impl.canDeleteSelection();
	}

	@Override
	public void deleteSelection() {
		impl.deleteSelection();
	}

	@Override
	public void doWork(final Runnable runnable) {
		impl.doWork(runnable);
	}

	@Override
	public void doWork(final Runnable runnable, final boolean savePosition) {
		impl.doWork(runnable, savePosition);
	}

	@Override
	public void endWork(final boolean success) {
		impl.endWork(success);
	}

	@Override
	public ContentPosition getCaretPosition() {
		return impl.getCaretPosition();
	}

	@Override
	public IElement getCurrentElement() {
		return impl.getCurrentElement();
	}

	@Override
	public INode getCurrentNode() {
		return impl.getCurrentNode();
	}

	@Override
	public IDocument getDocument() {
		return impl.getDocument();
	}

	@Override
	public int getLayoutWidth() {
		return impl.getLayoutWidth();
	}

	@Override
	public ContentRange getSelectedRange() {
		return impl.getSelectedRange();
	}

	@Override
	public ContentPositionRange getSelectedPositionRange() {
		return impl.getSelectedPositionRange();
	}

	@Override
	public IDocumentFragment getSelectedFragment() {
		return impl.getSelectedFragment();
	}

	@Override
	public String getSelectedText() {
		return impl.getSelectedText();
	}

	@Override
	public StyleSheet getStyleSheet() {
		return impl.getStyleSheet();
	}

	@Override
	public ElementName[] getValidInsertElements() {
		return impl.getValidInsertElements();
	}

	@Override
	public ElementName[] getValidMorphElements() {
		return impl.getValidMorphElements();
	}

	@Override
	public boolean hasSelection() {
		return impl.hasSelection();
	}

	@Override
	public void insertChar(final char c) throws DocumentValidationException {
		impl.insertChar(c);
	}

	@Override
	public boolean canInsertFragment(final IDocumentFragment fragment) {
		return impl.canInsertFragment(fragment);
	}

	@Override
	public void insertFragment(final IDocumentFragment fragment) throws DocumentValidationException {
		impl.insertFragment(fragment);
	}

	@Override
	public boolean canInsertElement(final QualifiedName elementName) {
		return impl.canInsertElement(elementName);
	}

	@Override
	public IElement insertElement(final QualifiedName elementName) throws DocumentValidationException {
		return impl.insertElement(elementName);
	}

	@Override
	public boolean canInsertText() {
		return impl.canInsertText();
	}

	@Override
	public void insertText(final String text) throws DocumentValidationException {
		impl.insertText(text);
	}

	@Override
	public void insertXML(final String xml) throws DocumentValidationException {
		impl.insertXML(xml);
	}

	@Override
	public IComment insertComment() throws DocumentValidationException {
		return impl.insertComment();
	}

	@Override
	public IProcessingInstruction insertProcessingInstruction(final String target) throws CannotRedoException, ReadOnlyException {
		return impl.insertProcessingInstruction(target);
	}

	@Override
	public void editProcessingInstruction(final String target, final String data) throws CannotRedoException, ReadOnlyException {
		impl.editProcessingInstruction(target, data);
	}

	@Override
	public boolean isDebugging() {
		return impl.isDebugging();
	}

	@Override
	public boolean canUnwrap() {
		return impl.canUnwrap();
	}

	@Override
	public void unwrap() throws DocumentValidationException {
		impl.unwrap();
	}

	@Override
	public boolean canMorph(final QualifiedName elementName) {
		return impl.canMorph(elementName);
	}

	@Override
	public void morph(final QualifiedName elementName) throws DocumentValidationException {
		impl.morph(elementName);
	}

	@Override
	public boolean canJoin() {
		return impl.canJoin();
	}

	@Override
	public void join() throws DocumentValidationException {
		impl.join();
	}

	@Override
	public void moveBy(final int distance) {
		impl.moveBy(distance);
	}

	@Override
	public void moveBy(final int distance, final boolean select) {
		impl.moveBy(distance, select);
	}

	@Override
	public void moveTo(final ContentPosition position) {
		impl.moveTo(position);
	}

	@Override
	public void moveTo(final ContentPosition position, final boolean select) {
		impl.moveTo(position, select);
	}

	@Override
	public void moveToLineEnd(final boolean select) {
		impl.moveToLineEnd(select);
	}

	@Override
	public void moveToLineStart(final boolean select) {
		impl.moveToLineStart(select);
	}

	@Override
	public void moveToNextLine(final boolean select) {
		impl.moveToNextLine(select);
	}

	@Override
	public void moveToNextPage(final boolean select) {
		impl.moveToNextPage(select);
	}

	@Override
	public void moveToNextWord(final boolean select) {
		impl.moveToNextWord(select);
	}

	@Override
	public void moveToPreviousLine(final boolean select) {
		impl.moveToPreviousLine(select);
	}

	@Override
	public void moveToPreviousPage(final boolean select) {
		impl.moveToPreviousPage(select);
	}

	@Override
	public void moveToPreviousWord(final boolean select) {
		impl.moveToPreviousWord(select);
	}

	@Override
	public void paste() throws DocumentValidationException, ReadOnlyException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot paste, because the editor is read-only.");
		}

		final Clipboard clipboard = new Clipboard(getDisplay());
		final IDocumentFragment fragment = (IDocumentFragment) clipboard.getContents(DocumentFragmentTransfer.getInstance());
		if (fragment != null) {
			insertXML(new XMLFragment(fragment).getXML());
		} else {
			pasteText();
		}
	}

	@Override
	public void pasteText() throws DocumentValidationException, ReadOnlyException {
		if (isReadOnly()) {
			throw new ReadOnlyException("Cannot paste text, because the editor is read-only.");
		}

		final Clipboard clipboard = new Clipboard(getDisplay());
		final String text = (String) clipboard.getContents(TextTransfer.getInstance());
		if (text != null) {
			insertText(text);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		impl.redo();
	}

	@Override
	public boolean canRemoveAttribute(final String attributeName) {
		return impl.canRemoveAttribute(attributeName);
	}

	@Override
	public void removeAttribute(final String attributeName) {
		impl.removeAttribute(attributeName);
	}

	@Override
	public void savePosition(final Runnable runnable) {
		impl.savePosition(runnable);
	}

	@Override
	public void selectAll() {
		impl.selectAll();
	}

	@Override
	public void selectWord() {
		impl.selectWord();
	}

	@Override
	public void selectContentOf(final INode node) {
		impl.selectContentOf(node);
	}

	@Override
	public void select(final INode node) {
		impl.select(node);
	}

	@Override
	public boolean canSetAttribute(final String attributeName, final String value) {
		return impl.canSetAttribute(attributeName, value);
	}

	@Override
	public void setAttribute(final String attributeName, final String value) {
		impl.setAttribute(attributeName, value);
	}

	@Override
	public void setDebugging(final boolean debugging) {
		impl.setDebugging(debugging);
	}

	@Override
	public boolean isReadOnly() {
		return impl.isReadOnly();
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		impl.setReadOnly(readOnly);
	}

	@Override
	public void setDocument(final IDocument doc, final StyleSheet styleSheet) {
		impl.setDocument(doc, styleSheet);
	}

	@Override
	public void setDocument(final IDocument document) {
		impl.setDocument(document);
	}

	@Override
	public void setLayoutWidth(final int width) {
		impl.setLayoutWidth(width);
	}

	@Override
	public void setStyleSheet(final StyleSheet styleSheet) {
		impl.setStyleSheet(styleSheet);
	}

	public void setStyleSheet(final URL ssUrl) throws IOException {
		impl.setStyleSheet(ssUrl);
	}

	@Override
	public void setWhitespacePolicy(final IWhitespacePolicy whitespacePolicy) {
		impl.setWhitespacePolicy(whitespacePolicy);
	}

	@Override
	public IWhitespacePolicy getWhitespacePolicy() {
		return impl.getWhitespacePolicy();
	}

	@Override
	public boolean canSplit() {
		return impl.canSplit();
	}

	@Override
	public void split() throws DocumentValidationException {
		impl.split();
	}

	@Override
	public void undo() throws CannotUndoException {
		impl.undo();
	}

	@Override
	public ContentPosition viewToModel(final int x, final int y) {
		return impl.viewToModel(x, y);
	}

	@Override
	public void declareNamespace(final String namespacePrefix, final String namespaceURI) {
		impl.declareNamespace(namespacePrefix, namespaceURI);
	}

	@Override
	public void removeNamespace(final String namespacePrefix) {
		impl.removeNamespace(namespacePrefix);
	}

	@Override
	public void declareDefaultNamespace(final String namespaceURI) {
		impl.declareDefaultNamespace(namespaceURI);
	}

	@Override
	public void removeDefaultNamespace() {
		impl.removeDefaultNamespace();
	}

	/**
	 * @return the location of the left bottom corner of the caret relative to the VexWidget
	 */
	public Point getLocationForContentAssist() {
		final Caret vexCaret = impl.getCaret();
		if (vexCaret == null) {
			return new Point(0, 0);
		}

		final Rectangle caretBounds = vexCaret.getBounds();
		return new Point(caretBounds.getX() + originX, caretBounds.getY() + originY + caretBounds.getHeight());
	}

	private static final char CHAR_NONE = 0;

	private static Map<KeyStroke, IVexWidgetHandler> keyMap = new HashMap<KeyStroke, IVexWidgetHandler>();

	static {
		buildKeyMap();
	}

	private final BaseVexWidget impl;

	// Fields controlling scrolling
	int originX = 0;
	int originY = 0;

	private final List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	private ISelection selection;

	private final Runnable caretTimerRunnable = new Runnable() {
		@Override
		public void run() {
			impl.toggleCaret();
		}
	};
	private final Timer caretTimer = new Timer(500, caretTimerRunnable);

	private final ControlListener controlListener = new ControlListener() {
		@Override
		public void controlMoved(final ControlEvent e) {
		}

		@Override
		public void controlResized(final ControlEvent e) {
			final org.eclipse.swt.graphics.Rectangle r = getClientArea();
			// There seems to be a bug in SWT (at least on Linux/GTK+)
			// When maximizing the editor, the width is first set to 1,
			// then to the correct width
			if (r.width == 1) {
				return;
			}
			impl.setLayoutWidth(r.width);

			final ScrollBar vbar = getVerticalBar();
			if (vbar != null) {
				vbar.setThumb(r.height);
				vbar.setPageIncrement(Math.round(r.height * 0.9f));
			}
		}
	};

	private final FocusListener focusListener = new FocusListener() {
		@Override
		public void focusGained(final FocusEvent e) {
			impl.setFocus(true);
			caretTimer.start();
		}

		@Override
		public void focusLost(final FocusEvent e) {
			impl.setFocus(false);
			caretTimer.stop();
		}
	};

	private final IHostComponent hostComponent = new IHostComponent() {

		@Override
		public Graphics createDefaultGraphics() {
			if (VexWidget.this.isDisposed()) {
				System.out.println("*** Woot! VexWidget is disposed!");
			}
			return new SwtGraphics(new GC(VexWidget.this));
		}

		@Override
		public void fireSelectionChanged() {

			if (hasSelection()) {
				final List<? extends INode> nodes = getDocument().getNodes(getSelectedRange());
				selection = new StructuredSelection(nodes);
			} else {
				selection = new StructuredSelection(getCurrentNode());
			}

			final SelectionChangedEvent e = new SelectionChangedEvent(VexWidget.this, selection);
			for (final ISelectionChangedListener listener : selectionListeners) {
				listener.selectionChanged(e);
			}
			caretTimer.reset();
		}

		@Override
		public Rectangle getViewport() {
			return new Rectangle(getClientArea().x - originX, getClientArea().y - originY, getClientArea().width, getClientArea().height);
		}

		@Override
		public void invokeLater(final Runnable runnable) {
			VexWidget.this.getDisplay().asyncExec(runnable);
		}

		@Override
		public void repaint() {
			if (!VexWidget.this.isDisposed()) {
				// We can sometimes get a repaint from the VexWidgetImpl's
				// caret timer thread after the Widget is disposed.
				VexWidget.this.redraw();
			}
		}

		@Override
		public void repaint(final int x, final int y, final int width, final int height) {
			VexWidget.this.redraw(x + originX, y + originY, width, height, true);
		}

		@Override
		public void scrollTo(final int left, final int top) {
			final ScrollBar vbar = getVerticalBar();
			if (vbar != null) {
				vbar.setSelection(top);
			}
			setOrigin(-left, -top);
		}

		@Override
		public void setPreferredSize(final int width, final int height) {
			final ScrollBar vbar = getVerticalBar();
			if (vbar != null) {
				vbar.setMaximum(height);
			}
		}

	};

	private static abstract class Action implements IVexWidgetHandler {

		@Override
		public void execute(final VexWidget widget) throws ExecutionException {
			runEx(widget);
		}

		public abstract void runEx(IDocumentEditor editor) throws ExecutionException;

	}

	private final KeyListener keyListener = new KeyAdapter() {

		@Override
		public void keyPressed(final KeyEvent event) {
			final KeyStroke keyStroke = new KeyStroke(event);
			final IVexWidgetHandler handler = keyMap.get(keyStroke);
			if (handler != null) {
				try {
					handler.execute(VexWidget.this);
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

	};

	private final MouseListener mouseListener = new MouseListener() {
		@Override
		public void mouseDoubleClick(final MouseEvent e) {
			if (e.button == 1) {
				selectWord();
			}
		}

		@Override
		public void mouseDown(final MouseEvent e) {
			if (e.button == 1) {
				final ContentPosition position = viewToModel(e.x - originX, e.y - originY);
				final boolean select = e.stateMask == SWT.SHIFT;
				moveTo(position, select);
			}
		}

		@Override
		public void mouseUp(final MouseEvent e) {
		}
	};

	private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {
		@Override
		public void mouseMove(final MouseEvent e) {
			if ((e.stateMask & SWT.BUTTON1) > 0) {
				final ContentPosition position = viewToModel(e.x - originX, e.y - originY);
				moveTo(position, true);
			}
		}
	};

	private final PaintListener painter = new PaintListener() {
		@Override
		public void paintControl(final PaintEvent e) {

			final SwtGraphics g = new SwtGraphics(e.gc);
			g.moveOrigin(originX, originY);

			Color bgColor = impl.getBackgroundColor();
			if (bgColor == null) {
				bgColor = new Color(255, 255, 255);
			}

			final ColorResource color = g.getColor(bgColor);
			final ColorResource oldColor = g.setColor(color);
			final Rectangle r = g.getClipBounds();
			g.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
			g.setColor(oldColor);

			impl.paint(g, 0, 0);
		}
	};

	private final SelectionListener selectionListener = new SelectionListener() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final ScrollBar vbar = getVerticalBar();
			if (vbar != null) {
				final int y = -vbar.getSelection();
				setOrigin(0, y);
			}
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
	};

	/**
	 * The DisposedEvent is Fired when this Widget is disposed. The event is used instead of re-implementing
	 * Wigdet#dispose, which does not work.
	 *
	 * @see org.eclipse.swt.widgets.Widget#dispose
	 */
	private final DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(final DisposeEvent e) {
			impl.dispose();
			caretTimer.stop();
		}
	};

	private static void addKey(final char character, final int keyCode, final int stateMask, final Action action) {
		keyMap.put(new KeyStroke(character, keyCode, stateMask), action);
	}

	private static void buildKeyMap() {

		// arrows: (Shift) Up/Down, {-, Shift, Ctrl, Shift+Ctrl} + Left/Right
		addKey(CHAR_NONE, SWT.ARROW_DOWN, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextLine(false);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_DOWN, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextLine(true);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_UP, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousLine(false);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_UP, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousLine(true);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_LEFT, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveBy(-1);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_LEFT, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveBy(-1, true);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_LEFT, SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousWord(false);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_LEFT, SWT.SHIFT | SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousWord(true);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_RIGHT, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveBy(+1);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_RIGHT, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveBy(+1, true);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_RIGHT, SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextWord(false);
			}
		});
		addKey(CHAR_NONE, SWT.ARROW_RIGHT, SWT.SHIFT | SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextWord(true);
			}
		});

		// Delete/Backspace
		addKey(SWT.BS, SWT.BS, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) throws ExecutionException {
				try {
					editor.deleteBackward();
				} catch (final DocumentValidationException e) {
					throw new ExecutionException(e.getMessage(), e);
				}
			}
		});
		addKey(SWT.DEL, SWT.DEL, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) throws ExecutionException {
				try {
					editor.deleteForward();
				} catch (final DocumentValidationException e) {
					throw new ExecutionException(e.getMessage(), e);
				}
			}
		});

		// {-, Shift, Ctrl, Shift+Ctrl} + Home/End
		addKey(CHAR_NONE, SWT.END, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToLineEnd(false);
			}
		});
		addKey(CHAR_NONE, SWT.END, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToLineEnd(true);
			}
		});
		addKey(CHAR_NONE, SWT.END, SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1));
			}
		});
		addKey(CHAR_NONE, SWT.END, SWT.SHIFT | SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1));
			}
		});
		addKey(CHAR_NONE, SWT.HOME, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToLineStart(false);
			}
		});
		addKey(CHAR_NONE, SWT.HOME, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToLineStart(true);
			}
		});
		addKey(CHAR_NONE, SWT.HOME, SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveTo(editor.getDocument().getStartPosition().moveBy(1));
			}
		});
		addKey(CHAR_NONE, SWT.HOME, SWT.SHIFT | SWT.CONTROL, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveTo(editor.getDocument().getStartPosition().moveBy(1), true);
			}
		});

		// (Shift) Page Up/Down
		addKey(CHAR_NONE, SWT.PAGE_DOWN, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextPage(false);
			}
		});
		addKey(CHAR_NONE, SWT.PAGE_DOWN, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToNextPage(true);
			}
		});
		addKey(CHAR_NONE, SWT.PAGE_UP, SWT.NONE, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousPage(false);
			}
		});
		addKey(CHAR_NONE, SWT.PAGE_UP, SWT.SHIFT, new Action() {
			@Override
			public void runEx(final IDocumentEditor editor) {
				editor.moveToPreviousPage(true);
			}
		});
	}

	/**
	 * Scrolls to the given position in the widget.
	 *
	 * @param x
	 *            x-coordinate of the position to which to scroll
	 * @param y
	 *            y-coordinate of the position to which to scroll
	 */
	private void setOrigin(final int x, final int y) {
		final int destX = x - originX;
		final int destY = y - originY;
		final org.eclipse.swt.graphics.Rectangle ca = getClientArea();
		scroll(destX, destY, 0, 0, ca.width, ca.height, false);
		originX = x;
		originY = y;
	}

}
