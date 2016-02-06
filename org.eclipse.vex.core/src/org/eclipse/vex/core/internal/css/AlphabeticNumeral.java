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

/**
 * @author Florian Thienel
 */
public class AlphabeticNumeral {

	private static final char[] LATIN_ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	private static final char[] GREEK_ALPHABET = "αβγδεζηθικλμνξοπρστυφχψω".toCharArray();

	private final int value;
	private final String numeral;

	public static AlphabeticNumeral toLatin(final int value) {
		return new AlphabeticNumeral(LATIN_ALPHABET, value);
	}

	public static AlphabeticNumeral toGreek(final int value) {
		return new AlphabeticNumeral(GREEK_ALPHABET, value);
	}

	public AlphabeticNumeral(final char[] alphabet, final int value) {
		this.value = value;
		numeral = numeral(alphabet, value);
	}

	private static String numeral(final char[] alphabet, final int value) {
		if (value <= 0) {
			return "";
		}
		final StringBuilder numeral = new StringBuilder();

		int i = value;
		while (i > 0) {
			final int remainder = (i - 1) % alphabet.length;
			numeral.insert(0, alphabet[remainder]);
			i = (i - 1) / alphabet.length;
		}

		return numeral.toString();
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
		final AlphabeticNumeral other = (AlphabeticNumeral) obj;
		if (numeral == null) {
			if (other.numeral != null) {
				return false;
			}
		} else if (!numeral.equals(other.numeral)) {
			return false;
		}
		return true;
	}

}
