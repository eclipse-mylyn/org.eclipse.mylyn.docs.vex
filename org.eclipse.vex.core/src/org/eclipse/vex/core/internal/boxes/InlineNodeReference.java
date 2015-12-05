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

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.NodeTag;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IPosition;

/**
 * @author Florian Thienel
 */
public class InlineNodeReference extends BaseBox implements IInlineBox, IDecoratorBox<IInlineBox>, IContentBox {

	private static final float HIGHLIGHT_LIGHTEN_AMOUNT = 0.6f;
	private static final int HIGHLIGHT_BORDER_WIDTH = 4;

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private int baseline;

	private IInlineBox component;

	private INode node;
	private IPosition startPosition;
	private IPosition endPosition;
	private boolean canContainText;

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
	public int getBaseline() {
		return baseline;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
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
	public void setComponent(final IInlineBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IInlineBox getComponent() {
		return component;
	}

	public void setNode(final INode node) {
		setSubrange(node, node.getStartOffset(), node.getEndOffset());
	}

	private void setSubrange(final INode node, final int startOffset, final int endOffset) {
		this.node = node;
		startPosition = node.getContent().createPosition(startOffset);
		endPosition = node.getContent().createPosition(endOffset);
	}

	public INode getNode() {
		return node;
	}

	public void setCanContainText(final boolean canContainText) {
		this.canContainText = canContainText;
	}

	public boolean canContainText() {
		return canContainText;
	}

	@Override
	public void layout(final Graphics graphics) {
		if (component == null) {
			return;
		}
		component.setPosition(0, 0);
		component.layout(graphics);
		width = component.getWidth();
		height = component.getHeight();
		baseline = component.getBaseline();
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldWidth = width;
		final int oldHeight = height;
		final int oldBaseline = baseline;
		width = component.getWidth();
		height = component.getHeight();
		baseline = component.getBaseline();

		layout(graphics);

		return oldWidth != width || oldHeight != height || oldBaseline != baseline;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}

	@Override
	public void highlight(final Graphics graphics, final Color foreground, final Color background) {
		final Color lightBackground = background.lighten(HIGHLIGHT_LIGHTEN_AMOUNT);
		fillBackground(graphics, lightBackground);
		drawBorder(graphics, background);

		accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final InlineNodeReference box) {
				if (box != InlineNodeReference.this) {
					box.highlightInside(graphics, foreground, lightBackground);
				}
				return super.visit(box);
			}

			@Override
			public Object visit(final TextContent box) {
				box.highlight(graphics, foreground, lightBackground);
				return super.visit(box);
			}
		});

		drawTag(graphics, foreground, background);
	}

	private void fillBackground(final Graphics graphics, final Color color) {
		graphics.setForeground(graphics.getColor(color));
		graphics.setBackground(graphics.getColor(color));
		graphics.fillRect(getAbsoluteLeft(), getAbsoluteTop(), width, height);
	}

	private void drawBorder(final Graphics graphics, final Color color) {
		graphics.setForeground(graphics.getColor(color));
		graphics.setBackground(graphics.getColor(color));
		graphics.setLineWidth(HIGHLIGHT_BORDER_WIDTH);
		graphics.drawRect(getAbsoluteLeft(), getAbsoluteTop(), width, height);
	}

	private void drawTag(final Graphics graphics, final Color foreground, final Color background) {
		graphics.setForeground(graphics.getColor(foreground));
		graphics.setBackground(graphics.getColor(background));
		NodeTag.drawTag(graphics, node, getAbsoluteLeft() + width / 2, getAbsoluteTop() + height / 2, true, true);
	}

	public void highlightInside(final Graphics graphics, final Color foreground, final Color background) {
		fillBackground(graphics, background);
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
	public ContentRange getRange() {
		if (node == null) {
			return ContentRange.NULL;
		}
		return new ContentRange(getStartOffset(), getEndOffset());
	}

	@Override
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		return new Rectangle(0, 0, width, height);
	}

	@Override
	public int getOffsetForCoordinates(final Graphics graphics, final int x, final int y) {
		if (isEmpty()) {
			return getEndOffset();
		}
		final int half = width / 2;
		if (x < half) {
			return getStartOffset();
		} else {
			return getEndOffset();
		}
	}

	@Override
	public boolean isEmpty() {
		return getEndOffset() - getStartOffset() <= 1;
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
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof InlineNodeReference)) {
			return false;
		}
		final InlineNodeReference otherNodeReference = (InlineNodeReference) other;
		if (node != otherNodeReference.node) {
			return false;
		}
		if (endPosition.getOffset() != otherNodeReference.getStartOffset() - 1) {
			return false;
		}
		if (!component.canJoin(otherNodeReference.component)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final InlineNodeReference otherNodeReference = (InlineNodeReference) other;

		component.join(otherNodeReference.component);

		node.getContent().removePosition(endPosition);
		node.getContent().removePosition(otherNodeReference.startPosition);
		endPosition = otherNodeReference.endPosition;

		width = component.getWidth();
		height = component.getHeight();
		baseline = component.getBaseline();

		return true;
	}

	@Override
	public boolean canSplit() {
		if (component == null) {
			return false;
		}
		return component.canSplit();
	}

	@Override
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		final int firstChildOffset = findStartOffset(component);

		final IInlineBox tailComponent = component.splitTail(graphics, headWidth, force);

		final int firstTailOffset = findStartOffset(tailComponent);

		final int splitPosition;
		if (firstChildOffset == firstTailOffset) {
			splitPosition = startPosition.getOffset();
		} else {
			splitPosition = firstTailOffset;
		}
		Assert.isTrue(splitPosition >= getStartOffset(), MessageFormat.format("Splitposition {0} is invalid.", splitPosition));

		final InlineNodeReference tail = new InlineNodeReference();
		tail.setComponent(tailComponent);
		tail.setSubrange(node, splitPosition, getEndOffset());
		tail.setCanContainText(canContainText);
		tail.setParent(parent);
		tail.layout(graphics);

		node.getContent().removePosition(endPosition);
		endPosition = node.getContent().createPosition(splitPosition - 1);

		layout(graphics);

		return tail;
	}

	private int findStartOffset(final IBox startBox) {
		return startBox.accept(new DepthFirstBoxTraversal<Integer>(-1) {
			@Override
			public Integer visit(final InlineNodeReference box) {
				if (box == startBox) {
					return super.visit(box);
				}
				return box.getStartOffset();
			}

			@Override
			public Integer visit(final TextContent box) {
				return box.getStartOffset();
			}
		});
	}

	@Override
	public String toString() {
		return "InlineNodeReference [top=" + top + ", left=" + left + ", width=" + width + ", height=" + height + ", baseline=" + baseline + ", startPosition=" + startPosition
				+ ", endPosition=" + endPosition + ", canContainText=" + canContainText + "]";
	}
}
