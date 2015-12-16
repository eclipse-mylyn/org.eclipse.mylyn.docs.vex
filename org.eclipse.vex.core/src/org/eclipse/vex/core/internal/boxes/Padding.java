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

import org.eclipse.vex.core.internal.core.Length;

/**
 * @author Florian Thienel
 */
public class Padding {

	public static final Padding NULL = new Padding(0);

	public final Length top;
	public final Length left;
	public final Length bottom;
	public final Length right;

	public Padding(final int size) {
		this(size, size, size, size);
	}

	public Padding(final int vertical, final int horizontal) {
		this(vertical, horizontal, vertical, horizontal);
	}

	public Padding(final int top, final int left, final int bottom, final int right) {
		this(Length.absolute(top), Length.absolute(left), Length.absolute(bottom), Length.absolute(right));
	}

	public Padding(final Length top, final Length left, final Length bottom, final Length right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bottom == null ? 0 : bottom.hashCode());
		result = prime * result + (left == null ? 0 : left.hashCode());
		result = prime * result + (right == null ? 0 : right.hashCode());
		result = prime * result + (top == null ? 0 : top.hashCode());
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
		if (bottom == null) {
			if (other.bottom != null) {
				return false;
			}
		} else if (!bottom.equals(other.bottom)) {
			return false;
		}
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		if (top == null) {
			if (other.top != null) {
				return false;
			}
		} else if (!top.equals(other.top)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Padding [top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right + "]";
	}

}
