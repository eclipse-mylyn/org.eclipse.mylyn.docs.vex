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
		translateChildrenToNewPosition(top, left);
		this.top = top;
		this.left = left;
	}

	private void translateChildrenToNewPosition(final int top, final int left) {
		for (final IInlineBox child : children) {
			child.setPosition(child.getTop() - this.top + top, child.getLeft() - this.left + left);
		}
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

	public boolean hasMoreThanOneChild() {
		return !children.isEmpty() && children.getFirst() != children.getLast();
	}

	public void prependChild(final IInlineBox box) {
		children.addFirst(box);
		width += box.getWidth();
	}

	public void appendChild(final IInlineBox box) {
		children.addLast(box);
		width += box.getWidth();
	}

	public boolean canJoinWithLastChild(final IInlineBox box) {
		if (children.isEmpty()) {
			return false;
		}
		final IInlineBox lastChild = children.getLast();
		return lastChild.canJoin(box);
	}

	public boolean joinWithLastChild(final IInlineBox box) {
		if (children.isEmpty()) {
			return false;
		}
		final IInlineBox lastChild = children.getLast();
		final boolean joined = lastChild.join(box);
		if (joined) {
			width += box.getWidth();
		}
		return joined;
	}

	public IInlineBox getLastChild() {
		return children.getLast();
	}

	public void removeLastChild() {
		if (children.isEmpty()) {
			return;
		}
		children.removeLast();
	}

	public void arrangeChildren() {
		calculateBoundsAndBaseline();
		arrangeChildrenOnBaseline();
	}

	private void calculateBoundsAndBaseline() {
		width = 0;
		height = 0;
		baseline = 0;
		int descend = 0;
		for (final IInlineBox child : children) {
			width += child.getWidth();
			descend = Math.max(descend, child.getHeight() - child.getBaseline());
			baseline = Math.max(baseline, child.getBaseline());
		}
		height = baseline + descend;
	}

	private void arrangeChildrenOnBaseline() {
		int childLeft = left;
		for (final IInlineBox child : children) {
			final int childTop = baseline - child.getBaseline() + top;
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
