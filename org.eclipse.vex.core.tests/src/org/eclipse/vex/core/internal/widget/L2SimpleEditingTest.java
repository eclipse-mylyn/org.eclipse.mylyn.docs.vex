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
import org.eclipse.vex.core.provisional.dom.IDocument;
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

	private FakeCursor cursor;
	private IDocumentEditor editor;
	private IElement rootElement;

	@Before
	public void setUp() throws Exception {
		final IDocument document = createDocumentWithDTD(TEST_DTD, "section");
		cursor = new FakeCursor(document);
		editor = new DocumentEditor(cursor, new CssWhitespacePolicy(readTestStyleSheet()));
		editor.setDocument(document);
		rootElement = editor.getDocument().getRootElement();
	}

	private void useDocument(final IDocument document) {
		cursor.setDocument(document);
		editor.setDocument(document);
		rootElement = editor.getDocument().getRootElement();
	}

	@Test
	public void shouldStartInRootElement() throws Exception {
		assertSame(rootElement, editor.getCurrentElement());
		assertEquals(rootElement.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void shouldMoveCaretIntoInsertedElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		assertEquals(titleElement.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void shouldProvideInsertionElementAsCurrentElement() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.moveBy(-1);
		assertEquals(titleElement.getStartPosition().getOffset(), editor.getCaretPosition().getOffset());
		assertSame(rootElement, editor.getCurrentElement());
	}

	@Test
	public void givenAnElementWithText_whenAtEndOfTextAndHittingBackspace_shouldDeleteLastCharacter() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello");
		editor.deleteBackward();
		assertEquals("Hell", titleElement.getText());
		assertEquals(titleElement.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenAnElementWithText_whenAtBeginningOfTextAndHittingDelete_shouldDeleteFirstCharacter() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello");
		editor.moveBy(-5);
		editor.deleteForward();
		assertEquals("ello", titleElement.getText());
		assertEquals(titleElement.getStartPosition().moveBy(1), editor.getCaretPosition());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBetweenStartAndEndTagAndHittingBackspace_shouldDeleteEmptyElement() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.deleteBackward();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBetweenStartAndEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.deleteForward();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretAfterEndTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.moveBy(1);
		editor.deleteBackward();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenAnEmptyElement_whenCaretBeforeStartTagAndHittingDelete_shouldDeleteEmptyElement() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.moveBy(-1);
		editor.deleteForward();
		assertEquals(1, rootElement.children().count());
		assertNull(paraElement.getParent());
		assertFalse(paraElement.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBetweenEndAndStartTagAndHittingBackspace_shouldJoinElements() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para1 = editor.insertElement(PARA);
		editor.insertText("Hello");
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.insertText("World");

		editor.moveTo(para2.getStartPosition());
		editor.deleteBackward();

		assertEquals(2, rootElement.children().count());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBetweenEndAndStartTagAndHittingDelete_shouldJoinElements() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para1 = editor.insertElement(PARA);
		editor.insertText("Hello");
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.insertText("World");

		editor.moveTo(para2.getStartPosition());
		editor.deleteForward();

		assertEquals(2, rootElement.children().count());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretAfterStartTagOfSecondElementAndHittingBackspace_shouldJoinElements() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para1 = editor.insertElement(PARA);
		editor.insertText("Hello");
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.insertText("World");

		editor.moveTo(para2.getStartPosition().moveBy(1));
		editor.deleteBackward();

		assertEquals(2, rootElement.children().count());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenTwoMatchingElements_whenCaretBeforeEndTagOfFirstElementAndHittingDelete_shouldJoinElements() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para1 = editor.insertElement(PARA);
		editor.insertText("Hello");
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.insertText("World");

		editor.moveTo(para1.getEndPosition());
		editor.deleteForward();

		assertEquals(2, rootElement.children().count());
		assertSame(rootElement, para1.getParent());
		assertTrue(para1.isAssociated());
		assertEquals("HelloWorld", para1.getText());
		assertNull(para2.getParent());
		assertFalse(para2.isAssociated());
	}

	@Test
	public void givenElementWithText_whenAllTextSelectedAndInsertingACharacter_shouldReplaceAllTextWithNewCharacter() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		editor.insertText("Hello World");

		editor.selectContentOf(titleElement);
		editor.insertChar('A');

		assertEquals("A", titleElement.getText());
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertText() throws Exception {
		editor.insertElement(TITLE); // need an element where text would be valid
		editor.setReadOnly(true);
		assertFalse(editor.canInsertText());
		editor.insertText("Hello World");
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertChar() throws Exception {
		editor.insertElement(TITLE); // need an element where text would be valid
		editor.setReadOnly(true);
		assertFalse(editor.canInsertText());
		editor.insertChar('H');
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertElement() throws Exception {
		editor.setReadOnly(true);
		assertTrue(editor.getValidInsertElements().length == 0);
		editor.insertElement(TITLE);
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertComment() throws Exception {
		editor.setReadOnly(true);
		assertFalse(editor.canInsertComment());
		editor.insertComment();
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotInsertFragment() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para = editor.insertElement(PARA);
		final IDocumentFragment fragment = editor.getDocument().getFragment(para.getRange());
		editor.moveTo(rootElement.getEndPosition());

		editor.setReadOnly(true);
		assertFalse(editor.canInsertFragment(fragment));
		editor.insertFragment(fragment);
	}

	@Test
	public void givenNonPreElement_whenInsertingNewline_shouldSplitElement() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para1 = editor.insertElement(PARA);
		editor.insertText("Hello");
		editor.moveTo(para1.getEndPosition());
		editor.insertText("\n");
		assertEquals("Hello", para1.getText());
		assertEquals(3, rootElement.children().count());
	}

	@Test
	public void givenPreElement_whenInsertingNewline_shouldInsertNewline() throws Exception {
		final IElement para = editor.insertElement(PARA);
		final IElement preElement = editor.insertElement(PRE);
		assertEquals(preElement.getEndPosition(), editor.getCaretPosition());
		editor.insertText("line1");
		editor.insertText("\n");
		assertEquals(1, para.children().count());
		assertEquals("line1\n", preElement.getText());
	}

	@Test
	public void insertingTextAtInvalidPosition_shouldNotAlterDocument() throws Exception {
		final IElement para1 = editor.insertElement(PARA);
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.moveTo(para1.getEndPosition());
		editor.insertText("Para1");
		editor.moveTo(para2.getEndPosition());
		editor.insertText("Para2");

		// Insert position is invalid
		editor.moveTo(para1.getEndPosition().moveBy(1));
		try {
			editor.insertText("Test");
		} catch (final Exception ex) {
		} finally {
			assertEquals("Para1", para1.getText());
			assertEquals("Para2", para2.getText());
		}
	}

	@Test
	public void insertingElementAtInvalidPosition_shouldNotAlterDocument() throws Exception {
		final IElement para1 = editor.insertElement(PARA);
		editor.moveBy(1);
		final IElement para2 = editor.insertElement(PARA);
		editor.moveTo(para1.getEndPosition());
		editor.insertText("Para1");
		editor.moveTo(para2.getEndPosition());
		editor.insertText("Para2");

		// Insert position is invalid
		editor.moveTo(para1.getEndPosition());
		try {
			editor.insertElement(PARA);
		} catch (final Exception ex) {
		} finally {
			assertEquals("Para1", para1.getText());
			assertEquals("Para2", para2.getText());
		}
	}

	@Test
	public void givenPreElement_whenInsertingTextWithNewline_shouldInsertNewline() throws Exception {
		editor.insertElement(PARA);
		final IElement preElement = editor.insertElement(PRE);
		assertEquals(preElement.getEndPosition(), editor.getCaretPosition());
		editor.insertText("line1\nline2");
		assertEquals("line1\nline2", preElement.getText());
	}

	@Test
	public void givenPreElement_whenInsertingText_shouldKeepWhitespace() throws Exception {
		editor.insertElement(PARA);
		final IElement preElement = editor.insertElement(PRE);

		editor.moveTo(preElement.getEndPosition());
		editor.insertText("line1\nline2   end");

		final List<? extends INode> children = preElement.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("line1\nline2   end", children.get(0).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressWhitespace() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.moveTo(para.getEndPosition());
		editor.insertText("line1\nline2   \t end");

		final List<? extends INode> children = rootElement.children().after(para.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line with compressed whitespace", "line2 end", children.get(1).getText());
	}

	@Test
	public void givenNonPreElement_whenInsertingText_shouldCompressNewlines() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.moveTo(para.getEndPosition());
		editor.insertText("line1\n\nline2");

		final List<? extends INode> children = rootElement.children().after(para.getStartPosition().getOffset()).asList();
		assertEquals("two para elements", 2, children.size());
		assertTrue("original para element", children.get(0) instanceof IParent);
		assertTrue("splitted para element", children.get(1) instanceof IParent);
		assertEquals("first line", "line1", children.get(0).getText());
		assertEquals("second line", "line2", children.get(1).getText());
	}

	@Test
	public void givenNonPreElement_whenSplitting_shouldSplitIntoTwoElements() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.moveTo(para.getEndPosition());
		editor.insertText("line1line2");
		editor.moveBy(-5);

		editor.split();

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
		final IElement para = editor.insertElement(PARA);
		final IElement preElement = editor.insertElement(PRE);
		editor.moveTo(preElement.getEndPosition());
		editor.insertText("line1line2");
		editor.moveBy(-5);

		editor.split();

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
		editor.insertElement(PARA);
		assertTrue(editor.canSplit());
	}

	@Test
	public void givenElementWithMultipleOccurrence_whenAnythingIsSelected_canSplitElement() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("12345");
		editor.moveBy(-3, true);
		assertTrue(editor.canSplit());
	}

	@Test
	public void givenElementWithMultipleOccurrence_whenSplittingWithAnythingSelected_shouldDeleteSelectionAndSplit() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("12345");
		editor.moveBy(-3, true);
		editor.split();

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
		final IElement para = editor.insertElement(PARA);
		editor.insertText("12345");
		editor.moveTo(para.getStartPosition().moveBy(1));
		editor.split();

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
		editor.insertElement(PARA);
		editor.insertText("12");
		editor.insertElement(PRE);
		editor.insertText("34");
		editor.moveBy(1);
		editor.insertText("56");
		editor.moveBy(-6);
		editor.split();

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
		editor.insertElement(PARA);
		editor.insertText("12");
		editor.insertElement(PRE);
		editor.insertText("34");
		editor.moveBy(1);
		editor.insertText("56");
		final String expectedXml = getCurrentXML(editor);

		editor.moveBy(-4);
		editor.split();

		editor.moveBy(-1);
		editor.split();

		editor.undo();
		editor.undo();

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void givenElementWithSingleOccurrence_cannotSplitElement() throws Exception {
		editor.insertElement(TITLE);
		assertFalse(editor.canSplit());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithSingleOccurrence_whenSplitting_shouldThrowCannotRedoException() throws Exception {
		editor.insertElement(TITLE);
		editor.split();
	}

	@Test
	public void givenComment_cannotSplit() throws Exception {
		editor.insertComment();
		assertFalse(editor.canSplit());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenComment_whenSplitting_shouldThrowDocumentValidationException() throws Exception {
		editor.insertComment();
		editor.split();
	}

	@Test
	public void givenBeforeRootElement_cannotSplitElement() throws Exception {
		editor.moveTo(rootElement.getStartPosition());
		assertFalse(editor.canSplit());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenBeforeRootElement_whenSplitting_shouldThrowDocumentValidationException() throws Exception {
		editor.moveTo(rootElement.getStartPosition());
		editor.split();
	}

	@Test
	public void undoRedoChangeNamespaceWithSubsequentDelete() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para = editor.insertElement(PARA);
		final String expectedXml = getCurrentXML(editor);

		editor.declareNamespace("ns1", "nsuri1");
		editor.select(para);
		editor.deleteSelection();

		editor.undo(); // delete
		editor.undo(); // declare namespace

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void undoRedoChangeAttributeWithSubsequentDelete() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement para = editor.insertElement(PARA);
		final String expectedXml = getCurrentXML(editor);

		editor.setAttribute("id", "newParaElement");
		editor.select(para);
		editor.deleteSelection();

		editor.undo(); // delete
		editor.undo(); // set attribute

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void whenReadOnly_cannotMorph() throws Exception {
		editor.insertElement(TITLE);
		editor.setReadOnly(true);
		assertFalse(editor.canMorph(PARA));
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotMorph() throws Exception {
		editor.insertElement(TITLE);
		editor.setReadOnly(true);
		editor.morph(PARA);
	}

	@Test
	public void cannotMorphRootElement() throws Exception {
		assertFalse(editor.canMorph(TITLE));
	}

	@Test
	public void morphEmptyElement() throws Exception {
		editor.insertElement(TITLE);

		assertTrue(editor.canMorph(PARA));
		assertCanMorphOnlyTo(editor, PARA);
		editor.morph(PARA);
	}

	@Test
	public void givenElementWithText_whenMorphing_shouldPreserveText() throws Exception {
		editor.insertElement(TITLE);
		editor.insertText("text");

		assertTrue(editor.canMorph(PARA));
		editor.morph(PARA);

		editor.selectAll();
		assertXmlEquals("<section><para>text</para></section>", editor);
	}

	@Test
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_cannotMorph() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("before");
		editor.insertElement(PRE);
		editor.insertText("within");
		editor.moveBy(1);
		editor.insertText("after");

		assertFalse(editor.canMorph(TITLE));
	}

	@Test
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_shouldNotProvideElementToMorph() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("before");
		editor.insertElement(PRE);
		editor.insertText("within");
		editor.moveBy(1);
		editor.insertText("after");

		assertCanMorphOnlyTo(editor /* nothing */);
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithChildren_whenStructureIsInvalidAfterMorphing_shouldNotMorph() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("before");
		editor.insertElement(PRE);
		editor.insertText("within");
		editor.moveBy(1);
		editor.insertText("after");

		editor.morph(TITLE);
	}

	@Test
	public void givenAlternativeElement_whenElementIsNotAllowedAtCurrentInsertionPosition_cannotMorph() throws Exception {
		editor.insertElement(PARA);
		editor.moveBy(1);
		editor.insertElement(PARA);

		assertFalse(editor.canMorph(TITLE));
	}

	@Test
	public void givenAlternativeElement_whenElementIsNotAllowedAtCurrentInsertionPosition_shouldNotProvideElementToMorph() throws Exception {
		editor.insertElement(PARA);
		editor.moveBy(1);
		editor.insertElement(PARA);

		assertCanMorphOnlyTo(editor /* nothing */);
	}

	public void givenElementWithAttributes_whenUndoMorph_shouldPreserveAttributes() throws Exception {
		editor.insertElement(PARA);
		editor.setAttribute("id", "idValue");
		editor.morph(TITLE);
		editor.undo();

		assertEquals("idValue", editor.getCurrentElement().getAttribute("id").getValue());
	}

	public void whenReadOnly_cannotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.insertText("text");
		editor.setReadOnly(true);
		assertFalse(editor.canUnwrap());
	}

	@Test(expected = ReadOnlyException.class)
	public void whenReadOnly_shouldNotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.insertText("text");
		editor.setReadOnly(true);
		editor.unwrap();
	}

	@Test
	public void cannotUnwrapRootElement() throws Exception {
		assertFalse(editor.canUnwrap());
	}

	@Test
	public void unwrapEmptyElement() throws Exception {
		editor.insertElement(PARA);
		editor.unwrap();

		editor.selectAll();
		assertXmlEquals("<section></section>", editor);
	}

	@Test
	public void givenInlineElementWithText_shouldUnwrapInlineElement() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.insertText("text");
		editor.unwrap();

		assertSame(para, editor.getCurrentElement());
		editor.selectAll();
		assertXmlEquals("<section><para>text</para></section>", editor);
	}

	@Test
	public void givenElementWithText_whenParentDoesNotAllowText_cannotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("text");

		assertFalse(editor.canUnwrap());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithText_whenParentDoesNotAllowText_shouldNotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertText("text");
		editor.unwrap();
	}

	@Test
	public void givenElementWithChildren_whenParentDoesNotAllowChildren_cannotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.moveBy(1);

		assertFalse(editor.canUnwrap());
	}

	@Test(expected = CannotApplyException.class)
	public void givenElementWithChildren_whenParentDoesNotAllowChildren_shouldNotUnwrap() throws Exception {
		editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.moveBy(1);
		editor.unwrap();
	}

	@Test
	public void givenElementWithAttributes_whenUndoUnwrap_shouldPreserveAttributes() throws Exception {
		editor.insertElement(PARA);
		editor.insertElement(PRE);
		editor.setAttribute("id", "idValue");

		editor.unwrap();
		editor.undo();

		assertEquals(PRE, editor.getCurrentElement().getQualifiedName());
		assertEquals("idValue", editor.getCurrentElement().getAttributeValue("id"));
	}

	@Test
	public void givenMultipleElementsOfSameTypeSelected_canJoin() throws Exception {
		final IElement firstPara = editor.insertElement(PARA);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		editor.moveBy(1);

		editor.moveTo(firstPara.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		assertTrue(editor.canJoin());
	}

	@Test
	public void givenMultipleElementsOfSameTypeSelected_shouldJoin() throws Exception {
		final IElement firstPara = editor.insertElement(PARA);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		editor.moveBy(1);

		editor.moveTo(firstPara.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		editor.join();

		assertXmlEquals("<section><para>123</para></section>", editor);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenJoining_shouldPreserveAttributesOfFirstElement() throws Exception {
		final IElement firstPara = editor.insertElement(PARA);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		firstPara.setAttribute("id", "para1");
		lastPara.setAttribute("id", "para3");

		editor.moveTo(firstPara.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		editor.join();

		assertXmlEquals("<section><para id=\"para1\">123</para></section>", editor);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenJoinUndone_shouldRestoreAttributesOfAllElements() throws Exception {
		final IElement firstPara = editor.insertElement(PARA);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		firstPara.setAttribute("id", "para1");
		lastPara.setAttribute("id", "para3");

		editor.moveTo(firstPara.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		editor.join();
		editor.undo();

		assertXmlEquals("<section><para id=\"para1\">1</para><para>2</para><para id=\"para3\">3</para></section>", editor);
	}

	@Test
	public void givenMultipleElementsOfSameKindSelected_whenStructureAfterJoinWouldBeInvalid_cannotJoin() throws Exception {
		useDocument(createDocumentWithDTD(TEST_DTD, "one-kind-of-child"));

		final IElement firstSection = editor.insertElement(SECTION);
		editor.insertElement(TITLE);
		editor.moveBy(2);
		editor.insertElement(SECTION);
		editor.insertElement(TITLE);
		editor.moveBy(2);
		final IElement lastSection = editor.insertElement(SECTION);
		editor.insertElement(TITLE);

		editor.moveTo(firstSection.getStartPosition());
		editor.moveTo(lastSection.getEndPosition(), true);

		assertFalse(editor.canJoin());
	}

	@Test(expected = CannotApplyException.class)
	public void givenMultipleElementsOfSameKindSelected_whenStructureAfterJoinWouldBeInvalid_shouldJoin() throws Exception {
		useDocument(createDocumentWithDTD(TEST_DTD, "one-kind-of-child"));

		final IElement firstSection = editor.insertElement(SECTION);
		editor.insertElement(TITLE);
		editor.moveBy(2);
		editor.insertElement(SECTION);
		editor.insertElement(TITLE);
		editor.moveBy(2);
		final IElement lastSection = editor.insertElement(SECTION);
		editor.insertElement(TITLE);

		editor.moveTo(firstSection.getStartPosition());
		editor.moveTo(lastSection.getEndPosition(), true);

		editor.join();
	}

	@Test
	public void givenMultipleElementsOfDifferentTypeSelected_cannotJoin() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		editor.moveBy(1);

		editor.moveTo(title.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		assertFalse(editor.canJoin());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenMultipleElementsOfDifferentTypeSelected_shouldNotJoin() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertElement(PARA);
		editor.insertText("2");
		editor.moveBy(1);
		final IElement lastPara = editor.insertElement(PARA);
		editor.insertText("3");
		editor.moveBy(1);

		editor.moveTo(title.getStartPosition());
		editor.moveTo(lastPara.getEndPosition(), true);

		editor.join();
	}

	@Test
	public void givenSelectionIsEmpty_cannotJoin() throws Exception {
		assertFalse(editor.canJoin());
	}

	@Test
	public void givenSelectionIsEmpty_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final String expectedXml = getCurrentXML(editor);

		editor.join();

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void givenOnlyTextSelected_cannotJoin() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("title text");
		editor.moveTo(title.getStartPosition().moveBy(1), true);

		assertFalse(editor.canJoin());
	}

	@Test
	public void givenOnlyTextSelected_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("title text");
		editor.selectContentOf(title);
		final String expectedXml = getCurrentXML(editor);

		editor.join();

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void givenOnlySingleElementSelected_cannotJoin() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.moveTo(title.getStartPosition(), true);

		assertFalse(editor.canJoin());
	}

	@Test
	public void givenOnlySingleElementSelected_whenRequestedToJoin_shouldIgnoreGracefully() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.moveTo(title.getStartPosition(), true);
		final String expectedXml = getCurrentXML(editor);

		editor.join();

		assertXmlEquals(expectedXml, editor);
	}

	@Test
	public void givenMultipleCommentsSelected_canJoin() throws Exception {
		final IComment firstComment = editor.insertComment();
		editor.insertText("comment1");
		editor.moveBy(1);
		editor.insertComment();
		editor.insertText("comment2");
		editor.moveBy(1);
		final IComment lastComment = editor.insertComment();
		editor.insertText("comment3");

		editor.moveTo(firstComment.getStartPosition());
		editor.moveTo(lastComment.getEndPosition(), true);

		assertTrue(editor.canJoin());
	}

	@Test
	public void givenMultipleCommentsSelected_shouldJoin() throws Exception {
		final IComment firstComment = editor.insertComment();
		editor.insertText("comment1");
		editor.moveBy(1);
		editor.insertComment();
		editor.insertText("comment2");
		editor.moveBy(1);
		final IComment lastComment = editor.insertComment();
		editor.insertText("comment3");

		editor.moveTo(firstComment.getStartPosition());
		editor.moveTo(lastComment.getEndPosition(), true);

		editor.join();

		assertXmlEquals("<section><!--comment1comment2comment3--></section>", editor);
	}

	@Test
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextEndsWithSpace_shouldJoin() throws Exception {
		editor.insertElement(PARA);
		final IElement firstElement = editor.insertElement(PRE);
		editor.insertText("1");
		editor.moveBy(1);
		final IElement lastElement = editor.insertElement(PRE);
		editor.insertText("2 ");

		editor.moveTo(firstElement.getStartPosition());
		editor.moveTo(lastElement.getEndPosition(), true);

		editor.join();

		assertXmlEquals("<section><para><pre>12 </pre></para></section>", editor);
	}

	@Test
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextBetweenElements_cannotJoin() throws Exception {
		editor.insertElement(PARA);
		final IElement firstElement = editor.insertElement(PRE);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertText("text between elements");
		final IElement lastElement = editor.insertElement(PRE);
		editor.insertText("2 ");

		editor.moveTo(firstElement.getStartPosition());
		editor.moveTo(lastElement.getEndPosition(), true);

		assertFalse(editor.canJoin());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenMultipleInlineElementsOfSameKindSelected_whenTextBetweenElements_shouldNotJoin() throws Exception {
		editor.insertElement(PARA);
		final IElement firstElement = editor.insertElement(PRE);
		editor.insertText("1");
		editor.moveBy(1);
		editor.insertText("text between elements");
		final IElement lastElement = editor.insertElement(PRE);
		editor.insertText("2 ");

		editor.moveTo(firstElement.getStartPosition());
		editor.moveTo(lastElement.getEndPosition(), true);

		editor.join();
	}

	@Test
	public void givenDeletedText_whenDeleteUndone_shouldSetCaretToEndOfRecoveredText() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("Hello World");
		editor.moveTo(title.getStartPosition().moveBy(1));
		editor.moveBy(5, true);
		final int expectedCaretPosition = editor.getSelectedRange().getEndOffset();

		editor.deleteSelection();
		editor.undo();

		assertEquals(expectedCaretPosition, editor.getCaretPosition().getOffset());
	}

	@Test
	public void afterDeletingSelection_CaretPositionShouldBeValid() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.moveTo(para.getEndPosition());
		final IElement pre = editor.insertElement(PRE);
		editor.insertText("Hello World");
		editor.moveTo(pre.getStartPosition());
		editor.moveTo(pre.getEndPosition().moveBy(-1), true);

		editor.deleteSelection();
		assertEquals(para.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void undoAndRedoDelete() throws Exception {
		final IElement para = editor.insertElement(PARA);
		editor.moveTo(para.getEndPosition());
		final IElement pre = editor.insertElement(PRE);
		editor.insertText("Hello World");
		final String beforeDeleteXml = getCurrentXML(editor);

		editor.moveTo(pre.getStartPosition());
		editor.moveTo(pre.getEndPosition().moveBy(-1), true);
		editor.deleteSelection();

		final String beforeUndoXml = getCurrentXML(editor);
		editor.undo();
		assertXmlEquals(beforeDeleteXml, editor);

		editor.redo();
		assertXmlEquals(beforeUndoXml, editor);
	}

	private static StyleSheet readTestStyleSheet() throws IOException {
		return new StyleSheetReader().read(TestResources.get("test.css"));
	}

}
