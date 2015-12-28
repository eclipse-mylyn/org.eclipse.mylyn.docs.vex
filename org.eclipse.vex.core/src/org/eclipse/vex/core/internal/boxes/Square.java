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

/**
 * @author Florian Thienel
 */
public class Square extends SimpleInlineBox {

	private int size;

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
}
