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
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class CursorPosition {

	private final Cursor cursor;
	private final ContentMap contentMap;

	public CursorPosition(final Cursor cursor, final ContentMap contentMap) {
		this.cursor = cursor;
		this.contentMap = contentMap;
	}

	public void moveToOffset(final int offset) {
		cursor.setPosition(offset);
		cursor.setPreferX(true);
	}

	public int getOffset() {
		return cursor.getPosition();
	}

	public void moveLeft() {
		cursor.setPosition(Math.max(0, getOffset() - 1));
		cursor.setPreferX(true);
	}

	public void moveRight() {
		cursor.setPosition(Math.min(getOffset() + 1, contentMap.getLastPosition()));
		cursor.setPreferX(true);
	}

	public void moveUp(final Graphics graphics) {
		if (getOffset() == 0) {
			return;
		}

		final IContentBox currentBox = cursor.getCurrentBox();
		if (isAtEndOfEmptyBox(currentBox)) {
			cursor.setPosition(currentBox.getStartOffset());
			cursor.setPreferX(false);
			return;
		}

		final Rectangle hotArea = cursor.getHotArea();
		final int x = cursor.getPreferredX();
		final int y = hotArea.getY();
		final IContentBox box = contentMap.findClosestBoxAbove(x, y);
		cursor.setPosition(box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop()));
		cursor.setPreferX(false);
	}

	private boolean isAtEndOfEmptyBox(final IContentBox box) {
		return getOffset() == box.getEndOffset() && box.getStartOffset() == box.getEndOffset() - 1;
	}

	public void moveToAbsoluteCoordinates(final Graphics graphics, final int x, final int y) {
		cursor.setPosition(findOffsetForAbsoluteCoordinates(graphics, x, y));
		cursor.setPreferX(true);
	}

	private int findOffsetForAbsoluteCoordinates(final Graphics graphics, final int x, final int y) {
		final IContentBox box = contentMap.findClosestBoxOnLineByCoordinates(x, y);
		if (box.containsCoordinates(x, y)) {
			return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
		} else if (box.isLeftOf(x)) {
			if (isLastEnclosedBox(box)) {
				return box.getEndOffset() + 1;
			} else {
				return box.getEndOffset();
			}
		} else if (box.isRightOf(x)) {
			return box.getStartOffset();
		} else {
			return getOffset();
		}
	}

	private static boolean isLastEnclosedBox(final IContentBox enclosedBox) {
		final IContentBox parent = findParentContentBox(enclosedBox);
		if (parent == null) {
			return true;
		}
		return enclosedBox.getEndOffset() == parent.getEndOffset() - 1;
	}

	private static IContentBox findParentContentBox(final IContentBox child) {
		return child.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (child == box) {
					return super.visit(box);
				}
				return box;
			}
		});
	}

}
