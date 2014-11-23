/*******************************************************************************
 * Copyright (c) 2009 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.swt.graphics.Point;

/**
 * Implements {@link IFindReplaceTarget} (find feature) by using Java's regular expressions.
 * <p/>
 * To use it in a specific editor or view the abstract methods must be implemented.
 */
public abstract class AbstractRegExFindReplaceTarget implements IFindReplaceTarget, IFindReplaceTargetExtension3 {

	private Matcher replacer;

	/**
	 * @return the beginning index, inclusive, of the selection or {@code -1} if nothing is selected
	 *
	 * @see #getSelectionEnd()
	 * @see #setSelection(int, int)
	 */
	protected abstract int getSelectionStart();

	/**
	 * @return the ending index, exclusive, of the selection or {@code -1} if nothing is selected
	 *
	 * @see #getSelectionStart()
	 * @see #setSelection(int, int)
	 */
	protected abstract int getSelectionEnd();

	/**
	 * Selects the specified continuous range of text or {@code setSelection(-1, -1)} to deselect.
	 * <p/>
	 * Examples: <blockquote>
	 *
	 * <pre>
	 * setSelection(2, 3) selects "c" in "abcde"
	 * setSelection(0, 4) selects "abcd" in "abcde"
	 * setSelection(4, 5) selects "e" in "abcde"
	 * setSelection(-1, -1) deselects
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param start
	 *            the beginning index, inclusive, of the text to select
	 * @param end
	 *            the ending index, exclusive, of the text to select
	 *
	 * @see #getSelectionStart()
	 * @see #getSelectionEnd()
	 */
	protected abstract void setSelection(int start, int end);

	/**
	 * @return the document in which to find/replace
	 */
	protected abstract CharSequence getDocument();

	/**
	 * In the document in which to find/replace replaces the current selected text with the given text.
	 *
	 * @param text
	 *            the text to replace with; not {@code null}
	 */
	protected abstract void inDocumentReplaceSelection(CharSequence text);

	@Override
	public boolean canPerformFind() {
		return true; // enable find
	}

	@Override
	public int findAndSelect(final int offset, final String findString, final boolean searchForward, final boolean caseSensitive, final boolean wholeWord) {
		return findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord, false);
	}

	@Override
	public Point getSelection() {
		final int offset = getSelectionStart();
		final int length = getSelectionEnd() - getSelectionStart();
		return new Point(offset, length);
	}

	@Override
	public String getSelectionText() {
		final int start = getSelectionStart();
		final int end = getSelectionEnd();
		return getDocument().subSequence(start, end).toString();
	}

	@Override
	public boolean isEditable() {
		return true; // = 'replace' enablement
	}

	@Override
	public void replaceSelection(final String text) {
		replaceSelection(text, false);
	}

	@Override
	public int findAndSelect(final int offset, final String findString, final boolean searchForward, final boolean caseSensitive, final boolean wholeWord, final boolean regExSearch) {
		replacer = null;

		// please note: in 'Wrap search' widgetOffset may be -1
		int correctedOffset = offset;
		if (correctedOffset < 0) {
			correctedOffset = searchForward ? 0 : getDocument().length();
		}

		// create regular expression
		String patternString = regExSearch ? findString : Pattern.quote(findString);
		if (wholeWord) {
			patternString = "\\b" + patternString + "\\b";
		}
		final int flagCaseSensitive = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		final Pattern pattern = Pattern.compile(patternString, flagCaseSensitive);
		final Matcher matcher = pattern.matcher(getDocument());

		// a) forward
		if (searchForward) {

			// find...
			final boolean success = matcher.find(correctedOffset);

			// ...and select
			if (success) {
				setSelection(matcher.start(), matcher.end());
				replacer = matcher;
			}

			return success ? matcher.end() : -1;
		}

		// b) backward
		boolean success = false;
		int start = 0;
		int end = 0;
		for (int i = 0; i < correctedOffset;) {
			final boolean currentFound = matcher.find(i);
			if (!currentFound || matcher.end() > correctedOffset) {
				break;
			}

			i = matcher.start() + 1;
			success = true;
			start = matcher.start();
			end = matcher.end();
		}
		if (success) {
			setSelection(start, end);
			replacer = matcher;
		}
		return success ? end : -1;

	}

	@Override
	public void replaceSelection(final String text, final boolean regExReplace) {

		// precondition: editable?
		if (!isEditable()) {
			final String msg = "'Replace' is unable because target is not editable.";
			throw new IllegalStateException(msg);
		}

		// nothing selected -> nothing to do
		final int selectionStart = getSelectionStart();
		if (selectionStart < 0) {
			return;
		}

		// compute replacement
		String replacement = text;
		if (regExReplace) {
			if (replacer == null) {
				throw new IllegalStateException();
			}
			final StringBuffer result = new StringBuffer();
			try {
				replacer.appendReplacement(result, text);
			} catch (final RuntimeException e) {
				final String message = e.getLocalizedMessage();
				throw new PatternSyntaxException(message, text, -1);
			}
			replacement = result.substring(selectionStart);
		}

		// replace
		inDocumentReplaceSelection(replacement);
		replacer = null;

		// select
		setSelection(selectionStart, selectionStart + replacement.length());
	}

}
