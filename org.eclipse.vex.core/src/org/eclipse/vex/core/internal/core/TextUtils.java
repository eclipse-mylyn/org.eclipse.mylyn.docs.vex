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
package org.eclipse.vex.core.internal.core;

import java.util.regex.Pattern;

import org.eclipse.vex.core.XML;

public class TextUtils {

	public static final char CURRENCY_SIGN = '\u00A4';
	public static final char PARAGRAPH_SIGN = '\u00B6';
	public static final char RAQUO = '\u00BB';

	public static final Pattern ANY_LINE_BREAKS = Pattern.compile("(\r\n|\r|\n)");

	public static int countWhitespaceAtStart(final String text) {
		int whitespaceCount = 0;
		while (whitespaceCount < text.length()) {
			if (XML.isWhitespace(text.charAt(whitespaceCount))) {
				whitespaceCount += 1;
			} else {
				break;
			}
		}
		return whitespaceCount;
	}

	public static int countWhitespaceAtEnd(final String text) {
		int whitespaceCount = 0;
		while (whitespaceCount < text.length()) {
			final int i = text.length() - 1 - whitespaceCount;
			if (XML.isWhitespace(text.charAt(i))) {
				whitespaceCount += 1;
			} else {
				break;
			}
		}
		return whitespaceCount;
	}

	public static String[] lines(final String s) {
		return ANY_LINE_BREAKS.split(s, -1);
	}
}
