/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.cursor;

/**
 * @author Florian Thienel
 */
public class CursorMoves {

	private static final ICursorMove LEFT = new MoveLeft();
	private static final ICursorMove RIGHT = new MoveRight();
	private static final ICursorMove UP = new MoveUp();
	private static final ICursorMove DOWN = new MoveDown();
	private static final ICursorMove TO_PREVIOUS_PAGE = new MoveToPreviousPage();
	private static final ICursorMove TO_NEXT_PAGE = new MoveToNextPage();
	private static final ICursorMove TO_WORD_START = new MoveToWordStart();
	private static final ICursorMove TO_WORD_END = new MoveToWordEnd();
	private static final ICursorMove TO_NEXT_WORD = new MoveToNextWord();
	private static final ICursorMove TO_PREVIOUS_WORD = new MoveToPreviousWord();
	private static final ICursorMove TO_LINE_START = new MoveToLineStart();
	private static final ICursorMove TO_LINE_END = new MoveToLineEnd();

	public static ICursorMove toOffset(final int offset) {
		return new MoveToOffset(offset);
	}

	public static ICursorMove toAbsoluteCoordinates(final int x, final int y) {
		return new MoveToAbsoluteCoordinates(x, y);
	}

	public static ICursorMove by(final int distance) {
		return new MoveBy(distance);
	}

	public static ICursorMove left() {
		return LEFT;
	}

	public static ICursorMove right() {
		return RIGHT;
	}

	public static ICursorMove up() {
		return UP;
	}

	public static ICursorMove down() {
		return DOWN;
	}

	public static ICursorMove toPreviousPage() {
		return TO_PREVIOUS_PAGE;
	}

	public static ICursorMove toNextPage() {
		return TO_NEXT_PAGE;
	}

	public static ICursorMove toWordStart() {
		return TO_WORD_START;
	}

	public static ICursorMove toWordEnd() {
		return TO_WORD_END;
	}

	public static ICursorMove toNextWord() {
		return TO_NEXT_WORD;
	}

	public static ICursorMove toPreviousWord() {
		return TO_PREVIOUS_WORD;
	}

	public static ICursorMove toLineStart() {
		return TO_LINE_START;
	}

	public static ICursorMove toLineEnd() {
		return TO_LINE_END;
	}
}
