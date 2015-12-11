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

import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class MoveLeft implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		int nextOffset = Math.max(0, currentOffset - 1);

		final IContentBox searchStartBox = getSearchStartBox(currentBox, nextOffset);
		while (contentTopology.findBoxForPosition(nextOffset, searchStartBox) == null && nextOffset > 0) {
			nextOffset = Math.max(0, nextOffset - 1);
		}

		return nextOffset;
	}

	private static IContentBox getSearchStartBox(final IContentBox currentBox, final int nextOffset) {
		final IContentBox parentBox = ContentTopology.getParentContentBox(currentBox);
		if (parentBox == null) {
			return currentBox;
		}
		if (parentBox.getRange().contains(nextOffset)) {
			return parentBox;
		}
		return getSearchStartBox(parentBox, nextOffset);
	}

	@Override
	public boolean preferX() {
		return true;
	}

	@Override
	public boolean isAbsolute() {
		return false;
	}

}
