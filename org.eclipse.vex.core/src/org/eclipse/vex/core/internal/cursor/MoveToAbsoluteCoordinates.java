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

import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.ParentTraversal;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
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
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final IContentBox outmostContentBox = contentMap.getOutmostContentBox();
		final IContentBox box = findClosestBoxByCoordinates(outmostContentBox, x, y);
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

	private static IContentBox findClosestBoxByCoordinates(final IContentBox outmostContentBox, final int x, final int y) {
		final IContentBox deepestContainer = findDeepestContainerOfCoordinates(outmostContentBox, x, y);
		if (deepestContainer == null) {
			return outmostContentBox;
		}
		return findClosestBoxInContainer(deepestContainer, x, y);
	}

	private static IContentBox findDeepestContainerOfCoordinates(final IContentBox rootBox, final int x, final int y) {
		return rootBox.accept(new DepthFirstTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (!box.containsCoordinates(x, y)) {
					return null;
				}
				final IContentBox deeperContainer = super.visit(box);
				if (deeperContainer != null) {
					return deeperContainer;
				}
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (!box.containsCoordinates(x, y)) {
					return null;
				}
				return box;
			}
		});
	}

	private static IContentBox findClosestBoxInContainer(final IContentBox container, final int x, final int y) {
		final LinkedList<IContentBox> candidates = new LinkedList<IContentBox>();
		container.accept(new DepthFirstTraversal<Object>() {
			@Override
			public Object visit(final TextContent box) {
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

	private static int horizontalDistance(final IBox box, final int x) {
		if (box.getAbsoluteLeft() > x) {
			return box.getAbsoluteLeft() - x;
		}
		if (box.getAbsoluteLeft() + box.getWidth() < x) {
			return x - box.getAbsoluteLeft() - box.getWidth();
		}
		return 0;
	}

	private static boolean isLastEnclosedBox(final IContentBox enclosedBox) {
		final IContentBox parent = getParentContentBox(enclosedBox);
		if (parent == null) {
			return true;
		}
		return enclosedBox.getEndOffset() == parent.getEndOffset() - 1;
	}

	private static IContentBox getParentContentBox(final IContentBox childBox) {
		return childBox.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box == childBox) {
					return super.visit(box);
				}
				return box;
			}
		});
	}

}
