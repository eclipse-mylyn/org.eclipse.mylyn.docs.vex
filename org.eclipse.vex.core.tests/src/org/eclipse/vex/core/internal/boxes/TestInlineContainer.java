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
package org.eclipse.vex.core.internal.boxes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.junit.Before;
import org.junit.Test;

public class TestInlineContainer {

	private final static FontSpec FONT = new FontSpec("fontname", 0, 10.0f);
	private FakeGraphics graphics;

	@Before
	public void setUp() throws Exception {
		graphics = new FakeGraphics();
	}

	@Test
	public void givenSeveralJoinableChildren_shouldJoinChildren() throws Exception {
		final InlineContainer container = new InlineContainer();
		container.appendChild(staticText("Lorem "));
		container.appendChild(staticText("ipsum"));

		assertEquals(1, count(container.getChildren()));
	}

	@Test
	public void givenTwoInlineContainers_shouldIndicateJoiningIsPossible() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		final InlineContainer container2 = new InlineContainer();

		assertTrue(container1.canJoin(container2));
	}

	@Test
	public void givenInlineContainer_whenCheckedForDifferentInlineBox_shouldIndicateJoiningIsNotPossible() throws Exception {
		final InlineContainer container = new InlineContainer();
		final StaticText staticText = staticText("Lorem ipsum");

		assertFalse(container.canJoin(staticText));
	}

	@Test
	public void givenTwoEmptyInlineContainers_shouldJoin() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		final InlineContainer container2 = new InlineContainer();

		assertTrue(container1.join(container2));
	}

	@Test
	public void givenTwoNonEmptyInlineContainers_whenJoiningAndChildrenDoMatch_shouldJoinAdjacentChildren() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		container1.appendChild(staticText("Lorem "));
		final InlineContainer container2 = new InlineContainer();
		container2.appendChild(staticText("ipsum"));

		container1.join(container2);

		assertEquals(1, count(container1.getChildren()));
		final StaticText joinedText = (StaticText) container1.getChildren().iterator().next();
		assertEquals("Lorem ipsum", joinedText.getText());
	}

	@Test
	public void givenTwoInlineContainers_whenJoiningAndChildrenDoNotMatch_shouldJustAppendChildren() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		container1.appendChild(staticText("Lorem "));
		final InlineContainer container2 = new InlineContainer();
		container2.appendChild(square(15));

		container1.join(container2);

		assertEquals(2, count(container1.getChildren()));
	}

	@Test
	public void givenTwoInlineContainers_whenJoining_shouldSetParentOfNewChildren() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		container1.appendChild(staticText("Lorem "));
		final InlineContainer container2 = new InlineContainer();
		container2.appendChild(square(15));

		container1.join(container2);

		for (final IInlineBox child : container1.getChildren()) {
			assertEquals(container1, child.getParent());
		}
	}

	@Test
	public void givenTwoInlineContainers_whenJoining_shouldAdaptWidthHeightAndBaseline() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		container1.appendChild(staticText("Lorem "));
		container1.layout(graphics);
		final int container1Width = container1.getWidth();
		final int container1Descend = container1.getHeight() - container1.getBaseline();
		final int container1Baseline = container1.getBaseline();
		final InlineContainer container2 = new InlineContainer();
		container2.appendChild(square(15));
		container2.layout(graphics);
		final int container2Width = container2.getWidth();
		final int container2Descend = container2.getHeight() - container2.getBaseline();
		final int container2Baseline = container2.getBaseline();

		container1.join(container2);

		assertEquals("width", container1Width + container2Width, container1.getWidth());
		assertEquals("height", Math.max(container1Baseline, container2Baseline) + Math.max(container1Descend, container2Descend), container1.getHeight());
		assertEquals("baseline", Math.max(container1Baseline, container2Baseline), container1.getBaseline());
	}

	@Test
	public void givenTwoInlineContainers_whenJoining_shouldAdaptLayout() throws Exception {
		final InlineContainer container1 = new InlineContainer();
		container1.appendChild(staticText("Lorem "));
		container1.layout(graphics);
		final InlineContainer container2 = new InlineContainer();
		container2.appendChild(square(15));
		container2.layout(graphics);

		container1.join(container2);

		final Iterator<IInlineBox> children = container1.getChildren().iterator();
		final IInlineBox child1 = children.next();
		final IInlineBox child2 = children.next();

		assertEquals("child2 left", child1.getLeft() + child1.getWidth(), child2.getLeft());
		assertEquals("child1 top", container1.getBaseline() - child1.getBaseline(), child1.getTop());
		assertEquals("child2 top", container1.getBaseline() - child2.getBaseline(), child2.getTop());
	}

	@Test
	public void givenEmptyContainer_shouldIndicateSplittingIsNotPossible() throws Exception {
		final InlineContainer container = new InlineContainer();

		assertFalse(container.canSplit());
	}

	@Test
	public void givenNonEmptyContainer_shouldIndicateSplittingIsPossible() throws Exception {
		final InlineContainer container = new InlineContainer();
		container.appendChild(staticText("Lorem ipsum"));

		assertTrue(container.canSplit());
	}

	@Test
	public void givenContainerWithOneSplittableChild_whenSplittingWithoutForce_shouldSplitAtWhitespace() throws Exception {
		final InlineContainer container = new InlineContainer();
		container.appendChild(staticText("Lorem ipsum"));
		container.layout(graphics);

		final InlineContainer tail = container.splitTail(graphics, 50, false);
		final String tailText = ((StaticText) tail.getChildren().iterator().next()).getText();
		assertEquals("ipsum", tailText);
	}

	private static int count(final Iterable<?> iterable) {
		int count = 0;
		final Iterator<?> iter = iterable.iterator();
		while (iter.hasNext()) {
			iter.next();
			count += 1;
		}
		return count;
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
