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
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.QualifiedName;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DocumentTest {

	@Test
	public void createDocumentWithRootElement() throws Exception {
		final Element rootElement = new Element("root");
		final Document document = new Document(rootElement);
		assertDocumentConnectedToRootElement(rootElement, document);
	}

	@Test
	public void createDocumentWithRootElementAndContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element rootElement = new Element("root");
		rootElement.associate(content, new ContentRange(0, 1));
		final Document document = new Document(content, rootElement);
		assertDocumentConnectedToRootElement(rootElement, document);
	}

	@Test(expected = AssertionFailedException.class)
	public void rootElementMustAlreadyBeAssociatedIfDocumentCreatedWithContent() throws Exception {
		final GapContent content = new GapContent(10);
		final Element rootElement = new Element("root");
		final Document document = new Document(content, rootElement);
		assertDocumentConnectedToRootElement(rootElement, document);
	}

	@Test
	public void createFragmentWithTextAndChild() throws Exception {
		final Document document = new Document(new Element("root"));
		final Element childElement = document.insertElement(1, new QualifiedName(null, "child"));
		document.insertText(childElement.getStartOffset(), "Hello ");
		document.insertText(childElement.getEndOffset(), "Child");
		document.insertText(childElement.getEndOffset() + 1, " World");
		final ContentRange range = childElement.getRange().moveBounds(-2, 2);
		final DocumentFragment fragment = document.getFragment(range);
		assertEquals(11, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes());
	}

	@Test
	public void createFragmentWithExactlyOneChild() throws Exception {
		final Document document = new Document(new Element("root"));
		final Element childElement = document.insertElement(1, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Child");
		final ContentRange range = childElement.getRange();
		final DocumentFragment fragment = document.getFragment(range);
		assertEquals(7, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes());
	}

	@Test
	public void givenElementWithText_whenRangeBeginsFromStartOffset_shouldProvideParentAsCommenElement() throws Exception {
		final Document document = new Document(new Element("root"));
		final Element childElement = document.insertElement(1, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Hello World");

		final Element commonElement = document.findCommonElement(childElement.getStartOffset(), childElement.getEndOffset() - 5);

		assertSame(document.getRootElement(), commonElement);
	}

	private static void assertNodesEqual(final List<? extends Node> expected, final List<? extends Node> actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertNodeEquals(expected.get(i), actual.get(i));
		}
	}

	private static void assertNodeEquals(final Node expected, final Node actual) {
		assertSame(expected.getClass(), actual.getClass());
		assertEquals(expected.getText(), actual.getText());
		if (expected instanceof Parent) {
			assertNodesEqual(((Parent) expected).getChildNodes(), ((Parent) actual).getChildNodes());
		}
	}

	private static void assertDocumentConnectedToRootElement(final Element rootElement, final Document document) {
		assertNotNull(document.getContent());
		assertTrue(document.isAssociated());
		assertTrue(rootElement.isAssociated());
		assertSame(document, rootElement.getParent());
		assertTrue(rootElement.getStartOffset() >= document.getStartOffset());
		assertTrue(rootElement.getEndOffset() <= document.getEndOffset());
	}

}
