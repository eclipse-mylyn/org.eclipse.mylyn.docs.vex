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

	public static ICursorMove toOffset(final int offset) {
		return new MoveToOffset(offset);
	}

	public static ICursorMove toAbsoluteCoordinates(final int x, final int y) {
		return new MoveToAbsoluteCoordinates(x, y);
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
}
