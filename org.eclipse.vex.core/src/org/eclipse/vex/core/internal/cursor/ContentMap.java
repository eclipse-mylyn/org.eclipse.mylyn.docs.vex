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
package org.eclipse.vex.core.internal.cursor;

import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;

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

	public int getLastOffset() {
		if (outmostContentBox == null) {
			return 0;
		}
		return outmostContentBox.getEndOffset();
	}

	public IContentBox getOutmostContentBox() {
		return outmostContentBox;
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
			public IContentBox visit(final TextContent box) {
				if (box.getStartOffset() <= offset && box.getEndOffset() >= offset) {
					return box;
				}
				return null;
			}
		});
	}

	public Environment findEnvironmentForCoordinates(final int x, final int y, final boolean preferClosest) {
		final IContentBox[] deepestContainer = new IContentBox[1];
		final Neighbourhood neighbours = new Neighbourhood();
		rootBox.accept(new DepthFirstTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final NodeReference box) {
				super.visit(box);
				if (box.isAbove(y)) {
					neighbours.setAbove(box, y - box.getAbsoluteTop() - box.getHeight(), preferClosest);
					return null;
				} else if (box.isBelow(y)) {
					neighbours.setBelow(box, box.getAbsoluteTop() - y, preferClosest);
					return null;
				} else if (box.isRightOf(x)) {
					neighbours.setRight(box, box.getAbsoluteLeft() - x, preferClosest);
					return null;
				} else if (box.isLeftOf(x)) {
					neighbours.setLeft(box, x - box.getAbsoluteLeft() - box.getWidth(), preferClosest);
					return null;
				} else {
					if (deepestContainer[0] == null) {
						deepestContainer[0] = box;
					}
					if (neighbours.getBelow().box != null) {
						return deepestContainer[0];
					} else {
						return null;
					}
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
				} else if (box.isRightOf(x)) {
					neighbours.setRight(box, box.getAbsoluteLeft() - x, true);
					return null;
				} else if (box.isLeftOf(x)) {
					neighbours.setLeft(box, x - box.getAbsoluteLeft() - box.getWidth(), true);
					return null;
				} else {
					deepestContainer[0] = box;
					return null;
				}
			}
		});
		return new Environment(neighbours, deepestContainer[0]);
	}

	public static class Neighbour {
		public static final Neighbour NULL = new Neighbour(null, Integer.MAX_VALUE);

		public final IContentBox box;
		public final int distance;

		public Neighbour(final IContentBox box, final int distance) {
			this.box = box;
			this.distance = distance;
		}
	}

	public static class Neighbourhood {
		private Neighbour above = Neighbour.NULL;
		private Neighbour below = Neighbour.NULL;
		private Neighbour left = Neighbour.NULL;
		private Neighbour right = Neighbour.NULL;

		public void setAbove(final IContentBox box, final int distance, final boolean overwrite) {
			if (above == Neighbour.NULL || overwrite && above.distance >= distance) {
				above = new Neighbour(box, distance);
			}
		}

		public Neighbour getAbove() {
			return above;
		}

		public void setBelow(final IContentBox box, final int distance, final boolean overwrite) {
			if (below == Neighbour.NULL || overwrite && below.distance >= distance) {
				below = new Neighbour(box, distance);
			}
		}

		public Neighbour getBelow() {
			return below;
		}

		public void setLeft(final IContentBox box, final int distance, final boolean overwrite) {
			if (left == Neighbour.NULL || overwrite && left.distance >= distance) {
				left = new Neighbour(box, distance);
			}
		}

		public void setRight(final IContentBox box, final int distance, final boolean overwrite) {
			if (right == Neighbour.NULL || overwrite && right.distance >= distance) {
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

	public static class Environment {
		public final Neighbourhood neighbours;
		public final IContentBox deepestContainer;

		public Environment(final Neighbourhood neighbours, final IContentBox deepestContainer) {
			this.neighbours = neighbours;
			this.deepestContainer = deepestContainer;
		}
	}
}
