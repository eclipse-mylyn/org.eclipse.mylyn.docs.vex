/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

/**
 * An empty inline box that simply takes up space.
 */
public class SpaceBox extends AbstractInlineBox {

	/**
	 * Class constructor.
	 * 
	 * @param width
	 *            width of the box
	 * @param height
	 *            height of the box
	 */
	public SpaceBox(final int width, final int height) {
		setWidth(width);
		setHeight(height);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	public int getBaseline() {
		return getHeight();
	}

	public boolean isEOL() {
		return false;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#split(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, boolean)
	 */
	public Pair split(final LayoutContext context, final int maxWidth, final boolean force) {
		return new Pair(null, this);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[spacer]";
	}

}
