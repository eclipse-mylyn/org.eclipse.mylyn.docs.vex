/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import java.net.URL;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Length;
import org.eclipse.vex.core.internal.core.Point;
import org.eclipse.vex.core.internal.core.Rectangle;

public class Image extends BaseBox implements IInlineBox {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private LineWrappingRule lineWrappingAtStart;
	private LineWrappingRule lineWrappingAtEnd;

	private URL imageUrl;
	private Length preferredWidth;
	private Length preferredHeight;

	private org.eclipse.vex.core.internal.core.Image image; // TODO use a cache for the actual image data

	private boolean layoutValid;

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
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	@Override
	public int getBaseline() {
		return height;
	}

	@Override
	public int getInvisibleGapAtStart(final Graphics graphics) {
		return 0;
	}

	@Override
	public int getInvisibleGapAtEnd(final Graphics graphics) {
		return 0;
	}

	@Override
	public LineWrappingRule getLineWrappingAtStart() {
		return lineWrappingAtStart;
	}

	public void setLineWrappingAtStart(final LineWrappingRule wrappingRule) {
		lineWrappingAtStart = wrappingRule;
	}

	@Override
	public LineWrappingRule getLineWrappingAtEnd() {
		return lineWrappingAtEnd;
	}

	public void setLineWrappingAtEnd(final LineWrappingRule wrappingRule) {
		lineWrappingAtEnd = wrappingRule;
	}

	@Override
	public boolean requiresSplitForLineWrapping() {
		return lineWrappingAtStart == LineWrappingRule.REQUIRED || lineWrappingAtEnd == LineWrappingRule.REQUIRED;
	}

	public URL getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(final URL imageUrl) {
		this.imageUrl = imageUrl;
		layoutValid = false;
	}

	public Length getPreferredWidth() {
		return preferredWidth;
	}

	public void setPreferredWidth(final Length preferredWidth) {
		this.preferredWidth = preferredWidth;
		layoutValid = false;
	}

	public Length getPreferredHeight() {
		return preferredHeight;
	}

	public void setPreferredHeight(final Length preferredHeight) {
		this.preferredHeight = preferredHeight;
		layoutValid = false;
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
		if (layoutValid) {
			return;
		}

		image = graphics.getImage(imageUrl);
		final Point dimensions = calculateActualDimensions();

		width = dimensions.getX();
		height = dimensions.getY();

		layoutValid = true;
	}

	private Point calculateActualDimensions() {
		final int width = preferredWidth == null ? 0 : preferredWidth.get(image.getWidth());
		final int height = preferredHeight == null ? 0 : preferredHeight.get(image.getHeight());
		if (width != 0 && height != 0) {
			return new Point(width, height);
		}
		if (width == 0 && height != 0) {
			return new Point(scale(image.getWidth(), image.getHeight(), height), height);
		}
		if (width != 0 && height == 0) {
			return new Point(width, scale(image.getHeight(), image.getWidth(), width));
		}
		return new Point(image.getWidth(), image.getHeight());
	}

	private static int scale(final int opposite, final int current, final int scaled) {
		return Math.round(1f * scaled / current * opposite);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		final int oldWidth = width;

		layout(graphics);

		return oldHeight != height || oldWidth != width;
	}

	@Override
	public void paint(final Graphics graphics) {
		graphics.drawImage(image, 0, 0, width, height);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		return false;
	}

	@Override
	public boolean join(final IInlineBox other) {
		return false;
	}

	@Override
	public boolean canSplit() {
		return false;
	}

	@Override
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		throw new UnsupportedOperationException("Image cannot be split!");
	}

}
