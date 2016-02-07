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
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public class Square extends SimpleInlineBox {

	private static final float ASCENT_RATIO = 0.6f;

	private int size;
	private Color color;
	private FontSpec fontSpec;

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

	public void setFont(final FontSpec fontSpec) {
		this.fontSpec = fontSpec;
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
		if (fontSpec == null) {
			return;
		}

		applyFont(graphics);
		size = Math.round(graphics.getFontMetrics().getAscent() * ASCENT_RATIO);
	}

	private void applyFont(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldSize = size;

		layout(graphics);

		return size != oldSize;
	}

	@Override
	public void paint(final Graphics graphics) {
		graphics.setColor(graphics.getColor(color));
		graphics.fillRect(0, 0, size, size);
	}
}
