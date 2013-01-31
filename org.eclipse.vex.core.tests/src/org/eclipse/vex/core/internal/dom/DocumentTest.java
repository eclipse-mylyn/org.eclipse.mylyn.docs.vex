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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.QualifiedName;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DocumentTest {

	@Test
	public void createDocumentWithRootElement() throws Exception {
		final Document document = new Document(new QualifiedName(null, "root"));
		assertDocumentConnectedToRootElement(document.getRootElement(), document);
	}

	@Test
	public void createDocumentWithRootElementAndContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
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
		final Document document = new Document(new QualifiedName(null, "root"));
		final Element childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getStartOffset(), "Hello ");
		document.insertText(childElement.getEndOffset(), "Child");
		document.insertText(childElement.getEndOffset() + 1, " World");
		final ContentRange range = childElement.getRange().resizeBy(-2, 2);
		final DocumentFragment fragment = document.getFragment(range);
		assertEquals(11, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes(), range.getStartOffset());
	}

	@Test
	public void createFragmentWithExactlyOneChild() throws Exception {
		final Document document = new Document(new QualifiedName(null, "root"));
		final Element childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Child");
		final ContentRange range = childElement.getRange();
		final DocumentFragment fragment = document.getFragment(range);
		assertEquals(7, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes(), range.getStartOffset());
	}

	@Test
	public void givenElementWithText_whenRangeBeginsFromStartOffset_shouldProvideParentAsCommenNode() throws Exception {
		final Document document = new Document(new QualifiedName(null, "root"));
		final Element childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Hello World");

		final Node commonNode = document.findCommonNode(childElement.getStartOffset(), childElement.getEndOffset() - 5);

		assertSame(document.getRootElement(), commonNode);
	}

	@Test
	public void givenElementWithText_whenRangeWithinText_shouldProvideElementAsCommonNode() throws Exception {
		final Document document = new Document(new QualifiedName(null, "root"));
		final Element childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Hello World");

		final Node commonNode = document.findCommonNode(childElement.getStartOffset() + 2, childElement.getEndOffset() - 5);

		assertSame(childElement, commonNode);
	}

	@Test
	public void insertFragmentWithChildGrandChildAndText() throws Exception {
		final Document document = new Document(new QualifiedName(null, "root"));
		final Element child = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(child.getEndOffset(), "Hello ");
		final Element grandChild = document.insertElement(child.getEndOffset(), new QualifiedName(null, "grandchild"));
		document.insertText(grandChild.getEndOffset(), "Grandchild");
		document.insertText(child.getEndOffset(), " World");

		final DocumentFragment expectedFragment = document.getFragment(child.getRange());
		document.insertFragment(document.getRootElement().getEndOffset(), expectedFragment);
		final DocumentFragment actualFragment = document.getFragment(new ContentRange(child.getEndOffset() + 1, document.getRootElement().getEndOffset() - 1));

		assertNodeEquals(expectedFragment, actualFragment, 0);
	}

	private static void assertNodesEqual(final Iterable<Node> expected, final Iterable<Node> actual, final int rangeOffsetExpected) {
		final Iterator<Node> expectedIterator = expected.iterator();
		final Iterator<Node> actualIterator = actual.iterator();
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			assertNodeEquals(expectedIterator.next(), actualIterator.next(), rangeOffsetExpected);
		}
		assertFalse("more elements expected", expectedIterator.hasNext());
		assertFalse("less elements expected", actualIterator.hasNext());
	}

	private static void assertNodeEquals(final Node expected, final Node actual, final int rangeOffsetExpected) {
		assertSame("node class", expected.getClass(), actual.getClass());
		assertEquals("node range", expected.getRange(), actual.getRange().moveBy(rangeOffsetExpected));
		assertEquals("node text", expected.getText(), actual.getText());
		if (expected instanceof Parent) {
			assertNodesEqual(((Parent) expected).children(), ((Parent) actual).children(), rangeOffsetExpected);
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
