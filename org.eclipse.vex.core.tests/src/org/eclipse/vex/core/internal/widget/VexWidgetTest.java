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
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.CommentElement;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.dom.Text;
import org.eclipse.vex.core.internal.dom.Validator;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.junit.Test;

public class VexWidgetTest {

	@Test
	public void provideOnlyAllowedElementsFromDtd() throws Exception {
		final VexWidgetImpl widget = new VexWidgetImpl(new MockHostComponent());
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
		final VexWidgetImpl widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocument(CONTENT_NS, "p"), StyleSheet.NULL);
		assertCanInsertOnly(widget, "b", "i");
		widget.insertElement(new QualifiedName(CONTENT_NS, "b"));
		assertCanInsertOnly(widget, "b", "i");
		widget.moveBy(1);
		assertCanInsertOnly(widget, "b", "i");
	}

	@Test
	public void provideOnlyAllowedElementFromComplexSchema() throws Exception {
		final VexWidgetImpl widget = new VexWidgetImpl(new MockHostComponent());
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
	public void undoRemoveCommentTag() throws Exception {
		final VexWidgetImpl widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocument(STRUCTURE_NS, "chapter"), StyleSheet.NULL);
		widget.insertElement(new QualifiedName(CONTENT_NS, "p"));
		widget.insertText("1text before comment1");
		widget.insertElement(new CommentElement());
		final Element commentElement = widget.getDocument().getElementAt(widget.getCaretOffset());
		widget.insertText("2comment text2");
		widget.moveBy(1);
		widget.insertText("3text after comment3");

		final String expectedContentStructure = getContentStructure(widget.getDocument().getRootElement());

		widget.doWork(new Runnable() {
			public void run() {
				widget.moveTo(commentElement.getStartOffset() + 1, false);
				widget.moveTo(commentElement.getEndOffset() - 1, true);
				final DocumentFragment fragment = widget.getSelectedFragment();
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

	private static Document createDocumentWithDTD(final String dtdIdentifier, final String rootElementName) {
		final Validator validator = new WTPVEXValidator(dtdIdentifier);
		final Document document = new Document(new Element(rootElementName));
		document.setValidator(validator);
		return document;
	}

	private static Document createDocument(final String rootSchemaIdentifier, final String rootElementName) {
		final Validator validator = new WTPVEXValidator();
		final Document document = new Document(new Element(new QualifiedName(rootSchemaIdentifier, rootElementName)));
		document.setValidator(validator);
		return document;
	}

	private static void assertCanInsertOnly(final IVexWidget widget, final String... elementNames) {
		final String[] expected = sortedCopyOf(elementNames);
		final String[] actual = sortedCopyOf(widget.getValidInsertElements());
		assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] sortedCopyOf(final Object[] objects) {
		final String[] result = new String[objects.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = objects[i].toString();
		}
		Arrays.sort(result);
		return result;
	}

	private static String getContentStructure(final Element element) {
		final StringBuilder result = new StringBuilder();
		result.append("<").append(element.getQualifiedName()).append(" (").append(element.getStartOffset()).append("-").append(element.getEndOffset()).append(")");
		result.append(" ").append(element.getText());
		final List<Node> children = element.getChildNodes();
		if (!children.isEmpty()) {
			result.append(" [");
			for (final Node child : children) {
				if (child instanceof Element) {
					result.append(getContentStructure((Element) child));
				} else if (child instanceof Text) {
					result.append(getContentStructure((Text) child));
				}
			}
			result.append("]");
		}
		result.append(">");
		return result.toString();
	}

	private static String getContentStructure(final Text text) {
		final StringBuilder result = new StringBuilder();
		result.append("'(").append(text.getStartOffset()).append("-").append(text.getEndOffset()).append(") ").append(text.getText()).append("'");
		return result.toString();
	}
}
