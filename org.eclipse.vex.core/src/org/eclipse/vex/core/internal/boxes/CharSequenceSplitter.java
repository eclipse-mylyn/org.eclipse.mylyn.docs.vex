/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public class CharSequenceSplitter {

	private CharSequence charSequence;
	private int startPosition;
	private int endPosition;

	public void setContent(final CharSequence charSequence) {
		setContent(charSequence, 0, charSequence.length() - 1);
	}

	public void setContent(final CharSequence charSequence, final int startPosition, final int endPosition) {
		this.charSequence = charSequence;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	private int textLength() {
		return endPosition - startPosition + 1;
	}

	private String substring(final int beginIndex, final int endIndex) {
		return charSequence.subSequence(startPosition + beginIndex, startPosition + endIndex).toString();
	}

	private char charAt(final int position) {
		return charSequence.charAt(startPosition + position);
	}

	public int findSplittingPositionBefore(final Graphics graphics, final int x, final int maxWidth, final boolean force) {
		final int positionAtWidth = findPositionAfter(graphics, x, maxWidth) - 1;
		final int properSplittingPosition = findProperSplittingPositionBefore(positionAtWidth);

		if (textLength() > properSplittingPosition + 2 && isSplittingCharacter(properSplittingPosition + 1) && !isSplittingCharacter(properSplittingPosition + 2)) {
			return properSplittingPosition + 2;
		}
		if (textLength() == properSplittingPosition + 2 && isSplittingCharacter(properSplittingPosition + 1)) {
			return properSplittingPosition + 2;
		}
		if (properSplittingPosition == -1 && force) {
			return positionAtWidth + 1;
		}
		return properSplittingPosition + 1;
	}

	public int findPositionAfter(final Graphics graphics, final int x, final int maxWidth) {
		if (x < 0) {
			return 0;
		}
		if (x >= maxWidth) {
			return textLength();
		}

		int begin = 0;
		int end = textLength();
		int pivot = guessPositionAt(x, maxWidth);
		while (begin < end - 1) {
			final int textWidth = stringWidthBeforeOffset(graphics, pivot);
			if (textWidth > x) {
				end = pivot;
			} else if (textWidth < x) {
				begin = pivot;
			} else {
				return pivot;
			}
			pivot = (begin + end) / 2;
		}

		return pivot;
	}

	private int guessPositionAt(final int x, final int maxWidth) {
		final float splittingRatio = (float) x / maxWidth;
		return Math.round(splittingRatio * textLength());
	}

	private int stringWidthBeforeOffset(final Graphics graphics, final int offset) {
		return graphics.stringWidth(substring(0, offset));
	}

	private int findProperSplittingPositionBefore(final int position) {
		for (int i = Math.min(position, textLength() - 1); i >= 0; i -= 1) {
			if (isSplittingCharacter(i)) {
				return i;
			}
		}
		return -1;
	}

	private boolean isSplittingCharacter(final int position) {
		return XML.isWhitespace(charAt(position));
	}

}
