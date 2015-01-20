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
public class MoveToAbsoluteCoordinates implements ICursorMove {

	private final int x;
	private final int y;

	public MoveToAbsoluteCoordinates(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		return findOffsetForAbsoluteCoordinates(graphics, contentMap, currentOffset);
	}

	private int findOffsetForAbsoluteCoordinates(final Graphics graphics, final ContentMap contentMap, final int currentOffset) {
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
			return currentOffset;
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

	@Override
	public boolean preferX() {
		return true;
	}

}
