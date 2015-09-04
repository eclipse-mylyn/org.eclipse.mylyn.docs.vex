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
package org.eclipse.vex.core.internal.cursor;

import static org.eclipse.vex.core.internal.cursor.CursorMoves.down;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.left;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.right;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toAbsoluteCoordinates;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.up;
import static org.junit.Assert.assertEquals;

import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.eclipse.vex.core.internal.visualization.DocumentRootVisualization;
import org.eclipse.vex.core.internal.visualization.ParagraphVisualization;
import org.eclipse.vex.core.internal.visualization.StructureElementVisualization;
import org.eclipse.vex.core.internal.visualization.TextVisualization;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestCursorPosition {

	private RootBox rootBox;
	private Cursor cursor;
	private FakeGraphics graphics;
	private UniversalTestDocument document;

	@Before
	public void setUp() throws Exception {
		graphics = new FakeGraphics();
		document = new UniversalTestDocument(2);
		cursor = new Cursor(new FakeSelector());

		visualizeDocument();
	}

	private void visualizeDocument() {
		rootBox = buildVisualizationChain().visualizeRoot(document.getDocument());
		cursor.setRootBox(rootBox);
		rootBox.setWidth(200);
		rootBox.layout(graphics);
	}

	@Test
	public void canMoveCursorOneCharacterLeft() throws Exception {
		final int position = document.getOffsetWithinText(0);
		cursorAt(position);
		cursor.move(left());
		assertCursorAt(position - 1);
	}

	@Test
	public void whenAtFirstPosition_cannotMoveCursorOneCharacterLeft() throws Exception {
		cursorAt(0);
		cursor.move(left());
		assertCursorAt(0);
	}

	@Test
	public void canMoveCursorOneCharacterRight() throws Exception {
		cursorAt(5);
		cursor.move(right());
		assertCursorAt(6);
	}

	@Test
	public void whenAtLastOffset_cannotMoveCursorOneCharacterRight() throws Exception {
		cursorAt(lastOffset());
		cursor.move(right());
		assertCursorAt(lastOffset());
	}

	@Test
	public void canMoveCursorOneLineUp() throws Exception {
		cursorAt(35);
		moveCursor(up());
		assertCursorAt(5);
	}

	@Test
	public void whenAtFirstPosition_cannotMoveCursorOneLineUp() throws Exception {
		cursorAt(0);
		moveCursor(up());
		assertCursorAt(0);
	}

	@Test
	public void givenInFirstLineOfParagraph_whenMovingUp_shouldMoveCursorToParagraphStartOffset() throws Exception {
		cursorAt(beginOfThirdParagraph() + 3);
		moveCursor(up());
		assertCursorAt(beginOfThirdParagraph());
	}

	@Test
	public void givenRightBeforeFirstParagraphInSection_whenMovingUp_shouldMoveCursorToSectionStartOffset() throws Exception {
		cursorAt(336);
		moveCursor(up());
		assertCursorAt(335);
	}

	@Test
	public void givenAtSectionStartOffset_whenMovingUp_shouldMoveCursorToPreviousSectionEndOffset() throws Exception {
		cursorAt(335);
		moveCursor(up());
		assertCursorAt(334);
	}

	@Test
	public void givenAtSectionEndOffset_whenMovingUp_shouldMoveCursorToLastParagraphEndOffset() throws Exception {
		cursorAt(endOfFirstSection());
		moveCursor(up());
		assertCursorAt(endOfSecondParagraph());
	}

	@Test
	public void givenAtEmptyParagraphEndOffset_whenMovingUp_shouldMoveCursorToEmptyParagraphStartOffset() throws Exception {
		cursorAt(endOfSecondParagraph());
		moveCursor(up());
		assertCursorAt(beginOfSecondParagraph());
	}

	@Test
	public void givenBelowLastLineLeftOfLastCharacter_whenMovingUp_shouldMoveCursorAtPreferredXInLastLine() throws Exception {
		cursorAt(340);
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(322);
	}

	@Test
	public void givenBelowLastLineRightOfLastCharacter_whenMovingUp_shouldMoveCursorToEndOfParagraph() throws Exception {
		cursorAt(360);
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(endOfFirstParagraph());
	}

	@Test
	public void givenAtStartOfEmptyLine_whenMovingUp_shouldMoveCursorToStartOfLineAbove() throws Exception {
		cursorAt(endOfSecondParagraph());
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(321);
	}

	@Test
	public void givenAtFirstLineOfThirdParagraphSecondParagraphContainsText_whenMovingUp_shouldMoveIntoTextOfSecondParagraph() throws Exception {
		document.getDocument().insertText(endOfSecondParagraph(), "lorem");
		visualizeDocument();

		cursorAt(beginOfThirdParagraph() + 4);
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(beginOfSecondParagraph() + 4);
	}

	@Test
	public void givenInParagraphWithOnlyOneLine_whenMovingUp_shouldMoveToEndOfContainingSection() throws Exception {
		document.getDocument().insertText(endOfSecondParagraph(), "lorem");
		visualizeDocument();

		cursorAt(beginOfSecondParagraph() + 4);
		moveCursor(down());
		assertCursorAt(endOfFirstSection());
	}

	@Test
	public void givenInFirstLineOfFirstParagraph_whenMovingUp_shouldMoveCursorToStartOffsetOfFirstParagraph() throws Exception {
		cursorAt(4);
		moveCursor(up());
		assertCursorAt(3);
	}

	@Test
	public void givenAtStartOfFirstLineWithPreferredXZero_whenMovingUp_shouldMoveCursorToStartOfFirstParagraph() throws Exception {
		cursorAt(0);
		moveCursor(down());
		moveCursor(down());
		moveCursor(down());
		moveCursor(down());
		moveCursor(up());
		assertCursorAt(3);
	}

	@Test
	public void givenAtLastOffset_whenMovingUp_shouldMoveCursorToRootElementEndOffset() throws Exception {
		cursorAt(lastOffset());
		moveCursor(up());
		assertCursorAt(document.getDocument().getRootElement().getEndOffset());
	}

	@Test
	public void givenAtParagraphEndOffset_whenMovingUp_shouldMoveCursorToLineAbove() throws Exception {
		cursorAt(endOfFirstParagraph());
		moveCursor(up());
		assertCursorAt(313);
	}

	@Test
	public void canMoveCursorOneLineDown() throws Exception {
		cursorAt(5);
		moveCursor(down());
		assertCursorAt(35);
	}

	@Test
	public void whenAtLastOffset_cannotMoveCursorDown() throws Exception {
		cursorAt(lastOffset());
		cursor.move(down());
		assertCursorAt(lastOffset());
	}

	@Test
	public void givenInLastLineOfParagraph_whenMovingDown_shouldMoveCursorToNextParagraphStartOffset() throws Exception {
		cursorAt(endOfFirstParagraph() - 2);
		moveCursor(down());
		assertCursorAt(beginOfSecondParagraph());
	}

	@Test
	public void givenAtEndOfParagraph_whenMovingDown_shouldMoveCursorToNextParagraphStartOffset() throws Exception {
		cursorAt(endOfFirstParagraph());
		moveCursor(down());
		assertCursorAt(beginOfSecondParagraph());
	}

	@Test
	public void givenAtEndOfLastParagraphInSection_whenMovingDown_shouldMoveCursorToSectionEndOffset() throws Exception {
		cursorAt(333);
		moveCursor(down());
		assertCursorAt(334);
	}

	@Test
	public void givenInLineBeforeLastLineRightOfLastCharacterInLastLine_whenMovingDown_shouldMoveCursorToParagraphEndOffset() throws Exception {
		cursorAt(317);
		moveCursor(down());
		assertCursorAt(endOfFirstParagraph());
	}

	@Test
	public void givenAtSectionStartOffset_whenMovingDown_shouldMoveCursorToFirstParagraphStartOffset() throws Exception {
		cursorAt(335);
		moveCursor(down());
		assertCursorAt(336);
	}

	@Test
	public void givenAtDocumentStartOffset_whenMovingDown_shouldMoveCursorToRootElementStartOffset() throws Exception {
		cursorAt(0);
		moveCursor(down());
		assertCursorAt(1);
	}

	@Test
	public void givenAtStartOfFirstLineWithPreferredXZero_whenMovingDown_shouldMoveCursorToStartOfSecondLine() throws Exception {
		cursorAt(0);
		moveCursor(down());
		moveCursor(down());
		moveCursor(down());
		moveCursor(down());
		moveCursor(down());
		assertCursorAt(34);
	}

	@Test
	public void givenAtEndOfLastEmptyParagraph_whenMovingDown_shouldMoveCursorToLastSectionEndOffset() throws Exception {
		cursorAt(endOfLastParagraph());
		moveCursor(down());
		assertCursorAt(endOfLastSection());
	}

	@Test
	public void givenAtEndOfLongLine_whenMovingDown_shouldMoveCursorToEndOfShorterLineBelow() throws Exception {
		cursorAt(149);
		moveCursor(down());
		assertCursorAt(170);
	}

	@Test
	public void whenClickingIntoText_shouldMoveToPositionInText() throws Exception {
		moveCursor(toAbsoluteCoordinates(18, 11));
		assertCursorAt(5);
	}

	@Test
	public void whenClickingRightOfLastLine_shouldMoveToEndOfParagraph() throws Exception {
		moveCursor(toAbsoluteCoordinates(133, 168));
		assertCursorAt(endOfFirstParagraph());
	}

	@Test
	public void whenClickingLeftOfLine_shouldMoveToBeginningOfLine() throws Exception {
		for (int x = 0; x < 10; x += 1) {
			cursorAt(0);
			moveCursor(toAbsoluteCoordinates(x, 11));
			assertCursorAt("x=" + x, 4);
		}
	}

	@Test
	public void whenClickingRightOfLine_shouldMoveToEndOfLine() throws Exception {
		for (int x = 199; x > 193; x -= 1) {
			cursorAt(0);
			moveCursor(toAbsoluteCoordinates(x, 11));
			assertCursorAt("x=" + x, 33);
		}
	}

	@Test
	public void whenClickingInEmptyLine_shouldMoveToEndOfParagraph() throws Exception {
		moveCursor(toAbsoluteCoordinates(10, 187));
		assertCursorAt(endOfSecondParagraph());
	}

	@Test
	public void whenClickingBelowLastLine_shouldMoveToEndOfParagraph() throws Exception {
		for (int x = 6; x < 194; x += 1) {
			cursorAt(0);
			moveCursor(toAbsoluteCoordinates(x, 181));
			assertCursorAt("x=" + x, endOfFirstParagraph());
		}
	}

	@Test
	public void whenClickingInLastEmptyParagraph_shouldMoveToEndOfParagraph() throws Exception {
		cursorAt(0);
		moveCursor(toAbsoluteCoordinates(10, 395));
		assertCursorAt(document.getEmptyParagraph(1).getEndOffset());
	}

	private void cursorAt(final int offset) {
		moveCursor(toOffset(offset));
	}

	private void moveCursor(final ICursorMove move) {
		cursor.move(move);
		cursor.applyMoves(graphics);
		cursor.paint(graphics);
	}

	private void assertCursorAt(final int offset) {
		cursor.applyMoves(graphics);
		assertEquals(offset, cursor.getOffset());
	}

	private void assertCursorAt(final String message, final int offset) {
		cursor.applyMoves(graphics);
		assertEquals(message, offset, cursor.getOffset());
	}

	private static VisualizationChain buildVisualizationChain() {
		final VisualizationChain visualizationChain = new VisualizationChain();
		visualizationChain.addForRoot(new DocumentRootVisualization());
		visualizationChain.addForStructure(new ParagraphVisualization());
		visualizationChain.addForStructure(new StructureElementVisualization());
		visualizationChain.addForInline(new TextVisualization());
		return visualizationChain;
	}

	private int endOfFirstParagraph() {
		return document.getParagraphWithText(0).getEndOffset();
	}

	private int beginOfSecondParagraph() {
		return document.getEmptyParagraph(0).getStartOffset();
	}

	private int endOfSecondParagraph() {
		return document.getEmptyParagraph(0).getEndOffset();
	}

	private int endOfFirstSection() {
		return document.getSection(0).getEndOffset();
	}

	private int beginOfThirdParagraph() {
		return document.getParagraphWithText(1).getStartOffset();
	}

	private int endOfLastParagraph() {
		return document.getEmptyParagraph(1).getEndOffset();
	}

	private int endOfLastSection() {
		return document.getSection(1).getEndOffset();
	}

	private int lastOffset() {
		return document.getDocument().getEndOffset();
	}

	/*
	 * For visualization of the box structure:
	 */
	@SuppressWarnings("unused")
	private void printBoxStructure() {
		rootBox.accept(new DepthFirstTraversal<Object>() {
			private String indent = "";

			@Override
			public Object visit(final StructuralNodeReference box) {
				printBox(box);
				indent += " ";
				super.visit(box);
				indent = indent.substring(1);
				return null;
			}

			@Override
			public Object visit(final TextContent box) {
				printBox(box);
				return null;
			}

			private void printBox(final IContentBox box) {
				System.out.println(indent + "[" + box.getAbsoluteLeft() + ". " + box.getAbsoluteTop() + ", " + box.getWidth() + ", " + box.getHeight() + "] [" + box.getStartOffset() + ", "
						+ box.getEndOffset() + "]");
			}
		});
	}
}
