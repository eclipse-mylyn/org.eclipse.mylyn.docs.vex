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

/**
 * @author Florian Thienel
 */
public class CursorPosition {

	private final ContentMap contentMap;
	private int offset;

	public CursorPosition(final ContentMap contentMap) {
		this.contentMap = contentMap;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void left() {
		offset = Math.max(0, offset - 1);
	}

	public void right() {
		offset = Math.min(offset + 1, contentMap.getLastPosition());
	}

	public void moveToAbsoluteCoordinates(final Graphics graphics, final int x, final int y) {
		final IContentBox box = contentMap.findClosestBoxOnLineByCoordinates(x, y);
		if (box.containsCoordinates(x, y)) {
			offset = box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
		} else if (box.isLeftFrom(x)) {
			if (isLastEnclosedBox(box)) {
				offset = box.getEndOffset() + 1;
			} else {
				offset = box.getEndOffset();
			}
		} else if (box.isRightFrom(x)) {
			offset = box.getStartOffset();
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
