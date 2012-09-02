/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the <code>Content</code> interface that manages changes efficiently. Implements a buffer that keeps
 * its free space (the "gap") at the location of the last change. Insertions at the start of the gap require no other
 * chars to be moved so long as the insertion is smaller than the gap. Deletions that end of the gap are also very
 * efficent. Furthermore, changes near the gap require relatively few characters to be moved.
 */
public class GapContent implements Content {

	private static final char ELEMENT_MARKER = '\0';

	private char[] content;
	private int gapStart;
	private int gapEnd;
	private final Set<GapContentPosition> positions = new HashSet<GapContentPosition>();

	/**
	 * Class constructor.
	 * 
	 * @param initialCapacity
	 *            initial capacity of the content.
	 */
	public GapContent(final int initialCapacity) {
		assertPositive(initialCapacity);

		content = new char[initialCapacity];
		gapStart = 0;
		gapEnd = initialCapacity;
	}

	/**
	 * Creates a new Position object at the given initial offset.
	 * 
	 * @param offset
	 *            initial offset of the position
	 */
	public Position createPosition(final int offset) {

		assertOffset(offset, 0, getLength());

		final GapContentPosition position = new GapContentPosition(offset);
		positions.add(position);

		return position;
	}

	public void removePosition(final Position position) {
		if (positions.remove(position)) {
			/*
			 * This cast is save: if the position can be removed, this instance must have created it, hence it is a
			 * GapContentPosition.
			 */
			((GapContentPosition) position).invalidate();
		}
	}

	/**
	 * Insert a string into the content.
	 * 
	 * @param offset
	 *            Offset at which to insert the string.
	 * @param s
	 *            String to insert.
	 */
	public void insertText(final int offset, final String s) {
		assertOffset(offset, 0, getLength());

		if (s.length() > gapEnd - gapStart) {
			expandContent(getLength() + s.length());
		}

		//
		// Optimization: no need to update positions if we're inserting
		// after existing content (offset == this.getLength()) and if
		// we don't have to move the gap to do it (offset == gapStart).
		//
		// This significantly improves document load speed.
		//
		final boolean atEnd = offset == getLength() && offset == gapStart;

		moveGap(offset);
		s.getChars(0, s.length(), content, offset);
		gapStart += s.length();

		if (!atEnd) {

			// Update positions
			for (final GapContentPosition position : positions) {
				if (position.getOffset() >= offset) {
					position.setOffset(position.getOffset() + s.length());
				}
			}

		}
	}

	public void insertElementMarker(final int offset) {
		assertOffset(offset, 0, getLength());

		insertText(offset, Character.toString(ELEMENT_MARKER));
	}

	public boolean isElementMarker(final int offset) {
		if (offset < 0 || offset >= getLength()) {
			return false;
		}

		return isElementMarker(content[getIndex(offset)]);
	}

	private int getIndex(final int offset) {
		if (offset < gapStart) {
			return offset;
		}
		return offset + gapEnd - gapStart;
	}

	private boolean isElementMarker(final char c) {
		return c == ELEMENT_MARKER;
	}

	/**
	 * Deletes the given range of characters.
	 * 
	 * @param offset
	 *            Offset from which characters should be deleted.
	 * @param length
	 *            Number of characters to delete.
	 */
	public void remove(final int offset, final int length) {

		assertOffset(offset, 0, getLength() - length);
		assertPositive(length);

		moveGap(offset + length);
		gapStart -= length;

		for (final GapContentPosition position : positions) {
			if (position.getOffset() >= offset + length) {
				position.setOffset(position.getOffset() - length);
			} else if (position.getOffset() >= offset) {
				position.setOffset(offset);
			}
		}
	}

	public String getString(final int offset, final int length) {

		assertOffset(offset, 0, getLength() - length);
		assertPositive(length);

		if (offset + length < gapStart) {
			return new String(content, offset, length);
		} else if (offset >= gapStart) {
			return new String(content, offset - gapStart + gapEnd, length);
		} else {
			final StringBuilder sb = new StringBuilder(length);
			sb.append(content, offset, gapStart - offset);
			sb.append(content, gapEnd, offset + length - gapStart);
			return sb.toString();
		}
	}

	public String getText() {
		return getText(0, getLength());
	}

	public String getText(final int offset, final int length) {
		assertOffset(offset, 0, getLength() - length);
		assertPositive(length);

		final StringBuilder result = new StringBuilder(length);
		if (offset + length < gapStart) {
			appendPlainText(result, offset, length);
		} else if (offset >= gapStart) {
			appendPlainText(result, offset - gapStart + gapEnd, length);
		} else {
			appendPlainText(result, offset, gapStart - offset);
			appendPlainText(result, gapEnd, offset + length - gapStart);
		}
		return result.toString();
	}

