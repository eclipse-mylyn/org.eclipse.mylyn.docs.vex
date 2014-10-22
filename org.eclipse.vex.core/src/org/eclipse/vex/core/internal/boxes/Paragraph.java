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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

	private final List<IInlineBox> children = new LinkedList<IInlineBox>();
	private List<Line> lines = Collections.emptyList();

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
		final IInlineBox lastChild = children.get(children.size() - 1);
		final boolean joined = lastChild.join(box);
		return joined;
	}

	@Override
	public void layout(final Graphics graphics) {
		clearLines();
		compressChildren();

		height = 0;
		Line line = new Line();
		final ListIterator<IInlineBox> iterator = children.listIterator();
		while (iterator.hasNext()) {
			final IInlineBox child = iterator.next();

			child.layout(graphics);

			if (line.getWidth() + child.getWidth() >= width) {
				if (child.canSplit()) {
					final IInlineBox tail = child.splitTail(graphics, width - line.getWidth());
					if (child.getWidth() > 0) {
						line.appendChild(child);
					} else {
						iterator.remove();
					}
					if (tail.getWidth() > 0) {
						iterator.add(tail);
						iterator.previous();
					}
				}

				finalizeLine(line);
				line = new Line();
			} else {
				line.appendChild(child);
			}
		}

		if (line.getWidth() > 0) {
			finalizeLine(line);
		}
	}

	private void clearLines() {
		lines = new LinkedList<Line>();
	}

	private void compressChildren() {
		final Iterator<IInlineBox> iterator = children.iterator();
		if (!iterator.hasNext()) {
			return;
		}

		IInlineBox lastChild = iterator.next();
		while (iterator.hasNext()) {
			final IInlineBox child = iterator.next();
			if (lastChild.join(child)) {
				iterator.remove();
			} else {
				lastChild = child;
			}
		}
	}

	private void finalizeLine(final Line line) {
		line.arrangeChildren();
		line.setPosition(height, 0);
		height += line.getHeight();
		lines.add(line);
	}

	@Override
	public void paint(final Graphics graphics) {
		for (final Line line : lines) {
			graphics.moveOrigin(line.getLeft(), line.getTop());
			line.paint(graphics);
			graphics.moveOrigin(-line.getLeft(), -line.getTop());
		}
	}

}
