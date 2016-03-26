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

	private static final int MARGIN = 2;
	private static final float SIZE_RATIO = 0.5f;
	private static final float BASELINE_RATIO = 1.1f;

	private int width;
	private int height;
	private int baseline;

	private Kind kind;
	private INode node;
	private Color color;
	private boolean showText;
	private float fontSize;

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

	public void setFontSize(final float fontSize) {
		this.fontSize = fontSize;
	}

	public float getFontSize() {
		return fontSize;
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
		width = tagSize.getX() + MARGIN;
		height = tagSize.getY();
		if (kind == Kind.NODE || showText) {
			baseline = NodeGraphics.getTagBaseline(graphics);
		} else {
			baseline = Math.round(height * BASELINE_RATIO);
		}
	}

	private Point getTagSize(final Graphics graphics) {
		switch (kind) {
		case NODE:
			return NodeGraphics.getTagSize(graphics, getText());
		case START:
			if (showText) {
				return NodeGraphics.getStartTagSize(graphics, getText());
			} else {
				final int size = Math.round(fontSize * SIZE_RATIO);
				return new Point(size, size);
			}
		case END:
			if (showText) {
				return NodeGraphics.getEndTagSize(graphics, getText());
			} else {
				final int size = Math.round(fontSize * SIZE_RATIO);
				return new Point(size, size);
			}
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
		graphics.setColor(graphics.getColor(color));
		switch (kind) {
		case NODE:
			NodeGraphics.drawTag(graphics, getText(), MARGIN / 2, 0, false, false, true);
			break;
		case START:
			if (showText) {
				NodeGraphics.drawStartTag(graphics, getText(), MARGIN, 0, false, true);
			} else {
				NodeGraphics.drawSimpleStartTag(graphics, MARGIN, 0, height, false, true);
			}
			break;
		case END:
			if (showText) {
				NodeGraphics.drawEndTag(graphics, getText(), 0, 0, false, true);
			} else {
				NodeGraphics.drawSimpleEndTag(graphics, 0, 0, height, false, true);
			}
			break;
		default:
			throw new IllegalStateException("Unknown kind " + kind + " of NodeTag.");
		}
	}
}
