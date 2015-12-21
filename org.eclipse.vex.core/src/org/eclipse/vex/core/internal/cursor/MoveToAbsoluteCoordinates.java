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

import static org.eclipse.vex.core.internal.cursor.ContentTopology.getParentContentBox;
import static org.eclipse.vex.core.internal.cursor.ContentTopology.horizontalDistance;

import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.TextContent;
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
	public boolean preferX() {
		return true;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final IContentBox box = findClosestBoxByCoordinates(contentTopology, x, y);
		if (box == null) {
			return currentOffset;
		}
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

	private static IContentBox findClosestBoxByCoordinates(final ContentTopology contentTopology, final int x, final int y) {
		final IContentBox deepestContainer = contentTopology.findBoxForCoordinates(x, y);
		if (deepestContainer == null) {
			return null;
		}
		return findClosestBoxInContainer(deepestContainer, x, y);
	}

	private static IContentBox findClosestBoxInContainer(final IContentBox container, final int x, final int y) {
		final LinkedList<IContentBox> candidates = new LinkedList<IContentBox>();
		container.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final TextContent box) {
				if (box.containsY(y)) {
					candidates.add(box);
				}
				return null;
			}

			@Override
			public Object visit(final NodeEndOffsetPlaceholder box) {
				if (box.containsY(y)) {
					candidates.add(box);
				}
				return null;
			}
		});

		int minHorizontalDistance = Integer.MAX_VALUE;
		IContentBox closestBox = container;
		for (final IContentBox candidate : candidates) {
			final int horizontalDistance = horizontalDistance(candidate, x);
			if (horizontalDistance < minHorizontalDistance) {
				minHorizontalDistance = horizontalDistance;
				closestBox = candidate;
			}
		}

		return closestBox;
	}

	private static boolean isLastEnclosedBox(final IContentBox enclosedBox) {
		final IContentBox parent = getParentContentBox(enclosedBox);
		if (parent == null) {
			return true;
		}
		return enclosedBox.getEndOffset() == parent.getEndOffset() - 1;
	}

}
