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

import static org.eclipse.vex.core.internal.boxes.CursorMoves.left;
import static org.eclipse.vex.core.internal.boxes.CursorMoves.right;
import static org.eclipse.vex.core.internal.boxes.CursorMoves.toAbsoluteCoordinates;
import static org.eclipse.vex.core.internal.boxes.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.boxes.CursorMoves.up;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.eclipse.vex.core.internal.visualization.DocumentRootVisualization;
import org.eclipse.vex.core.internal.visualization.ParagraphVisualization;
import org.eclipse.vex.core.internal.visualization.StructureElementVisualization;
import org.eclipse.vex.core.internal.visualization.TextVisualization;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestCursorPosition {

	private static final String LOREM_IPSUM_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.";

	private RootBox rootBox;
	private ContentMap contentMap;
	private Cursor cursor;
	private FakeGraphics graphics;

	@Before
	public void setUp() throws Exception {
		rootBox = createTestModel();
		contentMap = new ContentMap();
		contentMap.setRootBox(rootBox);
		cursor = new Cursor(contentMap);

		graphics = new FakeGraphics();
		rootBox.setWidth(200);
		rootBox.layout(graphics);
	}

	@Test
	public void canMoveCursorOneCharacterLeft() throws Exception {
		cursorAt(5);
		cursor.move(left());
		assertCursorAt(4);
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
	public void whenAtLastPosition_cannotMoveCursorOneCharacterRight() throws Exception {
		final int lastPosition = contentMap.getLastPosition();
		cursorAt(lastPosition);
		cursor.move(right());
		assertCursorAt(lastPosition);
	}

	@Test
	public void canMoveCursorOneLineUp() throws Exception {
		cursorAt(33);
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
	public void givenAtFirstLineOfParagraph_whenMovingUp_shouldMoveCursorToParagraphStartOffset() throws Exception {
		cursorAt(340);
		moveCursor(up());
		assertCursorAt(336);
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
		cursorAt(334);
		moveCursor(up());
		assertCursorAt(333);
	}

	@Test
	public void givenAtEmptyParagraphEndOffset_whenMovingUp_shouldMoveCursorToEmptyParagraphStartOffset() throws Exception {
		cursorAt(333);
		moveCursor(up());
		assertCursorAt(332);
	}

	@Test
	public void givenBelowLastLineLeftOfLastCharacter_whenMovingUp_shouldMoveCursorAtPreferredXInLastLine() throws Exception {
		cursorAt(338);
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(312);
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
		assertCursorAt(331);
	}

	@Test
	public void givenAtStartOfEmptyLine_whenMovingUp_shouldMoveCursorToStartOfLineAbove() throws Exception {
		cursorAt(333);
		moveCursor(up());
		moveCursor(up());
		assertCursorAt(311);
	}

	@Test
	public void whenClickingIntoText_shouldMoveToPositionInText() throws Exception {
		moveCursor(toAbsoluteCoordinates(18, 11));
		assertCursorAt(5);
	}

	@Test
	public void whenClickingRightOfLastLine_shouldMoveToEndOfParagraph() throws Exception {
		moveCursor(toAbsoluteCoordinates(133, 160));
		assertCursorAt(331);
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
			assertCursorAt("x=" + x, 31);
		}
	}

	@Test
	public void whenClickingInEmptyLine_shouldMoveToEndOfParagraph() throws Exception {
		moveCursor(toAbsoluteCoordinates(10, 175));
		assertCursorAt(333);
	}

	@Test
	public void whenClickingBelowLastLine_shouldMoveToEndOfParagraph() throws Exception {
		for (int x = 6; x < 194; x += 1) {
			cursorAt(0);
			moveCursor(toAbsoluteCoordinates(x, 170));
			assertCursorAt("x=" + x, 331);
		}
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
		assertEquals(offset, cursor.getPosition());
	}

	private void assertCursorAt(final String message, final int offset) {
		cursor.applyMoves(graphics);
		assertEquals(message, offset, cursor.getPosition());
	}

	private static RootBox createTestModel() {
		final IDocument document = createTestDocument();
		final VisualizationChain visualizationChain = buildVisualizationChain();
		return visualizationChain.visualizeRoot(document);
	}

	private static VisualizationChain buildVisualizationChain() {
		final VisualizationChain visualizationChain = new VisualizationChain();
		visualizationChain.addForRoot(new DocumentRootVisualization());
		visualizationChain.addForStructure(new ParagraphVisualization());
		visualizationChain.addForStructure(new StructureElementVisualization());
		visualizationChain.addForInline(new TextVisualization());
		return visualizationChain;
	}

	private static IDocument createTestDocument() {
		final Document document = new Document(new QualifiedName(null, "doc"));
		insertSection(document.getRootElement());
		insertSection(document.getRootElement());
		return document;
	}

	private static void insertSection(final IParent parent) {
		final IElement section = insertElement(parent, "section");
		insertText(insertElement(section, "para"), LOREM_IPSUM_LONG);
		insertElement(section, "para");
	}

	private static IElement insertElement(final IParent parent, final String localName) {
		final IDocument document = parent.getDocument();
		return document.insertElement(parent.getEndOffset(), new QualifiedName(null, localName));
	}

	private static void insertText(final IParent parent, final String text) {
		final IDocument document = parent.getDocument();
		document.insertText(parent.getEndOffset(), text);
	}

	/*
	 * For visualization of the box structure:
	 */
	@SuppressWarnings("unused")
	private void printBoxStructure() {
		rootBox.accept(new DepthFirstTraversal<Object>() {
			private String indent = "";

			@Override
			public Object visit(final NodeReference box) {
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
