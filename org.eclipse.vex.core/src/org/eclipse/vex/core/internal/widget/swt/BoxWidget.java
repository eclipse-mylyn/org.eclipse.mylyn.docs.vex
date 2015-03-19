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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.cursor.ContentMap;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.internal.widget.swt.DoubleBufferedRenderer.IRenderStep;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas {

	private IDocument document;
	private VisualizationChain visualizationChain;
	private RootBox rootBox;

	private final ContentMap contentMap;
	private final Cursor cursor;
	private final DoubleBufferedRenderer renderer;

	public BoxWidget(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		renderer = new DoubleBufferedRenderer(this);
		connectDispose();
		connectResize();
		if ((style & SWT.V_SCROLL) == SWT.V_SCROLL) {
			connectScrollVertically();
		}
		connectKeyboard();
		connectMouse();

		visualizationChain = new VisualizationChain();
		rootBox = new RootBox();
		contentMap = new ContentMap();
		contentMap.setRootBox(rootBox);
		cursor = new Cursor(contentMap);
	}

	public void setContent(final IDocument document) {
		this.document = document;
		rebuildRootBox();
	}

	public void setVisualization(final VisualizationChain visualizationChain) {
		Assert.isNotNull(visualizationChain);
		this.visualizationChain = visualizationChain;
		rebuildRootBox();
	}

	private void rebuildRootBox() {
		if (document != null) {
			rootBox = visualizationChain.visualizeRoot(document);
		} else {
			rootBox = new RootBox();
		}

		if (rootBox == null) {
			rootBox = new RootBox();
		}

		contentMap.setRootBox(rootBox);
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
	}

	private void widgetDisposed() {
		rootBox = null;
	}

	private void resize(final ControlEvent event) {
		System.out.println("Width: " + getClientArea().width);
		rootBox.setWidth(getClientArea().width);
		invalidateLayout();
	}

	private void scrollVertically(final SelectionEvent event) {
		invalidateViewport();
	}

	private void keyPressed(final KeyEvent event) {
		switch (event.keyCode) {
		case SWT.ARROW_LEFT:
			moveCursor(left());
			break;
		case SWT.ARROW_RIGHT:
			moveCursor(right());
			break;
		case SWT.ARROW_UP:
			moveCursor(up());
			break;
		case SWT.ARROW_DOWN:
			moveCursor(down());
			break;
		case SWT.HOME:
			moveCursor(toOffset(0));
			break;
		default:
			break;
		}
	}

	private void mouseDown(final MouseEvent event) {
		final int absoluteY = event.y + getVerticalBar().getSelection();
		moveCursor(toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void moveCursor(final ICursorMove move) {
		cursor.move(move);
		invalidateCursor();
	}

	private void invalidateViewport() {
		renderer.schedule(paintContent());
	}

	private void invalidateCursor() {
		renderer.schedule(renderCursorMovement(getVerticalBar().getSelection(), getSize().y), paintContent());
	}

	private void invalidateLayout() {
		renderer.schedule(layoutContent(), paintContent());
	}

	private IRenderStep paintContent() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				rootBox.paint(graphics);
				cursor.paint(graphics);
			}
		};
	}

	private IRenderStep layoutContent() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.reconcile(graphics);
				rootBox.layout(graphics);
				updateVerticalBar();
			}
		};
	}

	private void updateVerticalBar() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int maximum = rootBox.getHeight() + Cursor.CARET_BUFFER;
				final int pageSize = getClientArea().height;
				final int selection = getVerticalBar().getSelection();
				getVerticalBar().setValues(selection, 0, maximum, pageSize, pageSize / 4, pageSize);
			}
		});
	}

	private IRenderStep renderCursorMovement(final int top, final int height) {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.applyMoves(graphics);
				final int delta = cursor.getDeltaIntoVisibleArea(top, height);
				graphics.moveOrigin(0, -delta);
				moveVerticalBar(delta);
			}
		};
	}

	private void moveVerticalBar(final int delta) {
		if (delta == 0) {
			return;
		}

		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int selection = getVerticalBar().getSelection() + delta;
				getVerticalBar().setSelection(selection);
			}
		});
	}

}
