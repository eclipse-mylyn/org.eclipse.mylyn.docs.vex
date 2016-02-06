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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BulletStyleTest {

	@Test
	public void decimalBulletText() throws Exception {
		assertEquals("1.", BulletStyle.toDecimal(0));
		assertEquals("9.", BulletStyle.toDecimal(8));
		assertEquals("10.", BulletStyle.toDecimal(9));
	}

	@Test
	public void decimalWithLeadingZerosBulletText() throws Exception {
		assertEquals("1 digit", "1.", BulletStyle.toDecimalWithLeadingZeroes(0, 1));
		assertEquals("2 digits", "01.", BulletStyle.toDecimalWithLeadingZeroes(0, 12));
		assertEquals("3 digits", "001.", BulletStyle.toDecimalWithLeadingZeroes(0, 123));
		assertEquals("4 digits", "0001.", BulletStyle.toDecimalWithLeadingZeroes(0, 1234));
	}

	@Test
	public void lowerRomanBulletText() throws Exception {
		assertEquals("i.", BulletStyle.toLowerRoman(0));
		assertEquals("ii.", BulletStyle.toLowerRoman(1));
		assertEquals("ix.", BulletStyle.toLowerRoman(8));
		assertEquals("x.", BulletStyle.toLowerRoman(9));
	}

	@Test
	public void upperRomanBulletText() throws Exception {
		assertEquals("I.", BulletStyle.toUpperRoman(0));
		assertEquals("II.", BulletStyle.toUpperRoman(1));
		assertEquals("IX.", BulletStyle.toUpperRoman(8));
		assertEquals("X.", BulletStyle.toUpperRoman(9));
	}

	@Test
	public void lowerLatinBulletText() throws Exception {
		assertEquals("a.", BulletStyle.toLowerLatin(0));
		assertEquals("b.", BulletStyle.toLowerLatin(1));
		assertEquals("y.", BulletStyle.toLowerLatin(24));
		assertEquals("z.", BulletStyle.toLowerLatin(25));
		assertEquals("aa.", BulletStyle.toLowerLatin(26));
		assertEquals("ab.", BulletStyle.toLowerLatin(27));
		assertEquals("ba.", BulletStyle.toLowerLatin(52));
		assertEquals("zz.", BulletStyle.toLowerLatin(701));
		assertEquals("aaa.", BulletStyle.toLowerLatin(702));
		assertEquals("azz.", BulletStyle.toLowerLatin(1377));
		assertEquals("baa.", BulletStyle.toLowerLatin(1378));
		assertEquals("zzz.", BulletStyle.toLowerLatin(18277));
		assertEquals("aaaa.", BulletStyle.toLowerLatin(18278));
	}
}
