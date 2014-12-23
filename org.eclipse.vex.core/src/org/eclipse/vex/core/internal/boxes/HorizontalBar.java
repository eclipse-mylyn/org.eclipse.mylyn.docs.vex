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

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class HorizontalBar implements IChildBox {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private Color color;

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

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
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

	public void setHeight(final int height) {
		this.height = height;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public void layout(final Graphics graphics) {
		// ignore, everything is static
	}

	@Override
	public void paint(final Graphics graphics) {
		final ColorResource colorResource = graphics.getColor(color);
		graphics.setColor(colorResource);
		graphics.fillRect(0, 0, width, height);
	}
}
