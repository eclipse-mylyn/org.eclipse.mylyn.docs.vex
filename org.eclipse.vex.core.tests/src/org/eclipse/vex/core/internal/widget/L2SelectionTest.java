/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
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
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(-5, false);
		widget.moveTo(titleElement.getStartOffset(), true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedByOneBehindEndOffset_shouldExpandSelectionToStartOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedOneBehindStartOffset_shouldNotIncludeEndOffsetInSelectedRange() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset() + 1, true);
		assertEquals(titleElement.getRange().resizeBy(1, -1), widget.getSelectedRange());
		assertEquals(titleElement.getStartOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedByOneForward_shouldExpandSelectionBehindEndOffset() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset(), false);
		widget.moveBy(1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedOneForwardAndOneBackward_shouldSelectNothing() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset(), false);
		widget.moveBy(1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackOnce_shouldSelectOnlyFirstElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(titleElement.getStartOffset() + 3);
		widget.moveTo(paraElement.getEndOffset() + 1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackTwice_shouldSelectOnlyTextFragementOfFirstElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(titleElement.getStartOffset() + 3);
		widget.moveTo(paraElement.getEndOffset() + 1, true);
		widget.moveBy(-1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getRange().resizeBy(3, -1), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardOnce_shouldSelectOnlySecondElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(paraElement.getEndOffset() - 3);
		widget.moveTo(titleElement.getStartOffset(), true);
		widget.moveBy(1, true);
		assertEquals(paraElement.getRange(), widget.getSelectedRange());
		assertEquals(paraElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardTwice_shouldSelectOnlyTextFragementOfSecondElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(paraElement.getEndOffset() - 3);
		widget.moveTo(titleElement.getStartOffset(), true);
		widget.moveBy(1, true);
		widget.moveBy(1, true);
		assertEquals(paraElement.getRange().resizeBy(1, -4), widget.getSelectedRange());
		assertEquals(paraElement.getStartOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCarentInEmptyComment_whenMovedBeforeComment_shouldExpandSelectionToIncludeEndOffset() throws Exception {
		final IComment comment = widget.insertComment();
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(comment.getRange(), widget.getSelectedRange());
		assertEquals(comment.getStartOffset(), widget.getCaretOffset());
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

		assertEquals(title.getEndOffset(), widget.getCaretOffset());
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
		widget.moveTo(title.getStartOffset());
		widget.moveTo(title.getEndOffset(), true);
		final boolean selectionChangedWhileWorking = hostComponent.selectionChanged;
		widget.endWork(true);

		assertFalse(selectionChangedWhileWorking);
	}
}
