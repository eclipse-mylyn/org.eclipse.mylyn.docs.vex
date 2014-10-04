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

	private int visibleChildren;

	private final ArrayList<IInlineBox> children = new ArrayList<IInlineBox>();

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
		this.width = width;
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
		children.add(box);
	}

	@Override
	public void layout(final Graphics graphics) {
		height = 0;
		visibleChildren = 0;
		int baseline = 0;
		int currentWidth = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IInlineBox child = children.get(i);

			child.layout(graphics);

			height = Math.max(height, child.getHeight());
			baseline = Math.max(baseline, child.getBaseline());

			visibleChildren += 1;
			currentWidth += child.getWidth();
			if (currentWidth >= width) {
				break;
			}
		}

		int childLeft = 0;
		for (int i = 0; i < visibleChildren; i += 1) {
			final IInlineBox child = children.get(i);

			final int childTop = baseline - child.getBaseline();
			child.setPosition(childTop, childLeft);

			childLeft += child.getWidth();
			if (childLeft >= width) {
				break;
			}
		}
	}

	@Override
	public void paint(final Graphics graphics) {
		for (int i = 0; i < visibleChildren; i += 1) {
			final IInlineBox child = children.get(i);
			graphics.moveOrigin(child.getLeft(), child.getTop());
			child.paint(graphics);
			graphics.moveOrigin(-child.getLeft(), -child.getTop());
		}
	}

}
