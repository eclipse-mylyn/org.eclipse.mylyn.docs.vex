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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IRenderer;

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
public class DoubleBufferedRenderer implements IRenderer {

	private final Scrollable control;

	private final Object bufferMonitor = new Object();
	private final RenderBuffer[] buffer = new RenderBuffer[2];
	private int visibleIndex = 0;

	private final Map<URL, SwtImage> imageCache = new HashMap<URL, SwtImage>();

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
			for (int i = 0; i < buffer.length; i += 1) {
				if (buffer[i] != null) {
					buffer[i].dispose();
				}
				buffer[i] = null;
			}
		}
	}

	private void paintControl(final PaintEvent event) {
		event.gc.drawImage(getVisibleImage(), 0, 0);
	}

	private Image getVisibleImage() {
		synchronized (bufferMonitor) {
			if (buffer[visibleIndex] == null) {
				buffer[visibleIndex] = createRenderBuffer(Rectangle.NULL);
			}
			return buffer[visibleIndex].image;
		}
	}

	private RenderBuffer getRenderBuffer(final Rectangle viewPort) {
		synchronized (bufferMonitor) {
			final int index = (visibleIndex + 1) % 2;
			if (buffer[index] == null) {
				buffer[index] = createRenderBuffer(viewPort);
			} else if (!viewPortFitsIntoImage(viewPort, buffer[index].image)) {
				buffer[index].dispose();
				buffer[index] = createRenderBuffer(viewPort);
			}
			return buffer[index];
		}
	}

	private static boolean viewPortFitsIntoImage(final Rectangle viewPort, final Image image) {
		return image.getBounds().contains(viewPort.getWidth() - 1, viewPort.getHeight() - 1);
	}

	@Override
	public void render(final Rectangle viewPort, final IRenderStep... steps) {
		final RenderBuffer buffer = getRenderBuffer(viewPort);

		buffer.graphics.resetOrigin();
		clearViewPort(viewPort, buffer.graphics);
		moveOriginToViewPort(viewPort, buffer.graphics);

		for (final IRenderStep step : steps) {
			try {
				step.render(buffer.graphics);
			} catch (final Throwable t) {
				t.printStackTrace(); //TODO proper logging
			}
		}

		makeRenderedImageVisible(buffer.image);
	}

	private RenderBuffer createRenderBuffer(final Rectangle viewPort) {
		return new RenderBuffer(control.getDisplay(), viewPort.getWidth(), viewPort.getHeight(), imageCache);
	}

	private void moveOriginToViewPort(final Rectangle viewPort, final Graphics graphics) {
		graphics.moveOrigin(0, -viewPort.getY());
	}

	private void clearViewPort(final Rectangle viewPort, final Graphics graphics) {
		graphics.setColor(graphics.getColor(Color.WHITE));
		graphics.fillRect(0, 0, viewPort.getWidth(), viewPort.getHeight());
	}

	private void makeRenderedImageVisible(final Image newImage) {
		swapBufferImage(newImage);
		control.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				control.redraw();
			}
		});
	}

	private void swapBufferImage(final Image newImage) {
		synchronized (bufferMonitor) {
			visibleIndex = (visibleIndex + 1) % 2;
		}
	}

	private static class RenderBuffer {
		public final Image image;
		public final GC gc;
		public final SwtGraphics graphics;

		public RenderBuffer(final Device device, final int width, final int height, final Map<URL, SwtImage> imageCache) {
			image = new Image(device, Math.max(1, width), Math.max(1, height));
			gc = new GC(image);
			graphics = new SwtGraphics(gc, imageCache);
		}

		public void dispose() {
			graphics.dispose();
			gc.dispose();
			image.dispose();
		}
	}
}
