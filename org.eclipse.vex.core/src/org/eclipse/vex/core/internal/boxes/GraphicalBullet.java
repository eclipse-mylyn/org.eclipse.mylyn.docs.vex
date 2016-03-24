/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
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
import org.eclipse.vex.core.internal.css.BulletStyle;

/**
 * @author Florian Thienel
 */
public class GraphicalBullet extends SimpleInlineBox {

	private static final float HEIGHT_RATIO = 0.7f;
	private static final float LIFT_RATIO = 0.15f;

	private int width;
	private int height;

	private BulletStyle.Type bulletType;
	private FontSpec fontSpec;
	private Color color;

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getBaseline() {
		return height;
	}

	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public void setType(final BulletStyle.Type bulletType) {
		this.bulletType = bulletType;
	}

	public void setFont(final FontSpec fontSpec) {
		this.fontSpec = fontSpec;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	@Override
	public void layout(final Graphics graphics) {
		if (fontSpec == null) {
			return;
		}

		applyFont(graphics);
		final int ascent = graphics.getFontMetrics().getAscent();
		height = Math.round(ascent * HEIGHT_RATIO);
		final int lift = Math.round(ascent * LIFT_RATIO);
		width = height - lift;
	}

	private void applyFont(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;

		layout(graphics);

		return height != oldHeight;
	}

	@Override
	public void paint(final Graphics graphics) {
		graphics.setColor(graphics.getColor(color));

		switch (bulletType) {
		case SQUARE:
			graphics.fillRect(0, 0, width, width);
			break;
		case DISC:
			graphics.fillOval(0, 0, width, width);
			break;
		case CIRCLE:
			graphics.setLineWidth(1);
			graphics.drawOval(0, 0, width, width);
			break;
		default:
			graphics.fillRect(0, 0, width, width);
			break;
		}
	}
}
