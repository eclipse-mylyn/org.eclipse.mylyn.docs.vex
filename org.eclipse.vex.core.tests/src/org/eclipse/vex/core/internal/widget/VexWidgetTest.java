/*******************************************************************************
 * Copyright (c) 2010, Florian Thienel and others.
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
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.junit.Before;
import org.junit.Test;

public class VexWidgetTest {

	public static final QualifiedName TITLE = new QualifiedName(null, "title");
	public static final QualifiedName PARA = new QualifiedName(null, "para");
	public static final QualifiedName PRE = new QualifiedName(null, "pre");

	private IVexWidget widget;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
	}

	@Test
	public void provideOnlyAllowedElementsFromDtd() throws Exception {
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		assertCanInsertOnly(widget, "title", "para");
		widget.insertElement(new QualifiedName(null, "title"));
		assertCanInsertOnly(widget);
		widget.moveBy(1);
		assertCanInsertOnly(widget, "para");
		widget.insertElement(new QualifiedName(null, "para"));
		widget.moveBy(1);
		assertCanInsertOnly(widget, "para");
	}

	@Test
	public void provideOnlyAllowedElementsFromSimpleSchema() throws Exception {
		widget.setDocument(createDocument(CONTENT_NS, "p"), StyleSheet.NULL);
		assertCanInsertOnly(widget, "b", "i");
		widget.insertElement(new QualifiedName(CONTENT_NS, "b"));
		assertCanInsertOnly(widget, "b", "i");
		widget.moveBy(1);
		assertCanInsertOnly(widget, "b", "i");
	}

	@Test
	public void provideOnlyAllowedElementFromComplexSchema() throws Exception {
		widget.setDocument(createDocument(STRUCTURE_NS, "chapter"), StyleSheet.NULL);
		assertCanInsertOnly(widget, "title", "chapter", "p");
		widget.insertElement(new QualifiedName(STRUCTURE_NS, "title"));
		assertCanInsertOnly(widget);
		widget.moveBy(1);
		//		assertCanInsertOnly(widget, "chapter", "p");
		widget.insertElement(new QualifiedName(CONTENT_NS, "p"));
		assertCanInsertOnly(widget, "b", "i");
		widget.moveBy(1);
		//		assertCanInsertOnly(widget, "p");
		// FIXME: maybe the schema is still not what I mean
	}

	@Test
	public void provideNoAllowedElementsForInsertionInComment() throws Exception {
		final BaseVexWidget widget = new BaseVexWidget(new MockHostComponent());
		final IDocument document = createDocument(STRUCTURE_NS, "chapter");
		widget.setDocument(document, StyleSheet.NULL);
		widget.insertElement(new QualifiedName(STRUCTURE_NS, "title"));
		widget.moveBy(1);
		widget.insertElement(new QualifiedName(CONTENT_NS, "p"));
		widget.insertComment();

		assertCannotInsertAnything(widget);
	}

	@Test
	public void undoRemoveCommentTag() throws Exception {
		final BaseVexWidget widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(createDocument(STRUCTURE_NS, "chapter"), StyleSheet.NULL);
		widget.insertElement(new QualifiedName(CONTENT_NS, "p"));
		widget.insertText("1text before comment1");
		widget.insertComment();
		final INode comment = widget.getDocument().getChildAt(widget.getCaretOffset());
		widget.insertText("2comment text2");
		widget.moveBy(1);
		widget.insertText("3text after comment3");

		final String expectedContentStructure = getContentStructure(widget.getDocument().getRootElement());

		widget.doWork(new Runnable() {
			public void run() {
				widget.moveTo(comment.getStartOffset() + 1, false);
				widget.moveTo(comment.getEndOffset() - 1, true);
				final IDocumentFragment fragment = widget.getSelectedFragment();
				widget.deleteSelection();

				widget.moveBy(-1, false);
				widget.moveBy(1, true);
				widget.deleteSelection();

				widget.insertFragment(fragment);
			}
		});

		widget.undo();

		assertEquals(expectedContentStructure, getContentStructure(widget.getDocument().getRootElement()));
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

	public static void assertCanInsertOnly(final IVexWidget widget, final String... elementNames) {
		final String[] expected = sortedCopyOf(elementNames);
		final String[] actual = sortedCopyOf(widget.getValidInsertElements());
		assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	public static void assertCannotInsertAnything(final IVexWidget widget) {
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

	public static String getCurrentXML(final IVexWidget widget) {
		return new XMLFragment(widget.getDocument().getFragment(widget.getDocument().getRootElement().getRange())).getXML();
	}
}
