/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.cursor;

import static org.eclipse.vex.core.internal.cursor.ContentTopology.findClosestContentBoxChildAbove;
import static org.eclipse.vex.core.internal.cursor.ContentTopology.getParentContentBox;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;

public class MoveToPreviousPage implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final IViewPort viewPort, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final int x = preferredX;
		final int y = viewPort.getVisibleArea().getY() - Math.round(viewPort.getVisibleArea().getHeight() * 0.9f);

		final IContentBox outmostContentBox = contentTopology.getOutmostContentBox();
		if (y < outmostContentBox.getAbsoluteTop()) {
			return outmostContentBox.getStartOffset();
		}

		final IContentBox box = contentTopology.findClosestBoxByCoordinates(x, y);
		if (box == null) {
			return currentOffset;
		} else if (box.containsCoordinates(x, y)) {
			return findBestOffsetWithin(graphics, box, x, y);
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

	private static int findBestOffsetWithin(final Graphics graphics, final IContentBox closestBoxByCoordinates, final int x, final int y) {
		return closestBoxByCoordinates.accept(new BaseBoxVisitorWithResult<Integer>() {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				final IContentBox closestChild = findClosestContentBoxChildAbove(box, x, y);
				if (closestChild == null) {
					return box.getStartOffset();
				}
				return closestChild.accept(this);
			}

			@Override
			public Integer visit(final InlineNodeReference box) {
				return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
				return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
			}

			@Override
			public Integer visit(final TextContent box) {
				return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
			}
		});
	}

	private static boolean isLastEnclosedBox(final IContentBox enclosedBox) {
		final IContentBox parent = getParentContentBox(enclosedBox);
		if (parent == null) {
			return true;
		}
		return enclosedBox.getEndOffset() == parent.getEndOffset() - 1;
	}

	@Override
	public boolean preferX() {
		return false;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

}
