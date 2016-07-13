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
package org.eclipse.vex.core.internal.boxes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

public class InlineContainer extends BaseBox implements IInlineBox, IParentBox<IInlineBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;
	private int maxWidth;
	private boolean containsChildThatRequiresLineWrapping;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();

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

	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	@Override
	public int getBaseline() {
		return baseline;
	}

	@Override
	public int getMaxWidth() {
		return maxWidth;
	}

	@Override
	public void setMaxWidth(final int maxWidth) {
		this.maxWidth = maxWidth;
	}

	@Override
	public int getInvisibleGapAtStart(final Graphics graphics) {
		if (children.isEmpty()) {
			return 0;
		}
		return children.getFirst().getInvisibleGapAtStart(graphics);
	}

	@Override
	public int getInvisibleGapAtEnd(final Graphics graphics) {
		if (children.isEmpty()) {
			return 0;
		}
		return children.getLast().getInvisibleGapAtEnd(graphics);
	}

	@Override
	public LineWrappingRule getLineWrappingAtStart() {
		if (children.isEmpty()) {
			return LineWrappingRule.ALLOWED;
		}
		return children.getFirst().getLineWrappingAtStart();
	}

	@Override
	public LineWrappingRule getLineWrappingAtEnd() {
		if (children.isEmpty()) {
			return LineWrappingRule.ALLOWED;
		}
		return children.getLast().getLineWrappingAtEnd();
	}

	@Override
	public boolean requiresSplitForLineWrapping() {
		return containsChildThatRequiresLineWrapping;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
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
		final boolean joined = box.join(firstChild);
		if (joined) {
			children.removeFirst();
			children.addFirst(box);
		}
		return joined;
	}

	@Override
	public void appendChild(final IInlineBox child) {
		if (child == null) {
			return;
		}
		if (!joinWithLastChild(child)) {
			child.setParent(this);
			children.addLast(child);
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

	@Override
	public Iterable<IInlineBox> getChildren() {
		return children;
	}

	@Override
	public void layout(final Graphics graphics) {
		layoutChildren(graphics);
		calculateBoundsAndBaseline();
		arrangeChildrenOnBaseline();
		updateRequiresSplitForLineWrapping();
	}

	private void layoutChildren(final Graphics graphics) {
		for (final IInlineBox child : children) {
			child.setMaxWidth(maxWidth);
			child.layout(graphics);
			containsChildThatRequiresLineWrapping |= child.requiresSplitForLineWrapping();
		}
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
		int childLeft = 0;
		for (final IInlineBox child : children) {
			final int childTop = baseline - child.getBaseline();
			child.setPosition(childTop, childLeft);
			childLeft += child.getWidth();
		}
	}

	private void updateRequiresSplitForLineWrapping() {
		containsChildThatRequiresLineWrapping = false;
		for (final IInlineBox child : children) {
			containsChildThatRequiresLineWrapping |= child.requiresSplitForLineWrapping();
		}
	}

	@Override
	public Collection<IBox> reconcileLayout(final Graphics graphics) {
		final int oldWidth = width;
		final int oldHeight = height;
		final int oldBaseline = baseline;
		calculateBoundsAndBaseline();

		if (oldWidth != width || oldHeight != height || oldBaseline != baseline) {
			return Collections.singleton(getParent());
		} else {
			return NOTHING_INVALIDATED;
		}
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(children, graphics);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof InlineContainer)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final InlineContainer otherInlineContainer = (InlineContainer) other;

		for (int i = 0; i < otherInlineContainer.children.size(); i += 1) {
			final IInlineBox child = otherInlineContainer.children.get(i);
			appendChild(child);
		}

		calculateBoundsAndBaseline();
		arrangeChildrenOnBaseline();

		return true;
	}

	@Override
	public boolean canSplit() {
		if (children.isEmpty()) {
			return false;
		}
		if (children.size() == 1) {
			return children.getFirst().canSplit();
		}
		return true;
	}

	@Override
	public InlineContainer splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		final int splitIndex = findChildIndexToSplitAt(headWidth);
		if (splitIndex == -1) {
			return new InlineContainer();
		}

		final IInlineBox splitChild = children.get(splitIndex);

		final IInlineBox splitChildTail;
		if (splitChild.canSplit()) {
			splitChildTail = splitChild.splitTail(graphics, headWidth - splitChild.getLeft(), force && splitIndex == 0);
			if (splitChild.getWidth() == 0) {
				children.remove(splitChild);
				splitChild.setParent(null);
			}
		} else {
			splitChildTail = splitChild;
		}

		final InlineContainer tail = new InlineContainer();
		tail.setParent(parent);

		if (splitChildTail.getWidth() > 0 && splitChild != splitChildTail) {
			tail.appendChild(splitChildTail);
		}

		if (splitChild.getWidth() == 0 || splitChild == splitChildTail) {
			moveChildrenTo(tail, splitIndex);
		} else {
			moveChildrenTo(tail, splitIndex + 1);
		}

		layout(graphics);
		tail.layout(graphics);

		return tail;
	}

	private int findChildIndexToSplitAt(final int headWidth) {
		int i = 0;
		for (final IInlineBox child : children) {
			if (child.getLineWrappingAtStart() == LineWrappingRule.REQUIRED && i > 0) {
				return i - 1;
			}
			if (child.getLineWrappingAtEnd() == LineWrappingRule.REQUIRED) {
				return i;
			}
			if (child.requiresSplitForLineWrapping()) {
				return i;
			}
			if (child.getLeft() + child.getWidth() > headWidth) {
				return i;
			}
			i += 1;
		}
		return -1;
	}

	private void moveChildrenTo(final InlineContainer destination, final int startIndex) {
		while (startIndex < children.size()) {
			final IInlineBox child = children.get(startIndex);
			children.remove(startIndex);
			destination.appendChild(child);
		}
	}

}
