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

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.ParentTraversal;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.cursor.ContentMap.Environment;
import org.eclipse.vex.core.internal.cursor.ContentMap.Neighbour;

/**
 * @author Florian Thienel
 */
public class MoveDown implements ICursorMove {

	@Override
	public boolean preferX() {
		return false;
	}

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		if (isAtStartOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getEndOffset();
		}
		if (isAtStartOfBoxWithChildren(currentOffset, currentBox) && !canContainText(currentBox)) {
			return getFirstChild(currentBox).getStartOffset();
		}

		return findOffsetInNextBoxBelow(graphics, contentMap, currentBox, hotArea, preferredX);
	}

	private static boolean isAtStartOfEmptyBox(final int offset, final IContentBox box) {
		return box.isAtStart(offset) && box.isEmpty();
	}

	private static boolean isAtStartOfBoxWithChildren(final int offset, final IContentBox box) {
		return box.isAtStart(offset) && canHaveChildren(box);
	}

	private static boolean canHaveChildren(final IContentBox box) {
		return box.accept(new BaseBoxVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final NodeReference box) {
				return true;
			}
		});
	}

	private static boolean canContainText(final IContentBox box) {
		return box.accept(new BaseBoxVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final NodeReference box) {
				return box.canContainText();
			}
		});
	}

	private static IContentBox getParent(final IContentBox childBox) {
		return childBox.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box == childBox) {
					return super.visit(box);
				}
				return box;
			}
		});
	}

	private static IContentBox getFirstChild(final IContentBox parent) {
		return parent.accept(new DepthFirstTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box == parent) {
					return super.visit(box);
				}
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}
		});
	}

	private int findOffsetInNextBoxBelow(final Graphics graphics, final ContentMap contentMap, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final int x = preferredX;
		final int y = hotArea.getY() + hotArea.getHeight() - 1;
		final IContentBox box = findNextBoxBelow(contentMap, currentBox, x, y);
		if (box.isEmpty()) {
			return box.getStartOffset();
		}
		return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
	}

	private static IContentBox findNextBoxBelow(final ContentMap contentMap, final IContentBox currentBox, final int x, final int y) {
		final Environment environment = contentMap.findEnvironmentForCoordinates(x, y, true);
		final Neighbour neighbourBelow = environment.neighbours.getBelow();
		if (neighbourBelow.box == null) {
			final IContentBox parent = getParent(currentBox);
			if (parent == null) {
				return currentBox;
			}
			return parent;
		}

		final IContentBox boxBelow = neighbourBelow.box.accept(new BaseBoxVisitorWithResult<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (isLastChild(box) && box.isLeftOf(x)) {
					return getParent(box);
				}
				return box;
			}
		});

		return currentBox.accept(new BaseBoxVisitorWithResult<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (containerBottomCloserThanNeighbourBelow(box, neighbourBelow, y)) {
					return getParent(box);
				}
				return boxBelow;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return boxBelow;
			}
		});
	}

	private static boolean containerBottomCloserThanNeighbourBelow(final IContentBox box, final Neighbour neighbourBelow, final int y) {
		final IContentBox parent = getParent(box);
		final int distanceToContainerBottom = parent.getAbsoluteTop() + parent.getHeight() - y;
		return distanceToContainerBottom <= neighbourBelow.distance;
	}

	private static boolean isLastChild(final IContentBox box) {
		final IContentBox parent = getParent(box);
		if (parent == null) {
			return false;
		}
		if (box.getEndOffset() != parent.getEndOffset() - 1) {
			return false;
		}
		return true;
	}
}
