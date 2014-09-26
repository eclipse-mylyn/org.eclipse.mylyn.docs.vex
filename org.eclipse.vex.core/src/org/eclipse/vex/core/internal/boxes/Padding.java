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
public class Padding {

	public static final Padding NULL = new Padding(0);

	public final int top;
	public final int left;
	public final int bottom;
	public final int right;

	public Padding(final int size) {
		this(size, size, size, size);
	}

	public Padding(final int vertical, final int horizontal) {
		this(vertical, horizontal, vertical, horizontal);
	}

	public Padding(final int top, final int left, final int bottom, final int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bottom;
		result = prime * result + left;
		result = prime * result + right;
		result = prime * result + top;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Padding other = (Padding) obj;
		if (bottom != other.bottom) {
			return false;
		}
		if (left != other.left) {
			return false;
		}
		if (right != other.right) {
			return false;
		}
		if (top != other.top) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Padding [top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right + "]";
	}

}
