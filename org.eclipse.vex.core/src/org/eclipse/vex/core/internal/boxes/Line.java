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
package org.eclipse.vex.core.internal.boxes;

import java.util.LinkedList;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class Line {

	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();

	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBaseline() {
		return baseline;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void prependChild(final IInlineBox box) {
		if (!joinWithFirstChild(box)) {
			children.addFirst(box);
		}
		width += box.getWidth();
	}

	private boolean joinWithFirstChild(final IInlineBox box) {
		if (!hasChildren()) {
			return false;
		}
		final IInlineBox firstChild = children.removeFirst();
		final boolean joined = box.join(firstChild);
		if (joined) {
			children.addFirst(box);
		} else {
			children.addFirst(firstChild);
		}
		return joined;
	}

	public void appendChild(final IInlineBox box) {
		if (!joinWithLastChild(box)) {
			children.addLast(box);
		}
		width += box.getWidth();
	}

	private boolean joinWithLastChild(final IInlineBox box) {
		if (!hasChildren()) {
			return false;
		}
		final IInlineBox lastChild = children.getLast();
		final boolean joined = lastChild.join(box);
		return joined;
	}

	public void arrangeChildren() {
		calculateBoundsAndBaseline();
		arrangeChildrenOnBaseline();
	}

	private void calculateBoundsAndBaseline() {
		width = 0;
		height = 0;
		baseline = 0;
		for (final IInlineBox child : children) {
			width += child.getWidth();
			height = Math.max(height, child.getHeight());
			baseline = Math.max(baseline, child.getBaseline());
		}
	}

	private void arrangeChildrenOnBaseline() {
		int childLeft = 0;
		for (final IInlineBox child : children) {
			final int childTop = baseline - child.getBaseline();
			child.setPosition(childTop, childLeft);
			childLeft += child.getWidth();
		}
	}

	public void paint(final Graphics graphics) {
		for (final IInlineBox child : children) {
			graphics.moveOrigin(child.getLeft(), child.getTop());
			child.paint(graphics);
			graphics.moveOrigin(-child.getLeft(), -child.getTop());
		}
	}
}
