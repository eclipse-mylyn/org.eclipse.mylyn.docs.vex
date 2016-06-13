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
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class StructuralFrame extends BaseBox implements IStructuralBox, IDecoratorBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private Margin margin = Margin.NULL;
	private Border border = Border.NULL;
	private Padding padding = Padding.NULL;
	private Color backgroundColor = null;

	private IStructuralBox component;

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

	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = Math.max(0, width);
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
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

	public Margin getMargin() {
		return margin;
	}

	public void setMargin(final Margin margin) {
		this.margin = margin;
	}

	public Border getBorder() {
		return border;
	}

	public void setBorder(final Border border) {
		this.border = border;
	}

	public Padding getPadding() {
		return padding;
	}

	public void setPadding(final Padding padding) {
		this.padding = padding;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setComponent(final IStructuralBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IStructuralBox getComponent() {
		return component;
	}

	@Override
	public void layout(final Graphics graphics) {
		if (component == null) {
			return;
		}

		layoutComponent(graphics);

		height = component.getHeight();
		height += topFrame(component.getHeight());
		height += bottomFrame(component.getHeight());
	}

	private void layoutComponent(final Graphics graphics) {
		final int componentWidth = width - (leftFrame() + rightFrame());
		component.setWidth(componentWidth);
		component.layout(graphics);
		component.setPosition(topFrame(component.getHeight()), leftFrame());
	}

	private int topFrame(final int componentHeight) {
		return margin.top.get(componentHeight) + border.top.width + padding.top.get(componentHeight);
	}

	private int leftFrame() {
		return margin.left.get(width) + border.left.width + padding.left.get(width);
	}

	private int bottomFrame(final int componentHeight) {
		return margin.bottom.get(componentHeight) + border.bottom.width + padding.bottom.get(componentHeight);
	}

	private int rightFrame() {
		return margin.right.get(width) + border.right.width + padding.right.get(width);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;

		height = component.getHeight();
		height += topFrame(component.getHeight());
		height += bottomFrame(component.getHeight());

		return oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		drawBackground(graphics);
		drawBorder(graphics);
		paintComponent(graphics);
	}

	private void drawBackground(final Graphics graphics) {
		if (backgroundColor == null) {
			return;
		}

		graphics.setBackground(graphics.getColor(backgroundColor));
		graphics.fillRect(0, 0, width, height);
	}

	private void drawBorder(final Graphics graphics) {
		final int rectTop = margin.top.get(component.getHeight()) + border.top.width / 2;
		final int rectLeft = margin.left.get(width) + border.left.width / 2;
		final int rectBottom = height - margin.bottom.get(component.getHeight()) - border.bottom.width / 2;
		final int rectRight = width - margin.right.get(width) - border.right.width / 2;

		drawBorderLine(graphics, border.top, rectTop, rectLeft - border.left.width / 2, rectTop, rectRight + border.right.width / 2);
		drawBorderLine(graphics, border.left, rectTop - border.top.width / 2, rectLeft, rectBottom + border.bottom.width / 2, rectLeft);
		drawBorderLine(graphics, border.bottom, rectBottom, rectLeft - border.left.width / 2, rectBottom, rectRight + border.right.width / 2);
		drawBorderLine(graphics, border.right, rectTop - border.top.width / 2, rectRight, rectBottom + border.bottom.width / 2, rectRight);
	}

	private void drawBorderLine(final Graphics graphics, final BorderLine borderLine, final int top, final int left, final int bottom, final int right) {
		if (borderLine.width <= 0) {
			return;
		}
		graphics.setLineStyle(borderLine.style);
		graphics.setLineWidth(borderLine.width);
		graphics.setColor(graphics.getColor(borderLine.color));
		graphics.drawLine(left, top, right, bottom);
	}

	private void paintComponent(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}
}
