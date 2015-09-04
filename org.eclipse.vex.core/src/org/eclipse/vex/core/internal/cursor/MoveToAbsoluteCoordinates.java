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
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.ParentTraversal;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.cursor.ContentMap.Environment;

/**
 * @author Florian Thienel
 */
public class MoveToAbsoluteCoordinates implements ICursorMove {

	private final int x;
	private final int y;

	public MoveToAbsoluteCoordinates(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean preferX() {
		return true;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final IContentBox box = findClosestBoxOnLineByCoordinates(contentMap, x, y);
		if (box.containsCoordinates(x, y)) {
			return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
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

	private static IContentBox findClosestBoxOnLineByCoordinates(final ContentMap contentMap, final int x, final int y) {
		final Environment environment = contentMap.findEnvironmentForCoordinates(x, y, false);
		if (environment.deepestContainer == null) {
			return contentMap.getOutmostContentBox();
		}
		return environment.deepestContainer.accept(new BaseBoxVisitorWithResult<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				final IContentBox closestOnLine = environment.neighbours.getClosestOnLine().box;
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

	private static boolean isLastEnclosedBox(final IContentBox enclosedBox) {
		final IContentBox parent = findParentContentBox(enclosedBox);
		if (parent == null) {
			return true;
		}
		return enclosedBox.getEndOffset() == parent.getEndOffset() - 1;
	}

	private static IContentBox findParentContentBox(final IContentBox child) {
		return child.accept(new ParentTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final StructuralNodeReference box) {
				if (child == box) {
					return super.visit(box);
				}
				return box;
			}
		});
	}

}
