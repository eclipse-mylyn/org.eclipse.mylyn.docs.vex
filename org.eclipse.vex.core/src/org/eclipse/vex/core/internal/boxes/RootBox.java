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

/**
 * @author Florian Thienel
 */
public class RootBox implements IParentBox {

	private int width;
	private int height;
	private final ArrayList<IChildBox> children = new ArrayList<IChildBox>();
	private final ChildBoxPainter childBoxPainter = new ChildBoxPainter();

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public void appendChild(final IChildBox child) {
		children.add(child);
	}

	public void layout() {
		height = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IChildBox child = children.get(i);
			child.setPosition(height, 0);
			child.setWidth(width);
			child.layout();
			height += child.getHeight();
		}
	}

	@Override
	public void paint(final Graphics graphics) {
		childBoxPainter.paint(children, graphics);
	}
}