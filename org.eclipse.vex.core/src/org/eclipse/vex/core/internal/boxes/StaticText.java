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

import static org.eclipse.vex.core.internal.core.TextUtils.countWhitespaceAtEnd;
import static org.eclipse.vex.core.internal.core.TextUtils.countWhitespaceAtStart;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class StaticText extends BaseBox implements IInlineBox {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;

	private String text;
	private FontSpec fontSpec;
	private Color color;

	private final CharSequenceSplitter splitter = new CharSequenceSplitter();

	private boolean layoutValid;

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
	public int getHeight() {
		return height;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	@Override
	public int getBaseline() {
		return baseline;
	}

	public int getInvisibleGapAtStart(final Graphics graphics) {
		final String text = renderText(getText());
		final int whitespaceCount = countWhitespaceAtStart(text);
		return graphics.stringWidth(text.substring(0, whitespaceCount));
	}

	public int getInvisibleGapAtEnd(final Graphics graphics) {
		final String text = renderText(getText());
		final int whitespaceCount = countWhitespaceAtEnd(text);
		return graphics.stringWidth(text.substring(text.length() - whitespaceCount, text.length()));
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

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
		layoutValid = false;
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
		if (layoutValid) {
			return;
		}

		applyFont(graphics);
		width = graphics.stringWidth(renderText(getText()));

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		height = fontMetrics.getHeight();
		baseline = fontMetrics.getAscent() + fontMetrics.getLeading();

		layoutValid = true;
	}

	private static String renderText(final String rawText) {
		return rawText.replaceAll("\n", " ").replaceAll("\t", "    ");
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		final int oldWidth = width;
		final int oldBaseline = baseline;

		layout(graphics);

		return oldHeight != height || oldWidth != width || oldBaseline != baseline;
	}

	@Override
	public void paint(final Graphics graphics) {
		applyFont(graphics);
		graphics.setColor(graphics.getColor(color));
		graphics.drawString(renderText(getText()), 0, 0);
	}

	private void applyFont(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof StaticText)) {
			return false;
		}
		if (!hasEqualFont((StaticText) other)) {
			return false;
		}
		if (!hasEqualColor((StaticText) other)) {
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

	private boolean hasEqualColor(final StaticText other) {
		if (color != null && !color.equals(other.color)) {
			return false;
		}
		if (color == null && other.color != null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final StaticText otherText = (StaticText) other;

		setText(text + otherText.text);
		width += otherText.width;

		return true;
	}

	@Override
	public boolean canSplit() {
		return true;
	}

	@Override
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		applyFont(graphics);
		splitter.setContent(text);
		final int splittingPosition = splitter.findSplittingPositionBefore(graphics, headWidth, width, force);

		final StaticText tail = createTail(splittingPosition);
		tail.layout(graphics);
		removeTail(tail);

		return tail;
	}

	private StaticText createTail(final int splittingPosition) {
		final StaticText tail = new StaticText();
		if (splittingPosition < text.length()) {
			tail.setText(text.substring(Math.min(splittingPosition, text.length()), text.length()));
		} else {
			tail.setText("");
		}
		tail.setFont(fontSpec);
		tail.setColor(color);
		tail.setParent(parent);
		return tail;
	}

	private void removeTail(final StaticText tail) {
		final int headLength = text.length() - tail.text.length();
		text = text.substring(0, headLength);
		width -= tail.width;
	}

}
