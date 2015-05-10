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
 * This box arranges child boxes in one vertical column of given width. Its height depends on the sum of the height of
 * its children.
 *
 * @author Florian Thienel
 */
public class VerticalBlock extends BaseBox implements IStructuralBox, IParentBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private final ArrayList<IStructuralBox> children = new ArrayList<IStructuralBox>();

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

	public void setWidth(final int width) {
		this.width = Math.max(0, width);
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

	public void appendChild(final IStructuralBox child) {
		child.setParent(this);
		children.add(child);
	}

	public Iterable<IStructuralBox> getChildren() {
		return children;
	}

	public void layout(final Graphics graphics) {
		height = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			child.setPosition(height, 0);
			child.setWidth(width);
			child.layout(graphics);
			height += child.getHeight();
		}
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		height = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			child.setPosition(height, 0);
			height += child.getHeight();
		}
		return oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(children, graphics);
	}
}
