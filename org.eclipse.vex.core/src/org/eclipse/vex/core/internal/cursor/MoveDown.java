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

import static org.eclipse.vex.core.internal.cursor.ContentTopology.findHorizontallyClosestContentBox;
import static org.eclipse.vex.core.internal.cursor.ContentTopology.getParentContentBox;
import static org.eclipse.vex.core.internal.cursor.ContentTopology.verticalDistance;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;

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
	public int calculateNewOffset(final Graphics graphics, final IViewPort viewPort, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		if (isAtStartOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getEndOffset();
		}
		if (isAtStartOfBoxWithChildren(currentOffset, currentBox)) {
			final IContentBox firstChild = getFirstContentBoxChild(currentBox);
			if (firstChild != null) {
				if (canContainText(currentBox)) {
					return findOffsetInNextBoxBelow(graphics, currentOffset, firstChild, preferredX, hotArea.getY() + hotArea.getHeight() - 1);
				} else if (canContainText(firstChild)) {
					return findOffsetInNextBoxBelow(graphics, currentOffset, firstChild, preferredX, currentBox.getAbsoluteTop() - 1);
				} else {
					return firstChild.getStartOffset();
				}
			}
		}

		return findOffsetInNextBoxBelow(graphics, currentOffset, currentBox, preferredX, hotArea.getY() + hotArea.getHeight() - 1);
	}

	private static boolean isAtStartOfEmptyBox(final int offset, final IContentBox box) {
		return box.isAtStart(offset) && box.isEmpty() && box.getEndOffset() > box.getStartOffset();
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

			@Override
			public Boolean visit(final InlineNodeReference box) {
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
			public Boolean visit(final InlineNodeReference box) {
				return box.canContainText();
			}

			@Override
			public Boolean visit(final TextContent box) {
				return true;
			}

			@Override
			public Boolean visit(final NodeEndOffsetPlaceholder box) {
				return true;
			}
		});
	}

	private static IContentBox getFirstContentBoxChild(final IContentBox parent) {
		return parent.accept(new DepthFirstBoxTraversal<IContentBox>() {
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
			public IContentBox visit(final InlineNodeReference box) {
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

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
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

	private int findOffsetInNextBoxBelow(final Graphics graphics, final int currentOffset, final IContentBox currentBox, final int x, final int y) {
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
			public Integer visit(final InlineNodeReference box) {
				if (currentOffset >= box.getStartOffset()) {
					return box.getEndOffset();
				}
				return box.getStartOffset();
			}

			@Override
			public Integer visit(final TextContent box) {
				return box.getOffsetForCoordinates(graphics, hotX - box.getAbsoluteLeft(), hotY - box.getAbsoluteTop());
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
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
		parent.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final StructuralNodeReference box) {
				if (box == parent) {
					super.visit(box);
				} else {
					final int distance = verticalDistance(box, y);
					if (box.isBelow(y)) {
						candidates.add(box);
						minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
					}
				}
				return null;
			}

			@Override
			public Object visit(final TextContent box) {
				final int distance = verticalDistance(box, y);
				if (box.isBelow(y)) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}
				return null;
			}

			@Override
			public Object visit(final NodeEndOffsetPlaceholder box) {
				final int distance = verticalDistance(box, y);
				if (box.isBelow(y)) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}
				return null;
			}
		});

		for (final Iterator<IContentBox> iter = candidates.iterator(); iter.hasNext();) {
			final IContentBox candidate = iter.next();
			if (verticalDistance(candidate, y) > minVerticalDistance[0]) {
				iter.remove();
			}
		}
		return candidates;
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

		final IContentBox lastTextContentBox = parent.accept(new DepthFirstBoxTraversal<IContentBox>() {
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

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
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
