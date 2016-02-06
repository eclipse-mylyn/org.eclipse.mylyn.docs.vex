/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

public class RomanNumeral {

	private final int value;
	private final String numeral;

	public RomanNumeral(final int value) {
		this.value = value;
		numeral = toRoman(value);
	}

	private static String toRoman(final int value) {
		for (final Digit digit : Digit.values()) {
			if (value >= digit.value) {
				return digit.toString() + toRoman(value - digit.value);
			}
		}
		return "";
	}

	public int intValue() {
		return value;
	}

	@Override
	public String toString() {
		return numeral;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (numeral == null ? 0 : numeral.hashCode());
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
		final RomanNumeral other = (RomanNumeral) obj;
		if (numeral == null) {
			if (other.numeral != null) {
				return false;
			}
		} else if (!numeral.equals(other.numeral)) {
			return false;
		}
		return true;
	}

	private static enum Digit {

		M(1000), CM(900), D(500), CD(400), C(100), XC(90), L(50), XL(40), X(10), IX(9), V(5), IV(4), I(1);

		public final int value;

		private Digit(final int value) {
			this.value = value;
		}
	}

}
