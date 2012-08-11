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
 * Toolkit-independent point.
 */
public class Point {

	private final int x;
	private final int y;

	/**
	 * Class constructor.
	 * 
	 * @param x
	 *            X-coordinate.
	 * @param y
	 *            Y-coordinate.
	 */
	public Point(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(80);
		sb.append(Point.class.getName());
		sb.append("[x=");
		sb.append(getX());
		sb.append(",y=");
		sb.append(getY());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns the x-coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the y-coordinate.
	 */
	public int getY() {
		return y;
	}

}
