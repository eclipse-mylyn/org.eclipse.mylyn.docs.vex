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

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SelectionTest {

	private IVexWidget widget;
	private MockHostComponent hostComponent;

	@Before
	public void setUp() throws Exception {
		hostComponent = new MockHostComponent();
		widget = new BaseVexWidget(hostComponent);
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
	}

	@Test
	public void givenCaretInElement_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(-5, false);
		widget.moveTo(titleElement.getStartPosition(), true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionForwardIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("before");
		final IElement innerElement = widget.insertElement(PRE);
		widget.insertText("Selection");
		widget.moveTo(innerElement.getEndPosition().moveBy(1));
		widget.insertText("after");

		widget.moveTo(innerElement.getStartPosition().moveBy(-1));
		widget.moveTo(innerElement.getStartPosition().moveBy(1), true);

		assertTrue(widget.hasSelection());
		assertEquals(innerElement.getStartPosition().moveBy(-1), widget.getSelectedPositionRange().getStartPosition());
		assertEquals(innerElement.getEndPosition().moveBy(1), widget.getSelectedPositionRange().getEndPosition());
		assertEquals(innerElement.getEndPosition().moveBy(1), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionBackwardIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		widget.insertElement(PARA);
		final IElement innerElement = widget.insertElement(PRE);
		widget.insertText("Selection");
		widget.moveTo(innerElement.getEndPosition().moveBy(1));
		widget.insertText("after");
		widget.moveTo(innerElement.getEndPosition().moveBy(-1));
		widget.moveTo(innerElement.getStartPosition(), true);

		assertTrue(widget.hasSelection());
		assertEquals(innerElement.getRange(), widget.getSelectedRange());
		assertEquals(innerElement.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedByOneBehindEndOffset_shouldExpandSelectionToStartOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedOneBehindStartOffset_shouldNotIncludeEndOffsetInSelectedRange() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartPosition().moveBy(1), true);
		assertEquals(titleElement.getRange().resizeBy(1, -1), widget.getSelectedRange());
		assertEquals(titleElement.getStartPosition().moveBy(1), widget.getCaretPosition());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedByOneForward_shouldExpandSelectionBehindEndOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartPosition(), false);
		widget.moveBy(1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), widget.getCaretPosition());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedOneForwardAndOneBackward_shouldSelectNothing() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartPosition(), false);
		widget.moveBy(1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackOnce_shouldSelectOnlyFirstElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(titleElement.getStartPosition().moveBy(3));
		widget.moveTo(paraElement.getEndPosition().moveBy(1), true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndPosition().moveBy(1), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackTwice_shouldSelectOnlyTextFragementOfFirstElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(titleElement.getStartPosition().moveBy(3));
		widget.moveTo(paraElement.getEndPosition().moveBy(1), true);
		widget.moveBy(-1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getRange().resizeBy(3, -1), widget.getSelectedRange());
		assertEquals(titleElement.getEndPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardOnce_shouldSelectOnlySecondElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(paraElement.getEndPosition().moveBy(-3));
		widget.moveTo(titleElement.getStartPosition(), true);
		widget.moveBy(1, true);
		assertEquals(paraElement.getRange(), widget.getSelectedRange());
		assertEquals(paraElement.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardTwice_shouldSelectOnlyTextFragementOfSecondElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(paraElement.getEndPosition().moveBy(-3));
		widget.moveTo(titleElement.getStartPosition(), true);
		widget.moveBy(1, true);
		widget.moveBy(1, true);
		assertEquals(paraElement.getRange().resizeBy(1, -4), widget.getSelectedRange());
		assertEquals(paraElement.getStartPosition().moveBy(+1), widget.getCaretPosition());
	}

	@Test
	public void givenCarentInEmptyComment_whenMovedBeforeComment_shouldExpandSelectionToIncludeEndOffset() throws Exception {
		final IComment comment = widget.insertComment();
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(comment.getRange(), widget.getSelectedRange());
		assertEquals(comment.getStartPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenNodeInDocument_whenNodeHasContent_shouldSelectContentOfNode() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("Hello World");

		widget.selectContentOf(title);

		assertEquals(title.getRange().resizeBy(1, -1), widget.getSelectedRange());
		assertTrue(widget.hasSelection());
	}

	@Test
	public void givenNodeInDocument_whenNodeIsEmpty_shouldMoveCaretIntoNode() throws Exception {
		final IElement title = widget.insertElement(TITLE);

		widget.selectContentOf(title);

		assertEquals(title.getEndPosition(), widget.getCaretPosition());
		assertFalse(widget.hasSelection());
	}

	@Test
	public void givenNodeInDocument_shouldSelectCompleteNodeWithStartAndEndTags() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("Hello World");

		widget.select(title);

		assertEquals(title.getRange(), widget.getSelectedRange());
		assertTrue(widget.hasSelection());
	}

	@Test
	public void givenWorkBlockStarted_whenWorkBlockNotEnded_shouldNotFireSelectionChangedEvent() throws Exception {
		hostComponent.selectionChanged = false;
		widget.beginWork();
		final IElement title = widget.insertElement(TITLE);
		widget.moveTo(title.getStartPosition());
		widget.moveTo(title.getEndPosition(), true);
		final boolean selectionChangedWhileWorking = hostComponent.selectionChanged;
		widget.endWork(true);

		assertFalse(selectionChangedWhileWorking);
	}
}
