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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class MoveUp implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentMap contentMap, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		if (currentOffset == 0) {
			return currentOffset;
		}

		if (isAtEndOfEmptyBox(currentOffset, currentBox)) {
			return currentBox.getStartOffset();
		}

		final int x = preferredX;
		final int y = hotArea.getY();
		final IContentBox box = contentMap.findClosestBoxAbove(x, y);
		return box.getOffsetForCoordinates(graphics, x - box.getAbsoluteLeft(), y - box.getAbsoluteTop());
	}

	@Override
	public boolean preferX() {
		return false;
	}

	private static boolean isAtEndOfEmptyBox(final int offset, final IContentBox box) {
		return offset == box.getEndOffset() && box.getStartOffset() == box.getEndOffset() - 1;
	}

}
