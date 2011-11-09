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
package org.eclipse.wst.xml.vex.core.internal.layout;

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
	public SpaceBox(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}

	/**
	 * @see org.eclipse.wst.xml.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	public int getBaseline() {
		return this.getHeight();
	}

	public boolean isEOL() {
		return false;
	}

	/**
	 * @see org.eclipse.wst.xml.vex.core.internal.layout.InlineBox#split(org.eclipse.wst.xml.vex.core.internal.layout.LayoutContext,
	 *      int, boolean)
	 */
	public Pair split(LayoutContext context, int maxWidth, boolean force) {
		return new Pair(null, this);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[spacer]";
	}

}
