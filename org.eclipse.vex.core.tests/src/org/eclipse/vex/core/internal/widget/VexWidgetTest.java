/*******************************************************************************
 * Copyright (c) 2010, 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florian Thienel - bug 315914, initial implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.tests.TestResources.CONTENT_NS;
import static org.eclipse.vex.core.tests.TestResources.STRUCTURE_NS;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.junit.Before;
import org.junit.Test;

public class VexWidgetTest {

	public static final QualifiedName SECTION = new QualifiedName(null, "section");
	public static final QualifiedName TITLE = new QualifiedName(null, "title");
	public static final QualifiedName PARA = new QualifiedName(null, "para");
	public static final QualifiedName PRE = new QualifiedName(null, "pre");

	private IDocumentEditor editor;
	private FakeCursor cursor;

	@Before
	public void setUp() throws Exception {
		cursor = new FakeCursor(null);
		editor = new DocumentEditor(cursor);
	}

	private void useDocument(final IDocument document) {
		cursor.setDocument(document);
		editor.setDocument(document);
	}

	@Test
	public void provideOnlyAllowedElementsFromDtd() throws Exception {
		useDocument(createDocumentWithDTD(TEST_DTD, "section"));
		assertCanInsertOnly(editor, "title", "para");
		editor.insertElement(new QualifiedName(null, "title"));
		assertCanInsertOnly(editor);
		editor.moveBy(1);
		assertCanInsertOnly(editor, "para");
		editor.insertElement(new QualifiedName(null, "para"));
		editor.moveBy(1);
		assertCanInsertOnly(editor, "para");
	}

	@Test
	public void provideOnlyAllowedElementsFromSimpleSchema() throws Exception {
		useDocument(createDocument(CONTENT_NS, "p"));
		assertCanInsertOnly(editor, "b", "i");
		editor.insertElement(new QualifiedName(CONTENT_NS, "b"));
		assertCanInsertOnly(editor, "b", "i");
		editor.moveBy(1);
		assertCanInsertOnly(editor, "b", "i");
	}

	@Test
	public void provideOnlyAllowedElementFromComplexSchema() throws Exception {
		useDocument(createDocument(STRUCTURE_NS, "chapter"));
		assertCanInsertOnly(editor, "title", "chapter", "p");
		editor.insertElement(new QualifiedName(STRUCTURE_NS, "title"));
		assertCanInsertOnly(editor);
		editor.moveBy(1);
		//		assertCanInsertOnly(widget, "chapter", "p");
		editor.insertElement(new QualifiedName(CONTENT_NS, "p"));
		assertCanInsertOnly(editor, "b", "i");
		editor.moveBy(1);
		//		assertCanInsertOnly(widget, "p");
		// FIXME: maybe the schema is still not what I mean
	}

	@Test
	public void provideNoAllowedElementsForInsertionInComment() throws Exception {
		useDocument(createDocument(STRUCTURE_NS, "chapter"));
		editor.insertElement(new QualifiedName(STRUCTURE_NS, "title"));
		editor.moveBy(1);
		editor.insertElement(new QualifiedName(CONTENT_NS, "p"));
		editor.insertComment();

		assertCannotInsertAnything(editor);
	}

	@Test
	public void undoRemoveCommentTag() throws Exception {
		useDocument(createDocument(STRUCTURE_NS, "chapter"));
		editor.insertElement(new QualifiedName(CONTENT_NS, "p"));
		editor.insertText("1text before comment1");
		final INode comment = editor.insertComment();
		editor.insertText("2comment text2");
		editor.moveBy(1);
		editor.insertText("3text after comment3");

		final String expectedContentStructure = getContentStructure(editor.getDocument().getRootElement());

		editor.doWork(new Runnable() {
			@Override
			public void run() {
				editor.selectContentOf(comment);
				final IDocumentFragment fragment = editor.getSelectedFragment();
				editor.deleteSelection();

				editor.select(comment);
				editor.deleteSelection();

				editor.insertFragment(fragment);
			}
		});

		editor.undo();

		assertEquals(expectedContentStructure, getContentStructure(editor.getDocument().getRootElement()));
	}

	/* bug 421401 */
	@Test
	public void whenClickedRightToLineEnd_shouldSetCursorToLineEnd() throws Exception {
		// TODO move to a separate test class that is specific to BaseVexWidget, this here is not editing but mouse handling
		final BaseVexWidget widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(createDocument(STRUCTURE_NS, "chapter"));
		widget.insertElement(new QualifiedName(CONTENT_NS, "p"));
		widget.insertText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris.");
		widget.setLayoutWidth(200); // breaks after "amet, "

		final ContentPosition lastPositionInFirstLine = widget.viewToModel(210, 5);
		assertEquals("last position", 30, lastPositionInFirstLine.getOffset());

		final ContentPosition firstPositionInSecondLine = widget.viewToModel(0, 15);
		assertEquals("first position", 31, firstPositionInSecondLine.getOffset());
	}

	public static IDocument createDocumentWithDTD(final String dtdIdentifier, final String rootElementName) {
		final IValidator validator = new WTPVEXValidator(dtdIdentifier);
		final Document document = new Document(new QualifiedName(null, rootElementName));
		document.setValidator(validator);
		return document;
	}

	public static IDocument createDocument(final String rootSchemaIdentifier, final String rootElementName) {
		final IValidator validator = new WTPVEXValidator();
		final Document document = new Document(new QualifiedName(rootSchemaIdentifier, rootElementName));
		document.setValidator(validator);
		return document;
	}

	public static void assertCanInsertOnly(final IDocumentEditor widget, final Object... elementNames) {
		final String[] expected = sortedCopyOf(elementNames);
		final String[] actual = sortedCopyOf(widget.getValidInsertElements());
		assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	public static void assertCanMorphOnlyTo(final IDocumentEditor widget, final Object... elementNames) {
		final String[] expected = sortedCopyOf(elementNames);
		final String[] actual = sortedCopyOf(widget.getValidMorphElements());
		assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	public static void assertCannotInsertAnything(final IDocumentEditor widget) {
		assertCanInsertOnly(widget /* nothing */);
	}

	public static String[] sortedCopyOf(final Object[] objects) {
		final String[] result = new String[objects.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = objects[i].toString();
		}
		Arrays.sort(result);
		return result;
	}

	public static String getContentStructure(final IElement element) {
		final StringBuilder result = new StringBuilder();
		result.append("<").append(element.getQualifiedName()).append(" (").append(element.getStartOffset()).append("-").append(element.getEndOffset()).append(")");
		result.append(" ").append(element.getText());
		if (!element.hasChildren()) {
			result.append(" [");
			for (final INode child : element.children()) {
				if (child instanceof IElement) {
					result.append(getContentStructure((IElement) child));
				} else if (child instanceof IText) {
					result.append(getContentStructure((IText) child));
				}
			}
			result.append("]");
		}
		result.append(">");
		return result.toString();
	}

	public static String getContentStructure(final IText text) {
		final StringBuilder result = new StringBuilder();
		result.append("'(").append(text.getStartOffset()).append("-").append(text.getEndOffset()).append(") ").append(text.getText()).append("'");
		return result.toString();
	}

	public static String getCurrentXML(final IDocumentEditor widget) {
		return new XMLFragment(widget.getDocument().getFragment(widget.getDocument().getRootElement().getRange())).getXML();
	}

	public static void assertXmlEquals(final String expected, final IDocumentEditor widget) {
		assertEquals(expected, getCurrentXML(widget));
	}
}
