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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class NodeReference implements IChildBox, IDecoratorBox<IChildBox>, IContentBox {

	private int top;
	private int left;
	private int width;
	private int height;

	private IChildBox component;

	private INode node;

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
	public void setComponent(final IChildBox component) {
		this.component = component;
	}

	public void setNode(final INode node) {
		this.node = node;
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
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
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
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		return new Rectangle(left, top, width, height);
	}

}
