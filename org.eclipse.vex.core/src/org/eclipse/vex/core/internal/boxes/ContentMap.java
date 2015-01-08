/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

/**
 * @author Florian Thienel
 */
public class ContentMap {

	private RootBox rootBox;
	private IContentBox outmostContentBox;

	public void setRootBox(final RootBox rootBox) {
		this.rootBox = rootBox;
		outmostContentBox = findOutmostContentBox();
	}

	private IContentBox findOutmostContentBox() {
		return rootBox.accept(new DepthFirstTraversal<IContentBox>(null) {
			@Override
			public IContentBox visit(final NodeReference box) {
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}
		});
	}

	public int getLastPosition() {
		if (outmostContentBox == null) {
			return 0;
		}
		return outmostContentBox.getEndOffset();
	}

	public IContentBox findBoxForPosition(final int offset) {
		return rootBox.accept(new DepthFirstTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box.getStartOffset() == offset || box.getEndOffset() == offset) {
					return box;
				}
				if (box.getStartOffset() < offset && box.getEndOffset() > offset) {
					return box.getComponent().accept(this);
				}
				return null;
			}

			@Override
			public IContentBox visit(final Paragraph box) {
				return traverseChildren(box);
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box.getStartOffset() <= offset && box.getEndOffset() >= offset) {
					return box;
				}
				return null;
			}
		});
	}

	public IContentBox _findBoxByCoordinates(final int x, final int y) {
		return rootBox.accept(new DepthFirstTraversal<IContentBox>() {

			private IContentBox nearBy;

			@Override
			public IContentBox visit(final NodeReference box) {
				if (!box.containsCoordinates(x, y)) {
					return null;
				}

				final IContentBox componentResult = box.getComponent().accept(this);
				if (componentResult != null) {
					return componentResult;
				}
				if (nearBy != null && (nearBy.isRightFrom(x) && isFirstEnclosedBox(box, nearBy) || nearBy.isLeftFrom(x) && isLastEnclosedBox(box, nearBy))) {
					return nearBy;
				}
				return box;

			}

			private boolean isLastEnclosedBox(final IContentBox enclosingBox, final IContentBox enclosedBox) {
				return enclosedBox.getEndOffset() < enclosingBox.getEndOffset() - 1;
			}

			private boolean isFirstEnclosedBox(final IContentBox enclosingBox, final IContentBox enclosedBox) {
				return enclosedBox.getStartOffset() > enclosingBox.getStartOffset() + 1;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box.containsCoordinates(x, y)) {
					return box;
				}
				if (box.containsY(y)) {
					nearBy = box;
				}
				return null;
			}

		});
	}

	public IContentBox findClosestBoxOnLineByCoordinates(final int x, final int y) {
		final Neighbourhood neighbours = new Neighbourhood();
		final IContentBox deepestContainer = rootBox.accept(new DepthFirstTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				if (box.isAbove(y)) {
					neighbours.setAbove(box, box.getAbsoluteTop() + box.getHeight() - y, false);
					return super.visit(box);
				} else if (box.isBelow(y)) {
					neighbours.setBelow(box, y - box.getAbsoluteTop(), false);
					return super.visit(box);
				} else if (box.isRightFrom(x)) {
					neighbours.setRight(box, box.getAbsoluteLeft() - x, false);
					return super.visit(box);
				} else if (box.isLeftFrom(x)) {
					neighbours.setLeft(box, x - box.getAbsoluteLeft() - box.getWidth(), false);
					return super.visit(box);
				} else {
					final IContentBox deeperContainer = super.visit(box);
					if (deeperContainer != null) {
						return deeperContainer;
					}
					return box;
				}
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box.isAbove(y)) {
					neighbours.setAbove(box, y - box.getAbsoluteTop() - box.getHeight(), true);
					return null;
				} else if (box.isBelow(y)) {
					neighbours.setBelow(box, box.getAbsoluteTop() - y, true);
					return null;
				} else if (box.isRightFrom(x)) {
					neighbours.setRight(box, box.getAbsoluteLeft() - x, true);
					return null;
				} else if (box.isLeftFrom(x)) {
					neighbours.setLeft(box, x - box.getAbsoluteLeft() - box.getWidth(), true);
					return null;
				} else {
					return box;
				}
			}
		});
		if (deepestContainer == null) {
			return outmostContentBox;
		}
		return deepestContainer.accept(new BaseBoxVisitorWithResult<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				final IContentBox closestOnLine = neighbours.getClosestOnLine().box;
				if (closestOnLine != null) {
					return closestOnLine;
				}
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}
		});
	}

	private static class Neighbour {
		public static final Neighbour NULL = new Neighbour(null, Integer.MAX_VALUE);

		public final IContentBox box;
		public final int distance;

		public Neighbour(final IContentBox box, final int distance) {
			this.box = box;
			this.distance = distance;
		}
	}

	private static class Neighbourhood {
		private Neighbour above = Neighbour.NULL;
		private Neighbour below = Neighbour.NULL;
		private Neighbour left = Neighbour.NULL;
		private Neighbour right = Neighbour.NULL;

		public void setAbove(final IContentBox box, final int distance, final boolean overwrite) {
			if (above == null || overwrite && above.distance >= distance) {
				above = new Neighbour(box, distance);
			}
		}

		public void setBelow(final IContentBox box, final int distance, final boolean overwrite) {
			if (below == null || overwrite && below.distance >= distance) {
				below = new Neighbour(box, distance);
			}
		}

		public void setLeft(final IContentBox box, final int distance, final boolean overwrite) {
			if (left == null || overwrite && left.distance >= distance) {
				left = new Neighbour(box, distance);
			}
		}

		public void setRight(final IContentBox box, final int distance, final boolean overwrite) {
			if (right == null || overwrite && right.distance >= distance) {
				right = new Neighbour(box, distance);
			}
		}

		public Neighbour getClosestOnLine() {
			return closest(left, right);
		}

		private Neighbour closest(final Neighbour neighbour1, final Neighbour neighbour2) {
			if (neighbour1.distance < neighbour2.distance) {
				return neighbour1;
			} else {
				return neighbour2;
			}
		}
	}
}
