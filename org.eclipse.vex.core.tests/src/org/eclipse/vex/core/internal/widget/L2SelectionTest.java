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
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SelectionTest {

	private VexWidgetImpl widget;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void givenCaretInElement_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(-5, false);
		widget.moveTo(titleElement.getStartOffset(), true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedByOneBehindEndOffset_shouldExpandSelectionToStartOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedOneBehindStartOffset_shouldNotIncludeEndOffsetInSelectedRange() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset() + 1, true);
		assertEquals(titleElement.getRange().resizeBy(1, -1), widget.getSelectedRange());
		assertEquals(titleElement.getStartOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedByOneForward_shouldExpandSelectionBehindEndOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset(), false);
		widget.moveBy(1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretAtStartOffsetOfElementWithText_whenMovedOneForwardAndOneBackward_shouldSelectNothing() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(titleElement.getStartOffset(), false);
		widget.moveBy(1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackOnce_shouldSelectOnlyFirstElement() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(titleElement.getStartOffset() + 3);
		widget.moveTo(paraElement.getEndOffset() + 1, true);
		widget.moveBy(-1, true);
		assertEquals(titleElement.getRange(), widget.getSelectedRange());
		assertEquals(titleElement.getEndOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBehindFollowingElementAndMovedBackTwice_shouldSelectOnlyTextFragementOfFirstElement() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
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
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.insertText("Hello Again");
		widget.moveTo(paraElement.getEndOffset() - 3);
		widget.moveTo(titleElement.getStartOffset(), true);
		widget.moveBy(1, true);
		assertEquals(paraElement.getRange(), widget.getSelectedRange());
		assertEquals(paraElement.getStartOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenCaretInElementWithText_whenMovedBeforePrecedingElementAndMovedForwardTwice_shouldSelectOnlyTextFragementOfSecondElement() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
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
		final Comment comment = widget.insertComment();
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(comment.getRange(), widget.getSelectedRange());
		assertEquals(comment.getStartOffset(), widget.getCaretOffset());
	}
}