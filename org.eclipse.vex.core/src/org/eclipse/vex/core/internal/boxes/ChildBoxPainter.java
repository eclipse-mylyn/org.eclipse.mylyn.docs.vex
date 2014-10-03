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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.core.RelocatedGraphics;

/**
 * @author Florian Thienel
 */
public class ChildBoxPainter {

	public void paint(final Iterable<IChildBox> children, final Graphics graphics) {
		final Rectangle clipBounds = graphics.getClipBounds();
		for (final IChildBox child : children) {
			if (child.getBounds().intersects(clipBounds)) {
				final Graphics childGraphics = new RelocatedGraphics(graphics, child.getLeft(), child.getTop());
				child.paint(childGraphics);
			} else {
				break;
			}
		}
	}

}
