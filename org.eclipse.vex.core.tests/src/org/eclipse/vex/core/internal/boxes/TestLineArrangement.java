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

import org.eclipse.vex.core.internal.core.FontSpec;
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
		lines.arrangeBoxes(graphics, joinableBoxes.listIterator(), 210);
		assertEquals(1, lines.getLines().size());
	}

	@Test
	public void givenJoinableBoxes_whenBoxesFitIntoSameLine_shouldJoinBoxes() throws Exception {
		lines.arrangeBoxes(graphics, joinableBoxes.listIterator(), 210);
		assertEquals(1, joinableBoxes.size());
	}

	@Test
	public void givenUnjoinableBoxes_whenBoxesFitIntoSameLane_shouldNotJoinBoxes() throws Exception {
		lines.arrangeBoxes(graphics, unjoinableBoxes.listIterator(), 210);
		assertEquals(3, unjoinableBoxes.size());
	}

	@Test
	public void givenUnjoinableBoxFollowedByJoinableBoxWithoutProperSplitPointAtLineEnd_whenAdditionalBoxWithoutProperSplitPointDoesNotFitIntoLine_shouldWrapCompleteJoinedBoxIntoNextLine()
			throws Exception {
		final List<IInlineBox> boxes = boxes(square(10), staticText("L"), staticText("or"));
		lines.arrangeBoxes(graphics, boxes.listIterator(), 18);

		assertEquals(2, boxes.size());
		assertEquals("Lor", ((StaticText) boxes.get(1)).getText());
	}

	@Test
	public void givenUnjoinableBoxFollowedByJoinableBoxWithoutProperSplitPointAtLineEnd_whenAdditionalBoxWithoutProperSplitPointDoesNotFitIntoLine_shouldRemoveOriginalLastBox() throws Exception {
		final List<IInlineBox> boxes = boxes(square(10), staticText("L"), staticText("or"));
		lines.arrangeBoxes(graphics, boxes.listIterator(), 18);

		for (final IInlineBox box : boxes) {
			if (box.getWidth() == 0) {
				fail("Splitting left over an empty box.");
			}
		}
	}

	private static List<IInlineBox> boxes(final IInlineBox... boxes) {
		return new ArrayList<IInlineBox>(Arrays.asList(boxes));
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
		return square;
	}
}
