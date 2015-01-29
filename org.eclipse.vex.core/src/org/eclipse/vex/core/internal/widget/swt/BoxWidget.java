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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.cursor.ContentMap;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas {

	private RootBox rootBox;

	private final ContentMap contentMap;
	private final Cursor cursor;

	/*
	 * Use double buffering with a dedicated render thread to render the box model: This prevents flickering and keeps
	 * the UI responsive even for big box models.
	 * 
	 * The prevention of flickering works only in conjunction with the style bit SWT.NO_BACKGROUND.
	 * 
	 * @see http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org
	 * /eclipse/swt/snippets/Snippet48.java
	 */
	private Image bufferImage;
	private final Object bufferMonitor = new Object();

	private Runnable currentRenderer;
	private Runnable nextRenderer;
	private final Object rendererMonitor = new Object();

	public BoxWidget(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		connectDispose();
		connectPaintControl();
		connectResize();
		if ((style & SWT.V_SCROLL) == SWT.V_SCROLL) {
			connectScrollVertically();
		}
		connectKeyboard();
		connectMouse();

		rootBox = new RootBox();
		contentMap = new ContentMap();
		contentMap.setRootBox(rootBox);
		cursor = new Cursor(contentMap);
	}

	public void setContent(final RootBox rootBox) {
		this.rootBox = rootBox;
		contentMap.setRootBox(rootBox);
	}

	public void invalidate() {
		scheduleRenderer(new Painter(getDisplay(), getVerticalBar().getSelection(), getSize().x, getSize().y));
	}

	private void connectDispose() {
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				BoxWidget.this.widgetDisposed();
			}
		});
	}

	private void connectPaintControl() {
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(final PaintEvent e) {
				BoxWidget.this.paintControl(e);
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
		if (bufferImage != null) {
			bufferImage.dispose();
		}
	}

	private void paintControl(final PaintEvent event) {
		event.gc.drawImage(getBufferImage(), 0, 0);
	}

	private void resize(final ControlEvent event) {
		System.out.println("Width: " + getClientArea().width);
		rootBox.setWidth(getClientArea().width);
		scheduleRenderer(new Layouter(getDisplay(), getVerticalBar().getSelection(), getSize().x, getSize().y));
	}

	private void scrollVertically(final SelectionEvent event) {
		invalidate();
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
		invalidate();
	}

	private void scheduleRenderer(final Runnable renderer) {
		synchronized (rendererMonitor) {
			if (currentRenderer != null) {
				nextRenderer = renderer;
				return;
			}
			currentRenderer = renderer;
		}
		new Thread(renderer).start();
	}

	private void rendererFinished() {
		final Runnable renderer;
		synchronized (rendererMonitor) {
			currentRenderer = nextRenderer;
			nextRenderer = null;
			renderer = currentRenderer;
		}
		if (renderer != null) {
			new Thread(renderer).start();
		}
	}

	private Image getBufferImage() {
		synchronized (bufferMonitor) {
			if (bufferImage == null) {
				bufferImage = new Image(getDisplay(), getSize().x, getSize().y);
			}
			return bufferImage;
		}
	}

	private void swapBufferImage(final Image newImage) {
		final Image oldImage;
		synchronized (bufferMonitor) {
			oldImage = bufferImage;
			bufferImage = newImage;
		}

		if (oldImage != null) {
			oldImage.dispose();
		}
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				redraw();
			}
		});
	}

	private void layoutContent(final Graphics graphics) {
		rootBox.layout(graphics);
		cursor.reconcile(graphics);
	}

	private void paintContent(final Graphics graphics) {
		rootBox.paint(graphics);
		cursor.applyMoves(graphics);
		cursor.paint(graphics);
	}

	private void updateVerticalBar() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int maximum = rootBox.getHeight() + 30;
				final int pageSize = getClientArea().height;
				final int selection = getVerticalBar().getSelection();
				getVerticalBar().setValues(selection, 0, maximum, pageSize, pageSize / 4, pageSize);
			}
		});
	}

	private class Layouter implements Runnable {

		private final int top;
		private final Image image;

		public Layouter(final Display display, final int top, final int width, final int height) {
			this.top = top;
			image = new Image(display, width, height);
		}

		@Override
		public void run() {
			final GC gc = new GC(image);
			final Graphics graphics = new SwtGraphics(gc);
			graphics.moveOrigin(0, -top);

			layoutContent(graphics);
			paintContent(graphics);

			graphics.dispose();
			gc.dispose();

			updateVerticalBar();
			swapBufferImage(image);
			rendererFinished();
		}

	}

	private class Painter implements Runnable {

		private final int top;
		private final Image image;

		public Painter(final Display display, final int top, final int width, final int height) {
			this.top = top;
			image = new Image(display, width, height);
		}

		@Override
		public void run() {
			final GC gc = new GC(image);
			final Graphics graphics = new SwtGraphics(gc);
			graphics.moveOrigin(0, -top);

			paintContent(graphics);

			graphics.dispose();
			gc.dispose();

			swapBufferImage(image);
			rendererFinished();
		}
	}

}
