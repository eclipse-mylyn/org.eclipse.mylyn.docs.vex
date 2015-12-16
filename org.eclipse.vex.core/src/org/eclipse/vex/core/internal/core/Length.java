/*******************************************************************************
 * Copyright (c) 2004, 2015 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - generalize for use outside of the CSS package
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

/**
 * A length that may be expressed as an absolute or relative value.
 */
public class Length {

	private static final Length ZERO = new Length(0, 0, true);

	private final float percentageValue;
	private final int absoluteValue;
	private final boolean absolute;

	private Length(final float percentage, final int absolute, final boolean isAbsolute) {
		percentageValue = percentage;
		absoluteValue = absolute;
		this.absolute = isAbsolute;
	}

	/**
	 * @return a length representing an absolute value.
	 */
	public static Length absolute(final int value) {
		if (value == 0) {
			return ZERO;
		} else {
			return new Length(0, value, true);
		}
	}

	/**
	 * @return a length representing a relative value.
	 */
	public static Length relative(final float percentage) {
		return new Length(percentage, 0, false);
	}

	/**
	 * Return the value of the length given a reference value. If this object represents an absolute value, that value
	 * is simply returned. Otherwise, returns the given reference length multiplied by the given percentage and rounded
	 * to the nearest integer.
	 *
	 * @param referenceLength
	 *            reference length by which percentage lengths will by multiplied.
	 * @return the actual value
	 */
	public int get(final int referenceLength) {
		if (absolute) {
			return absoluteValue;
		} else {
			return Math.round(percentageValue * referenceLength);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absolute ? 1231 : 1237);
		result = prime * result + absoluteValue;
		result = prime * result + Float.floatToIntBits(percentageValue);
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
		final Length other = (Length) obj;
		if (absolute != other.absolute) {
			return false;
		}
		if (absoluteValue != other.absoluteValue) {
			return false;
		}
		if (Float.floatToIntBits(percentageValue) != Float.floatToIntBits(other.percentageValue)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Length [percentageValue=" + percentageValue + ", absoluteValue=" + absoluteValue + ", absolute=" + absolute + "]";
	}

}
