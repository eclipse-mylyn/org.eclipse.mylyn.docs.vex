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
		rootElement.associate(content, 0, 1);
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

	private static void assertDocumentConnectedToRootElement(final Element rootElement, final Document document) {
		assertNotNull(document.getContent());
		assertTrue(document.isAssociated());
		assertTrue(rootElement.isAssociated());
		assertSame(document, rootElement.getParent());
		assertTrue(rootElement.getStartOffset() >= document.getStartOffset());
		assertTrue(rootElement.getEndOffset() <= document.getEndOffset());
	}

	@Test
	public void createFragmentWithTextAndChild() throws Exception {
		final Document document = new Document(new Element("root"));
		final Element childElement = new Element("child");
		document.insertElement(1, childElement);
		document.insertText(childElement.getStartOffset(), "Hello ");
		document.insertText(childElement.getEndOffset(), "Child");
		document.insertText(childElement.getEndOffset() + 1, " World");
		final int startOffset = childElement.getStartOffset() - 2;
		final int endOffset = childElement.getEndOffset() + 2;
		final DocumentFragment fragment = document.getFragment(startOffset, endOffset);
		assertEquals(11, fragment.getLength());
		assertNodesEqual(document.getNodes(startOffset, endOffset), fragment.getNodes());
	}

	@Test
	public void createFragmentWithExactlyOneChild() throws Exception {
		final Document document = new Document(new Element("root"));
		final Element childElement = new Element("child");
		document.insertElement(1, childElement);
		document.insertText(childElement.getEndOffset(), "Child");
		final int startOffset = childElement.getStartOffset();
		final int endOffset = childElement.getEndOffset();
		final DocumentFragment fragment = document.getFragment(startOffset, endOffset);
		assertEquals(7, fragment.getLength());
		assertNodesEqual(document.getNodes(startOffset, endOffset), fragment.getNodes());
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
}
