/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.batik.css.parser.LexicalUnits;
import org.apache.batik.css.parser.ParseException;
import org.apache.batik.css.parser.Scanner;

/**
 * This class extends {@link org.apache.batik.css.parser.Scanner} with a crude hack to support 'pseudo namespaces'. This
 * is no real namespace support, but will make defined stylesheet compatible with a future CSS3 version of VEX.
 */
public class CssScanner extends Scanner {

	/**
	 * Creates a new Scanner object.
	 *
	 * @param r
	 *            The reader to scan.
	 */
	public CssScanner(final Reader r) throws ParseException {
		super(r);
	}

	/**
	 * Creates a new Scanner object.
	 *
	 * @param is
	 *            The input stream to scan.
	 * @param enc
	 *            The encoding to use to decode the input stream, or null.
	 */
	public CssScanner(final InputStream is, final String enc) throws ParseException {
		super(is, enc);
	}

	/**
	 * Creates a new Scanner object.
	 *
	 * @param s
	 *            The string to scan.
	 */
	public CssScanner(final String s) throws ParseException {
		super(s);
	}

	/**
	 * Returns the last scanned character.
	 *
	 * @return
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Joins two subsequent identifiers which are seperated by single char.
	 *
	 * @throws ParseException
	 */
	public void joinIdentifier() throws ParseException {
		final int lastStart = start;
		try {
			nextChar(); // Skip the current char
			final int type = next();
			if (type == LexicalUnits.IDENTIFIER) {
				start = lastStart;
			} else {
				throw new ParseException("identifier.character", reader.getLine(), reader.getColumn());
			}
		} catch (final IOException e) {
			throw new ParseException(e);
		}

	}
}
