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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SimpleEditingTest {

	private VexWidgetImpl widget;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void shouldStartInRootElement() throws Exception {
		assertSame(rootElement, widget.getCurrentElement());
		assertEquals(rootElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void shouldMoveCaretIntoInsertedElement() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void shouldProvideInsertionElementAsCurrentElement() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1);
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
		assertSame(rootElement, widget.getCurrentElement());
	}

	@Test
	public void givenAnElementWithText_whenAtEndOfTextAndHittingBackspace_shouldDeleteLastCharacter() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello");
		widget.deletePreviousChar();
		assertEquals("Hell", titleElement.getText());
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithText_whenAtBeginningOfTextAndHittingDelete_shouldDeleteFirstCharacter() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello");
		widget.moveBy(-5);
		widget.deleteNextChar();
		assertEquals("ello", titleElement.getText());
		assertEquals(titleElement.getStartOffset() + 1, widget.getCaretOffset());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBetweenStartAndEndTagAndHittingBackspace_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.deletePreviousChar();
		assertEquals(1, rootElement.getChildCount());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBetweenStartAndEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.deleteNextChar();
		assertEquals(1, rootElement.getChildCount());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretAfterEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.moveBy(1);
		widget.deletePreviousChar();
		assertEquals(1, rootElement.getChildCount());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBeforeStartTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element paraElement = widget.insertElement(PARA);
		widget.moveBy(-1);
		widget.deleteNextChar();
		assertEquals(1, rootElement.getChildCount());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBetweenEndAndStartTagAndHittingBackspace_shouldJoinElements() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final Element para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset());
		widget.deletePreviousChar();

		assertEquals(2, rootElement.getChildCount());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBetweenEndAndStartTagAndHittingDelete_shouldJoinElements() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final Element para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset());
		widget.deleteNextChar();

		assertEquals(2, rootElement.getChildCount());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretAfterStartTagOfSecondElementAndHittingBackspace_shouldJoinElements() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final Element para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset() + 1);
		widget.deletePreviousChar();

		assertEquals(2, rootElement.getChildCount());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBeforeEndTagOfFirstElementAndHittingDelete_shouldJoinElements() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final Element para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final Element para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para1.getEndOffset());
		widget.deleteNextChar();

		assertEquals(2, rootElement.getChildCount());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenElementWithText_whenAllTextSelectedAndInsertingACharacter_shouldReplaceAllTextWithNewCharacter() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");

		widget.moveTo(titleElement.getStartOffset() + 1, true);
		widget.insertChar('A');

		assertEquals("A", titleElement.getText());
	}

	@Test
	public void givenAnElement_whenInsertingAComment_elementShouldContainComment() throws Exception {
		final Comment comment = widget.insertComment();
		assertTrue(rootElement.getRange().contains(comment.getRange()));
		assertSame(rootElement, comment.getParent());
		assertEquals(comment.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithComment_whenInsertingTextWithinComment_shouldAddTextToComment() throws Exception {
		final Comment comment = widget.insertComment();
		widget.insertText("Hello World");
		assertEquals("Hello World", comment.getText());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingBackspace_shouldDeleteComment() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		final Comment comment = widget.insertComment();
		widget.deletePreviousChar();
		assertEquals(0, titleElement.getChildCount());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingDelete_shouldDeleteComment() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		final Comment comment = widget.insertComment();
		widget.deleteNextChar();
		assertEquals(0, titleElement.getChildCount());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAComment_whenCaretInComment_shouldNotAllowToInsertAComment() throws Exception {
		widget.insertComment();
		assertFalse("can insert comment within comment", widget.canInsertComment());
	}
}
