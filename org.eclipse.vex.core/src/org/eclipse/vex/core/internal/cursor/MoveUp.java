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
import static org.eclipse.vex.core.internal.cursor.ContentTopology.horizontalDistance;
import static org.eclipse.vex.core.internal.cursor.ContentTopology.verticalDistance;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
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
public class MoveUp implements ICursorMove {

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
		if (isAtEndOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getStartOffset();
		}
		if (isAtPlaceholderForEndOfEmptyBox(currentOffset, currentBox)) {
			return getParentContentBox(currentBox).getStartOffset();
		}
		if (isAtEndOfBoxWithChildren(currentOffset, currentBox)) {
			final IContentBox lastChild = getLastContentBoxChild(currentBox);
			if (lastChild != null) {
				if (containsInlineContent(currentBox)) {
					return findOffsetInNextBoxAbove(graphics, currentOffset, lastChild, preferredX, hotArea.getY());
				} else if (containsInlineContent(lastChild)) {
					return findOffsetInNextBoxAbove(graphics, currentOffset, lastChild, preferredX, currentBox.getAbsoluteTop() + currentBox.getHeight());
				} else {
					return lastChild.getEndOffset();
				}
			}
		}

		return findOffsetInNextBoxAbove(graphics, currentOffset, currentBox, preferredX, hotArea.getY());
	}

	private static boolean isAtEndOfEmptyBox(final int offset, final IContentBox box) {
		return box.isAtEnd(offset) && box.isEmpty() && box.getEndOffset() > box.getStartOffset();
	}

	private boolean isAtPlaceholderForEndOfEmptyBox(final int offset, final IContentBox box) {
		final boolean isAtPlaceholder = box.accept(new BaseBoxVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final NodeEndOffsetPlaceholder box) {
				return true;
			}
		});
		if (isAtPlaceholder) {
			return isAtEndOfEmptyBox(offset, getParentContentBox(box));
		}
		return false;
	}

	private static boolean isAtEndOfBoxWithChildren(final int offset, final IContentBox box) {
		return box.isAtEnd(offset) && canHaveChildren(box);
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

	private static boolean containsInlineContent(final IContentBox box) {
		return box.accept(new BaseBoxVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final StructuralNodeReference box) {
				return box.containsInlineContent();
			}

			@Override
			public Boolean visit(final InlineNodeReference box) {
				return true;
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

	private static IContentBox getLastContentBoxChild(final IContentBox parent) {
		return parent.accept(new DepthFirstBoxTraversal<IContentBox>() {
			private IContentBox lastChild;

			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box == parent) {
					super.visit(box);
					return lastChild;
				}
				lastChild = box;
				return null;
			}

			@Override
			public IContentBox visit(final InlineNodeReference box) {
				lastChild = box;
				return super.visit(box);
			}

			@Override
			public IContentBox visit(final TextContent box) {
				lastChild = box;
				return null;
			}

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				lastChild = box;
				return super.visit(box);
			}
		});
	}

	private int findOffsetInNextBoxAbove(final Graphics graphics, final int currentOffset, final IContentBox currentBox, final int x, final int y) {
		final IContentBox nextBoxAbove = findNextContentBoxAbove(currentBox, x, y);
		return findOffsetInBox(graphics, currentOffset, x, y, nextBoxAbove);
	}

	private static int findOffsetInBox(final Graphics graphics, final int currentOffset, final int hotX, final int hotY, final IContentBox box) {
		if (box.isEmpty() && currentOffset == box.getEndOffset()) {
			return box.getStartOffset();
		}
		return box.accept(new BaseBoxVisitorWithResult<Integer>() {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				if (currentOffset <= box.getEndOffset()) {
					return box.getStartOffset();
				}
				return box.getEndOffset();
			}

			@Override
			public Integer visit(final InlineNodeReference box) {
				if (currentOffset <= box.getEndOffset()) {
					return box.getStartOffset();
				}
				return box.getEndOffset();
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

	private static IContentBox findNextContentBoxAbove(final IContentBox currentBox, final int x, final int y) {
		final IContentBox parent = getParentContentBox(currentBox);
		if (parent == null) {
			return currentBox;
		}

		final IContentBox childAbove = findClosestContentBoxChildAbove(parent, x, y);
		if (childAbove == null) {
			return parent.accept(new BaseBoxVisitorWithResult<IContentBox>() {
				@Override
				public IContentBox visit(final StructuralNodeReference box) {
					return parent;
				}

				@Override
				public IContentBox visit(final InlineNodeReference box) {
					return findNextContentBoxAbove(parent, x, y);
				}

				@Override
				public IContentBox visit(final TextContent box) {
					return findNextContentBoxAbove(parent, x, y);
				}

				@Override
				public IContentBox visit(final NodeEndOffsetPlaceholder box) {
					return findNextContentBoxAbove(parent, x, y);
				}
			});
		}

		return childAbove;
	}

	private static IContentBox findClosestContentBoxChildAbove(final IContentBox parent, final int x, final int y) {
		final Iterable<IContentBox> candidates = findVerticallyClosestContentBoxChildrenAbove(parent, y);
		final IContentBox finalCandidate = findHorizontallyClosestContentBox(candidates, x);
		return handleSpecialCaseMovingIntoLastLineOfParagraph(finalCandidate, x, y);
	}

	private static Iterable<IContentBox> findVerticallyClosestContentBoxChildrenAbove(final IContentBox parent, final int y) {
		final LinkedList<IContentBox> candidates = new LinkedList<IContentBox>();
		final int[] minVerticalDistance = new int[1];
		minVerticalDistance[0] = Integer.MAX_VALUE;
		parent.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final StructuralNodeReference box) {
				final int distance = verticalDistance(box, y);
				if (box != parent && !box.isAbove(y)) {
					return box;
				}

				if (box == parent) {
					super.visit(box);
				}

				if (box != parent) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}

				return null;
			}

			@Override
			public Object visit(final TextContent box) {
				final int distance = verticalDistance(box, y);
				if (box.isAbove(y) && distance <= minVerticalDistance[0]) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}
				return null;
			}

			@Override
			public Object visit(final NodeEndOffsetPlaceholder box) {
				final int distance = verticalDistance(box, y);
				if (box.isAbove(y) && distance <= minVerticalDistance[0]) {
					candidates.add(box);
					minVerticalDistance[0] = Math.min(distance, minVerticalDistance[0]);
				}
				return null;
			}
		});

		removeVerticallyDistantBoxes(candidates, y, minVerticalDistance[0]);
		return candidates;
	}

	private static IContentBox handleSpecialCaseMovingIntoLastLineOfParagraph(final IContentBox candidate, final int x, final int y) {
		if (candidate == null) {
			return null;
		}
		if (!(verticalDistance(candidate, y) >= 0 && horizontalDistance(candidate, x) == 0)) {
			return candidate;
		}

		final List<IContentBox> candidates = findVerticallyClosestTextContentChildrenAbove(candidate, y);
		final IContentBox closestTextContentBox = findHorizontallyClosestContentBox(candidates, x);

		if (closestTextContentBox != null && !closestTextContentBox.isLeftOf(x)) {
			return closestTextContentBox;
		}

		return candidate;
	}

	private static List<IContentBox> findVerticallyClosestTextContentChildrenAbove(final IBox parent, final int y) {
		final List<IContentBox> candidates = parent.accept(new DepthFirstBoxTraversal<List<IContentBox>>() {
			private final LinkedList<IContentBox> candidates = new LinkedList<IContentBox>();
			private int minVerticalDistance = Integer.MAX_VALUE;

			@Override
			public List<IContentBox> visit(final StructuralNodeReference box) {
				if (box != parent) {
					return Collections.emptyList();
				}
				super.visit(box);

				removeVerticallyDistantBoxes(candidates, y, minVerticalDistance);

				return candidates;
			}

			@Override
			public List<IContentBox> visit(final TextContent box) {
				final int distance = verticalDistance(box, y);
				if (distance <= minVerticalDistance) {
					minVerticalDistance = Math.min(distance, minVerticalDistance);
					candidates.add(box);
				}
				if (box == parent) {
					return candidates;
				}
				return null;
			}

			@Override
			public List<IContentBox> visit(final NodeEndOffsetPlaceholder box) {
				final int distance = verticalDistance(box, y);
				if (distance <= minVerticalDistance) {
					minVerticalDistance = Math.min(distance, minVerticalDistance);
					candidates.add(box);
				}
				if (box == parent) {
					return candidates;
				}
				return null;
			}
		});
		if (candidates == null) {
			return Collections.<IContentBox> emptyList();
		}
		return candidates;
	}

	private static void removeVerticallyDistantBoxes(final List<? extends IContentBox> boxes, final int y, final int minVerticalDistance) {
		for (final Iterator<? extends IContentBox> iter = boxes.iterator(); iter.hasNext();) {
			final IContentBox candidate = iter.next();
			if (verticalDistance(candidate, y) > minVerticalDistance) {
				iter.remove();
			}
		}
	}
}
