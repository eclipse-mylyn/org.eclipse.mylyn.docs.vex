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
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class Paragraph implements IChildBox {

	private int top;
	private int left;
	private int width;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();
	private final LineArrangement lines = new LineArrangement();

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, lines.getHeight());
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
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
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

	@Override
	public void layout(final Graphics graphics) {
		arrangeChildrenOnLines(graphics);
	}

	private void arrangeChildrenOnLines(final Graphics graphics) {
		lines.arrangeChildren(graphics, children.listIterator(), width);
	}

	@Override
	public void paint(final Graphics graphics) {
		for (final Line line : lines.getLines()) {
			graphics.moveOrigin(line.getLeft(), line.getTop());
			line.paint(graphics);
			graphics.moveOrigin(-line.getLeft(), -line.getTop());
		}
	}

	public static class LineArrangement {
		private final LinkedList<Line> lines = new LinkedList<Line>();

		private ListIterator<IInlineBox> childIterator;
		private int width;
		private int height;
		private boolean lastChildWrappedCompletely;
		private Line currentLine;

		public void arrangeChildren(final Graphics graphics, final ListIterator<IInlineBox> childIterator, final int width) {
			this.childIterator = childIterator;
			this.width = width;
			reset();

			while (childIterator.hasNext()) {
				final IInlineBox child = childIterator.next();
				child.layout(graphics);
				appendChild(graphics, child);
			}
			finalizeCurrentLine();
		}

		private void reset() {
			lines.clear();
			height = 0;
			lastChildWrappedCompletely = false;
			currentLine = new Line();
		}

		private void appendChild(final Graphics graphics, final IInlineBox child) {
			final boolean childWrappedCompletely;
			if (currentLine.canJoinWithLastChild(child)) {
				childWrappedCompletely = arrangeWithLastChild(graphics, child);
			} else if (childFitsIntoCurrentLine(child)) {
				childWrappedCompletely = appendToCurrentLine(child);
			} else if (child.canSplit()) {
				childWrappedCompletely = splitAndWrapToNextLine(graphics, child);
			} else {
				childWrappedCompletely = wrapCompletelyToNextLine(child);
			}

			lastChildWrappedCompletely = childWrappedCompletely;
		}

		private boolean arrangeWithLastChild(final Graphics graphics, final IInlineBox child) {
			currentLine.joinWithLastChild(child);
			childIterator.remove();
			if (currentLine.getWidth() <= width) {
				return false;
			}

			final IInlineBox previousChild = currentLine.getLastChild();
			if (!previousChild.canSplit()) {
				throw new IllegalStateException("An IInlineBox that supports joining must also support splitting!");
			}

			final int headWidth = previousChild.getWidth() - currentLine.getWidth() + width;
			final IInlineBox tail = previousChild.splitTail(graphics, headWidth, !currentLine.hasMoreThanOneChild());
			final boolean childWrappedCompletely = previousChild.getWidth() == 0;
			if (tail.getWidth() > 0) {
				insertNextChild(tail);
			}

			lineBreak();
			return childWrappedCompletely;
		}

		private boolean appendToCurrentLine(final IInlineBox child) {
			currentLine.appendChild(child);
			return false;
		}

		private boolean splitAndWrapToNextLine(final Graphics graphics, final IInlineBox child) {
			final int headWidth = width - currentLine.getWidth();
			final IInlineBox tail = child.splitTail(graphics, headWidth, !currentLine.hasChildren());
			final boolean childWrappedCompletely = child.getWidth() == 0;
			if (childWrappedCompletely) {
				childIterator.remove();
			} else {
				currentLine.appendChild(child);
			}
			if (tail.getWidth() > 0) {
				insertNextChild(tail);
			}

			lineBreak();
			return childWrappedCompletely;
		}

		private boolean wrapCompletelyToNextLine(final IInlineBox child) {
			lineBreak();
			if (childFitsIntoCurrentLine(child)) {
				currentLine.appendChild(child);
			}
			return true;
		}

		private void insertNextChild(final IInlineBox tail) {
			childIterator.add(tail);
			backupChildIterator();
		}

		private void backupChildIterator() {
			if (!lastChildWrappedCompletely) {
				childIterator.previous();
			}
		}

		private boolean childFitsIntoCurrentLine(final IInlineBox child) {
			return currentLine.getWidth() + child.getWidth() < width;
		}

		private void lineBreak() {
			finalizeCurrentLine();
			currentLine = new Line();
		}

		private void finalizeCurrentLine() {
			if (currentLine.getWidth() <= 0) {
				return;
			}
			currentLine.arrangeChildren();
			currentLine.setPosition(height, 0);
			height += currentLine.getHeight();
			lines.add(currentLine);
		}

		public Collection<Line> getLines() {
			return lines;
		}

		public int getHeight() {
			return height;
		}
	}
}
