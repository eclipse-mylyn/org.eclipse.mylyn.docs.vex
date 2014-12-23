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
public class Paragraph implements IChildBox, IParentBox<IInlineBox> {

	private int top;
	private int left;
	private int width;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();
	private final LineArrangement lines = new LineArrangement();

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(final int width) {
		this.width = Math.max(0, width);
	}

	@Override
	public int getHeight() {
		return lines.getHeight();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, lines.getHeight());
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void appendChild(final IInlineBox box) {
		if (!joinWithLastChild(box)) {
			children.add(box);
		}
	}

	private boolean joinWithLastChild(final IInlineBox box) {
		if (!hasChildren()) {
			return false;
		}
		final IInlineBox lastChild = children.getLast();
		final boolean joined = lastChild.join(box);
		return joined;
	}

	public Iterable<IInlineBox> getChildren() {
		return children;
	}

	@Override
	public void layout(final Graphics graphics) {
		arrangeChildrenOnLines(graphics);
	}

	private void arrangeChildrenOnLines(final Graphics graphics) {
		lines.arrangeBoxes(graphics, children.listIterator(), width);
	}

	@Override
	public void paint(final Graphics graphics) {
		for (final Line line : lines.getLines()) {
			/*
			 * Line takes care of moving the origin for each child box. The coordinates of the child boxes are relative
			 * to the Paragraph, not relative to the Line, because Paragraph is the children's parent. The Line is a
			 * transparent utility with regards to the box structure, which is used internally by Paragraph.
			 */
			line.paint(graphics);
		}
	}

}
