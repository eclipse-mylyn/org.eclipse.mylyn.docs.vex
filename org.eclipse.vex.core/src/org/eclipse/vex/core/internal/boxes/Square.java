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
public class Square implements IInlineBox {

	private int top;
	private int left;
	private int width;

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, width);
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public int getLeft() {
		return left;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return width;
	}

	@Override
	public int getBaseline() {
		return width;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void layout(final Graphics graphics) {
		// ignore
	}

	@Override
	public void paint(final Graphics graphics) {
		final ColorResource colorResource = graphics.createColor(Color.BLACK); // TODO store square color
		graphics.setColor(colorResource);

		graphics.fillRect(left, top, width, width);

		colorResource.dispose();
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
	public IInlineBox splitTail(final Graphics graphics, final int splittingWidth) {
		throw new UnsupportedOperationException("Splitting is not supported for Square.");
	}
}
