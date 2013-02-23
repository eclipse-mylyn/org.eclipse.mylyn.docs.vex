/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - additional tests (bug 407827, 409032)
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

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SimpleEditingTest {

	private IVexWidget widget;
	private IElement rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
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
		final IElement titleElement = widget.insertElement(TITLE);
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void shouldProvideInsertionElementAsCurrentElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1);
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
		assertSame(rootElement, widget.getCurrentElement());
	}

	@Test
	public void givenAnElementWithText_whenAtEndOfTextAndHittingBackspace_shouldDeleteLastCharacter() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello");
		widget.deletePreviousChar();
		assertEquals("Hell", titleElement.getText());
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithText_whenAtBeginningOfTextAndHittingDelete_shouldDeleteFirstCharacter() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
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
		final IElement paraElement = widget.insertElement(PARA);
		widget.deletePreviousChar();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBetweenStartAndEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.deleteNextChar();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretAfterEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.moveBy(1);
		widget.deletePreviousChar();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBeforeStartTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.moveBy(-1);
		widget.deleteNextChar();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBetweenEndAndStartTagAndHittingBackspace_shouldJoinElements() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset());
		widget.deletePreviousChar();

		assertEquals(2, rootElement.children().count());
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
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset());
		widget.deleteNextChar();

		assertEquals(2, rootElement.children().count());
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
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para2.getStartOffset() + 1);
		widget.deletePreviousChar();

		assertEquals(2, rootElement.children().count());
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
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.insertText("World");

		widget.moveTo(para1.getEndOffset());
		widget.deleteNextChar();

		assertEquals(2, rootElement.children().count());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenElementWithText_whenAllTextSelectedAndInsertingACharacter_shouldReplaceAllTextWithNewCharacter() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");

		widget.moveTo(titleElement.getStartOffset() + 1, true);
		widget.insertChar('A');

		assertEquals("A", titleElement.getText());
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertText() throws Exception {
		widget.insertElement(TITLE); // need an element where text would be valid
		widget.setReadOnly(true);
		assertFalse(widget.canInsertText());
		widget.insertText("Hello World");
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertChar() throws Exception {
		widget.insertElement(TITLE); // need an element where text would be valid
		widget.setReadOnly(true);
		assertFalse(widget.canInsertText());
		widget.insertChar('H');
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertElement() throws Exception {
		widget.setReadOnly(true);
		assertTrue(widget.getValidInsertElements().length == 0);
		widget.insertElement(TITLE);
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertComment() throws Exception {
		widget.setReadOnly(true);
		assertFalse(widget.canInsertComment());
		widget.insertComment();
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertFragment() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para = widget.insertElement(PARA);
		final IDocumentFragment fragment = widget.getDocument().getFragment(para.getRange());
		widget.moveTo(widget.getDocument().getRootElement().getEndOffset());

		widget.setReadOnly(true);
		assertFalse(widget.canInsertFragment(fragment));
		widget.insertFragment(fragment);
	}

	@Test
	public void givenNonPreElement_whenInsertingNewline_shouldSplitElement() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		rootElement = widget.getDocument().getRootElement();

		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveTo(para1.getEndOffset());
		widget.insertText("\n");
		assertEquals("Hello", para1.getText());
		assertEquals(3, rootElement.children().count());
	}

	@Test
	public void givenPreElement_whenInsertingNewline_shouldInsertNewline() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "para"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		rootElement = widget.getDocument().getRootElement();

		final IElement preElement = widget.insertElement(new QualifiedName(null, "pre"));
		assertEquals(preElement.getEndOffset(), widget.getCaretOffset());
		widget.insertText("line1");
		widget.insertText("\n");
		assertEquals(1, rootElement.children().count());
		assertEquals("line1\n", preElement.getText());
	}

	@Test
	public void insertingTextAtInvalidPosition_shouldNotAlterDocument() throws Exception {
		final IElement para1 = widget.insertElement(PARA);
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.moveTo(para1.getEndOffset());
		widget.insertText("Para1");
		widget.moveTo(para2.getEndOffset());
		widget.insertText("Para2");

		// Insert position is invalid
		widget.moveTo(para1.getEndOffset() + 1);
		try {
			widget.insertText("Test");
		} catch (final Exception ex) {
		} finally {
			assertEquals("Para1", para1.getText());
			assertEquals("Para2", para2.getText());
		}
	}

	@Test
	public void insertingElementAtInvalidPosition_shouldNotAlterDocument() throws Exception {
		final IElement para1 = widget.insertElement(PARA);
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.moveTo(para1.getEndOffset());
		widget.insertText("Para1");
		widget.moveTo(para2.getEndOffset());
		widget.insertText("Para2");

		// Insert position is invalid
		widget.moveTo(para1.getEndOffset());
		try {
			widget.insertElement(PARA);
		} catch (final Exception ex) {
		} finally {
			assertEquals("Para1", para1.getText());
			assertEquals("Para2", para2.getText());
		}
	}

	@Test
	public void givenPreElement_whenInsertingTextWithNewline_shouldInsertNewline() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "para"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		final IElement preElement = widget.insertElement(new QualifiedName(null, "pre"));
		assertEquals(preElement.getEndOffset(), widget.getCaretOffset());
		widget.insertText("line1\nline2");
		assertEquals("line1\nline2", preElement.getText());
	}

	@Test
	public void givenPreElement_whenInsertingText_shouldKeepWhitespace() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "para"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		final IElement preElement = widget.insertElement(new QualifiedName(null, "pre"));

		widget.moveTo(preElement.getEndOffset());
		widget.insertText("line1\nline2   end");

		final List<? extends INode> children = preElement.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("line1\nline2   end", children.get(0).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressWhitespace() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndOffset());
		widget.insertText("line1\nline2   \t end");

		final List<? extends INode> children = widget.getDocument().getRootElement().children().after(para.getStartOffset()).asList();
		assertEquals("single para element", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line with compressed whitespace", "line2 end", children.get(1).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressNewlines() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndOffset());
		widget.insertText("line1\n\nline2");

		final List<? extends INode> children = widget.getDocument().getRootElement().children().after(para.getStartOffset()).asList();
		assertEquals("single para element", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line", "line2", children.get(1).getText());
	}

	private static StyleSheet readTestStyleSheet() throws IOException {
		return new StyleSheetReader().read(TestResources.get("test.css"));
	}

}
