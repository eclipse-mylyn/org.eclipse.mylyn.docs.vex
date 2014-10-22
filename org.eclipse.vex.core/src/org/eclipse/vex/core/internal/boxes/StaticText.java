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

import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class StaticText implements IInlineBox {

	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;

	private String text;
	private FontSpec fontSpec;

	private boolean layoutValid;

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
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
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getBaseline() {
		return baseline;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
		layoutValid = false;
	}

	public FontSpec getFont() {
		return fontSpec;
	}

	public void setFont(final FontSpec fontSpec) {
		this.fontSpec = fontSpec;
		layoutValid = false;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void layout(final Graphics graphics) {
		if (layoutValid) {
			return;
		}

		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
		width = graphics.stringWidth(text);

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		height = fontMetrics.getHeight();
		baseline = fontMetrics.getAscent() + fontMetrics.getLeading();

		layoutValid = true;
	}

	@Override
	public void paint(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
		graphics.drawString(text, 0, 0);
	}

	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof StaticText)) {
			return false;
		}
		if (!hasEqualFont((StaticText) other)) {
			return false;
		}

		return true;
	}

	private boolean hasEqualFont(final StaticText other) {
		if (fontSpec != null && !fontSpec.equals(other.fontSpec)) {
			return false;
		}
		if (fontSpec == null && other.fontSpec != null) {
			return false;
		}

		return true;
	}

	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final StaticText otherText = (StaticText) other;

		text = text + otherText.text;
		width += otherText.width;

		return true;
	}

	public boolean canSplit() {
		return true;
	}

	public StaticText splitTail(final Graphics graphics, final int splittingWidth) {
		final int splittingPosition = findPositionAtWidth(graphics, splittingWidth);
		final StaticText tail = new StaticText();
		tail.setText(text.substring(splittingPosition, text.length()));
		tail.layout(graphics);
		text = text.substring(0, splittingPosition);
		width -= tail.width;
		return tail;
	}

	private int findPositionAtWidth(final Graphics graphics, final int positionWidth) {
		if (positionWidth >= width) {
			return text.length();
		}

		final float splittingRatio = (float) positionWidth / width;
		int begin = 0;
		int end = text.length();
		int pivot = Math.round(splittingRatio * text.length());
		while (begin < end - 1) {
			final int stringWidth = graphics.stringWidth(text.substring(0, pivot));
			if (stringWidth > positionWidth) {
				end = pivot;
			} else if (stringWidth < positionWidth) {
				begin = pivot;
			} else {
				return pivot;
			}
			pivot = (begin + end) / 2;
		}
		return pivot;
	}

}
