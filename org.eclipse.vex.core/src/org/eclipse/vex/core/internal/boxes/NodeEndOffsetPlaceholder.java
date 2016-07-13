/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class NodeEndOffsetPlaceholder extends BaseBox implements IInlineBox, IContentBox {

	private IBox parent;
	private int top;
	private int left;
	private final int width = 1;
	private int height;
	private int baseline;
	private int maxWidth;
	private LineWrappingRule lineWrappingAtStart;
	private LineWrappingRule lineWrappingAtEnd;

	private INode node;

	private FontSpec fontSpec;

	private boolean layoutValid = false;

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

	@Override
	public int getMaxWidth() {
		return maxWidth;
	}

	@Override
	public void setMaxWidth(final int maxWidth) {
		this.maxWidth = maxWidth;
	}

	@Override
	public int getInvisibleGapAtStart(final Graphics graphics) {
		return 0;
	}

	@Override
	public int getInvisibleGapAtEnd(final Graphics graphics) {
		return 0;
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

	public INode getNode() {
		return node;
	}

	public void setNode(final INode node) {
		this.node = node;
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

		graphics.setCurrentFont(graphics.getFont(fontSpec));
		final FontMetrics fontMetrics = graphics.getFontMetrics();
		height = fontMetrics.getHeight();
		baseline = fontMetrics.getAscent() + fontMetrics.getLeading();

		layoutValid = true;
	}

	@Override
	public Collection<IBox> reconcileLayout(final Graphics graphics) {
		if (layoutValid) {
			return NOTHING_INVALIDATED;
		}
		layout(graphics);
		return Collections.singleton(getParent());
	}

	@Override
	public void paint(final Graphics graphics) {
		// ignore, the box is not visible
	}

	@Override
	public void highlight(final Graphics graphics, final Color foreground, final Color background) {
		// ignore, the box is not visible
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
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		throw new UnsupportedOperationException("Splitting is not supported for InlineContentPlaceholder.");
	}

	@Override
	public IContent getContent() {
		return node.getContent();
	}

	@Override
	public int getStartOffset() {
		return node.getEndOffset();
	}

	@Override
	public int getEndOffset() {
		return node.getEndOffset();
	}

	@Override
	public ContentRange getRange() {
		return new ContentRange(getStartOffset(), getEndOffset());
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean isAtStart(final int offset) {
		return getStartOffset() == offset;
	}

	@Override
	public boolean isAtEnd(final int offset) {
		return getEndOffset() == offset;
	}

	@Override
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		if (getStartOffset() > offset || getEndOffset() < offset) {
			return Rectangle.NULL;
		}
		return new Rectangle(0, 0, width, height);
	}

	@Override
	public int getOffsetForCoordinates(final Graphics graphics, final int x, final int y) {
		if (x < 0) {
			return getStartOffset();
		}
		return getEndOffset();
	}

	@Override
	public String toString() {
		return "NodeEndOffsetPlaceholder [top=" + top + ", left=" + left + ", width=" + width + ", height=" + height + ", baseline=" + baseline + ", startPosition=" + getStartOffset()
				+ ", endPosition=" + getEndOffset() + "]";
	}
}
