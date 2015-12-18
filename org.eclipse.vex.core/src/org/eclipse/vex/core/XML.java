/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - extracted whitespace handling from DocumentBuilder (bug 408453)
 *******************************************************************************/
package org.eclipse.vex.core;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Namespace;

/**
 * Common processing methods according to http://www.w3.org/TR/REC-xml/
 *
 * @see http://www.w3.org/TR/REC-xml/#sec-white-space
 * @see http://www.w3.org/TR/REC-xml/#sec-line-ends
 * @see http://www.w3.org/TR/REC-xml/#NT-S
 * @see http://www.w3.org/TR/xmlbase/
 */
public class XML {

	public static final String VALIDATE_OK = "OK";

	/**
	 * The xml:base attribute re-defines the base URI for a part of an XML document, according to the XML Base
	 * Recommendation.
	 *
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	public static final QualifiedName BASE_ATTRIBUTE = new QualifiedName(Namespace.XML_NAMESPACE_URI, "base");

	/**
	 * @param c
	 * @return <code>true</code> if c is a whitespace according to the W3C recommendation<br />
	 *         (http://www.w3.org/TR/REC-xml/#NT-S)
	 */
	public static boolean isWhitespace(final char c) {
		return c == 0x20 || c == 0x9 || c == 0xD || c == 0xA;
	}

	public static final Pattern XML_WHITESPACE_PATTERN = Pattern.compile("[\\u0020\\u0009\\u000d\\u000a]");

	/**
	 * Replace runs of XML whitespace (see {@link #isWhitespace}) with a single space. Newlines in the input should be
	 * normalized before calling this method.
	 *
	 * @param input
	 *            String to compress.
	 * @param trimLeading
	 *            <code>true</code> to remove leading whitespace
	 * @param trimTrailing
	 *            <code>true</code> to remove trailing whitespace
	 * @param keepNewlines
	 *            <code>true</code> to keep newlines (runs of newlines will still be compressed), <code>false</code> to
	 *            replace newlines with a space.
	 * @return A new String with whitespace compressed.
	 */
	public static String compressWhitespace(final String input, final boolean trimLeading, final boolean trimTrailing, final boolean keepNewlines) {
		return compressWhitespace(new StringBuilder(input), trimLeading, trimTrailing, keepNewlines).toString();
	}

	/**
	 * Replace runs of XML whitespace (see {@link #isWhitespace}) with a single space. Newlines in the input should be
	 * normalized before calling this method.
	 *
	 * @param sb
	 *            StringBuilder to compress.
	 * @param trimLeading
	 *            <code>true</code> to remove leading whitespace
	 * @param trimTrailing
	 *            <code>true</code> to remove trailing whitespace
	 * @param keepNewlines
	 *            <code>true</code> to keep newlines (runs of newlines will still be compressed), <code>false</code> to
	 *            replace newlines with a space.
	 * @return A new StringBuilder with whitespace compressed.
	 */
	public static StringBuilder compressWhitespace(final StringBuilder sb, final boolean trimLeading, final boolean trimTrailing, final boolean keepNewlines) {

		final StringBuilder result = new StringBuilder(sb.length());

		boolean ws = false; // true if we're in a run of whitespace
		char last = 0;
		for (int i = 0; i < sb.length(); i++) {
			final char c = sb.charAt(i);
			if (XML.isWhitespace(c)) {
				if (c != last && last == '\n' && keepNewlines) {
					result.append('\n');
				}
				ws = true;
			} else {
				if (ws) {
					result.append(last == '\n' && keepNewlines ? '\n' : ' ');
					ws = false;
				}
				result.append(c);
			}
			last = c;
		}
		if (ws) {
			result.append(last == '\n' && keepNewlines ? '\n' : ' ');
		}
		// trim leading and trailing space, if necessary
		if (trimLeading && result.length() > 0 && result.charAt(0) == ' ') {
			result.deleteCharAt(0);
		}
		if (trimTrailing && result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
			result.setLength(result.length() - 1);
		}

		return result;
	}

	/**
	 * Convert lines that end in CR and CRLFs to plain newlines.
	 *
	 * @param input
	 *            String to be normalized.
	 */
	public static String normalizeNewlines(final String input) {
		final StringBuilder sb = new StringBuilder(input);
		normalizeNewlines(sb);
		return sb.toString();
	}

	/**
	 * Convert lines that end in CR and CRLFs to plain newlines.
	 *
	 * @param sb
	 *            StringBuilder to be normalized.
	 */
	public static void normalizeNewlines(final StringBuilder sb) {

		// State machine states
		final int START = 0;
		final int SEEN_CR = 1;

		int state = START;
		int i = 0;
		while (i < sb.length()) {
			// No simple 'for' here, since we may delete chars

			final char c = sb.charAt(i);

			switch (state) {
			case START:
				if (c == '\r') {
					state = SEEN_CR;
				}
				i++;
				break;

			case SEEN_CR:
				if (c == '\n') {
					// CR-LF, just delete the previous CR
					sb.deleteCharAt(i - 1);
					state = START;
					// no need to advance i, since it's done implicitly
				} else if (c == '\r') {
					// CR line ending followed by another
					// Replace the first with a newline...
					sb.setCharAt(i - 1, '\n');
					i++;
					// ...and stay in the SEEN_CR state
				} else {
					// CR line ending, replace it with a newline
					sb.setCharAt(i - 1, '\n');
					i++;
					state = START;
				}
			}
		}

		if (state == SEEN_CR) {
			// CR line ending, replace it with a newline
		}
	}

	/**
	 * Validate the target of an processing instruction.
	 *
	 * @param target
	 *            The target String to validate.
	 * @return The IValidationResult. Use {@link IValidationResult#isOK()} to check if there is an error.
	 */
	public static IValidationResult validateProcessingInstructionTarget(final String target) {

		if (target.isEmpty()) {
			return ValidationResult.error("Processing instruction target must not be empty.");
		}

		if (XML.XML_WHITESPACE_PATTERN.matcher(target).find()) {
			return ValidationResult.error("Processing instruction target must not contain whitespace characters.");
		}

		if (target.indexOf("?>") > -1) {
			return ValidationResult.error("Cannot insert entity end '?>' into a processing instruction.");
		}

		if (target.equalsIgnoreCase("xml")) {
			return ValidationResult.error("Processing instruction target 'xml' is not allowed.");
		}

		return ValidationResult.VALIDATE_OK;
	}

	/**
	 * Validate the data of an processing instruction.
	 *
	 * @param data
	 *            The data String to validate.
	 * @return The IValidationResult. Use {@link IValidationResult#isOK()} to check if there is an error.
	 */
	public static IValidationResult validateProcessingInstructionData(final String data) {
		if (data.indexOf("?>") > -1) {
			return ValidationResult.error("Cannot insert entity end '?>' into a processing instruction.");
		}

		return ValidationResult.VALIDATE_OK;
	}
}
