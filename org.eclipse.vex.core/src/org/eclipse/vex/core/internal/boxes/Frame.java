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
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class Frame extends BaseBox implements IStructuralBox, IDecoratorBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private Margin margin = Margin.NULL;
	private Border border = Border.NULL;
	private Padding padding = Padding.NULL;

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
		height = margin.top + border.top + padding.top;
		layoutComponent(graphics);
		height += margin.bottom + border.bottom + padding.bottom;
	}

	private void layoutComponent(final Graphics graphics) {
		if (component == null) {
			return;
		}

		final int componentLeft = margin.left + border.left + padding.left;
		final int componentRight = padding.right + border.right + margin.right;
		final int componentWidth = width - (componentLeft + componentRight);
		component.setPosition(height, componentLeft);
		component.setWidth(componentWidth);
		component.layout(graphics);
		height += component.getHeight();
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;

		height = margin.top + border.top + padding.top;
		height += component.getHeight();
		height += margin.bottom + border.bottom + padding.bottom;

		return oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		drawBorder(graphics);
		paintComponent(graphics);
	}

	private void drawBorder(final Graphics graphics) {
		final ColorResource colorResource = graphics.getColor(Color.BLACK); // TODO store border color
		graphics.setColor(colorResource);

		drawBorderLine(graphics, border.top, margin.top, margin.left - border.left / 2, margin.top, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.left, margin.top - border.top / 2, margin.left, height - margin.bottom + border.bottom / 2, margin.left);
		drawBorderLine(graphics, border.bottom, height - margin.bottom, margin.left - border.left / 2, height - margin.bottom, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.right, margin.top - border.top / 2, width - margin.right, height - margin.bottom + border.bottom / 2, width - margin.right);
	}

	private void drawBorderLine(final Graphics graphics, final int lineWidth, final int top, final int left, final int bottom, final int right) {
		if (lineWidth <= 0) {
			return;
		}
		graphics.setLineWidth(lineWidth);
		graphics.drawLine(left, top, right, bottom);
	}

	private void paintComponent(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}
}
