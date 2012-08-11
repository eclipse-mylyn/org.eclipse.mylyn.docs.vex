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
package org.eclipse.vex.core.internal.core;

/**
 * Toolkit-independent insets.
 */
public class Insets {

	private final int top;
	private final int left;
	private final int bottom;
	private final int right;

	/** Zero insets */
	public static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

	/**
	 * Class constructor.
	 * 
	 * @param top
	 *            Top inset.
	 * @param left
	 *            Left inset.
	 * @param bottom
	 *            Bottom inset.
	 * @param right
	 *            Right inset.
	 */
	public Insets(final int top, final int left, final int bottom, final int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	/**
	 * @return Returns the top.
	 */
	public int getTop() {
		return top;
	}

	/**
	 * @return Returns the left.
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @return Returns the bottom.
	 */
	public int getBottom() {
		return bottom;
	}

	/**
	 * Returns the right inset.
	 */
	public int getRight() {
		return right;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(80);
		sb.append(Insets.class.getName());
		sb.append("[top=");
		sb.append(getTop());
		sb.append(",left=");
		sb.append(getLeft());
		sb.append(",bottom=");
		sb.append(getBottom());
		sb.append(",right=");
		sb.append(getRight());
		sb.append("]");
		return sb.toString();
	}

}
