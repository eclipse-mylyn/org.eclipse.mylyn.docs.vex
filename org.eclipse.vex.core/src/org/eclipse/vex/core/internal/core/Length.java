/*******************************************************************************
 * Copyright (c) 2004, 2016 John Krasnay and others.
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
public abstract class Length {

	public static final Length ZERO = new Length() {
		@Override
		public int get(final int referenceLength) {
			return 0;
		}

		@Override
		public int getBaseValue() {
			return 0;
		}
	};

	/**
	 * @return a length representing an absolute value.
	 */
	public static Length absolute(final int value) {
		return new Length() {
			@Override
			public int get(final int referenceLength) {
				return value;
			}

			@Override
			public int getBaseValue() {
				return value;
			}
		};
	}

	/**
	 * @return a length representing a relative value.
	 */
	public static Length relative(final float percentage) {
		return new Length() {
			@Override
			public int get(final int referenceLength) {
				return Math.round(percentage * referenceLength);
			}

			@Override
			public int getBaseValue() {
				return Float.floatToIntBits(percentage);
			}
		};
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
	public abstract int get(final int referenceLength);

	public abstract int getBaseValue();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getBaseValue();
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
		if (getBaseValue() != other.getBaseValue()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Length [baseValue=" + getBaseValue() + "]";
	}

}
