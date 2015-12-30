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
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PRE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.SECTION;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.assertCanMorphOnlyTo;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.assertXmlEquals;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getCurrentXML;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
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
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void shouldStartInRootElement() throws Exception {
		assertSame(rootElement, widget.getCurrentElement());
		assertEquals(rootElement.getEndPosition(), widget.getCaretPosition());
	}

	@Test
	public void shouldMoveCaretIntoInsertedElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		assertEquals(titleElement.getEndPosition(), widget.getCaretPosition());
	}

	@Test
	public void shouldProvideInsertionElementAsCurrentElement() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1);
		assertEquals(titleElement.getStartPosition(), widget.getCaretPosition());
		assertSame(rootElement, widget.getCurrentElement());
	}

	@Test
	public void givenAnElementWithText_whenAtEndOfTextAndHittingBackspace_shouldDeleteLastCharacter() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello");
		widget.deletePreviousChar();
		assertEquals("Hell", titleElement.getText());
		assertEquals(titleElement.getEndPosition(), widget.getCaretPosition());
	}

	@Test
	public void givenAnElementWithText_whenAtBeginningOfTextAndHittingDelete_shouldDeleteFirstCharacter() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello");
		widget.moveBy(-5);
		widget.deleteNextChar();
		assertEquals("ello", titleElement.getText());
		assertEquals(titleElement.getStartPosition().moveBy(1), widget.getCaretPosition());
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

		widget.moveTo(para2.getStartPosition());
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

		widget.moveTo(para2.getStartPosition());
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

		widget.moveTo(para2.getStartPosition().moveBy(1));
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

		widget.moveTo(para1.getEndPosition());
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

		widget.selectContentOf(titleElement);
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
		widget.moveTo(rootElement.getEndPosition());

		widget.setReadOnly(true);
		assertFalse(widget.canInsertFragment(fragment));
		widget.insertFragment(fragment);
	}

	@Test
	public void givenNonPreElement_whenInsertingNewline_shouldSplitElement() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para1 = widget.insertElement(PARA);
		widget.insertText("Hello");
		widget.moveTo(para1.getEndPosition());
		widget.insertText("\n");
		assertEquals("Hello", para1.getText());
		assertEquals(3, rootElement.children().count());
	}

	@Test
	public void givenPreElement_whenInsertingNewline_shouldInsertNewline() throws Exception {
		final IElement para = widget.insertElement(PARA);
		final IElement preElement = widget.insertElement(PRE);
		assertEquals(preElement.getEndPosition(), widget.getCaretPosition());
		widget.insertText("line1");
		widget.insertText("\n");
		assertEquals(1, para.children().count());
		assertEquals("line1\n", preElement.getText());
	}

	@Test
	public void insertingTextAtInvalidPosition_shouldNotAlterDocument() throws Exception {
		final IElement para1 = widget.insertElement(PARA);
		widget.moveBy(1);
		final IElement para2 = widget.insertElement(PARA);
		widget.moveTo(para1.getEndPosition());
		widget.insertText("Para1");
		widget.moveTo(para2.getEndPosition());
		widget.insertText("Para2");

		// Insert position is invalid
		widget.moveTo(para1.getEndPosition().moveBy(1));
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
		widget.moveTo(para1.getEndPosition());
		widget.insertText("Para1");
		widget.moveTo(para2.getEndPosition());
		widget.insertText("Para2");

		// Insert position is invalid
		widget.moveTo(para1.getEndPosition());
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
		widget.insertElement(PARA);
		final IElement preElement = widget.insertElement(PRE);
		assertEquals(preElement.getEndPosition(), widget.getCaretPosition());
		widget.insertText("line1\nline2");
		assertEquals("line1\nline2", preElement.getText());
	}

	@Test
	public void givenPreElement_whenInsertingText_shouldKeepWhitespace() throws Exception {
		widget.insertElement(PARA);
		final IElement preElement = widget.insertElement(PRE);

		widget.moveTo(preElement.getEndPosition());
		widget.insertText("line1\nline2   end");

		final List<? extends INode> children = preElement.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("line1\nline2   end", children.get(0).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressWhitespace() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndPosition());
		widget.insertText("line1\nline2   \t end");

		final List<? extends INode> children = rootElement.children().after(para.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line with compressed whitespace", "line2 end", children.get(1).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressNewlines() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndPosition());
		widget.insertText("line1\n\nline2");

		final List<? extends INode> children = rootElement.children().after(para.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line", "line2", children.get(1).getText());
	}

	@Test
	public void givenNonPreElement_whenSplitting_shouldSplitIntoTwoElements() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndPosition());
		widget.insertText("line1line2");
		widget.moveBy(-5);

		widget.split();

		final List<? extends INode> children = rootElement.children().after(para.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("second line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line", "line2", children.get(1).getText());
	}

	@Test
	public void givenPreElement_whenSplitting_shouldSplitIntoTwoElements() throws Exception {
		final IElement para = widget.insertElement(PARA);
		final IElement preElement = widget.insertElement(PRE);
		widget.moveTo(preElement.getEndPosition());
		widget.insertText("line1line2");
		widget.moveBy(-5);

		widget.split();

		final List<? extends INode> children = para.children().after(preElement.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original pre element", children.get(0) instanceof IParent);
		assertTrue("splitted pre element", children.get(1) instanceof IParent);
		assertEquals("first line element", PRE, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("second line element", PRE, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line", "line2", children.get(1).getText());
	}

	@Test
	public void givenElementWithMultipleOccurrence_canSplitElement() throws Exception {
		widget.insertElement(PARA);
		assertTrue(widget.canSplit());
	}

	@Test
	public void givenElementWithMultipleOccurrence_whenAnythingIsSelected_canSplitElement() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("12345");
		widget.moveBy(-3, true);
		assertTrue(widget.canSplit());
	}

	@Test
	public void givenElementWithMultipleOccurrence_whenSplittingWithAnythingSelected_shouldDeleteSelectionAndSplit() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("12345");
		widget.moveBy(-3, true);
		widget.split();

		final List<? extends INode> children = rootElement.children().asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("second line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("first line", "12", children.get(0).getText());
		assertEquals("second line", "", children.get(1).getText());
	}

	@Test
	public void givenElementWithMultipleOccurrence_whenCaretRightAfterStartIndex_shouldSplit() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.insertText("12345");
		widget.moveTo(para.getStartPosition().moveBy(1));
		widget.split();

		final List<? extends INode> children = rootElement.children().asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("second line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("first line", "", children.get(0).getText());
		assertEquals("second line", "12345", children.get(1).getText());
	}

	@Test
	public void givenElementWithMultipleOccurrenceAndInlineElement_whenCaretAtInlineStartOffset_shouldSplit() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("12");
		widget.insertElement(PRE);
		widget.insertText("34");
		widget.moveBy(1);
		widget.insertText("56");
		widget.moveBy(-6);
		widget.split();

		final List<? extends INode> children = rootElement.children().asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("second line element", PARA, ((IElement) children.get(0)).getQualifiedName());
		assertEquals("first line", "12", children.get(0).getText());
		assertEquals("second line", "3456", children.get(1).getText());
		assertEquals("pre in second line", PRE, ((IElement) ((IElement) children.get(1)).children().get(0)).getQualifiedName());
	}

	@Test
	public void undoSubsequentSplitsOfInlineAndBlock() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("12");
		widget.insertElement(PRE);
		widget.insertText("34");
		widget.moveBy(1);
		widget.insertText("56");
		final String expectedXml = getCurrentXML(widget);

		widget.moveBy(-4);
		widget.split();

		widget.moveBy(-1);
		widget.split();

		widget.undo();
		widget.undo();

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void givenElementWithSingleOccurrence_cannotSplitElement() throws Exception {
		widget.insertElement(TITLE);
		assertFalse(widget.canSplit());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithSingleOccurrence_whenSplitting_shouldThrowCannotRedoException() throws Exception {
		widget.insertElement(TITLE);
		widget.split();
	}

	@Test
	public void givenComment_cannotSplit() throws Exception {
		widget.insertComment();
		assertFalse(widget.canSplit());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenComment_whenSplitting_shouldThrowDocumentValidationException() throws Exception {
		widget.insertComment();
		widget.split();
	}

	@Test
	public void givenBeforeRootElement_cannotSplitElement() throws Exception {
		widget.moveTo(rootElement.getStartPosition());
		assertFalse(widget.canSplit());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenBeforeRootElement_whenSplitting_shouldThrowDocumentValidationException() throws Exception {
		widget.moveTo(rootElement.getStartPosition());
		widget.split();
	}

	@Test
	public void undoRedoChangeNamespaceWithSubsequentDelete() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para = widget.insertElement(PARA);
		final String expectedXml = getCurrentXML(widget);

		widget.declareNamespace("ns1", "nsuri1");
		widget.select(para);
		widget.deleteSelection();

		widget.undo(); // delete
		widget.undo(); // declare namespace

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void undoRedoChangeAttributeWithSubsequentDelete() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement para = widget.insertElement(PARA);
		final String expectedXml = getCurrentXML(widget);

		widget.setAttribute("id", "newParaElement");
		widget.select(para);
		widget.deleteSelection();

		widget.undo(); // delete
		widget.undo(); // set attribute

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void whenReadOnly_cannotMorph() throws Exception {
		widget.insertElement(TITLE);
		widget.setReadOnly(true);
		assertFalse(widget.canMorph(PARA));
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotMorph() throws Exception {
		widget.insertElement(TITLE);
		widget.setReadOnly(true);
		widget.morph(PARA);
	}

	@Test
	public void cannotMorphRootElement() throws Exception {
		assertFalse(widget.canMorph(TITLE));
	}

	@Test
	public void morphEmptyElement() throws Exception {
		widget.insertElement(TITLE);

		assertTrue(widget.canMorph(PARA));
		assertCanMorphOnlyTo(widget, PARA);
		widget.morph(PARA);
	}

	@Test
	public void givenElementWithText_whenMorphing_shouldPreserveText() throws Exception {
		widget.insertElement(TITLE);
		widget.insertText("text");

		assertTrue(widget.canMorph(PARA));
		widget.morph(PARA);

		widget.selectAll();
		assertXmlEquals("<section><para>text</para></section>", widget);
	}

	@Test
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_cannotMorph() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("before");
		widget.insertElement(PRE);
		widget.insertText("within");
		widget.moveBy(1);
		widget.insertText("after");

		assertFalse(widget.canMorph(TITLE));
	}

	@Test
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_shouldNotProvideElementToMorph() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("before");
		widget.insertElement(PRE);
		widget.insertText("within");
		widget.moveBy(1);
		widget.insertText("after");

		assertCanMorphOnlyTo(widget /* nothing */);
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_shouldNotMorph() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("before");
		widget.insertElement(PRE);
		widget.insertText("within");
		widget.moveBy(1);
		widget.insertText("after");

		widget.morph(TITLE);
	}

	@Test
	public void givenAlternativeElement_whenElementIsNotAllowedAtCurrentInsertionPosition_cannotMorph() throws Exception {
		widget.insertElement(PARA);
		widget.moveBy(1);
		widget.insertElement(PARA);

		assertFalse(widget.canMorph(TITLE));
	}

	@Test
	public void givenAlternativeElement_whenElementIsNotAllowedAtCurrentInsertionPosition_shouldNotProvideElementToMorph() throws Exception {
		widget.insertElement(PARA);
		widget.moveBy(1);
		widget.insertElement(PARA);

		assertCanMorphOnlyTo(widget /* nothing */);
	}

	public void givenElementWithAttributes_whenUndoMorph_shouldPreserveAttributes() throws Exception {
		widget.insertElement(PARA);
		widget.setAttribute("id", "idValue");
		widget.morph(TITLE);
		widget.undo();

		assertEquals("idValue", widget.getCurrentElement().getAttribute("id").getValue());
	}

	public void whenReadOnly_cannotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.insertText("text");
		widget.setReadOnly(true);
		assertFalse(widget.canUnwrap());
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.insertText("text");
		widget.setReadOnly(true);
		widget.unwrap();
	}

	@Test
	public void cannotUnwrapRootElement() throws Exception {
		assertFalse(widget.canUnwrap());
	}

	@Test
	public void unwrapEmptyElement() throws Exception {
		widget.insertElement(PARA);
		widget.unwrap();

		widget.selectAll();
		assertXmlEquals("<section></section>", widget);
	}

	@Test
	public void givenInlineElementWithText_shouldUnwrapInlineElement() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.insertText("text");
		widget.unwrap();

		assertSame(para, widget.getCurrentElement());
		widget.selectAll();
		assertXmlEquals("<section><para>text</para></section>", widget);
	}

	@Test
	public void givenElementWithText_whenParentDoesNotAllowText_cannotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("text");

		assertFalse(widget.canUnwrap());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithText_whenParentDoesNotAllowText_shouldNotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertText("text");
		widget.unwrap();
	}

	@Test
	public void givenElementWithChildren_whenParentDoesNotAllowChildren_cannotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.moveBy(1);

		assertFalse(widget.canUnwrap());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithChildren_whenParentDoesNotAllowChildren_shouldNotUnwrap() throws Exception {
		widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.moveBy(1);
		widget.unwrap();
	}

	@Test
	public void givenElementWithAttributes_whenUndoUnwrap_shouldPreserveAttributes() throws Exception {
		widget.insertElement(PARA);
		widget.insertElement(PRE);
		widget.setAttribute("id", "idValue");

		widget.unwrap();
		widget.undo();

		assertEquals(PRE, widget.getCurrentElement().getQualifiedName());
		assertEquals("idValue", widget.getCurrentElement().getAttributeValue("id"));
	}

	@Test
	public void givenMultipleElementsOfSameTypeSelected_canJoin() throws Exception {
		final IElement firstPara = widget.insertElement(PARA);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		widget.moveBy(1);

		widget.moveTo(firstPara.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		assertTrue(widget.canJoin());
	}

	@Test
	public void givenMultipleElementsOfSameTypeSelected_shouldJoin() throws Exception {
		final IElement firstPara = widget.insertElement(PARA);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		widget.moveBy(1);

		widget.moveTo(firstPara.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		widget.join();

		assertXmlEquals("<section><para>123</para></section>", widget);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenJoining_shouldPreserveAttributesOfFirstElement() throws Exception {
		final IElement firstPara = widget.insertElement(PARA);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		firstPara.setAttribute("id", "para1");
		lastPara.setAttribute("id", "para3");

		widget.moveTo(firstPara.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		widget.join();

		assertXmlEquals("<section><para id=\"para1\">123</para></section>", widget);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenJoinUndone_shouldRestoreAttributesOfAllElements() throws Exception {
		final IElement firstPara = widget.insertElement(PARA);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		firstPara.setAttribute("id", "para1");
		lastPara.setAttribute("id", "para3");

		widget.moveTo(firstPara.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		widget.join();
		widget.undo();

		assertXmlEquals("<section><para id=\"para1\">1</para><para>2</para><para id=\"para3\">3</para></section>", widget);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenStructureAfterJoinWouldBeInvalid_cannotJoin() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "one-kind-of-child"), readTestStyleSheet());
		rootElement = widget.getDocument().getRootElement();

		final IElement firstSection = widget.insertElement(SECTION);
		widget.insertElement(TITLE);
		widget.moveBy(2);
		widget.insertElement(SECTION);
		widget.insertElement(TITLE);
		widget.moveBy(2);
		final IElement lastSection = widget.insertElement(SECTION);
		widget.insertElement(TITLE);

		widget.moveTo(firstSection.getStartPosition());
		widget.moveTo(lastSection.getEndPosition(), true);

		assertFalse(widget.canJoin());
	}

	@Test(expected = CannotApplyException.class)
	public void givenMultipleElementsOfSameKindSelected_whenStructureAfterJoinWouldBeInvalid_shouldJoin() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "one-kind-of-child"), readTestStyleSheet());
		rootElement = widget.getDocument().getRootElement();

		final IElement firstSection = widget.insertElement(SECTION);
		widget.insertElement(TITLE);
		widget.moveBy(2);
		widget.insertElement(SECTION);
		widget.insertElement(TITLE);
		widget.moveBy(2);
		final IElement lastSection = widget.insertElement(SECTION);
		widget.insertElement(TITLE);

		widget.moveTo(firstSection.getStartPosition());
		widget.moveTo(lastSection.getEndPosition(), true);

		widget.join();
	}

	@Test
	public void givenMultipleElementsOfDifferentTypeSelected_cannotJoin() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		widget.moveBy(1);

		widget.moveTo(title.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		assertFalse(widget.canJoin());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenMultipleElementsOfDifferentTypeSelected_shouldNotJoin() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.insertText("2");
		widget.moveBy(1);
		final IElement lastPara = widget.insertElement(PARA);
		widget.insertText("3");
		widget.moveBy(1);

		widget.moveTo(title.getStartPosition());
		widget.moveTo(lastPara.getEndPosition(), true);

		widget.join();
	}

	@Test
	public void givenSelectionIsEmpty_cannotJoin() throws Exception {
		assertFalse(widget.canJoin());
	}

	@Test
	public void givenSelectionIsEmpty_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final String expectedXml = getCurrentXML(widget);

		widget.join();

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void givenOnlyTextSelected_cannotJoin() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("title text");
		widget.moveTo(title.getStartPosition().moveBy(1), true);

		assertFalse(widget.canJoin());
	}

	@Test
	public void givenOnlyTextSelected_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("title text");
		widget.selectContentOf(title);
		final String expectedXml = getCurrentXML(widget);

		widget.join();

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void givenOnlySingleElementSelected_cannotJoin() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.moveTo(title.getStartPosition(), true);

		assertFalse(widget.canJoin());
	}

	@Test
	public void givenOnlySingleElementSelected_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.moveTo(title.getStartPosition(), true);
		final String expectedXml = getCurrentXML(widget);

		widget.join();

		assertXmlEquals(expectedXml, widget);
	}

	@Test
	public void givenMultipleCommentsSelected_canJoin() throws Exception {
		final IComment firstComment = widget.insertComment();
		widget.insertText("comment1");
		widget.moveBy(1);
		widget.insertComment();
		widget.insertText("comment2");
		widget.moveBy(1);
		final IComment lastComment = widget.insertComment();
		widget.insertText("comment3");

		widget.moveTo(firstComment.getStartPosition());
		widget.moveTo(lastComment.getEndPosition(), true);

		assertTrue(widget.canJoin());
	}

	@Test
	public void givenMultipleCommentsSelected_shouldJoin() throws Exception {
		final IComment firstComment = widget.insertComment();
		widget.insertText("comment1");
		widget.moveBy(1);
		widget.insertComment();
		widget.insertText("comment2");
		widget.moveBy(1);
		final IComment lastComment = widget.insertComment();
		widget.insertText("comment3");

		widget.moveTo(firstComment.getStartPosition());
		widget.moveTo(lastComment.getEndPosition(), true);

		widget.join();

		assertXmlEquals("<section><!--comment1comment2comment3--></section>", widget);
	}

	@Test
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextEndsWithSpace_shouldJoin() throws Exception {
		widget.insertElement(PARA);
		final IElement firstElement = widget.insertElement(PRE);
		widget.insertText("1");
		widget.moveBy(1);
		final IElement lastElement = widget.insertElement(PRE);
		widget.insertText("2 ");

		widget.moveTo(firstElement.getStartPosition());
		widget.moveTo(lastElement.getEndPosition(), true);

		widget.join();

		assertXmlEquals("<section><para><pre>12 </pre></para></section>", widget);
	}

	@Test
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextBetweenElements_cannotJoin() throws Exception {
		widget.insertElement(PARA);
		final IElement firstElement = widget.insertElement(PRE);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertText("text between elements");
		final IElement lastElement = widget.insertElement(PRE);
		widget.insertText("2 ");

		widget.moveTo(firstElement.getStartPosition());
		widget.moveTo(lastElement.getEndPosition(), true);

		assertFalse(widget.canJoin());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextBetweenElements_shouldNotJoin() throws Exception {
		widget.insertElement(PARA);
		final IElement firstElement = widget.insertElement(PRE);
		widget.insertText("1");
		widget.moveBy(1);
		widget.insertText("text between elements");
		final IElement lastElement = widget.insertElement(PRE);
		widget.insertText("2 ");

		widget.moveTo(firstElement.getStartPosition());
		widget.moveTo(lastElement.getEndPosition(), true);

		widget.join();
	}

	@Test
	public void givenDeletedText_whenDeleteUndone_shouldSetCaretToEndOfRecoveredText() throws Exception {
		final IElement title = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveTo(title.getStartPosition().moveBy(1));
		widget.moveBy(5, true);
		final int expectedCaretPosition = widget.getSelectedRange().getEndOffset() + 1;

		widget.deleteSelection();
		widget.undo();

		assertEquals(expectedCaretPosition, widget.getCaretPosition().getOffset());
	}

	@Test
	public void afterDeletingSelection_CaretPositionShouldBeValid() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndPosition());
		final IElement pre = widget.insertElement(PRE);
		widget.insertText("Hello World");
		widget.moveTo(pre.getStartPosition());
		widget.moveTo(pre.getEndPosition().moveBy(-1), true);

		widget.deleteSelection();
		assertEquals(para.getEndPosition(), widget.getCaretPosition());
	}

	@Test
	public void undoAndRedoDelete() throws Exception {
		final IElement para = widget.insertElement(PARA);
		widget.moveTo(para.getEndPosition());
		final IElement pre = widget.insertElement(PRE);
		widget.insertText("Hello World");
		final String beforeDeleteXml = getCurrentXML(widget);

		widget.moveTo(pre.getStartPosition());
		widget.moveTo(pre.getEndPosition().moveBy(-1), true);
		widget.deleteSelection();

		final String beforeUndoXml = getCurrentXML(widget);
		widget.undo();
		assertXmlEquals(beforeDeleteXml, widget);

		widget.redo();
		assertXmlEquals(beforeUndoXml, widget);
	}

	private static StyleSheet readTestStyleSheet() throws IOException {
		return new StyleSheetReader().read(TestResources.get("test.css"));
	}

}
