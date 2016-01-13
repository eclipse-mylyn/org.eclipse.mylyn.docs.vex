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
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.LineStyle;
import org.eclipse.vex.core.internal.core.NodeGraphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class StructuralNodeReference extends BaseBox implements IStructuralBox, IDecoratorBox<IStructuralBox>, IContentBox {

	private static final float HIGHLIGHT_LIGHTEN_AMOUNT = 0.6f;
	private static final int HIGHLIGHT_BORDER_WIDTH = 4;

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private IStructuralBox component;

	private INode node;
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
	public void setWidth(final int width) {
		this.width = Math.max(0, width);
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
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void setComponent(final IStructuralBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IStructuralBox getComponent() {
		return component;
	}

	public void setNode(final INode node) {
		this.node = node;
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
		component.setWidth(width);
		component.layout(graphics);
		height = component.getHeight();
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		height = component.getHeight();
		return oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}

	public void highlight(final Graphics graphics, final Color foreground, final Color background) {
		final Color lightBackground = background.lighten(HIGHLIGHT_LIGHTEN_AMOUNT);
		fillBackground(graphics, lightBackground);
		drawBorder(graphics, background);

		accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final StructuralNodeReference box) {
				if (box != StructuralNodeReference.this) {
					box.highlightInside(graphics, foreground, lightBackground);
				}
				return super.visit(box);
			}

			@Override
			public Object visit(final InlineNodeReference box) {
				box.highlightInside(graphics, foreground, lightBackground);
				return super.visit(box);
			}

			@Override
			public Object visit(final TextContent box) {
				box.highlight(graphics, foreground, lightBackground);
				return super.visit(box);
			}

			@Override
			public Object visit(final NodeEndOffsetPlaceholder box) {
				box.highlight(graphics, foreground, lightBackground);
				return super.visit(box);
			}

			@Override
			public Object visit(final NodeTag box) {
				paintBox(graphics, box);
				return super.visit(box);
			}

			@Override
			public Object visit(final Square box) {
				paintBox(graphics, box);
				return super.visit(box);
			}

			@Override
			public Object visit(final StaticText box) {
				paintBox(graphics, box);
				return super.visit(box);
			}

			private void paintBox(final Graphics graphics, final IBox box) {
				graphics.moveOrigin(box.getAbsoluteLeft(), box.getAbsoluteTop());
				box.paint(graphics);
				graphics.moveOrigin(-box.getAbsoluteLeft(), -box.getAbsoluteTop());
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
		graphics.setLineStyle(LineStyle.SOLID);
		graphics.setLineWidth(HIGHLIGHT_BORDER_WIDTH);
		graphics.drawRect(getAbsoluteLeft(), getAbsoluteTop(), width, height);
	}

	private void drawTag(final Graphics graphics, final Color foreground, final Color background) {
		graphics.setForeground(graphics.getColor(foreground));
		graphics.setBackground(graphics.getColor(background));
		NodeGraphics.drawTag(graphics, node, getAbsoluteLeft() + width / 2, getAbsoluteTop() + height / 2, true, true, false);
	}

	public void highlightInside(final Graphics graphics, final Color foreground, final Color background) {
		fillBackground(graphics, background);
	}

	@Override
	public IContent getContent() {
		return node.getContent();
	}

	@Override
	public int getStartOffset() {
		if (node == null) {
			return 0;
		}
		return node.getStartOffset();
	}

	@Override
	public int getEndOffset() {
		if (node == null) {
			return 0;
		}
		return node.getEndOffset();
	}

	@Override
	public ContentRange getRange() {
		if (node == null) {
			return ContentRange.NULL;
		}
		return node.getRange();
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

		final int half = height / 2;
		if (y < half) {
			return getStartOffset();
		} else {
			return getEndOffset();
		}
	}

	@Override
	public boolean isEmpty() {
		return getEndOffset() - getStartOffset() <= 1;
	}

	public boolean isAtStart(final int offset) {
		return getStartOffset() == offset;
	}

	public boolean isAtEnd(final int offset) {
		return getEndOffset() == offset;
	}

	@Override
	public String toString() {
		String result = "StructuralNodeReference{ ";
		result += "x: " + left + ", y: " + top + ", width: " + width + ", height: " + height;
		if (node != null) {
			result += ", startOffset: " + node.getStartOffset() + ", endOffset: " + node.getEndOffset();
		}
		result += " }";
		return result;
	}
}
