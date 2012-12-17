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

/**
 * Represents a range of integers. Zero-length ranges (i.e. ranges where start == end) are permitted. This class is
 * immutable.
 */
public class VerticalRange {

	/**
	 * Class constuctor.
	 * 
	 * @param start
	 *            Start of the range.
	 * @param end
	 *            End of the range. Must be >= start.
	 */
	public VerticalRange(final int start, final int end) {
		if (start > end) {
			throw new IllegalArgumentException("start (" + start + ") is greater than end (" + end + ")");
		}
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
	 * Returns true if this range intersects the given range, even if the result would be an empty range.
	 * 
	 * @param range
	 *            Range with which to intersect.
	 */
	public boolean intersects(final VerticalRange range) {
		return start <= range.end && end >= range.start;
	}

	/**
	 * Returns true if start and end are equal.
	 */
	public boolean isEmpty() {
		return start == end;
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

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("IntRange(");
		sb.append(start);
		sb.append(",");
		sb.append(end);
		sb.append(")");
		return sb.toString();
	}

	// ============================================================= PRIVATE

	private final int start;
	private final int end;
}
