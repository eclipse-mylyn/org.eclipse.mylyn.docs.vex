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
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IPosition;

/**
 * @author Florian Thienel
 */
public class TextContent extends BaseBox implements IInlineBox, IContentBox {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;

	private IContent content;
	private IPosition startPosition;
	private IPosition endPosition;

	private FontSpec fontSpec;

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

	public String getText() {
		return content.getText(new ContentRange(startPosition.getOffset(), endPosition.getOffset()));
	}

	public void setContent(final IContent content, final ContentRange range) {
		this.content = content;
		startPosition = content.createPosition(range.getStartOffset());
		endPosition = content.createPosition(range.getEndOffset());
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
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void layout(final Graphics graphics) {
		if (layoutValid) {
			return;
		}

		applyFont(graphics);
		width = graphics.stringWidth(getText());

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		height = fontMetrics.getHeight();
		baseline = fontMetrics.getAscent() + fontMetrics.getLeading();

		layoutValid = true;
	}

	@Override
	public void paint(final Graphics graphics) {
		applyFont(graphics);
		graphics.drawString(getText(), 0, 0);
	}

	private void applyFont(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof TextContent)) {
			return false;
		}
		if (!isAdjacent((TextContent) other)) {
			return false;
		}
		if (!hasEqualFont((TextContent) other)) {
			return false;
		}

		return true;
	}

	private boolean isAdjacent(final TextContent other) {
		if (content != other.content) {
			return false;
		}
		if (endPosition.getOffset() != other.startPosition.getOffset() - 1) {
			return false;
		}
		return true;
	}

	private boolean hasEqualFont(final TextContent other) {
		if (fontSpec != null && !fontSpec.equals(other.fontSpec)) {
			return false;
		}
		if (fontSpec == null && other.fontSpec != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final TextContent otherText = (TextContent) other;

		content.removePosition(endPosition);
		content.removePosition(otherText.startPosition);
		endPosition = otherText.endPosition;
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
		splitter.setContent(content, startPosition.getOffset(), endPosition.getOffset());
		final int splittingPosition = splitter.findSplittingPositionBefore(graphics, headWidth, width, force);

		final TextContent tail = createTail(splittingPosition);
		tail.layout(graphics);
		removeTail(tail);

		return tail;
	}

	private TextContent createTail(final int splittingPosition) {
		final TextContent tail = new TextContent();
		tail.setContent(content, new ContentRange(startPosition.getOffset() + splittingPosition, endPosition.getOffset()));
		tail.setFont(fontSpec);
		tail.setParent(parent);
		return tail;
	}

	private void removeTail(final TextContent tail) {
		final int headLength = endPosition.getOffset() - startPosition.getOffset() - (tail.endPosition.getOffset() - tail.startPosition.getOffset());
		content.removePosition(endPosition);
		endPosition = content.createPosition(startPosition.getOffset() + headLength - 1);
		width -= tail.width;
	}

	@Override
	public int getStartOffset() {
		return startPosition.getOffset();
	}

	@Override
	public int getEndOffset() {
		return endPosition.getOffset();
	}

	@Override
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		if (startPosition.getOffset() > offset || endPosition.getOffset() < offset) {
			return Rectangle.NULL;
		}

		applyFont(graphics);
		final char c = content.charAt(offset);
		final String head = content.subSequence(startPosition.getOffset(), offset).toString();
		final int left = graphics.stringWidth(head);
		final int charWidth = graphics.stringWidth(Character.toString(c));
		return new Rectangle(left, 0, charWidth, height);
	}

	@Override
	public int getOffsetForCoordinates(final Graphics graphics, final int x, final int y) {
		applyFont(graphics);
		splitter.setContent(content, startPosition.getOffset(), endPosition.getOffset());
		final int offset = getStartOffset() + splitter.findPositionBefore(graphics, x, width);
		final Rectangle area = getPositionArea(graphics, offset);
		final int halfWidth = area.getWidth() / 2 + 1;
		if (x < area.getX() + halfWidth) {
			return offset;
		} else {
			return Math.min(offset + 1, getEndOffset());
		}
	}

	@Override
	public String toString() {
		return "TextContent{ x: " + left + ", y: " + top + ", width: " + width + ", height: " + height + ", startOffset: " + startPosition + ", endOffset: " + endPosition + " }";
	}
}
