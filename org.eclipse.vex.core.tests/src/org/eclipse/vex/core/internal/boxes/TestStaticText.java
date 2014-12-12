/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestStaticText {

	@Test
	public void givenTwoStaticTexts_whenFontIsEqual_shouldIndicatePossibleJoin() throws Exception {
		final StaticText front = new StaticText();
		front.setFont(new FontSpec("font", FontSpec.BOLD, 10));
		final StaticText back = new StaticText();
		back.setFont(new FontSpec("font", FontSpec.BOLD, 10));

		assertTrue(front.canJoin(back));
	}

	@Test
	public void givenTwoStaticTexts_whenFontIsDifferent_shouldIndicateImpossibleJoin() throws Exception {
		final StaticText front = new StaticText();
		front.setFont(new FontSpec("frontFont", FontSpec.BOLD, 10));
		final StaticText back = new StaticText();
		back.setFont(new FontSpec("backFont", FontSpec.BOLD, 10));

		assertFalse(front.canJoin(back));
	}

	@Test
	public void whenJoiningWithStaticText_shouldAppendBackTextToFrontText() throws Exception {
		final StaticText front = new StaticText();
		front.setText("front");
		final StaticText back = new StaticText();
		back.setText("back");

		front.join(back);
		assertEquals("frontback", front.getText());
	}

	@Test
	public void whenJoiningWithStaticText_shouldAddWidth() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText front = new StaticText();
		front.setText("front");
		front.layout(graphics);
		final StaticText back = new StaticText();
		back.setText("back");
		back.layout(graphics);

		front.join(back);
		assertEquals(54, front.getWidth());
	}

	@Test
	public void whenJoiningWithStaticText_shouldIndicateSuccessfulJoin() throws Exception {
		final StaticText front = new StaticText();
		front.setText("front");
		final StaticText back = new StaticText();
		back.setText("back");

		assertTrue(front.join(back));
	}

	@Test
	public void givenJoiningTwoStaticTexts_whenJoinIsImpossible_shouldIndicateFailedJoin() throws Exception {
		final StaticText front = new StaticText();
		front.setFont(new FontSpec("frontFont", FontSpec.BOLD, 10));
		final StaticText back = new StaticText();
		back.setFont(new FontSpec("backFont", FontSpec.BOLD, 10));

		assertFalse(front.join(back));
	}

	@Test
	public void splitAtCharacterBoundary() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 6, true);

		assertSplitEquals("1", "234567890", text, tail);
		assertEquals(6, text.getWidth());
		assertEquals(54, tail.getWidth());
	}

	@Test
	public void whenSplitting_shouldAdaptWidth() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 11, true);

		assertEquals(6, text.getWidth());
		assertEquals(54, tail.getWidth());
	}

	@Test
	public void whenSplitting_shouldApplyFontToTheTail() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 15, true);

		assertSame(text.getFont(), tail.getFont());
	}

	@Test
	public void whenSplittingLeftOfTheFirstCharacter_shouldMoveAllContentToTheTail() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 5, true);

		assertSplitEquals("", "1234567890", text, tail);
	}

	@Test
	public void whenSplittingBeforeNextCharacterBoundary_shouldMoveNextCharacterToTheTail() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 11, true);

		assertSplitEquals("1", "234567890", text, tail);
	}

	@Test
	public void whenSplittingWayBehindTheBox_shouldReturnEmptyTail() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 100, true);

		assertSplitEquals("1234567890", "", text, tail);
	}

	@Test
	public void givenTextContainsWhitespace_whenSplittingAfterWhitespace_shouldSplitRightAfterWhitespace() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234 567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 34, false);

		assertSplitEquals("1234 ", "567890", text, tail);
	}

	@Test
	public void givenTextContainsWhitespace_whenSplittingBeforeFirstWhitespace_shouldMoveAllContentToTheTail() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234 567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 15, false);

		assertSplitEquals("", "1234 567890", text, tail);
	}

	@Test
	public void givenTextContainsWhitespace_whenSplittingAtWhitespace_shouldSplitRightAfterWhitespace() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234 567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 28, false);

		assertSplitEquals("1234 ", "567890", text, tail);
	}

	@Test
	public void givenTextContainsWhitespace_whenSplittingRightBeforeWhitespace_shouldSplitRightAfterWhitespace() throws Exception {
		final FakeGraphics graphics = new FakeGraphics();
		final StaticText text = new StaticText();
		text.setText("1234 567890");
		text.layout(graphics);

		final StaticText tail = (StaticText) text.splitTail(graphics, 24, false);

		assertSplitEquals("1234 ", "567890", text, tail);
	}

	private static void assertSplitEquals(final String head, final String tail, final StaticText headBox, final StaticText tailBox) {
		assertEquals(head, headBox.getText());
		assertEquals(tail, tailBox.getText());
	}
}
