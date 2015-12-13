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
import org.eclipse.vex.core.internal.visualization.IBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.BalancingSelector;
import org.eclipse.vex.core.internal.widget.BoxView;
import org.eclipse.vex.core.internal.widget.DOMController;
import org.eclipse.vex.core.internal.widget.IRenderer;
import org.eclipse.vex.core.internal.widget.IViewPort;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas implements ISelectionProvider {

	private final org.eclipse.swt.graphics.Cursor mouseCursor;
	private final BoxView view;
	private final Cursor cursor;
	private final BalancingSelector selector;
	private final DOMController controller;

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

		final IRenderer renderer = new DoubleBufferedRenderer(this);
		final IViewPort viewPort = new ViewPort();
		selector = new BalancingSelector();
		cursor = new Cursor(selector);
		connectCursor();

		view = new BoxView(renderer, viewPort, cursor);
		controller = new DOMController(cursor, view);
	}

	public void setContent(final IDocument document) {
		controller.setDocument(document);
		selector.setDocument(document);
	}

	public void setBoxModelBuilder(final IBoxModelBuilder visualizationChain) {
		controller.setBoxModelBuilder(visualizationChain);
	}

	public DOMController getDOMController() {
		return controller;
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
			public void positionChanged(final int offset) {
				cursorPositionChanged(offset);
			}
		});
	}

	private void widgetDisposed() {
		view.dispose();
		mouseCursor.dispose();
	}

	private void resize(final ControlEvent event) {
		view.invalidateWidth(getClientArea().width);
	}

	private void scrollVertically(final SelectionEvent event) {
		view.invalidateViewport();
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
		default:
			if (event.character > 0 && Character.isDefined(event.character)) {
				controller.enterChar(event.character);
			}
			break;
		}
	}

	private void moveOrSelect(final int stateMask, final ICursorMove move) {
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
			controller.moveSelection(move);
		} else {
			controller.moveCursor(move);
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
		controller.moveSelection(toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void cursorPositionChanged(final int offset) {
		fireSelectionChanged(createSelectionForOffset(offset));
	}

	@Override
	public IVexSelection getSelection() {
		return createSelectionForOffset(cursor.getOffset());
	}

	@Override
	public void setSelection(final ISelection selection) {
		Assert.isLegal(selection instanceof IVexSelection, "BoxWidget can only handle instances of IVexSelection");
		final IVexSelection vexSelection = (IVexSelection) selection;

		controller.moveCursor(toOffset(vexSelection.getCaretOffset()));
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
