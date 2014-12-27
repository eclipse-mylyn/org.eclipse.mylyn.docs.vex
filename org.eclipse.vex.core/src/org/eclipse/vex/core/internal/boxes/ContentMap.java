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

	public void setRootBox(final RootBox rootBox) {
		this.rootBox = rootBox;
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

	public IContentBox findBoxByCoordinates(final int x, final int y) {
		return rootBox.accept(new DepthFirstTraversal<IContentBox>() {

			private IContentBox nearBy;

			@Override
			public IContentBox visit(final NodeReference box) {
				if (!containsCoordinates(box, x, y)) {
					return null;
				}

				final IContentBox componentResult = box.getComponent().accept(this);
				if (componentResult != null) {
					return componentResult;
				}
				if (nearBy != null && (rightFromX(nearBy, x) && isFirstEnclosedBox(box, nearBy) || leftFromX(nearBy, x) && isLastEnclosedBox(box, nearBy))) {
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
				if (containsCoordinates(box, x, y)) {
					return box;
				}
				if (containsY(box, y)) {
					nearBy = box;
				}
				return null;
			}

			private boolean containsCoordinates(final IBox box, final int x, final int y) {
				return x >= box.getAbsoluteLeft() && x <= box.getAbsoluteLeft() + box.getWidth() && containsY(box, y);
			}

			private boolean containsY(final IBox box, final int y) {
				return y >= box.getAbsoluteTop() && y <= box.getAbsoluteTop() + box.getHeight();
			}

			private boolean rightFromX(final IBox box, final int x) {
				return x < box.getAbsoluteLeft();
			}

			private boolean leftFromX(final IBox box, final int x) {
				return x > box.getAbsoluteLeft() + box.getWidth();
			}
		});
	}
}
