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

	private final IRenderer renderer;
	private final IViewPort viewPort;
	private final Cursor cursor;
	private RootBox rootBox;

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
				box.layout(graphics);
				reconcileParentsLayout(box, graphics);
				reconcileViewPort();
			}
		};
	}

	private void reconcileViewPort() {
		viewPort.reconcile(rootBox.getHeight() + Cursor.CARET_BUFFER);
	}

	private void reconcileParentsLayout(final IBox box, final Graphics graphics) {
		IBox parentBox = getParentBox(box);
		while (parentBox != null && parentBox.reconcileLayout(graphics)) {
			parentBox = getParentBox(parentBox);
		}
	}

	private IBox getParentBox(final IBox box) {
		if (box instanceof IChildBox) {
			return ((IChildBox) box).getParent();
		}
		return null;
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
		return cursor.getDeltaIntoVisibleArea(top, height);
	}

}
