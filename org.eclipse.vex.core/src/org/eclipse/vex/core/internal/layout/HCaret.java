/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * A horizontal caret representing the insertion point between two block boxes.
 */
public class HCaret extends Caret {

	private static final int LINE_WIDTH = 2;

	/**
	 * Class constructor.
	 *
	 * @param x
	 *            x-coordinate of the top left corner of the caret
	 * @param y
	 *            y-coordinate of the top left corner of the caret
	 * @param length
	 *            Horizontal length of the caret.
	 */
	public HCaret(final int x, final int y, final int length) {
		super(x, y);
		this.length = length;
	}

	@Override
	public void draw(final Graphics g, final Color color) {
		final ColorResource newColor = g.getColor(color);
		final ColorResource oldColor = g.setColor(newColor);
		g.fillRect(getX(), getY(), length, LINE_WIDTH);
		g.setColor(oldColor);
	}

	/**
	 * @see org.eclipse.vex.core.internal.core.Caret#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), length, LINE_WIDTH);
	}

	// ====================================================== PRIVATE

	private final int length;
}
