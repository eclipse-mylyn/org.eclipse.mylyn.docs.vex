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

import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public class CharSequenceSplitter {

	private CharSequence charSequence;
	private int startPosition;
	private int endPosition;

	public void setContent(final CharSequence charSequence) {
		setContent(charSequence, 0, charSequence.length());
	}

	public void setContent(final CharSequence charSequence, final int startPosition, final int endPosition) {
		this.charSequence = charSequence;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	private int textLength() {
		return endPosition - startPosition;
	}

	private String substring(final int beginIndex, final int endIndex) {
		return charSequence.subSequence(startPosition + beginIndex, startPosition + endIndex).toString();
	}

	private char charAt(final int position) {
		return charSequence.charAt(startPosition + position);
	}

	public int findSplittingPositionBefore(final Graphics graphics, final int y, final int maxWidth, final boolean force) {
		final int positionAtWidth = findPositionBefore(graphics, y, maxWidth);
		final int properSplittingPosition = findProperSplittingPositionBefore(positionAtWidth);
		final int splittingPosition;
		if (properSplittingPosition == -1 && force) {
			splittingPosition = positionAtWidth;
		} else {
			splittingPosition = properSplittingPosition + 1;
		}
		return splittingPosition;
	}

	private int findPositionBefore(final Graphics graphics, final int y, final int maxWidth) {
		if (y < 0) {
			return 0;
		}
		if (y >= maxWidth) {
			return textLength();
		}

		int begin = 0;
		int end = textLength();
		int pivot = guessPositionAt(y, maxWidth);
		while (begin < end - 1) {
			final int textWidth = graphics.stringWidth(substring(0, pivot));
			if (textWidth > y) {
				end = pivot;
			} else if (textWidth < y) {
				begin = pivot;
			} else {
				return pivot;
			}
			pivot = (begin + end) / 2;
		}
		return pivot;
	}

	private int guessPositionAt(final int y, final int maxWidth) {
		final float splittingRatio = (float) y / maxWidth;
		return Math.round(splittingRatio * textLength());
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
		return Character.isWhitespace(charAt(position));
	}

}
