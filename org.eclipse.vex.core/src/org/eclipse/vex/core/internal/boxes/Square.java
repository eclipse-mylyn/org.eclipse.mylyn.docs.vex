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
public class Square extends BaseBox implements IInlineBox {

	private IBox parent;
	private int top;
	private int left;
	private int size;

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
		return size;
	}

	@Override
	public int getHeight() {
		return size;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, size, size);
	}

	public void setSize(final int size) {
		this.size = size;
	}

	@Override
	public int getBaseline() {
		return size;
	}

	@Override
	public int getInvisibleGapAtStart(final Graphics graphics) {
		return 0;
	}

	@Override
	public int getInvisibleGapAtEnd(final Graphics graphics) {
		return 0;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void layout(final Graphics graphics) {
		// ignore, everything is static
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		// ignore, everything is static
		return false;
	}

	@Override
	public void paint(final Graphics graphics) {
		final ColorResource colorResource = graphics.getColor(Color.BLACK); // TODO store square color
		graphics.setColor(colorResource);

		graphics.fillRect(0, 0, size, size);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		return false;
	}

	@Override
	public boolean join(final IInlineBox other) {
		return false;
	}

	@Override
	public boolean canSplit() {
		return false;
	}

	@Override
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		throw new UnsupportedOperationException("Splitting is not supported for Square.");
	}
}
