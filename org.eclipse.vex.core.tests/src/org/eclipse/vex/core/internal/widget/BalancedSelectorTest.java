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
package org.eclipse.vex.core.internal.widget;

import static org.junit.Assert.assertEquals;

import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class BalancedSelectorTest {

	private BalancedSelector selector;
	private UniversalTestDocument document;

	@Before
	public void setUp() throws Exception {
		document = new UniversalTestDocument(3);
		selector = new BalancedSelector();
		selector.setDocument(document.getDocument());
	}

	@Test
	public void givenMarkInText_whenSelectingOneCharForward_shouldIncludeNextChar() throws Exception {
		final int mark = document.getOffsetWithinText(0);
		select(mark, mark + 1);
		assertBalancedSelectionIs(mark, mark + 1, mark + 1);
	}

	@Test
	public void givenMarkInText_whenSelectingOneCharBackward_shouldIncludePreviousChar() throws Exception {
		final int mark = document.getOffsetWithinText(0);
		select(mark, mark - 1);
		assertBalancedSelectionIs(mark - 1, mark, mark - 1);
	}

	@Test
	public void givenMarkAtFirstTextPosition_whenSelectingOneCharBackward_shouldSelectWholeParagraph() throws Exception {
		final IElement paragraph = document.getParagraphWithText(0);
		select(paragraph.getStartOffset() + 1, paragraph.getStartOffset());
		assertBalancedSelectionIs(paragraph.getStartOffset(), paragraph.getEndOffset() + 1, paragraph.getStartOffset());
	}

	@Test
	public void givenMarkAtLastTextPosition_whenSelectingOneCharForward_shouldSelectWholeParagraph() throws Exception {
		final IElement paragraph = document.getParagraphWithText(0);
		select(paragraph.getEndOffset(), paragraph.getEndOffset() + 1);
		assertBalancedSelectionIs(paragraph.getStartOffset(), paragraph.getEndOffset() + 1, paragraph.getEndOffset() + 1);
	}

	@Test
	public void givenMarkAtStartOffsetOfEmptyParagraph_whenSelectingOneForward_shouldSelectWholeParagraph() throws Exception {
		final IElement paragraph = document.getEmptyParagraph(0);
		select(paragraph.getStartOffset(), paragraph.getEndOffset());
		assertBalancedSelectionIs(paragraph.getStartOffset(), paragraph.getEndOffset() + 1, paragraph.getEndOffset() + 1);
	}

	@Test
	public void givenMarkInText_whenSelectingToStartOffsetOfSection_shouldSelectWholeSection() throws Exception {
		final IElement section = document.getSection(1);
		final int mark = document.getOffsetWithinText(1);
		select(mark, section.getStartOffset());
		assertBalancedSelectionIs(section.getStartOffset(), section.getEndOffset() + 1, section.getStartOffset());
	}

	private void select(final int mark, final int caretPosition) {
		selector.setMark(mark);
		selector.moveTo(caretPosition);
	}

	private void assertBalancedSelectionIs(final int startOffset, final int endOffset, final int caretOffset) {
		assertEquals("selection", new ContentRange(startOffset, endOffset), selector.getRange());
		assertEquals("caret offset", caretOffset, selector.getCaretOffset());
	}
}
