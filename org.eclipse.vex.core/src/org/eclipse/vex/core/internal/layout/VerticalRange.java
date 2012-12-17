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
package org.eclipse.vex.core.internal.layout;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;

/**
 * Represents a vertical range used in the layouting algorithm. Zero-length ranges (i.e. ranges where start == end) are
 * permitted. This class is immutable.
 */
public class VerticalRange {

	private final int start;
	private final int end;

	/**
	 * Class constuctor.
	 * 
	 * @param start
	 *            Start of the range.
	 * @param end
	 *            End of the range. Must be >= start.
	 */
	public VerticalRange(final int start, final int end) {
		Assert.isTrue(start <= end, MessageFormat.format("start {0} must not be greater than end {1}", start, end));
		this.start = start;
		this.end = end;
	}

	/**
	 * Returns the start of the range.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Returns the end of the range.
	 */
	public int getEnd() {
		return end;
	}

	public int getHeight() {
		return end - start;
	}

	/**
	 * Returns true if start and end are equal.
	 */
	public boolean isEmpty() {
		return getHeight() == 0;
	}

	public boolean contains(final VerticalRange other) {
		return start <= other.start && end >= other.end;
	}

	public boolean contains(final int y) {
		return start <= y && y <= end;
	}

	/**
	 * Returns true if this range intersects the given range, even if the result would be an empty range.
	 * 
	 * @param range
	 *            Range with which to intersect.
	 */
	public boolean intersects(final VerticalRange range) {
		return start <= range.end && end >= range.start;
	}

	/**
	 * Returns the range that represents the intersection of this range and the given range. If the ranges do not
	 * intersect, returns null. May return an empty range.
	 * 
	 * @param range
	 *            Range with which to perform an intersection.
	 */
	public VerticalRange intersection(final VerticalRange range) {
		if (intersects(range)) {
			return new VerticalRange(Math.max(start, range.start), Math.min(end, range.end));
		} else {
			return null;
		}

	}

	/**
	 * Returns a range that is the union of this range and the given range. If the ranges are disjoint, the gap between
	 * the ranges is included in the result.
	 * 
	 * @param range
	 *            Rnage with which to perform the union
	 */
	public VerticalRange union(final VerticalRange range) {
		return new VerticalRange(Math.min(start, range.start), Math.min(end, range.end));
	}

	public VerticalRange moveBy(final int delta) {
		return resizeBy(delta, delta);
	}

	public VerticalRange resizeBy(final int deltaStart, final int deltaEnd) {
		return new VerticalRange(start + deltaStart, end + deltaEnd);
	}

	@Override
	public String toString() {
		return "VerticalRange[" + start + ", " + end + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
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
		if (end != other.end) {
			return false;
		}
		if (start != other.start) {
			return false;
		}
		return true;
	}

}
