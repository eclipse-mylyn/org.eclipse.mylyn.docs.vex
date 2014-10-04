/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.HorizontalBar;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;

/**
 * A widget to show the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas {

	private/* final */RootBox rootBox;

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

		rootBox = new RootBox();
		for (int i = 0; i < 500000; i += 1) {
			final HorizontalBar bar = new HorizontalBar();
			bar.setHeight(10);
			bar.setColor(Color.BLACK);
			final VerticalBlock block = new VerticalBlock();
			block.appendChild(bar);
			block.setMargin(new Margin(10, 20, 30, 40));
			block.setBorder(new Border(10));
			block.setPadding(new Padding(15, 25, 35, 45));
			rootBox.appendChild(block);
		}
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
		addControlListener(new ControlListener() {
			@Override
			public void controlResized(final ControlEvent e) {
				resize(e);
			}

			@Override
			public void controlMoved(final ControlEvent e) {
				// ignore
			}
		});
	}

	private void connectScrollVertically() {
		getVerticalBar().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				scrollVertically(e);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// ignore
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
		rootBox.setWidth(getClientArea().width);
		scheduleRenderer(new Layouter(getDisplay(), getVerticalBar().getSelection(), getSize().x, getSize().y));
	}

	private void scrollVertically(final SelectionEvent event) {
		scheduleRenderer(new Painter(getDisplay(), getVerticalBar().getSelection(), getSize().x, getSize().y));
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

	private void layoutRootBox(final Graphics graphics) {
		System.out.print("Layout ");
		final long start = System.currentTimeMillis();
		rootBox.layout(graphics);
		System.out.println("took " + (System.currentTimeMillis() - start));
	}

	private void paintRootBox(final Graphics graphics) {
		System.out.print("Painting ");
		final long start = System.currentTimeMillis();
		rootBox.paint(graphics);
		System.out.println("took " + (System.currentTimeMillis() - start));
	}

	private void updateVerticalBar() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int maximum = rootBox.getHeight();
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

			layoutRootBox(graphics);
			paintRootBox(graphics);

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

			paintRootBox(graphics);

			graphics.dispose();
			gc.dispose();

			swapBufferImage(image);
			rendererFinished();
		}
	}
}
