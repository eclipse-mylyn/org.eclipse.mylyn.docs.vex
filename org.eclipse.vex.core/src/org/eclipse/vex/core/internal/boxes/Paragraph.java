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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private final List<IInlineBox> children = new ArrayList<IInlineBox>();
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
		height = 0;
		Line line = new Line();
		for (int i = 0; i < children.size(); i += 1) {
			final IInlineBox child = children.get(i);

			child.layout(graphics);

			if (line.getWidth() + child.getWidth() >= width) {
				line.arrangeChildren();
				line.setPosition(height, 0);
				height += line.getHeight();
				lines.add(line);
				line = new Line();
			}
			line.appendChild(child);
		}
	}

	private void clearLines() {
		lines = new ArrayList<Line>();
	}

	@Override
	public void paint(final Graphics graphics) {
		for (int i = 0; i < lines.size(); i += 1) {
			final Line line = lines.get(i);
			graphics.moveOrigin(line.getLeft(), line.getTop());
			line.paint(graphics);
			graphics.moveOrigin(-line.getLeft(), -line.getTop());
		}
	}

}
