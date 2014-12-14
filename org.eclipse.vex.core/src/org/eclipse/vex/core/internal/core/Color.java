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
 * Toolkit-independent representation of a color. Colors consist of three integers in the range 0..255 representing red,
 * green, and blue components. Objects of this class are immutable.
 */
public class Color {

	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color RED = new Color(255, 0, 0);
	public static final Color GREEN = new Color(0, 255, 0);
	public static final Color BLUE = new Color(0, 0, 255);

	private final int red;
	private final int green;
	private final int blue;

	/**
	 * @param red
	 *            red value, 0..255
	 * @param green
	 *            green value, 0..255
	 * @param blue
	 *            blue value, 0..255
	 */
	public Color(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/**
	 * Returns the blue component of the color, in the range 0..255
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * Returns the green component of the color, in the range 0..255
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * Returns the red component of the color, in the range 0..255
	 */
	public int getRed() {
		return red;
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

		final Color other = (Color) obj;
		return red == other.red && green == other.green && blue == other.blue;
	}

	@Override
	public int hashCode() {
		return red + green << 16 + blue << 24;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(20);
		sb.append("Color[r=");
		sb.append(red);
		sb.append(",g=");
		sb.append(green);
		sb.append(",b=");
		sb.append(blue);
		sb.append("]");
		return sb.toString();
	}

}
