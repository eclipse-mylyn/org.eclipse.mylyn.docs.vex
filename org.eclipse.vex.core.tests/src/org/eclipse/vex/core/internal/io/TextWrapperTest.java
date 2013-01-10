/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Florian Thienel - migrated to JUnit 4
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the TextWrapper class.
 */
public class TextWrapperTest {

	@Test
	public void shouldWrap() {
		String[] results;
		String[] inputs;
		final TextWrapper wrapper = new TextWrapper();

		results = wrapper.wrap(40);
		assertEquals(0, results.length);

		inputs = new String[] { "Here ", "are ", "some ", "short ", "words ", "and here are some long ones. We make sure we have some short stuff and some long stuff, just to make sure it all wraps." };

		for (final String input : inputs) {
			wrapper.add(input);
		}
		results = wrapper.wrap(40);
		assertWidth(results, 40);
		assertPreserved(inputs, results);

		wrapper.clear();
		results = wrapper.wrap(40);
		assertEquals(0, results.length);

		final String s1 = "yabba ";
		final String s3 = "yabba yabba yabba ";
		wrapper.add(s1);
		wrapper.addNoSplit(s3);
		wrapper.addNoSplit(s3);
		wrapper.add(s1);
		results = wrapper.wrap(18);
		assertEquals(4, results.length);
		assertEquals(s1, results[0]);
		assertEquals(s3, results[1]);
		assertEquals(s3, results[2]);
		assertEquals(s1, results[3]);
	}

	/**
	 * Ensure the two string arrays represent the same run of text after all elements are concatenated.
	 */
	private void assertPreserved(final String[] inputs, final String[] results) {
		final StringBuffer inputSB = new StringBuffer();
		final StringBuffer resultSB = new StringBuffer();
		for (final String input : inputs) {
			inputSB.append(input);
		}
		for (final String result : results) {
			resultSB.append(result);
		}
		assertEquals(inputSB.toString(), resultSB.toString());
	}

	/**
	 * Ensure all lines fit within the given width, and that adding an extra token from the next line would blow it.
	 */
	private void assertWidth(final String[] results, final int width) {
		for (int i = 0; i < results.length; i++) {
			assertTrue(results[i].length() > 0);
			assertTrue(results[i].length() <= width);
			if (i < results.length - 1) {
				assertTrue(results[i].length() + getToken(results[i + 1]).length() > width);
			}
		}
	}

	/**
	 * Get a token from a string.
	 */
	private String getToken(final String s) {
		int i = 0;
		while (i < s.length() && !Character.isWhitespace(s.charAt(i))) {
			i++;
		}
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
		return s.substring(0, i);
	}

}
