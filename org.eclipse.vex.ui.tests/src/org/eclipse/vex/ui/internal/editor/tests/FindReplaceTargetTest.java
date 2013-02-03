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
package org.eclipse.vex.ui.internal.editor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.vex.ui.internal.editor.AbstractRegExFindReplaceTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class FindReplaceTargetTest {

	private static enum Direction {
		FORWARD, BACKWARD
	};

	private static enum Case {
		SENSITVE, INSENSITVE
	};

	private static enum WholeWord {
		ON, OFF
	};

	// options
	private String findString;
	private Direction direction;
	private Case caseSensitiveness;
	private WholeWord wholeWord;
	private boolean regExSearch;

	// state
	private AbstractRegExFindReplaceTarget finder;
	private int selectionStart;
	private int selectionEnd;
	private String content;

	private class MyFindReplaceTarget extends AbstractRegExFindReplaceTarget {

		@Override
		protected int getSelectionStart() {
			return selectionStart;
		}

		@Override
		protected int getSelectionEnd() {
			return selectionEnd;
		}

		@Override
		protected void setSelection(final int start, final int end) {
			selectionStart = start;
			selectionEnd = end;
		}

		@Override
		protected CharSequence getDocument() {
			return content;
		}

		@Override
		protected void inDocumentReplaceSelection(final CharSequence text) {
			content = content.substring(0, selectionStart) + text + content.substring(selectionEnd);
		}

	}

	@After
	public void tearDown() throws Exception {
		finder = null;
		findString = null;
		content = null;
	}

	private void setUp(final String content) {
		setUp(content, -1, -1);
	}

	private void setUp(final String content, final int selectionStart, final int selectionEnd) {
		this.content = content;
		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
		finder = new MyFindReplaceTarget();
	}

	private void setFindOptions(final String findString, final Direction direction, final Case caseSensitve, final WholeWord wholeWord) {
		this.findString = findString;
		this.direction = direction;
		caseSensitiveness = caseSensitve;
		this.wholeWord = wholeWord;
		regExSearch = false;
	}

	private void setRegExOptions(final String findString, final Direction direction, final Case caseSensitve) {
		this.findString = findString;
		this.direction = direction;
		caseSensitiveness = caseSensitve;
		wholeWord = WholeWord.OFF;
		regExSearch = true;
	}

	/**
	 * @param expected
	 *            content with the expected selection which is market by square brackets; example: {code
	 *            "xyz [selected] ..."}
	 */
	private void findAndAssertSelectedTextEquals(final String expected) {
		find();
		assertSelectedTextEquals(expected);
	}

	/**
	 * @param expected
	 *            content with the expected selection which is market by square brackets; example: {code
	 *            "xyz [selected] ..."}
	 */
	private void assertSelectedTextEquals(final String expected) {
		final int start = expected.indexOf('[');
		final int end = expected.indexOf(']') - 1;
		assertTrue("selection must marked by square brackets: [...]", start >= 0 && end >= 0 && end >= start);

		String is = content;
		if (selectionStart >= 0 && selectionEnd >= 0) {
			is = content.substring(0, selectionStart) + "[" + content.substring(selectionStart, selectionEnd) + "]" + content.substring(selectionEnd);
		}
		assertEquals(expected, is);
		final CharSequence selection = expected.subSequence(start + 1, end + 1);
		assertEquals(selection, finder.getSelectionText());
		assertEquals(start, selectionStart);
		assertEquals(end, selectionEnd);
	}

	private boolean find() {
		final int offset = direction == Direction.FORWARD ? selectionEnd : selectionStart;
		final int result = finder.findAndSelect(offset, findString, direction == Direction.FORWARD, caseSensitiveness == Case.SENSITVE, wholeWord == WholeWord.ON, regExSearch);
		return result >= 0;
	}

	@Test
	public void testSelection() throws Exception {
		Point selection = null;

		// nothing selected; cursor at the end
		setUp("aaabbbccc", 9, 9);
		selection = finder.getSelection();
		assertEquals("selection offset incorrect", 9, selection.x);
		assertEquals("selection length incorrect", 0, selection.y);
		assertEquals("", finder.getSelectionText());

		// selected: 'BB' selected
		setUp("aaaaaBBcccccc", 5, 7);
		selection = finder.getSelection();
		assertEquals("selection offset incorrect", 5, selection.x);
		assertEquals("selection length incorrect", 2, selection.y);
		assertEquals("BB", finder.getSelectionText());
	}

	@Test
	public void testEnablement() throws Exception {
		setUp("abc");
		assertTrue(finder.canPerformFind());
		assertTrue(finder.isEditable());
	}

	@Test
	public void testFind() throws Exception {
		setUp("aAa.bBb.cCc");
		setFindOptions("a", Direction.FORWARD, Case.SENSITVE, WholeWord.OFF);

		findAndAssertSelectedTextEquals("[a]Aa.bBb.cCc");
		findAndAssertSelectedTextEquals("aA[a].bBb.cCc");
		assertFalse(find());
		direction = Direction.BACKWARD;
		findAndAssertSelectedTextEquals("[a]Aa.bBb.cCc");
		assertFalse(find());

		// in 'Wrap search' offset may be -1 ...
		setUp("abba", -1, -1);
		setFindOptions("a", Direction.FORWARD, Case.SENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("[a]bba");
		findAndAssertSelectedTextEquals("abb[a]");
		assertFalse(find());

		// ... and in backward -1 means find from the end
		setUp("abba", -1, -1);
		setFindOptions("a", Direction.BACKWARD, Case.SENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("abb[a]");
		findAndAssertSelectedTextEquals("[a]bba");
		assertFalse(find());
	}

	@Test
	public void testFindWithSpecialChars() throws Exception {
		// '.' (in regular expression a special char):
		setUp("aAa.bBb.cCc");
		setFindOptions(".", Direction.FORWARD, Case.SENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("aAa[.]bBb.cCc");
		findAndAssertSelectedTextEquals("aAa.bBb[.]cCc");
		assertFalse(find());
		direction = Direction.BACKWARD;
		findAndAssertSelectedTextEquals("aAa[.]bBb.cCc");
		assertFalse(find());
	}

	@Test
	public void testFindCaseInsensitive() throws Exception {
		setUp("ab1 AB1 xxx Ab1 aB1 yyy");
		setFindOptions("Ab1", Direction.FORWARD, Case.INSENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("[ab1] AB1 xxx Ab1 aB1 yyy");
		findAndAssertSelectedTextEquals("ab1 [AB1] xxx Ab1 aB1 yyy");
		findAndAssertSelectedTextEquals("ab1 AB1 xxx [Ab1] aB1 yyy");
		findAndAssertSelectedTextEquals("ab1 AB1 xxx Ab1 [aB1] yyy");
		assertFalse(find());

		// crosscheck: case-sensitive
		setFindOptions("Ab1", Direction.BACKWARD, Case.SENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("ab1 AB1 xxx [Ab1] aB1 yyy");
		assertFalse(find());
	}

	@Test
	public void testFindWholeWord() throws Exception {

		// including special chars and German umlauts: sharp s = \u00df
		//                                             ae      = \u00e4
		setUp("xx aa xx xyx xx-cc a-xx%c \u00e4xx xx\u00df xx");
		setFindOptions("xx", Direction.FORWARD, Case.SENSITVE, WholeWord.ON);
		findAndAssertSelectedTextEquals("[xx] aa xx xyx xx-cc a-xx%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa [xx] xyx xx-cc a-xx%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx [xx]-cc a-xx%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx xx-cc a-[xx]%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx xx-cc a-xx%c \u00e4xx xx\u00df [xx]");
		assertFalse(find());

		// crosscheck: backward; 'Whole word' disabled
		setFindOptions("xx", Direction.BACKWARD, Case.SENSITVE, WholeWord.OFF);
		findAndAssertSelectedTextEquals("xx aa xx xyx xx-cc a-xx%c \u00e4xx [xx]\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx xx-cc a-xx%c \u00e4[xx] xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx xx-cc a-[xx]%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa xx xyx [xx]-cc a-xx%c \u00e4xx xx\u00df xx");
		findAndAssertSelectedTextEquals("xx aa [xx] xyx xx-cc a-xx%c \u00e4xx xx\u00df xx");
	}

	@Test
	public void testFindRegEx() throws Exception {
		setUp("h2o h5o h42o hoo h234o h3O");
		setRegExOptions("h[2-4]{1,2}o", Direction.FORWARD, Case.SENSITVE);
		findAndAssertSelectedTextEquals("[h2o] h5o h42o hoo h234o h3O");
		findAndAssertSelectedTextEquals("h2o h5o [h42o] hoo h234o h3O");
		assertFalse(find());

		// case-insensitive
		caseSensitiveness = Case.INSENSITVE;
		findAndAssertSelectedTextEquals("h2o h5o h42o hoo h234o [h3O]");
		assertFalse(find());

		// backward
		direction = Direction.BACKWARD;
		caseSensitiveness = Case.SENSITVE;
		findAndAssertSelectedTextEquals("h2o h5o [h42o] hoo h234o h3O");
		findAndAssertSelectedTextEquals("[h2o] h5o h42o hoo h234o h3O");
		assertFalse(find());
	}

	@Test
	public void testReplace() throws Exception {
		setUp("He__o W____!");
		setFindOptions("__", Direction.FORWARD, Case.SENSITVE, WholeWord.OFF);

		// a) replacement of same length than selection
		findAndAssertSelectedTextEquals("He[__]o W____!");
		finder.replaceSelection("ll");
		assertSelectedTextEquals("He[ll]o W____!");

		// b) replacement shorter than selection
		findAndAssertSelectedTextEquals("Hello W[__]__!");
		finder.replaceSelection("o");
		assertSelectedTextEquals("Hello W[o]__!");

		// c) replacement longer than selection
		findAndAssertSelectedTextEquals("Hello Wo[__]!");
		finder.replaceSelection("rld");
		assertSelectedTextEquals("Hello Wo[rld]!");

		// nothing selected: nothing must be happen
		setUp("abc", -1, -1);
		finder.replaceSelection("x");
		Assert.assertEquals(-1, selectionStart);
		Assert.assertEquals(-1, selectionEnd);
		Assert.assertEquals("abc", content);
	}

	@Test
	public void testRegExReplace() throws Exception {
		setUp("Hello World!", 0, 1);
		setRegExOptions("(.)(l+)", Direction.FORWARD, Case.SENSITVE);

		findAndAssertSelectedTextEquals("H[ell]o World!");
		finder.replaceSelection("$1_$2", true);
		assertSelectedTextEquals("H[e_ll]o World!");
		findAndAssertSelectedTextEquals("He_llo Wo[rl]d!");
		finder.replaceSelection("$2$1", true);
	}

	@Test
	public void testRegExReplaceThrowsIllegalStateException() throws Exception {
		setUp("Hello World!", 0, 1);
		setRegExOptions("(.)(l+)", Direction.FORWARD, Case.SENSITVE);

		// 'replace' must preceded by 'findAndSelect',
		// otherwise -> IllegalStateException
		setUp("Hello World!", 0, 1);
		try {
			finder.replaceSelection("x", true);
			fail();
		} catch (final IllegalStateException e) {
			// expected
		}

		// calling replace twice
		assertTrue(find());
		finder.replaceSelection("x", true);
		replaceAndExpectIllegalStateException();

		// not found
		assertTrue(find());
		assertFalse(find());
		replaceAndExpectIllegalStateException();
	}

	private void replaceAndExpectIllegalStateException() {
		try {
			finder.replaceSelection("x", true);
			fail();
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testRegExReplaceThrowsPatternSyntaxException() throws Exception {
		setUp("abcde", 0, 1);
		setRegExOptions("(.)", Direction.FORWARD, Case.SENSITVE);

		assertTrue(find());
		replaceAndExpectPatternSyntaxException("single backslash -> \\");

		assertTrue(find());
		replaceAndExpectPatternSyntaxException("no group 2 -> $2");
	}

	private void replaceAndExpectPatternSyntaxException(final String replacement) {
		try {
			finder.replaceSelection(replacement, true);
			fail("Invalid replace pattern '" + replacement + "' does not throw PatternSyntaxException.");
		} catch (final PatternSyntaxException e) {
			// expected
		}
	}

}
