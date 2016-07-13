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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.core.TextAlign;

/**
 * @author Florian Thienel
 */
public class Paragraph extends BaseBox implements IStructuralBox, IParentBox<IInlineBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;

	private TextAlign textAlign = TextAlign.LEFT;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();
	private final LineArrangement lines = new LineArrangement();

	@Override
	public void setParent(final IBox parent) {
		this.parent = parent;
	}

	@Override
	public IBox getParent() {
		return parent;
	}

	@Override
	public int getAbsoluteTop() {
		if (parent == null) {
			return top;
		}
		return parent.getAbsoluteTop() + top;
	}

	@Override
	public int getAbsoluteLeft() {
		if (parent == null) {
			return left;
		}
		return parent.getAbsoluteLeft() + left;
	}

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

	public TextAlign getTextAlign() {
		return textAlign;
	}

	public void setTextAlign(final TextAlign textAlign) {
		this.textAlign = textAlign;
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

	public void prependChild(final IInlineBox child) {
		if (child == null) {
			return;
		}
		if (!joinWithFirstChild(child)) {
			child.setParent(this);
			children.addFirst(child);
		}
	}

	private boolean joinWithFirstChild(final IInlineBox box) {
		if (!hasChildren()) {
			return false;
		}
		final IInlineBox firstChild = children.getFirst();
		final boolean joined = firstChild.join(box);
		if (joined) {
			children.removeFirst();
			children.addFirst(box);
		}
		return joined;
	}

	public void appendChild(final IInlineBox child) {
		if (child == null) {
			return;
		}
		if (!joinWithLastChild(child)) {
			child.setParent(this);
			children.add(child);
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

	@Override
	public void replaceChildren(final Collection<? extends IBox> oldChildren, final IInlineBox newChild) {
		boolean newChildInserted = false;

		for (final ListIterator<IInlineBox> iter = children.listIterator(); iter.hasNext();) {
			final IInlineBox child = iter.next();
			if (oldChildren.contains(child)) {
				iter.remove();
				if (!newChildInserted) {
					iter.add(newChild);
					newChild.setParent(this);
					newChildInserted = true;
				}
			}
		}
	}

	public Iterable<IInlineBox> getChildren() {
		return children;
	}

	@Override
	public void layout(final Graphics graphics) {
		arrangeChildrenOnLines(graphics);
	}

	private void arrangeChildrenOnLines(final Graphics graphics) {
		lines.arrangeBoxes(graphics, children.listIterator(), width, textAlign);
	}

	@Override
	public Collection<IBox> reconcileLayout(final Graphics graphics) {
		final int oldHeight = lines.getHeight();
		arrangeChildrenOnLines(graphics);

		if (oldHeight != lines.getHeight()) {
			return Collections.singleton(getParent());
		} else {
			return NOTHING_INVALIDATED;
		}
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
