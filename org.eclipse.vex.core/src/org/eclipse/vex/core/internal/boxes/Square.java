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
import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public class Square extends SimpleInlineBox {

	private int size;
	private Color color;

	@Override
	public int getWidth() {
		return size;
	}

	@Override
	public int getHeight() {
		return size;
	}

	@Override
	public int getBaseline() {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void layout(final Graphics graphics) {
		// ignore, static size
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		return false; // static size
	}

	@Override
	public void paint(final Graphics graphics) {
		graphics.setColor(graphics.getColor(color));
		graphics.fillRect(0, 0, size, size);
	}
}
