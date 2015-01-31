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
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.vex.core.internal.core.Graphics;

/**
 * This class implements double buffering with a dedicated render thread for SWT. This prevents flickering and keeps the
 * UI responsive.<br/>
 *
 * <b>CAUTION:</b> The prevention of flickering works only in conjunction with the style bit SWT.NO_BACKGROUND.
 *
 * @see http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org
 *      /eclipse/swt/snippets/Snippet48.java
 * @author Florian Thienel
 */
public class DoubleBufferedRenderer {

	private final Scrollable control;

	private Image bufferImage;
	private final Object bufferMonitor = new Object();

	private RenderTask currentRenderTask;
	private RenderTask nextRenderTask;
	private final Object renderTaskMonitor = new Object();

	public DoubleBufferedRenderer(final Scrollable control) {
		this.control = control;
		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				DoubleBufferedRenderer.this.widgetDisposed(e);
			}
		});
		control.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(final PaintEvent e) {
				DoubleBufferedRenderer.this.paintControl(e);
			}
		});
	}

	private void widgetDisposed(final DisposeEvent e) {
		synchronized (bufferMonitor) {
			if (bufferImage != null) {
				bufferImage.dispose();
			}
			bufferImage = null;
		}
	}

	private void paintControl(final PaintEvent event) {
		event.gc.drawImage(getBufferImage(), 0, 0);
	}

	private Image getBufferImage() {
		synchronized (bufferMonitor) {
			if (bufferImage == null) {
				bufferImage = new Image(control.getDisplay(), control.getSize().x, control.getSize().y);
			}
			return bufferImage;
		}
	}

	public void schedule(final IRenderStep... steps) {
		schedule(new RenderTask(control.getDisplay(), control.getVerticalBar().getSelection(), control.getSize().x, control.getSize().y, steps));
	}

	private void schedule(final RenderTask renderer) {
		synchronized (renderTaskMonitor) {
			if (currentRenderTask != null) {
				nextRenderTask = renderer;
				return;
			}
			currentRenderTask = renderer;
		}
		new Thread(renderer).start();
	}

	private void taskFinished(final Image image) {
		swapBufferImage(image);
		startNextRenderTask();
	}

	private void startNextRenderTask() {
		final Runnable renderer;
		synchronized (renderTaskMonitor) {
			currentRenderTask = nextRenderTask;
			nextRenderTask = null;
			renderer = currentRenderTask;
		}
		if (renderer != null) {
			new Thread(renderer).start();
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
		control.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				control.redraw();
			}
		});
	}

	public static interface IRenderStep {
		void render(Graphics graphics);
	}

	private class RenderTask implements Runnable {
		private final int top;
		private final IRenderStep[] steps;
		private final Image image;

		public RenderTask(final Display display, final int top, final int width, final int height, final IRenderStep... steps) {
			this.top = top;
			this.steps = steps;
			image = new Image(display, width, height);
		}

		@Override
		public final void run() {
			final GC gc = new GC(image);
			final Graphics graphics = new SwtGraphics(gc);
			graphics.moveOrigin(0, -top);

			try {
				for (final IRenderStep step : steps) {
					step.render(graphics);
				}
			} finally {
				graphics.dispose();
				gc.dispose();
			}

			taskFinished(image);
		}
	}
}
