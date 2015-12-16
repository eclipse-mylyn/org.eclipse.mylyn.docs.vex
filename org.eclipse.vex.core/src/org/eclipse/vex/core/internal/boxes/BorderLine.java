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

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.LineStyle;

public class BorderLine {

	public static final BorderLine NULL = new BorderLine(0);

	public final int width;
	public final LineStyle style;
	public final Color color;

	public BorderLine(final int width) {
		this(width, LineStyle.SOLID, Color.BLACK);
	}

	public BorderLine(final int width, final LineStyle style, final Color color) {
		this.width = width;
		this.style = style;
		this.color = color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color == null ? 0 : color.hashCode());
		result = prime * result + (style == null ? 0 : style.hashCode());
		result = prime * result + width;
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
		final BorderLine other = (BorderLine) obj;
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (style != other.style) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SingleBorder [width=" + width + ", style=" + style + ", color=" + color + "]";
	}
}
