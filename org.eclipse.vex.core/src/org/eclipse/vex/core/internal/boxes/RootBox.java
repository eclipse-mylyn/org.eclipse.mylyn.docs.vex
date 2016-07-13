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
import java.util.Collection;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class RootBox extends BaseBox implements IParentBox<IStructuralBox> {

	private int width;
	private int height;
	private final ArrayList<IStructuralBox> children = new ArrayList<IStructuralBox>();

	@Override
	public int getAbsoluteTop() {
		return 0;
	}

	@Override
	public int getAbsoluteLeft() {
		return 0;
	}

	@Override
	public int getTop() {
		return 0;
	}

	@Override
	public int getLeft() {
		return 0;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = Math.max(0, width);
	}

	public int getHeight() {
		return height;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(0, 0, width, height);
	}

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
	public void prependChild(final IStructuralBox child) {
		if (child == null) {
			return;
		}
		child.setParent(this);
		children.add(0, child);
	}

	@Override
	public void appendChild(final IStructuralBox child) {
		if (child == null) {
			return;
		}
		child.setParent(this);
		children.add(child);
	}

	@Override
	public void replaceChildren(final Collection<? extends IBox> oldChildren, final IStructuralBox newChild) {
		boolean newChildInserted = false;

		for (final ListIterator<IStructuralBox> iter = children.listIterator(); iter.hasNext();) {
			final IStructuralBox child = iter.next();
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
	public Collection<IBox> reconcileLayout(final Graphics graphics) {
		height = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			child.setPosition(height, 0);
			height += child.getHeight();
		}
		return NOTHING_INVALIDATED;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(children, graphics);
	}
}
