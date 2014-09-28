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

import java.util.ArrayList;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.RelocatedGraphics;

/**
 * This box arranges child boxes in one vertical column of given width. It has a margin, a border and padding, which
 * reduce the children's width. This box's height depends on the sum of the height of its children.
 *
 * @author Florian Thienel
 */
public class VerticalBlock implements IChildBox, IParentBox {

	private int top;
	private int left;
	private int width;
	private int height;
	private final ArrayList<IChildBox> children = new ArrayList<IChildBox>();
	private Margin margin = Margin.NULL;
	private Border border = Border.NULL;
	private Padding padding = Padding.NULL;

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
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void appendChild(final IChildBox child) {
		children.add(child);
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

	public void layout() {
		height = margin.top + border.top + padding.top;
		final int left = margin.left + border.left + padding.left;
		final int childrenWidth = width - (margin.left + margin.right + border.left + border.right + padding.left + padding.right);
		for (final IChildBox child : children) {
			child.setPosition(height, left);
			child.setWidth(childrenWidth);
			child.layout();
			height += child.getHeight();
		}
		height += margin.bottom + border.bottom + padding.bottom;
	}

	@Override
	public void paint(final Graphics graphics) {
		drawBorder(graphics);
		for (final IChildBox child : children) {
			final Graphics childGraphics = new RelocatedGraphics(graphics, child.getLeft(), child.getTop());
			child.paint(childGraphics);
		}
	}

	private void drawBorder(final Graphics graphics) {

		final ColorResource colorResource = graphics.createColor(Color.BLACK); // TODO store border color
		graphics.setColor(colorResource);

		drawBorderLine(graphics, border.top, margin.top, margin.left - border.left / 2, margin.top, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.left, margin.top - border.top / 2, margin.left, height - margin.bottom + border.bottom / 2, margin.left);
		drawBorderLine(graphics, border.bottom, height - margin.bottom, margin.left - border.left / 2, height - margin.bottom, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.right, margin.top - border.top / 2, width - margin.right, height - margin.bottom + border.bottom / 2, width - margin.right);

		colorResource.dispose();
	}

	private void drawBorderLine(final Graphics graphics, final int lineWidth, final int top, final int left, final int bottom, final int right) {
		if (lineWidth <= 0) {
			return;
		}
		graphics.setLineWidth(lineWidth);
		graphics.drawLine(left, top, right, bottom);
	}
}
