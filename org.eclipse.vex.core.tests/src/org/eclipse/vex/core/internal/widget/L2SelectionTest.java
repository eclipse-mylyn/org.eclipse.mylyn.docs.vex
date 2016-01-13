/*******************************************************************************
 * Copyright (c) 2012, 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *      Carsten Hiesserich - additional tests
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PRE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SelectionTest {

	private IDocumentEditor editor;

	@Before
	public void setUp() throws Exception {
		editor = new BaseVexWidget(new MockHostComponent());
		editor.setDocument(createDocumentWithDTD(TEST_DTD, "section"));
	}

	@Test
	public void givenCaretInElement_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.moveBy(-1, true);
		assertTrue(editor.hasSelection());
		assertEquals(titleElement.getRange(), editor.getSelectedRange());
		assertEquals(titleElement.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(-5, false);
		editor.moveTo(titleElement.getStartPosition(), true);
		assertTrue(editor.hasSelection());
		assertEquals(titleElement.getRange(), editor.getSelectedRange());
		assertEquals(titleElement.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionForwardIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("before");
		final IElement innerElement = editor.insertElement(PRE);
		editor.insertText("Selection");
		editor.moveTo(innerElement.getEndPosition().moveBy(1));
		editor.insertText("after");

		editor.moveTo(innerElement.getStartPosition().moveBy(-1));
		editor.moveTo(innerElement.getStartPosition().moveBy(1), true);

		assertTrue(editor.hasSelection());
		assertEquals(innerElement.getStartPosition().moveBy(-1), editor.getSelectedPositionRange().getStartPosition());
		assertEquals(innerElement.getEndPosition().moveBy(1), editor.getSelectedPositionRange().getEndPosition());
		assertEquals(innerElement.getEndPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionBackwardIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		editor.insertElement(PARA);
		final IElement innerElement = editor.insertElement(PRE);
		editor.insertText("Selection");
		editor.moveTo(innerElement.getEndPosition().moveBy(1));
		editor.insertText("after");
		editor.moveTo(innerElement.getEndPosition().moveBy(-1));
		editor.moveTo(innerElement.getStartPosition(), true);

		assertTrue(editor.hasSelection());
		assertEquals(innerElement.getRange(), editor.getSelectedRange());
		assertEquals(innerElement.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedByOneBehindEndOffset_shouldExpandSelectionToStartOffset() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(1, true);
		assertTrue(editor.hasSelection());
		assertEquals(titleElement.getRange(), editor.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedOneBehindStartOffset_shouldNotIncludeEndOffsetInSelectedRange() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveTo(titleElement.getStartPosition().moveBy(1), true);
		assertEquals(titleElement.getRange().resizeBy(1, -1), editor.getSelectedRange());
		assertEquals(titleElement.getStartPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedByOneForward_shouldExpandSelectionBehindEndOffset() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveTo(titleElement.getStartPosition(), false);
		editor.moveBy(1, true);
		assertEquals(titleElement.getRange(), editor.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedOneForwardAndOneBackward_shouldSelectNothing() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveTo(titleElement.getStartPosition(), false);
		editor.moveBy(1, true);
		editor.moveBy(-1, true);
		assertEquals(titleElement.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackOnce_shouldSelectOnlyFirstElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Hello Again");
		editor.moveTo(titleElement.getStartPosition().moveBy(3));
		editor.moveTo(paraElement.getEndPosition().moveBy(1), true);
		editor.moveBy(-1, true);
		assertEquals(titleElement.getRange(), editor.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackTwice_shouldSelectOnlyTextFragementOfFirstElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Hello Again");
		editor.moveTo(titleElement.getStartPosition().moveBy(3));
		editor.moveTo(paraElement.getEndPosition().moveBy(1), true);
		editor.moveBy(-1, true);
		editor.moveBy(-1, true);
		assertEquals(titleElement.getRange().resizeBy(3, -1), editor.getSelectedRange());
		assertEquals(titleElement.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardOnce_shouldSelectOnlySecondElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Hello Again");
		editor.moveTo(paraElement.getEndPosition().moveBy(-3));
		editor.moveTo(titleElement.getStartPosition(), true);
		editor.moveBy(1, true);
		assertEquals(paraElement.getRange(), editor.getSelectedRange());
		assertEquals(paraElement.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardTwice_shouldSelectOnlyTextFragementOfSecondElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Hello Again");
		editor.moveTo(paraElement.getEndPosition().moveBy(-3));
		editor.moveTo(titleElement.getStartPosition(), true);
		editor.moveBy(1, true);
		editor.moveBy(1, true);
		assertEquals(paraElement.getRange().resizeBy(1, -4), editor.getSelectedRange());
		assertEquals(paraElement.getStartPosition().moveBy(+1), editor.getCaretPosition());
	}

	@Test
	public void givenCarentInEmptyComment_whenMovedBeforeComment_shouldExpandSelectionToIncludeEndOffset() throws Exception {
		final IComment comment = editor.insertComment();
		editor.moveBy(-1, true);
		assertTrue(editor.hasSelection());
		assertEquals(comment.getRange(), editor.getSelectedRange());
		assertEquals(comment.getStartPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenNodeInDocument_whenNodeHasContent_shouldSelectContentOfNode() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("Hello World");

		editor.selectContentOf(title);

		assertEquals(title.getRange().resizeBy(1, -1), editor.getSelectedRange());
		assertTrue(editor.hasSelection());
	}

	@Test
	public void givenNodeInDocument_whenNodeIsEmpty_shouldMoveCaretIntoNode() throws Exception {
		final IElement title = editor.insertElement(TITLE);

		editor.selectContentOf(title);

		assertEquals(title.getEndPosition(), editor.getCaretPosition());
		assertFalse(editor.hasSelection());
	}

	@Test
	public void givenNodeInDocument_shouldSelectCompleteNodeWithStartAndEndTags() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("Hello World");

		editor.select(title);

		assertEquals(title.getRange(), editor.getSelectedRange());
		assertTrue(editor.hasSelection());
	}

	@Test
	public void givenWorkBlockStarted_whenWorkBlockNotEnded_shouldNotFireSelectionChangedEvent() throws Exception {
		// TODO should IDocumentEditor implement ISelectionProvider?
		//		hostComponent.selectionChanged = false;
		//		editor.beginWork();
		//		final IElement title = editor.insertElement(TITLE);
		//		editor.moveTo(title.getStartPosition());
		//		editor.moveTo(title.getEndPosition(), true);
		//		final boolean selectionChangedWhileWorking = hostComponent.selectionChanged;
		//		editor.endWork(true);
		//
		//		assertFalse(selectionChangedWhileWorking);
	}
}
