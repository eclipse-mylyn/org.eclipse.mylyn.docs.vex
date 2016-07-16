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
package org.eclipse.vex.core.internal.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.widget.IRenderer.IRenderStep;

/**
 * @author Florian Thienel
 */
public class BoxView {

	private final int SCROLL_PADDING = Cursor.CARET_BUFFER * 2;

	private final IRenderer renderer;
	private final IViewPort viewPort;
	private final Cursor cursor;
	private RootBox rootBox;
	private int width;

	public BoxView(final IRenderer renderer, final IViewPort viewPort, final Cursor cursor) {
		this.renderer = renderer;
		this.viewPort = viewPort;
		this.cursor = cursor;
		rootBox = new RootBox();
	}

	public void dispose() {
		rootBox = null;
	}

	public void setRootBox(final RootBox rootBox) {
		this.rootBox = rootBox;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void invalidateLayout(final IBox box) {
		render(reconcileLayout(box), paintContent());
	}

	public void invalidateViewport() {
		render(paintContent());
	}

	public void invalidateCursor() {
		render(renderCursorMovement(), paintContent());
	}

	public void invalidateWidth(final int width) {
		this.width = width;
		invalidateEverything();
	}

	public void invalidateEverything() {
		final int width = this.width;
		final IRenderStep invalidateWidth = new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				rootBox.setWidth(width);
			}
		};

		render(invalidateWidth, layoutContent(), paintContent());
	}

	private void render(final IRenderStep... steps) {
		renderer.render(viewPort.getVisibleArea(), steps);
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
				rootBox.layout(graphics);
				cursor.reconcile(graphics);
				reconcileViewPort();
			}

		};
	}

	private IRenderStep reconcileLayout(final IBox box) {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				reconcileBoxLayout(graphics, box);
				reconcileViewPort();
			}
		};
	}

	private static void reconcileBoxLayout(final Graphics graphics, final IBox box) {
		final LinkedList<IBox> invalidatedBoxes = new LinkedList<IBox>();
		invalidatedBoxes.add(box);

		while (!invalidatedBoxes.isEmpty()) {
			final IBox invalidatedBox = invalidatedBoxes.pollFirst();
			final Collection<IBox> nextBoxes = invalidatedBox.reconcileLayout(graphics);
			cover(invalidatedBoxes, nextBoxes);
		}
	}

	private static void cover(final LinkedList<IBox> queue, final Collection<IBox> newBoxes) {
		for (final IBox box : newBoxes) {
			cover(queue, box);
		}
	}

	private static void cover(final LinkedList<IBox> queue, final IBox box) {
		final ListIterator<IBox> iterator = queue.listIterator();
		while (iterator.hasNext()) {
			final IBox queuedBox = iterator.next();
			if (isCovered(queuedBox, box)) {
				iterator.set(box);
				return;
			} else if (isCovered(box, queuedBox)) {
				return;
			}
		}
		queue.add(box);
	}

	private static boolean isCovered(final IBox box, final IBox cover) {
		return isAncestor(box, cover);
	}

	private static boolean isAncestor(final IBox box, final IBox ancestor) {
		if (ancestor == null) {
			return false;
		}
		if (box == ancestor) {
			return true;
		}
		return isAncestor(box, getParent(ancestor));
	}

	private static IBox getParent(final IBox box) {
		if (box instanceof IChildBox) {
			return ((IChildBox) box).getParent();
		}
		return null;
	}

	private void reconcileViewPort() {
		viewPort.reconcile(rootBox.getHeight() + Cursor.CARET_BUFFER);
	}

	private IRenderStep renderCursorMovement() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.applyMoves(graphics);
				moveViewPortToCursor(graphics);
			}
		};
	}

	private void moveViewPortToCursor(final Graphics graphics) {
		final int delta = getDeltaIntoVisibleArea(viewPort.getVisibleArea());
		graphics.moveOrigin(0, -delta);
		viewPort.moveRelative(delta);
	}

	private int getDeltaIntoVisibleArea(final Rectangle visibleArea) {
		final int top = visibleArea.getY();
		final int height = visibleArea.getHeight();
		final int delta = cursor.getDeltaIntoVisibleArea(top, height);
		if (delta < 0) {
			return delta - Math.min(SCROLL_PADDING, top + delta);
		} else if (delta > 0) {
			return delta + Math.min(SCROLL_PADDING, rootBox.getHeight() + Cursor.CARET_BUFFER - top - height - delta);
		} else {
			return delta;
		}
	}

}
