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

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
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
public class MoveDown implements ICursorMove {

	@Override
	public boolean preferX() {
		return false;
	}

	@Override
	public boolean isAbsolute() {
		return false;
	}

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		if (isAtStartOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getEndOffset();
		}
		if (isAtStartOfBoxWithChildren(currentOffset, currentBox)) {
			final IContentBox firstChild = getFirstContentBoxChild(currentBox);
			if (firstChild != null) {
				if (canContainText(firstChild)) {
					return findOffsetInNextBoxBelow(graphics, currentOffset, firstChild, hotArea, preferredX);
				} else {
					return firstChild.getStartOffset();
				}
			}
		}

		return findOffsetInNextBoxBelow(graphics, currentOffset, currentBox, hotArea, preferredX);
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
			public Boolean visit(final StructuralNodeReference box) {
				return true;
			}

		});
	}

	private static boolean canContainText(final IContentBox box) {
		return box.accept(new BaseBoxVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final StructuralNodeReference box) {
				return box.canContainText();
			}

			@Override
			public Boolean visit(final TextContent box) {
				return true;
			}
		});
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

	private static IContentBox getFirstContentBoxChild(final IContentBox parent) {
		return parent.accept(new DepthFirstTraversal<IContentBox>() {
			private IContentBox firstChild;

			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box == parent) {
					super.visit(box);
					return firstChild;
				}

				if (firstChild == null) {
					firstChild = box;
				}
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box == parent) {
					return null;
				}

				if (firstChild == null) {
					firstChild = box;
				}
				return box;
			}
		});
	}

	private int findOffsetInNextBoxBelow(final Graphics graphics, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final int x = preferredX;
		final int y = hotArea.getY() + hotArea.getHeight() - 1;
		final IContentBox nextBoxBelow = findNextContentBoxBelow(currentBox, x, y);
		return findOffsetInBox(graphics, currentOffset, x, y, nextBoxBelow);
	}

	private static int findOffsetInBox(final Graphics graphics, final int currentOffset, final int hotX, final int hotY, final IContentBox box) {
		if (box.isEmpty()) {
			return box.getStartOffset();
		}
		return box.accept(new BaseBoxVisitorWithResult<Integer>() {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				if (currentOffset >= box.getStartOffset()) {
					return box.getEndOffset();
				}
				return box.getStartOffset();
			}

			@Override
			public Integer visit(final TextContent box) {
				return box.getOffsetForCoordinates(graphics, hotX - box.getAbsoluteLeft(), hotY - box.getAbsoluteTop());
			}
		});
	}

	private static IContentBox findNextContentBoxBelow(final IContentBox currentBox, final int x, final int y) {
		final IContentBox parent = getParentContentBox(currentBox);
		if (parent == null) {
			return currentBox;
		}

		final IContentBox childBelow = findClosestContentBoxChildBelow(parent, x, y);
		if (childBelow == null) {
			if (canContainText(parent)) {
				return findNextContentBoxBelow(parent, x, y);
			}
			return parent;
		}

		return childBelow;
	}

	private static IContentBox findClosestContentBoxChildBelow(final IContentBox parent, final int x, final int y) {
		final Iterable<IContentBox> candidates = findVerticallyClosestContentBoxChildrenBelow(parent, y);
		final IContentBox finalCandidate = findHorizontallyClosestContentBox(candidates, x);
		final IContentBox realFinalCandidate = handleSpecialCaseMovingIntoLastLineOfParagraph(finalCandidate, x, y);
		return realFinalCandidate;
	}

	private static Iterable<IContentBox> findVerticallyClosestContentBoxChildrenBelow(final IContentBox parent, final int y) {
		final LinkedList<IContentBox> candidates = new LinkedList<IContentBox>();
		final int[] minVerticalDistance = new int[1];
		minVerticalDistance[0] = Integer.MAX_VALUE;
		parent.accept(new DepthFirstTraversal<Object>() {

			private boolean isBelow(final int distance) {
				return distance > 0;
			}

			@Override
			public Object visit(final StructuralNodeReference box) {
				if (box == parent) {
					super.visit(box);
				} else {
					final int distance = verticalDistanceFromBelow(box, y);
					if (isBelow(distance)) {
						candidates.add(box);
						minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
					}
				}
				return null;
			}

			@Override
			public Object visit(final TextContent box) {
				final int distance = verticalDistanceFromBelow(box, y);
				if (isBelow(distance)) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}
				return null;
			}
		});

		for (final Iterator<IContentBox> iter = candidates.iterator(); iter.hasNext();) {
			final IContentBox candidate = iter.next();
			if (verticalDistanceFromBelow(candidate, y) > minVerticalDistance[0]) {
				iter.remove();
			}
		}
		return candidates;
	}

	private static int verticalDistanceFromBelow(final IContentBox box, final int y) {
		return box.accept(new BaseBoxVisitorWithResult<Integer>(0) {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				return box.getAbsoluteTop() - y;
			}

			@Override
			public Integer visit(final TextContent box) {
				return box.getAbsoluteTop() + box.getBaseline() - y;
			}
		});
	}

	private static IContentBox findHorizontallyClosestContentBox(final Iterable<IContentBox> candidates, final int x) {
		IContentBox finalCandidate = null;
		int minHorizontalDistance = Integer.MAX_VALUE;
		for (final IContentBox candidate : candidates) {
			final int distance = horizontalDistance(candidate, x);
			if (distance < minHorizontalDistance) {
				finalCandidate = candidate;
				minHorizontalDistance = distance;
			}
		}
		return finalCandidate;
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

	private static IContentBox handleSpecialCaseMovingIntoLastLineOfParagraph(final IContentBox candidate, final int x, final int y) {
		if (candidate == null) {
			return null;
		}

		if (!candidate.isLeftOf(x)) {
			return candidate;
		}

		final IContentBox parent = getParentContentBox(candidate);
		if (parent == null) {
			return candidate;
		}

		final IContentBox lastTextContentBox = parent.accept(new DepthFirstTraversal<IContentBox>() {
			private IContentBox lastTextContentBox;

			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box != parent) {
					return null;
				}
				super.visit(box);
				return lastTextContentBox;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				lastTextContentBox = box;
				return super.visit(box);
			}
		});

		if (candidate == lastTextContentBox) {
			return parent;
		}

		return candidate;
	}
}
