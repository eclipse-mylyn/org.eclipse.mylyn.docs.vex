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

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.NodeGraphics;
import org.eclipse.vex.core.internal.core.Point;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class NodeTag extends SimpleInlineBox {

	private int width;
	private int height;
	private int baseline;

	private Color color;

	private INode node;

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

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setNode(final INode node) {
		this.node = node;
	}

	public INode getNode() {
		return node;
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
		final Point tagSize = NodeGraphics.getTagSize(graphics, node);
		width = tagSize.getX();
		height = tagSize.getY();
		baseline = NodeGraphics.getTagBaseline(graphics);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldWidth = width;
		final int oldHeight = height;
		layout(graphics);
		return oldWidth != width || oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		graphics.setForeground(graphics.getColor(color));
		NodeGraphics.drawTag(graphics, node, 0, 0, false, false, true);
	}
}
