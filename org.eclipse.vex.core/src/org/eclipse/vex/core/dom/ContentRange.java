/*******************************************************************************
 * Copyright (c) 2012,2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.dom;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;

/**
 * An immutable representation of a range within <code>IContent</code>.
 * 
 * @see IContent
 * @author Florian Thienel
 */
public class ContentRange {

	public static final ContentRange NULL = new ContentRange(-1, -1);
	public static final ContentRange ALL = new ContentRange(0, Integer.MAX_VALUE);

	private final int startOffset;
	private final int endOffset;

	/**
	 * @param startOffset
	 *            the start offset of this range
	 * @param endOffset
	 *            the end offset of this range
	 */
	public ContentRange(final int startOffset, final int endOffset) {
		Assert.isTrue(startOffset <= endOffset, MessageFormat.format("startOffset {0} must not be greater than endOffset {1}", startOffset, endOffset));
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	/**
	 * The length is always >= 1, since a range includes all characters from its start offset to its end offset.
	 * 
	 * @return the length of this range
	 */
	public int length() {
		return endOffset - startOffset + 1;
	}

	/**
	 * Indicate whether this range contains the given range.
	 * 
	 * @return true if this range contains the given range
	 */
	public boolean contains(final ContentRange other) {
		return startOffset <= other.startOffset && endOffset >= other.endOffset;
	}

	/**
	 * Indicate whether this range contains the given offset.
	 * 
	 * @return true if this range contains the given offset
	 */
	public boolean contains(final int offset) {
		return startOffset <= offset && offset <= endOffset;
	}

	/**
	 * Indicate whether this range intersects with the given range. Intersection is weaker than containment: one range
	 * may contain only a part of the other range.
	 * 
	 * @return true if this range intersects with the given range
	 */
	public boolean intersects(final ContentRange other) {
		return startOffset <= other.endOffset && endOffset >= other.startOffset;
	}

	/**
	 * @return the intersection of this and the given range
	 */
	public ContentRange intersection(final ContentRange other) {
		return new ContentRange(Math.max(other.getStartOffset(), startOffset), Math.min(endOffset, other.getEndOffset()));
	}

	/**
	 * The union of this and the given range may also include characters between both ranges, if they do not intersect.
	 * 
	 * @return the union of this and the given range
	 */
	public ContentRange union(final ContentRange other) {
		return new ContentRange(Math.min(startOffset, other.startOffset), Math.min(endOffset, other.endOffset));
	}

	/**
	 * Move this range by the given distance. Since ContentRange is immutable, a new moved range is returned.
	 * 
	 * @return the moved range
	 */
	public ContentRange moveBy(final int distance) {
		return resizeBy(distance, distance);
	}

	/**
	 * Resize this range by the given delta. Since ContentRange is immutable, a new resized range is returned.
	 * 
	 * @return the resized range
	 */
	public ContentRange resizeBy(final int deltaStart, final int deltaEnd) {
		return new ContentRange(startOffset + deltaStart, endOffset + deltaEnd);
	}

	@Override
	public String toString() {
		return "ContentRange[" + startOffset + ", " + endOffset + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endOffset;
		result = prime * result + startOffset;
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
		final ContentRange other = (ContentRange) obj;
		if (endOffset != other.endOffset) {
			return false;
		}
		if (startOffset != other.startOffset) {
			return false;
		}
		return true;
	}

}
