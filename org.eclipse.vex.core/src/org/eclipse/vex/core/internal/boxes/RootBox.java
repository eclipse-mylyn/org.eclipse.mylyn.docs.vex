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

/**
 * @author Florian Thienel
 */
public class RootBox implements IParentBox {

	private int width;
	private int height;
	private final ArrayList<IChildBox> children = new ArrayList<IChildBox>();

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
		for (final IChildBox child : children) {
			child.setPosition(height, 0);
			child.setWidth(width);
			child.layout();
			height += child.getHeight();
		}
	}
}
