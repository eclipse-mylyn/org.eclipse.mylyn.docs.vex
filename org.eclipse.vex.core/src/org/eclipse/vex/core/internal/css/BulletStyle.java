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

import org.eclipse.vex.core.internal.core.Image;

public class BulletStyle {

	public final Type type;
	public final Position position;
	public final Image image;
	public final char character;

	public static BulletStyle fromStyles(final Styles styles) {
		return new BulletStyle(getBulletType(styles), Position.OUTSIDE, null, '\0');
	}

	private static Type getBulletType(final Styles styles) {
		final String listStyleType = styles.getListStyleType();
		if (CSS.DECIMAL.equals(listStyleType)) {
			return Type.DECIMAL;
		} else if (CSS.DECIMAL_LEADING_ZERO.equals(listStyleType)) {
			return Type.DECIMAL_LEADING_ZERO;
		} else if (CSS.LOWER_ROMAN.equals(listStyleType)) {
			return Type.LOWER_ROMAN;
		} else if (CSS.UPPER_ROMAN.equals(listStyleType)) {
			return Type.UPPER_ROMAN;
		} else if (CSS.LOWER_ALPHA.equals(listStyleType)) {
			return Type.LOWER_LATIN;
		} else if (CSS.UPPER_ALPHA.equals(listStyleType)) {
			return Type.UPPER_LATIN;
		} else if (CSS.LOWER_LATIN.equals(listStyleType)) {
			return Type.LOWER_LATIN;
		} else if (CSS.UPPER_LATIN.equals(listStyleType)) {
			return Type.UPPER_LATIN;
		} else if (CSS.LOWER_GREEK.equals(listStyleType)) {
			return Type.LOWER_GREEK;
		} else if (CSS.DISC.equals(listStyleType)) {
			return Type.DISC;
		} else if (CSS.CIRCLE.equals(listStyleType)) {
			return Type.CIRCLE;
		} else if (CSS.SQUARE.equals(listStyleType)) {
			return Type.SQUARE;
		} else {
			return Type.NONE;
		}
	}

	public BulletStyle(final Type type, final Position position, final Image image, final char character) {
		this.type = type;
		this.position = position;
		this.image = image;
		this.character = character;
	}

	public String getBulletAsText(final int zeroBasedIndex, final int itemCount) {
		switch (type) {
		case DECIMAL:
			return toDecimal(zeroBasedIndex);
		case DECIMAL_LEADING_ZERO:
			return toDecimalWithLeadingZeroes(zeroBasedIndex, itemCount);
		case LOWER_ROMAN:
			return toLowerRoman(zeroBasedIndex);
		case UPPER_ROMAN:
			return toUpperRoman(zeroBasedIndex);
		case LOWER_LATIN:
			return toLowerLatin(zeroBasedIndex);
		case UPPER_LATIN:
			return toUpperLatin(zeroBasedIndex);
		case LOWER_GREEK:
			return toLowerGreek(zeroBasedIndex);
		default:
			return "";
		}
	}

	public static String toDecimal(final int zeroBasedIndex) {
		return String.format("%d.", zeroBasedIndex + 1);
	}

	public static String toDecimalWithLeadingZeroes(final int zeroBasedIndex, final int itemCount) {
		final int digitCount = Integer.toString(itemCount).length();
		return String.format("%0" + digitCount + "d.", zeroBasedIndex + 1);
	}

	public static String toLowerRoman(final int zeroBasedIndex) {
		return toUpperRoman(zeroBasedIndex).toLowerCase();
	}

	public static String toUpperRoman(final int zeroBasedIndex) {
		return new RomanNumeral(zeroBasedIndex + 1).toString() + ".";
	}

	public static String toLowerLatin(final int zeroBasedIndex) {
		return AlphabeticNumeral.toLatin(zeroBasedIndex + 1).toString() + ".";
	}

	public static String toUpperLatin(final int zeroBasedIndex) {
		return toLowerLatin(zeroBasedIndex).toUpperCase();
	}

	public static String toLowerGreek(final int zeroBasedIndex) {
		return AlphabeticNumeral.toGreek(zeroBasedIndex + 1).toString() + ".";
	}

	public static enum Type {
		NONE, DECIMAL, DECIMAL_LEADING_ZERO, LOWER_ROMAN, UPPER_ROMAN, LOWER_LATIN, UPPER_LATIN, LOWER_GREEK, DISC, CIRCLE, SQUARE;

		public boolean isTextual() {
			if (this == NONE) {
				return false;
			}
			return ordinal() < DISC.ordinal();
		}

		public boolean isGraphical() {
			if (this == NONE) {
				return false;
			}
			return ordinal() >= DISC.ordinal();
		}
	}

	public static enum Position {
		INSIDE, OUTSIDE;
	}
}
