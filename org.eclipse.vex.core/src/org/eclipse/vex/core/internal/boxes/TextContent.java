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

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.core.Color;
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
	private LineWrappingRule lineWrappingAtStart;
	private LineWrappingRule lineWrappingAtEnd;

	private IContent content;
	private IPosition startPosition;
	private IPosition endPosition;

	private FontSpec fontSpec;
	private Color color;

	private final CharSequenceSplitter splitter = new CharSequenceSplitter();

	private boolean layoutValid;
	private int layoutStartOffset;
	private int layoutEndOffset;

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

	private static String renderText(final String rawText) {
		return rawText.replaceAll("\n", " ").replaceAll("\t", "    ");
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

	@Override
	public LineWrappingRule getLineWrappingAtStart() {
		return lineWrappingAtStart;
	}

	public void setLineWrappingAtStart(final LineWrappingRule wrappingRule) {
		lineWrappingAtStart = wrappingRule;
	}

	@Override
	public LineWrappingRule getLineWrappingAtEnd() {
		return lineWrappingAtEnd;
	}

	public void setLineWrappingAtEnd(final LineWrappingRule wrappingRule) {
		lineWrappingAtEnd = wrappingRule;
	}

	@Override
	public boolean requiresSplitForLineWrapping() {
		return lineWrappingAtStart == LineWrappingRule.REQUIRED || lineWrappingAtEnd == LineWrappingRule.REQUIRED;
	}

	private void invalidateLayout() {
		layoutValid = false;
	}

	private void validateLayout() {
		layoutStartOffset = startPosition.getOffset();
		layoutEndOffset = endPosition.getOffset();
		layoutValid = true;
	}

	private boolean isLayoutValid() {
		return layoutValid && layoutStartOffset == startPosition.getOffset() && layoutEndOffset == endPosition.getOffset();
	}

	public void setContent(final IContent content, final ContentRange range) {
		this.content = content;
		startPosition = content.createPosition(range.getStartOffset());
		endPosition = content.createPosition(range.getEndOffset());
		invalidateLayout();
	}

	public FontSpec getFont() {
		return fontSpec;
	}

	public void setFont(final FontSpec fontSpec) {
		this.fontSpec = fontSpec;
		invalidateLayout();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
		invalidateLayout();
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
		if (isLayoutValid()) {
			return;
		}

		applyFont(graphics);
		width = graphics.stringWidth(renderText(getText()));

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		height = fontMetrics.getHeight();
		baseline = fontMetrics.getAscent() + fontMetrics.getLeading();

		validateLayout();
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
		graphics.setForeground(graphics.getColor(color));
		graphics.drawString(renderText(getText()), 0, 0);
	}

	private void applyFont(final Graphics graphics) {
		final FontResource font = graphics.getFont(fontSpec);
		graphics.setCurrentFont(font);
	}

	@Override
	public void highlight(final Graphics graphics, final Color foreground, final Color background) {
		graphics.setForeground(graphics.getColor(foreground));
		graphics.setBackground(graphics.getColor(background));
		graphics.fillRect(getAbsoluteLeft(), getAbsoluteTop(), width, height);
		applyFont(graphics);
		graphics.drawString(renderText(getText()), getAbsoluteLeft(), getAbsoluteTop());
	}

	public void highlight(final Graphics graphics, final int startOffset, final int endOffset, final Color foreground, final Color background) {
		final int highlightStartOffset = Math.max(getStartOffset(), Math.min(startOffset, getEndOffset())) - getStartOffset();
		final int highlightEndOffset = Math.max(getStartOffset(), Math.min(endOffset, getEndOffset() + 1)) - getStartOffset();
		final String text = getText();
		final String highlightPrefix = renderText(text.substring(0, highlightStartOffset));
		final String highlightText = renderText(text.substring(highlightStartOffset, highlightEndOffset));

		applyFont(graphics);
		final int widthBefore = graphics.stringWidth(highlightPrefix);
		final int widthHighlight = graphics.stringWidth(highlightText);

		graphics.setForeground(graphics.getColor(foreground));
		graphics.setBackground(graphics.getColor(background));
		graphics.fillRect(getAbsoluteLeft() + widthBefore, getAbsoluteTop(), widthHighlight, height);
		graphics.drawString(highlightText, getAbsoluteLeft() + widthBefore, getAbsoluteTop());
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof TextContent)) {
			return false;
		}
		if (!isAdjacent((TextContent) other)) {
			return false;
		}
		if (!lineSplittingRulesAllowJoining((TextContent) other)) {
			return false;
		}
		if (!hasEqualFont((TextContent) other)) {
			return false;
		}
		if (!hasEqualColor((TextContent) other)) {
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

	private boolean lineSplittingRulesAllowJoining(final TextContent other) {
		if (lineWrappingAtEnd == LineWrappingRule.REQUIRED) {
			return false;
		}
		if (other.lineWrappingAtStart == LineWrappingRule.REQUIRED) {
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

	private boolean hasEqualColor(final TextContent other) {
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
		final TextContent otherText = (TextContent) other;

		content.removePosition(endPosition);
		content.removePosition(otherText.startPosition);
		endPosition = otherText.endPosition;
		width += otherText.width;

		lineWrappingAtEnd = otherText.lineWrappingAtEnd;

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
		final int splittingOffset = startPosition.getOffset() + splittingPosition;
		if (splittingOffset > endPosition.getOffset()) {
			return new TextContent();
		}

		final TextContent tail = createTail(splittingOffset);
		tail.layout(graphics);
		removeTail(tail);

		adjustSplittingRules(tail);

		return tail;
	}

	private TextContent createTail(final int splittingOffset) {
		final TextContent tail = new TextContent();
		tail.setContent(content, new ContentRange(splittingOffset, endPosition.getOffset()));
		tail.setFont(fontSpec);
		tail.setColor(color);
		tail.setParent(parent);
		return tail;
	}

	private void removeTail(final TextContent tail) {
		final int headLength = endPosition.getOffset() - startPosition.getOffset() - (tail.endPosition.getOffset() - tail.startPosition.getOffset());
		content.removePosition(endPosition);
		endPosition = content.createPosition(startPosition.getOffset() + headLength - 1);
		width -= tail.width;
	}

	private void adjustSplittingRules(final TextContent tail) {
		if (width <= 0) {
			tail.setLineWrappingAtStart(lineWrappingAtStart);
		}
		if (tail.getWidth() > 0) {
			tail.setLineWrappingAtEnd(lineWrappingAtEnd);
			lineWrappingAtEnd = LineWrappingRule.ALLOWED;
		}
	}

	@Override
	public IContent getContent() {
		return content;
	}

	@Override
	public int getStartOffset() {
		return startPosition.getOffset();
	}

	public void setStartOffset(final int startOffset) {
		Assert.isTrue(endPosition == null || startOffset <= endPosition.getOffset(), "startPosition > endPosition");
		content.removePosition(startPosition);
		startPosition = content.createPosition(startOffset);
		invalidateLayout();
	}

	@Override
	public int getEndOffset() {
		return endPosition.getOffset();
	}

	public void setEndOffset(final int endOffset) {
		Assert.isTrue(startPosition == null || endOffset >= startPosition.getOffset(), "endPosition < startPosition");
		content.removePosition(endPosition);
		endPosition = content.createPosition(endOffset);
		invalidateLayout();
	}

	@Override
	public ContentRange getRange() {
		return new ContentRange(getStartOffset(), getEndOffset());
	}

	@Override
	public boolean isEmpty() {
		return getEndOffset() - getStartOffset() <= 0;
	}

	public boolean isAtStart(final int offset) {
		return getStartOffset() == offset;
	}

	public boolean isAtEnd(final int offset) {
		return getEndOffset() == offset;
	}

	@Override
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		if (startPosition.getOffset() > offset || endPosition.getOffset() < offset) {
			return Rectangle.NULL;
		}

		applyFont(graphics);
		final char c = content.charAt(offset);
		final String head = renderText(content.subSequence(startPosition.getOffset(), offset).toString());
		final int left = graphics.stringWidth(head);
		final int charWidth = graphics.stringWidth(renderText(Character.toString(c)));
		return new Rectangle(left, 0, charWidth, height);
	}

	@Override
	public int getOffsetForCoordinates(final Graphics graphics, final int x, final int y) {
		if (x < 0) {
			return getStartOffset();
		}
		if (x > width) {
			return getEndOffset();
		}

		applyFont(graphics);
		final String text = getText();
		int i = 0;
		while (renderedWidth(graphics, text.substring(0, i)) < x && i < text.length()) {
			i += 1;
		}
		final int offset = Math.max(getStartOffset(), getStartOffset() + i - 1);

		final Rectangle area = getPositionArea(graphics, offset);
		final int halfWidth = area.getWidth() / 2 + 1;
		if (x < area.getX() + halfWidth) {
			return offset;
		} else {
			return Math.min(offset + 1, getEndOffset());
		}
	}

	private static int renderedWidth(final Graphics graphics, final String text) {
		return graphics.stringWidth(renderText(text));
	}

	@Override
	public String toString() {
		return "TextContent{ x: " + left + ", y: " + top + ", width: " + width + ", height: " + height + ", startOffset: " + startPosition + ", endOffset: " + endPosition + " }";
	}
}
