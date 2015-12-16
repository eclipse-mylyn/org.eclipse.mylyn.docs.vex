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
public class Border {

	public static final Border NULL = new Border(BorderLine.NULL);

	public final BorderLine top;
	public final BorderLine left;
	public final BorderLine bottom;
	public final BorderLine right;

	public Border(final int size) {
		this(size, size, size, size);
	}

	public Border(final int vertical, final int horizontal) {
		this(vertical, horizontal, vertical, horizontal);
	}

	public Border(final int top, final int left, final int bottom, final int right) {
		this(new BorderLine(top), new BorderLine(left), new BorderLine(bottom), new BorderLine(right));
	}

	public Border(final BorderLine border) {
		this(border, border, border, border);
	}

	public Border(final BorderLine top, final BorderLine left, final BorderLine bottom, final BorderLine right) {
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
		final Border other = (Border) obj;
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
		return "Border [top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right + "]";
	}
}
