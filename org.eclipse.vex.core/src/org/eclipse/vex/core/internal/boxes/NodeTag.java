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

	public static enum Kind {
		NODE, START, END;
	}

	private int width;
	private int height;
	private int baseline;

	private Kind kind;
	private INode node;
	private Color color;
	private boolean showText;

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

	public void setKind(final Kind kind) {
		this.kind = kind;
	}

	public void setNode(final INode node) {
		this.node = node;
	}

	public INode getNode() {
		return node;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setShowText(final boolean showText) {
		this.showText = showText;
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
		final Point tagSize = getTagSize(graphics);
		width = tagSize.getX();
		height = tagSize.getY();
		baseline = NodeGraphics.getTagBaseline(graphics);
	}

	private Point getTagSize(final Graphics graphics) {
		switch (kind) {
		case NODE:
			return NodeGraphics.getTagSize(graphics, getText());
		case START:
			return NodeGraphics.getStartTagSize(graphics, getText());
		case END:
			return NodeGraphics.getEndTagSize(graphics, getText());
		default:
			throw new IllegalStateException("Unknown kind " + kind + " of NodeTag.");
		}
	}

	private String getText() {
		if (!showText) {
			return "";
		}
		return NodeGraphics.getNodeName(node);
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
		switch (kind) {
		case NODE:
			NodeGraphics.drawTag(graphics, getText(), 0, 0, false, false, true);
			break;
		case START:
			NodeGraphics.drawStartTag(graphics, getText(), 0, 0, false, true);
			break;
		case END:
			NodeGraphics.drawEndTag(graphics, getText(), 0, 0, false, true);
			break;
		default:
			throw new IllegalStateException("Unknown kind " + kind + " of NodeTag.");
		}
	}
}
