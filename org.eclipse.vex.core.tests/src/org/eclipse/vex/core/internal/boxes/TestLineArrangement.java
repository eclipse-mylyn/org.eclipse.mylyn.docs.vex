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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.TextAlign;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestLineArrangement {
	private final static FontSpec FONT = new FontSpec("fontname", 0, 10.0f);
	private FakeGraphics graphics;
	private List<IInlineBox> joinableBoxes;
	private List<IInlineBox> unjoinableBoxes;
	private LineArrangement lines;

	@Before
	public void setUp() throws Exception {
		graphics = new FakeGraphics();
		joinableBoxes = boxes(staticText("Lor"), staticText("em ipsum front "), staticText("Lorem ipsum back"));
		unjoinableBoxes = boxes(staticText("Lorem ipsum front"), square(10), staticText("Lorem ipsum back"));
		lines = new LineArrangement();
	}

	@Test
	public void givenAllBoxesFitIntoOneLine_shouldArrangeBoxesInOneLine() throws Exception {
		lines.arrangeBoxes(graphics, joinableBoxes.listIterator(), 210, TextAlign.LEFT);
		assertEquals(1, lines.getLines().size());
	}

	@Test
	public void givenJoinableBoxes_whenBoxesFitIntoSameLine_shouldJoinBoxes() throws Exception {
		lines.arrangeBoxes(graphics, joinableBoxes.listIterator(), 210, TextAlign.LEFT);
		assertEquals(1, joinableBoxes.size());
	}

	@Test
	public void givenUnjoinableBoxes_whenBoxesFitIntoSameLane_shouldNotJoinBoxes() throws Exception {
		lines.arrangeBoxes(graphics, unjoinableBoxes.listIterator(), 210, TextAlign.LEFT);
		assertEquals(3, unjoinableBoxes.size());
	}

	@Test
	public void givenUnjoinableBoxFollowedByJoinableBoxWithoutProperSplitPointAtLineEnd_whenAdditionalBoxWithoutProperSplitPointDoesNotFitIntoLine_shouldWrapCompleteJoinedBoxIntoNextLine() throws Exception {
		final List<IInlineBox> boxes = boxes(square(10), staticText("L"), staticText("or"));
		lines.arrangeBoxes(graphics, boxes.listIterator(), 18, TextAlign.LEFT);

		assertEquals(2, boxes.size());
		assertEquals("Lor", ((StaticText) boxes.get(1)).getText());
	}

	@Test
	public void givenUnjoinableBoxFollowedByJoinableBoxWithoutProperSplitPointAtLineEnd_whenAdditionalBoxWithoutProperSplitPointDoesNotFitIntoLine_shouldRemoveOriginalLastBox() throws Exception {
		final List<IInlineBox> boxes = boxes(square(10), staticText("L"), staticText("or"));
		lines.arrangeBoxes(graphics, boxes.listIterator(), 18, TextAlign.LEFT);

		for (final IInlineBox box : boxes) {
			if (box.getWidth() == 0) {
				fail("Splitting left over an empty box.");
			}
		}
	}

	@Test
	public void givenInlineContainerFollowedBySingleSpace_whenSplittingWithinSpace_shouldKeepSpaceOnFirstLine() throws Exception {
		final List<IInlineBox> boxes = boxes(staticText("Lorem "), inlineContainer(staticText("ipsum")), staticText(" "));
		layout(boxes);
		final int widthOfHeadBoxes = boxes.get(0).getWidth() + boxes.get(1).getWidth();

		lines.arrangeBoxes(graphics, boxes.listIterator(), widthOfHeadBoxes + 1, TextAlign.LEFT);

		assertEquals(1, lines.getLines().size());
		assertEquals(boxes.get(2), lines.getLines().iterator().next().getLastChild());
	}

	@Test
	public void givenSquareFollowedBySingleSpace_whenSplittingWithinSpace_shouldKeepSpaceOnFirstLine() throws Exception {
		final List<IInlineBox> boxes = boxes(staticText("Lorem "), square(15), staticText(" "));
		layout(boxes);
		final int widthOfHeadBoxes = boxes.get(0).getWidth() + boxes.get(1).getWidth();

		lines.arrangeBoxes(graphics, boxes.listIterator(), widthOfHeadBoxes + 1, TextAlign.LEFT);

		assertEquals(1, lines.getLines().size());
		assertEquals(boxes.get(2), lines.getLines().iterator().next().getLastChild());
	}

	@Test
	public void givenInlineContainerFollowedByTextThatStartsWithSpace_whenSplittingWithinText_shouldSplitAfterSpace() throws Exception {
		final List<IInlineBox> boxes = boxes(staticText("Lorem "), inlineContainer(staticText("ipsum")), staticText(" dolor"));
		layout(boxes);
		final int widthOfHeadBoxes = boxes.get(0).getWidth() + boxes.get(1).getWidth();

		lines.arrangeBoxes(graphics, boxes.listIterator(), widthOfHeadBoxes + 10, TextAlign.LEFT);

		assertEquals(2, lines.getLines().size());
		assertEquals(" ", ((StaticText) lines.getLines().iterator().next().getLastChild()).getText());
	}

	@Test
	public void givenInlineContainerFollowedByTextThatStartsWithSpace_whenSplittingAnywhereWithinSpaceAndText_shouldSplitAfterSpace() throws Exception {
		for (int x = 1; x < graphics.stringWidth(" dolor"); x += 1) {
			final List<IInlineBox> boxes = boxes(staticText("Lorem "), inlineContainer(staticText("ipsum")), staticText(" dolor"));
			layout(boxes);
			final int widthOfHeadBoxes = boxes.get(0).getWidth() + boxes.get(1).getWidth();

			lines.arrangeBoxes(graphics, boxes.listIterator(), widthOfHeadBoxes + x, TextAlign.LEFT);

			assertEquals("x = " + x, 2, lines.getLines().size());
			assertEquals("x = " + x, " ", ((StaticText) lines.getLines().iterator().next().getLastChild()).getText());
		}
	}

	private void layout(final List<IInlineBox> boxes) {
		for (final IInlineBox box : boxes) {
			box.layout(graphics);
		}
	}

	private static List<IInlineBox> boxes(final IInlineBox... boxes) {
		return new ArrayList<IInlineBox>(Arrays.asList(boxes));
	}

	private static InlineContainer inlineContainer(final IInlineBox... boxes) {
		final InlineContainer container = new InlineContainer();
		for (final IInlineBox box : boxes) {
			container.appendChild(box);
		}
		return container;
	}

	private static StaticText staticText(final String text) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(FONT);
		return staticText;
	}

	private static Square square(final int size) {
		final Square square = new Square();
		square.setSize(size);
		square.setColor(Color.BLACK);
		return square;
	}
}