	private void appendPlainText(final StringBuilder stringBuilder, final int offset, final int length) {
		for (int i = offset; i < offset + length; i++) {
			final char c = content[i];
			if (!isElementMarker(c)) {
				stringBuilder.append(c);
			}
		}
	}

	public String getRawText() {
		return getRawText(0, getLength());
	}

	public String getRawText(final int offset, final int length) {
		assertOffset(offset, 0, getLength() - length);
		assertPositive(length);

		final StringBuilder result = new StringBuilder(length);
		if (offset + length < gapStart) {
			appendRawText(result, offset, length);
		} else if (offset >= gapStart) {
			appendRawText(result, offset - gapStart + gapEnd, length);
		} else {
			appendRawText(result, offset, gapStart - offset);
			appendRawText(result, gapEnd, offset + length - gapStart);
		}
		return result.toString();
	}

	private void appendRawText(final StringBuilder stringBuilder, final int offset, final int length) {
		stringBuilder.append(content, offset, length);
	}

	public void insertContent(final int offset, final Content content) {
		assertOffset(offset, 0, getLength());

		copyContent(content, this, 0, offset, content.getLength());
	}

	public Content getContent(final int offset, final int length) {
		assertOffset(offset, 0, getLength() - length);
		assertPositive(length);

		final GapContent result = new GapContent(length);
		copyContent(this, result, offset, 0, length);
		return result;
	}

	public Content getContent() {
		return getContent(0, getLength());
	}

	private static void copyContent(final Content source, final Content destination, final int sourceOffset, final int destinationOffset, final int length) {
		for (int i = 0; i < length; i++) {
			if (source.isElementMarker(sourceOffset + i)) {
				destination.insertElementMarker(destinationOffset + i);
			} else {
				destination.insertText(destinationOffset + i, source.getText(sourceOffset + i, 1));
			}
		}
	}

	/**
	 * Return the length of the content.
	 */
	public int getLength() {
		return content.length - (gapEnd - gapStart);
	}

	// ====================================================== PRIVATE

	private static final int GROWTH_SLOWDOWN_SIZE = 100000;
	private static final int GROWTH_RATE_FAST = 2;
	private static final float GROWTH_RATE_SLOW = 1.1f;

	/**
	 * Implementation of the Position interface.
	 */
	private static class GapContentPosition implements Position {

		private int offset;

		private boolean valid = true;

		public GapContentPosition(final int offset) {
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(final int offset) {
			this.offset = offset;
		}

		public boolean isValid() {
			return valid;
		};

		public void invalidate() {
			valid = false;
		}

		@Override
		public String toString() {
			return Integer.toString(offset);
		}
	}

	/**
	 * Assert that the given offset is within the given range, throwing IllegalArgumentException if not.
	 */
	private static void assertOffset(final int offset, final int min, final int max) {
		if (offset < min || offset > max) {
			throw new IllegalArgumentException("Bad offset " + offset + " must be between " + min + " and " + max);
		}
	}

	/**
	 * Assert that the given value is zero or positive. throwing IllegalArgumentException if not.
	 */
	private static void assertPositive(final int value) {
		if (value < 0) {
			throw new IllegalArgumentException("Value should be zero or positive, but it was " + value);
		}
	}

	/**
	 * Expand the content array to fit at least the given length.
	 */
	private void expandContent(final int newLength) {

		// grow quickly when small, slower when large

		int newCapacity;

		if (newLength < GROWTH_SLOWDOWN_SIZE) {
			newCapacity = Math.max(newLength * GROWTH_RATE_FAST, 32);
		} else {
			newCapacity = (int) (newLength * GROWTH_RATE_SLOW);
		}

		final char[] newContent = new char[newCapacity];

		System.arraycopy(content, 0, newContent, 0, gapStart);

		final int tailLength = content.length - gapEnd;
		System.arraycopy(content, gapEnd, newContent, newCapacity - tailLength, tailLength);

		content = newContent;
		gapEnd = newCapacity - tailLength;
	}

	/**
	 * Move the gap to the given offset.
	 */
	private void moveGap(final int offset) {

		assertOffset(offset, 0, getLength());

		if (offset <= gapStart) {
			final int length = gapStart - offset;
			System.arraycopy(content, offset, content, gapEnd - length, length);
			gapStart -= length;
			gapEnd -= length;
		} else {
			final int length = offset - gapStart;
			System.arraycopy(content, gapEnd, content, gapStart, length);
			gapStart += length;
			gapEnd += length;
		}
	}
}
