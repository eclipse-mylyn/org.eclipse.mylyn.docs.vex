/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

/**
 * Toolkit-independent rectangle.
 */
public class Rectangle {

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	public Rectangle(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean intersects(final Rectangle rect) {
		return rect.x < x + width && rect.x + rect.width > x && rect.y < y + height && rect.y + rect.height > y;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(80);
		sb.append(Rectangle.class.getName());
		sb.append("[x=");
		sb.append(getX());
		sb.append(",y=");
		sb.append(getY());
		sb.append(",width=");
		sb.append(getWidth());
		sb.append(",height=");
		sb.append(getHeight());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * @return Returns the x.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return Returns the y.
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns a Rectangle that is the union of this rectangle with another.
	 *
	 * @param rect
	 *            Rectangle with which to union this one.
	 */
	public Rectangle union(final Rectangle rect) {
		final int left = Math.min(x, rect.x);
		final int top = Math.min(y, rect.y);
		final int right = Math.max(x + width, rect.x + rect.width);
		final int bottom = Math.max(y + height, rect.y + rect.height);
		return new Rectangle(left, top, right - left, bottom - top);
	}
}
