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

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.ParentTraversal;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class ContentTopology {

	private RootBox rootBox;
	private IContentBox outmostContentBox;

	public void setRootBox(final RootBox rootBox) {
		this.rootBox = rootBox;
		outmostContentBox = findOutmostContentBox(rootBox);
	}

	private static IContentBox findOutmostContentBox(final RootBox rootBox) {
		return rootBox.accept(new DepthFirstBoxTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				return box;
			}

			@Override
			public IContentBox visit(final InlineNodeReference box) {
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
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
		return findBoxForPosition(offset, rootBox);
	}

	public IContentBox findBoxForPosition(final int offset, final IBox startBox) {
		if (startBox == null) {
			if (rootBox == null) {
				return null;
			}
			return findBoxForPosition(offset, rootBox);
		}

		return startBox.accept(new DepthFirstBoxTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box.getStartOffset() == offset || box.getEndOffset() == offset) {
					final IContentBox childBox = box.getComponent().accept(this);
					if (childBox != null) {
						return childBox;
					}
					return box;
				}
				if (box.getStartOffset() < offset && box.getEndOffset() > offset) {
					return box.getComponent().accept(this);
				}
				return null;
			}

			@Override
			public IContentBox visit(final InlineNodeReference box) {
				if (box.getStartOffset() == offset || box.getEndOffset() == offset) {
					final IContentBox childBox = box.getComponent().accept(this);
					if (childBox != null) {
						return childBox;
					}
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

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				if (box.getStartOffset() <= offset && box.getEndOffset() >= offset) {
					return box;
				}
				return null;
			}
		});
	}

	public IContentBox findBoxForRange(final ContentRange range) {
		return rootBox.accept(new DepthFirstBoxTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box.getRange().contains(range)) {
					final IContentBox childBox = box.getComponent().accept(this);
					if (childBox == null) {
						return box;
					} else {
						return childBox;
					}
				}

				return null;
			}

			@Override
			public IContentBox visit(final InlineNodeReference box) {
				if (box.getRange().contains(range)) {
					final IContentBox childBox = box.getComponent().accept(this);
					if (childBox == null) {
						return box;
					} else {
						return childBox;
					}
				}
				return null;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box.getRange().contains(range)) {
					return box;
				}
				return null;
			}

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				if (box.getRange().contains(range)) {
					return box;
				}
				return null;
			}
		});
	}

	public IContentBox findBoxForCoordinates(final int x, final int y) {
		if (outmostContentBox == null) {
			return null;
		}

		return outmostContentBox.accept(new DepthFirstBoxTraversal<IContentBox>() {
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
			public IContentBox visit(final InlineNodeReference box) {
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

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				if (!box.containsCoordinates(x, y)) {
					return null;
				}
				return box;
			}
		});
	}

	public Collection<IContentBox> findBoxesForNode(final INode node) {
		return rootBox.accept(new DepthFirstBoxTraversal<Collection<IContentBox>>() {
			private final LinkedList<IContentBox> boxesForNode = new LinkedList<IContentBox>();

			@Override
			public Collection<IContentBox> visit(final RootBox box) {
				super.visit(box);
				return boxesForNode;
			}

			@Override
			public Collection<IContentBox> visit(final StructuralNodeReference box) {
				if (node == box.getNode()) {
					boxesForNode.add(box);
					return null;
				}
				if (box.getStartOffset() > node.getEndOffset()) {
					return boxesForNode;
				}
				if (!box.getRange().intersects(node.getRange())) {
					return null;
				}
				super.visit(box);
				return null;
			}

			@Override
			public Collection<IContentBox> visit(final InlineNodeReference box) {
				if (node == box.getNode()) {
					boxesForNode.add(box);
				}
				if (box.getStartOffset() > node.getEndOffset()) {
					return boxesForNode;
				}
				if (!box.getRange().intersects(node.getRange())) {
					return null;
				}
				super.visit(box);
				return null;
			}
		});
	}

	public static IContentBox getParentContentBox(final IContentBox childBox) {
		return childBox.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (box == childBox) {
					return super.visit(box);
				}
				return box;
			}

			@Override
			public IContentBox visit(final InlineNodeReference box) {
				if (box == childBox) {
					return super.visit(box);
				}
				return box;
			}
		});
	}

	public static int verticalDistance(final IContentBox box, final int y) {
		return box.accept(new BaseBoxVisitorWithResult<Integer>(0) {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				return Math.abs(y - box.getAbsoluteTop() - box.getHeight());
			}

			@Override
			public Integer visit(final InlineNodeReference box) {
				return Math.abs(y - box.getAbsoluteTop() - box.getBaseline());
			}

			@Override
			public Integer visit(final TextContent box) {
				return Math.abs(y - box.getAbsoluteTop() - box.getBaseline());
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
				return Math.abs(y - box.getAbsoluteTop() - box.getBaseline());
			}
		});
	}

	public static IContentBox findHorizontallyClosestContentBox(final Iterable<? extends IContentBox> candidates, final int x) {
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

	public static int horizontalDistance(final IBox box, final int x) {
		if (box.getAbsoluteLeft() > x) {
			return box.getAbsoluteLeft() - x;
		}
		if (box.getAbsoluteLeft() + box.getWidth() < x) {
			return x - box.getAbsoluteLeft() - box.getWidth();
		}
		return 0;
	}

}
