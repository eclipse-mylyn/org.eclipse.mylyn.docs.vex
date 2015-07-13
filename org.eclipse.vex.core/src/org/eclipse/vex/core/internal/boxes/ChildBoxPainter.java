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

import java.util.List;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class ChildBoxPainter {

	public static void paint(final List<? extends IChildBox> children, final Graphics graphics) {
		if (children.isEmpty()) {
			return;
		}
		final Rectangle clipBounds = graphics.getClipBounds();
		int i = findIndexOfFirstVisibleChild(children, clipBounds);
		while (i < children.size()) {
			final IChildBox child = children.get(i);
			if (!child.getBounds().intersects(clipBounds)) {
				break;
			}

			paint(child, graphics);

			i += 1;
		}
	}

	public static void paint(final IChildBox child, final Graphics graphics) {
		graphics.moveOrigin(child.getLeft(), child.getTop());
		child.paint(graphics);
		graphics.moveOrigin(-child.getLeft(), -child.getTop());
	}

	private static int findIndexOfFirstVisibleChild(final List<? extends IChildBox> children, final Rectangle clipBounds) {
		int lowerBound = 0;
		int upperBound = children.size() - 1;

		while (upperBound - lowerBound > 1) {
			final int pivotIndex = center(lowerBound, upperBound);
			final Rectangle pivotBounds = children.get(pivotIndex).getBounds();

			if (pivotBounds.below(clipBounds)) {
				upperBound = pivotIndex;
			} else if (pivotBounds.above(clipBounds)) {
				lowerBound = pivotIndex;
			} else {
				upperBound = pivotIndex;
			}
		}

		if (children.get(lowerBound).getBounds().intersects(clipBounds)) {
			return lowerBound;
		}
		return upperBound;
	}

	private static int center(final int lowerBound, final int upperBound) {
		return lowerBound + (upperBound - lowerBound) / 2;
	}
}
