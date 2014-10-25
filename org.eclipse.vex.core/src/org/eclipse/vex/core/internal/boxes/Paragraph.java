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
	private int height;

	private final LinkedList<IInlineBox> children = new LinkedList<IInlineBox>();
	private final LinkedList<Line> lines = new LinkedList<Line>();

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
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
		return height;
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
		lines.clear();
		arrangeChildrenOnLines(graphics);
	}

	private void arrangeChildrenOnLines(final Graphics graphics) {
		final ListIterator<IInlineBox> iterator = children.listIterator();
		final LineAppender appender = new LineAppender(iterator, lines, width);
		while (iterator.hasNext()) {
			final IInlineBox child = iterator.next();
			child.layout(graphics);
			appender.appendChild(graphics, child);
		}
		appender.finalizeLastLine();
		height = appender.getHeight();
	}

	@Override
	public void paint(final Graphics graphics) {
		for (final Line line : lines) {
			graphics.moveOrigin(line.getLeft(), line.getTop());
			line.paint(graphics);
			graphics.moveOrigin(-line.getLeft(), -line.getTop());
		}
	}

	public static class LineAppender {
		private final ListIterator<IInlineBox> childIterator;
		private final Collection<Line> lines;
		private final int width;
		private int height;
		private boolean lastChildWrappedCompletely;
		private Line currentLine = new Line();

		public LineAppender(final ListIterator<IInlineBox> childIterator, final Collection<Line> lines, final int width) {
			this.childIterator = childIterator;
			this.lines = lines;
			this.width = width;
		}

		public void appendChild(final Graphics graphics, final IInlineBox child) {
			final boolean childWrappedCompletely;
			if (currentLine.joinWithLastChild(child)) {
				childIterator.remove();
				if (currentLine.getWidth() > width) {
					final IInlineBox previousChild = currentLine.getLastChild();
					if (!previousChild.canSplit()) {
						throw new IllegalStateException("An IInlineBox that supports joining must also support splitting!");
					}

					final IInlineBox tail = previousChild.splitTail(graphics, previousChild.getWidth() - currentLine.getWidth() + width, !currentLine.hasMoreThanOneChild());
					childWrappedCompletely = previousChild.getWidth() == 0;
					if (tail.getWidth() > 0) {
						insertNextChild(tail);
					}

					finalizeCurrentLine();
					currentLine = new Line();
				} else {
					childWrappedCompletely = false;
				}
			} else if (childFitsIntoCurrentLine(child)) {
				currentLine.appendChild(child);
				childWrappedCompletely = false;
			} else if (child.canSplit()) {
				final IInlineBox tail = child.splitTail(graphics, width - currentLine.getWidth(), !currentLine.hasChildren());
				childWrappedCompletely = child.getWidth() == 0;
				if (childWrappedCompletely) {
					childIterator.remove();
				} else {
					currentLine.appendChild(child);
				}
				if (tail.getWidth() > 0) {
					insertNextChild(tail);
				}

				finalizeCurrentLine();
				currentLine = new Line();
			} else {
				finalizeCurrentLine();
				currentLine = new Line();
				if (childFitsIntoCurrentLine(child)) {
					currentLine.appendChild(child);
				}
				childWrappedCompletely = true;
			}

			lastChildWrappedCompletely = childWrappedCompletely;
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

		private void finalizeCurrentLine() {
			if (currentLine.getWidth() <= 0) {
				return;
			}
			currentLine.arrangeChildren();
			currentLine.setPosition(height, 0);
			height += currentLine.getHeight();
			lines.add(currentLine);
		}

		public void finalizeLastLine() {
			finalizeCurrentLine();
		}

		public int getHeight() {
			return height;
		}
	}
}
