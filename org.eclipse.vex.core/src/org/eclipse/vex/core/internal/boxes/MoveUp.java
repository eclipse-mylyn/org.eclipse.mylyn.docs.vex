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

import org.eclipse.vex.core.internal.boxes.ContentMap.Environment;
import org.eclipse.vex.core.internal.boxes.ContentMap.Neighbour;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class MoveUp implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		if (isAtEndOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getStartOffset();
		}
		if (isAtEndOfBoxWithChildren(currentOffset, currentBox) && !canContainText(currentBox)) {
			return getLastChild(currentBox).getEndOffset();
		}

		final int x = preferredX;
		final int y = hotArea.getY();
		final IContentBox box = findClosestBoxAbove(contentMap, currentBox, x, y);
		return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
	}

	@Override
	public boolean preferX() {
		return false;
	}

	private static boolean isAtEndOfEmptyBox(final int offset, final IContentBox box) {
		return isAtEndOfBox(offset, box) && box.isEmpty();
	}

	private static boolean isAtEndOfBoxWithChildren(final int offset, final IContentBox box) {
		return isAtEndOfBox(offset, box) && canHaveChildren(box);
	}

	private static boolean isAtEndOfBox(final int offset, final IContentBox box) {
		return offset == box.getEndOffset();
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

	private static IContentBox getLastChild(final IContentBox parent) {
		return parent.accept(new DepthFirstTraversal<IContentBox>() {
			private IContentBox lastChild;

			@Override
			public IContentBox visit(final NodeReference box) {
				if (box == parent) {
					super.visit(box);
					return lastChild;
				}
				lastChild = box;
				return null;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				lastChild = box;
				return null;
			}
		});
	}

	private static IContentBox findClosestBoxAbove(final ContentMap contentMap, final IContentBox currentBox, final int x, final int y) {
		final Environment environment = contentMap.findEnvironmentForCoordinates(x, y, true);
		final Neighbour neighbourAbove = environment.neighbours.getAbove();
		if (environment.deepestContainer == null) {
			return currentBox;
		}

		if (neighbourAbove.box == null) {
			final IContentBox parent = getParent(currentBox);
			if (parent == null) {
				return currentBox;
			}
			return parent;
		}

		final IContentBox boxAbove = neighbourAbove.box.accept(new BaseBoxVisitorWithResult<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box.canContainText()) {
					final IContentBox lastChild = deepestLastChild(box);
					if (lastChild.isRightOf(x)) {
						return lastChild;
					}
					if (lastChild.containsX(x)) {
						return lastChild;
					}
				}
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}
		});

		return currentBox.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box == currentBox) {
					return super.visit(box);
				}
				final int distanceToContainerTop = y - box.getAbsoluteTop();
				if (distanceToContainerTop <= neighbourAbove.distance) {
					return box;
				}
				return boxAbove;
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

	private static IContentBox deepestLastChild(final IContentBox parentBox) {
		return parentBox.accept(new DepthFirstTraversal<IContentBox>() {
			private IContentBox lastChild;

			@Override
			public IContentBox visit(final NodeReference box) {
				lastChild = box;
				super.visit(box);
				if (box == parentBox) {
					return lastChild;
				} else {
					return null;
				}
			}

			@Override
			public IContentBox visit(final TextContent box) {
				lastChild = box;
				return null;
			}
		});
	}

}
