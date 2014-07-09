/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - inroduce specific implementations for layout and content to separate both domains
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.text.MessageFormat;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * Represents a vertical range used in the layouting algorithm. Zero-length ranges (i.e. ranges where top == bottom) are
 * permitted. This class is immutable.
 */
public class VerticalRange {

	private final int top;
	private final int bottom;

	/**
	 * Create a new VerticalRange from top to bottom.
	 *
	 * @param top
	 *            top of the range
	 * @param bottom
	 *            bottom of the range, must be >= top
	 */
	public VerticalRange(final int top, final int bottom) {
		if (top > bottom) {
			throw new AssertionFailedException(MessageFormat.format("top {0} must not be above bottom {1}", top, bottom));
		}
		this.top = top;
		this.bottom = bottom;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getHeight() {
		return bottom - top;
	}

	/**
	 * @return true if top and bottom are equal.
	 */
	public boolean isEmpty() {
		return getHeight() == 0;
	}

	/**
	 * Indicates if this range fully contains the given range.
	 *
	 * @param other
	 *            the other range
	 * @return true if this range fully contains the other range
	 */
	public boolean contains(final VerticalRange other) {
		return top <= other.top && bottom >= other.bottom;
	}

	/**
	 * Indicates if this range contains the given y coordinate.
	 *
	 * @param y
	 *            the coordinate
	 * @return true if this range contains the y coordinate
	 */
	public boolean contains(final int y) {
		return top <= y && y <= bottom;
	}

	/**
	 * Indicates if this range intersects with the given range, even if the given range is only adjacent.
	 *
	 * @param other
	 *            the other range
	 * @return true if this range intersects with the other range
	 */
	public boolean intersects(final VerticalRange other) {
		return top <= other.bottom && bottom >= other.top;
	}

	/**
	 * Create a range that represents the intersection of this range and the given range. If the ranges do not
	 * intersect, returns null. May return an empty range if this and the other range are only adjacent.
	 *
	 * @param other
	 *            the other range
	 * @return the intersection of this range and the other range
	 */
	public VerticalRange intersection(final VerticalRange other) {
		if (intersects(other)) {
			return new VerticalRange(Math.max(top, other.top), Math.min(bottom, other.bottom));
		} else {
			return null;
		}

	}

	/**
	 * Create a range that is the union of this range and the given range. If the ranges are disjoint, the gap between
	 * the ranges is included in the result.
	 *
	 * @param other
	 *            the other range
	 * @return the union of this and the other range
	 */
	public VerticalRange union(final VerticalRange other) {
		return new VerticalRange(Math.min(top, other.top), Math.min(bottom, other.bottom));
	}

	/**
	 * Create a copy of this range moved by the given distance.
	 *
	 * @param distance
	 *            the distance to move
	 * @return the moved range
	 */
	public VerticalRange moveBy(final int distance) {
		return resizeBy(distance, distance);
	}

	/**
	 * Create a resized copy of this range.
	 *
	 * @param deltaTop
	 *            the amount by which the top of this range should be moved
	 * @param deltaBottom
	 *            the amount by which the bottom of this range should be moved
	 * @return the resized range
	 */
	public VerticalRange resizeBy(final int deltaTop, final int deltaBottom) {
		return new VerticalRange(top + deltaTop, bottom + deltaBottom);
	}

	@Override
	public String toString() {
		return "VerticalRange[" + top + ", " + bottom + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bottom;
		result = prime * result + top;
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
		final VerticalRange other = (VerticalRange) obj;
		if (bottom != other.bottom) {
			return false;
		}
		if (top != other.top) {
			return false;
		}
		return true;
	}

}
