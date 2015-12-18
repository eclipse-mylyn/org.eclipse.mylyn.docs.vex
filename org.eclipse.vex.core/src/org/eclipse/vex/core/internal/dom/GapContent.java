/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Florian Thienel - refactoring to full fledged DOM
 *     Carsten Hiesserich - some optimization in getText methods
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.MultilineText;

/**
 * Implementation of the <code>Content</code> interface that manages changes efficiently. Implements a buffer that keeps
 * its free space (the "gap") at the location of the last change. Insertions at the start of the gap require no other
 * chars to be moved so long as the insertion is smaller than the gap. Deletions that end of the gap are also very
 * efficent. Furthermore, changes near the gap require relatively few characters to be moved.
 */
public class GapContent implements IContent {

	private static final int GROWTH_SLOWDOWN_SIZE = 100000;
	private static final int GROWTH_RATE_FAST = 2;
	private static final float GROWTH_RATE_SLOW = 1.1f;

	private static final char TAG_MARKER = '\0';
	private static final char LINE_BREAK = '\n';

	private char[] content;
	private int gapStart;
	private int gapEnd;
	private final TreeSet<GapContentPosition> positions = new TreeSet<GapContentPosition>();

	/**
	 * Create a GapContent with the given initial capacity.
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

	@Override
	public IPosition createPosition(final int offset) {

		assertOffset(offset, 0, length());

		final GapContentPosition newPosition = new GapContentPosition(offset);
		if (positions.contains(newPosition)) {
			final SortedSet<GapContentPosition> tailSet = positions.tailSet(newPosition);
			final GapContentPosition storedPosition = tailSet.first();
			storedPosition.increaseUse();
			return storedPosition;
		}
		positions.add(newPosition);
		return newPosition;
	}

	@Override
	public void removePosition(final IPosition position) {
		if (positions.contains(position)) {
			/*
			 * This cast is save: if the position can be removed, this instance must have created it, hence it is a
			 * GapContentPosition.
			 */
			final SortedSet<GapContentPosition> tailSet = positions.tailSet((GapContentPosition) position);
			final GapContentPosition storedPosition = tailSet.first();
			storedPosition.decreaseUse();
			if (!storedPosition.isValid()) {
				positions.remove(storedPosition);
			}
		}
	}

	public int getPositionCount() {
		return positions.size();
	}

	@Override
	public void insertText(final int offset, final String text) {
		assertOffset(offset, 0, length());

		if (text.length() > gapEnd - gapStart) {
			expandContent(length() + text.length());
		}

		//
		// Optimization: no need to update positions if we're inserting
		// after existing content (offset == this.getLength()) and if
		// we don't have to move the gap to do it (offset == gapStart).
		//
		// This significantly improves document load speed.
		//
		final boolean atEnd = offset == length() && offset == gapStart;

		moveGap(offset);
		text.getChars(0, text.length(), content, offset);
		gapStart += text.length();

		if (!atEnd) {
			movePositions(offset, text.length());
		}
	}

	private void movePositions(final int startOffset, final int delta) {
		for (final GapContentPosition position : positions.tailSet(new GapContentPosition(startOffset))) {
			position.setOffset(position.getOffset() + delta);
		}
	}

	@Override
	public void insertTagMarker(final int offset) {
		assertOffset(offset, 0, length());

		insertText(offset, Character.toString(TAG_MARKER));
	}

	@Override
	public boolean isTagMarker(final int offset) {
		if (offset < 0 || offset >= length()) {
			return false;
		}

		return isTagMarker(content[getIndex(offset)]);
	}

	private int getIndex(final int offset) {
		if (offset < gapStart) {
			return offset;
		}
		return offset + gapEnd - gapStart;
	}

	private boolean isTagMarker(final char c) {
		return c == TAG_MARKER;
	}

	public boolean isLineBreak(final int offset) {
		if (offset < 0 || offset >= length()) {
			return false;
		}

		return isLineBreak(content[getIndex(offset)]);
	}

	private boolean isLineBreak(final char c) {
		return c == LINE_BREAK;
	}

	@Override
	public void remove(final ContentRange range) {
		assertOffset(range.getStartOffset(), 0, length() - range.length());
		assertPositive(range.length());

		moveGap(range.getEndOffset() + 1);
		gapStart -= range.length();

		final SortedSet<GapContentPosition> tail = positions.tailSet(new GapContentPosition(range.getStartOffset()));
		for (final Iterator<GapContentPosition> iterator = tail.iterator(); iterator.hasNext();) {
			final GapContentPosition position = iterator.next();

			if (position.getOffset() <= range.getEndOffset()) {
				position.invalidate();
				iterator.remove();
			} else {
				position.setOffset(position.getOffset() - range.length());
			}
		}
	}

	@Override
	public String getText() {
		return getText(getRange());
	}

	@Override
	public String getText(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));

		final List<ContentRange> affectedRanges = expandAroundGap(range);

		// Use range length as initial capacity. This might be a bit too much, but that's better than having to resize the StringBuilder.
		final StringBuilder result = new StringBuilder(range.length());
		for (final ContentRange affectedRange : affectedRanges) {
			appendPlainText(result, affectedRange);
		}
		return result.toString();
	}

	private List<ContentRange> expandAroundGap(final ContentRange range) {
		final List<ContentRange> affectedRanges = new ArrayList<ContentRange>();
		final int delta = gapEnd - gapStart;
		if (range.getEndOffset() < gapStart) {
			affectedRanges.add(range);
		} else if (range.getStartOffset() >= gapStart) {
			affectedRanges.add(range.moveBy(delta));
		} else {
			affectedRanges.add(new ContentRange(range.getStartOffset(), gapStart - 1));
			affectedRanges.add(new ContentRange(gapEnd, range.getEndOffset() + delta));
		}
		return affectedRanges;
	}

	private void appendPlainText(final StringBuilder stringBuilder, final ContentRange range) {
		final int endOffset = range.getEndOffset();
		for (int i = range.getStartOffset(); i <= endOffset; i++) {
			final char c = content[i];
			if (!isTagMarker(c)) {
				stringBuilder.append(c);
			}
		}
	}

	@Override
	public String getRawText() {
		return getRawText(getRange());
	}

	@Override
	public String getRawText(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));

		final List<ContentRange> affectedRanges = expandAroundGap(range);

		// Use range length as initial capacity. This might be a bit too much, but that's better than having to resize the StringBuilder.
		final StringBuilder result = new StringBuilder(range.length());
		for (final ContentRange affectedRange : affectedRanges) {
			appendRawText(result, affectedRange);
		}
		return result.toString();
	}

	private void appendRawText(final StringBuilder stringBuilder, final ContentRange range) {
		stringBuilder.append(content, range.getStartOffset(), range.length());
	}

	@Override
	public void insertLineBreak(final int offset) {
		insertText(offset, Character.toString(LINE_BREAK));
	}

	@Override
	public MultilineText getMultilineText(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));
		final MultilineText result = new MultilineText();

		StringBuilder currentLine = new StringBuilder();
		int lineStart = range.getStartOffset();
		for (int i = range.getStartOffset(); i <= range.getEndOffset(); i += 1) {
			final char c = charAt(i);
			if (isTagMarker(c)) {
				// ignore tag markers
			} else if (isLineBreak(c)) {
				currentLine.append(c);
				final ContentRange lineRange = new ContentRange(lineStart, i);
				result.appendLine(currentLine.toString(), lineRange);
				currentLine = new StringBuilder();
				lineStart = i + 1;
			} else {
				currentLine.append(c);
			}
		}

		if (currentLine.length() > 0) {
			result.appendLine(currentLine.toString(), new ContentRange(lineStart, range.getEndOffset()));
		}

		return result;
	}

	@Override
	public void insertContent(final int offset, final IContent content) {
		assertOffset(offset, 0, length());

		copyContent(content, this, content.getRange(), offset);
	}

	@Override
	public IContent getContent() {
		return getContent(getRange());
	}

	@Override
	public IContent getContent(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));

		final GapContent result = new GapContent(range.length());
		copyContent(this, result, range, 0);
		return result;
	}

	private static void copyContent(final IContent source, final IContent destination, final ContentRange sourceRange, final int destinationStartOffset) {
		for (int i = 0; i < sourceRange.length(); i++) {
			final int sourceOffset = sourceRange.getStartOffset() + i;
			final int destinationOffset = destinationStartOffset + i;
			if (source.isTagMarker(sourceOffset)) {
				destination.insertTagMarker(destinationOffset);
			} else {
				destination.insertText(destinationOffset, Character.toString(source.charAt(sourceOffset)));
			}
		}
	}

	/**
	 * @see CharSequence#length()
	 * @return the length of the raw textual content, including tag markers.
	 */
	@Override
	public int length() {
		return content.length - (gapEnd - gapStart);
	}

	@Override
	public ContentRange getRange() {
		return new ContentRange(0, length() - 1);
	}

	/**
	 * @see CharSequence#charAt(int)
	 * @param offset
	 *            the offset of the character within the raw textual content
	 * @return the character at the given offset (tag markers included)
	 */
	@Override
	public char charAt(final int offset) {
		return content[getIndex(offset)];
	}

	/**
	 * Get the raw text of a region of this content. The plain text does also contain the tag markers in this content.
	 *
	 * @see CharSequence#subSequence(int, int)
	 * @param startOffset
	 *            Offset at which the substring begins.
	 * @param endOffset
	 *            Offset at which the substring ends.
	 * @return the text of the given region including tag markers
	 */
	@Override
	public CharSequence subSequence(final int startOffset, final int endOffset) {
		Assert.isTrue(startOffset <= endOffset);
		if (startOffset == endOffset) {
			return "";
		}

		return getRawText(new ContentRange(startOffset, endOffset - 1));
	}

	/*
	 * Implementation of the Position interface.
	 */
	private static class GapContentPosition implements IPosition {

		private int offset;

		private int useCount = 1;

		public GapContentPosition(final int offset) {
			this.offset = offset;
		}

		@Override
		public int getOffset() {
			return offset;
		}

		public void setOffset(final int offset) {
			this.offset = offset;
		}

		public void increaseUse() {
			useCount++;
		}

		public void decreaseUse() {
			useCount--;
		}

		@Override
		public boolean isValid() {
			return useCount > 0;
		};

		public void invalidate() {
			useCount = 0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + offset;
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
			final GapContentPosition other = (GapContentPosition) obj;
			if (offset != other.offset) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return Integer.toString(offset);
		}

		@Override
		public int compareTo(final IPosition other) {
			return offset - other.getOffset();
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

		assertOffset(offset, 0, length());

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
