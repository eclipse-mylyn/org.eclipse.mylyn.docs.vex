/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;

/**
 * @author Florian Thienel
 */
public class Range {

	private final int startOffset;
	private final int endOffset;

	public Range(final int startOffset, final int endOffset) {
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

	public int length() {
		return endOffset - startOffset + 1;
	}

	public boolean contains(final Range other) {
		return startOffset <= other.startOffset && endOffset >= other.endOffset;
	}

	public boolean contains(final int offset) {
		return startOffset <= offset && offset <= endOffset;
	}

	public Range trimTo(final Range limit) {
		return new Range(Math.max(limit.getStartOffset(), startOffset), Math.min(endOffset, limit.getEndOffset()));
	}

	public Range moveBounds(final int deltaStart, final int deltaEnd) {
		return new Range(startOffset + deltaStart, endOffset + deltaEnd);
	}

	@Override
	public String toString() {
		return "Range[" + startOffset + ", " + endOffset + "]";
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
		final Range other = (Range) obj;
		if (endOffset != other.endOffset) {
			return false;
		}
		if (startOffset != other.startOffset) {
			return false;
		}
		return true;
	}

}
