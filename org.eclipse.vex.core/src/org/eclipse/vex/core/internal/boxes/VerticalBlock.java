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
		return 0;
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

}
