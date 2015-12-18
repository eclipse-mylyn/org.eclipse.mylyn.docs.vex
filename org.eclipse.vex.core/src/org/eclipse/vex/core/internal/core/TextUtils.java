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

public class TextUtils {

	public static int countWhitespaceAtStart(final String text) {
		int whitespaceCount = 0;
		while (whitespaceCount < text.length()) {
			if (Character.isWhitespace(text.charAt(whitespaceCount))) {
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
			if (Character.isWhitespace(text.charAt(i))) {
				whitespaceCount += 1;
			} else {
				break;
			}
		}
		return whitespaceCount;
	}

}
