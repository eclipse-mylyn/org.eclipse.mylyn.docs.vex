/*******************************************************************************
 * Copyright (c) 2004, 2016 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - hashCode, equals, toString
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

/**
 * Toolkit-independent point.
 */
public class Point {

	private final int x;
	private final int y;

	/**
	 * @param x
	 *            X-coordinate.
	 * @param y
	 *            Y-coordinate.
	 */
	public Point(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x-coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y-coordinate.
	 */
	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Point other = (Point) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}
}
