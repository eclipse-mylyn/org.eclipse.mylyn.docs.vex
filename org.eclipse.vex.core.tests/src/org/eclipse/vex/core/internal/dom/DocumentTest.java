/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - automatically declare undeclared namespaces when copiing (bug 409647)
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
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
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
		final IDocument document = new Document(new QualifiedName(null, "root"));
		final IElement childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getStartOffset(), "Hello ");
		document.insertText(childElement.getEndOffset(), "Child");
		document.insertText(childElement.getEndOffset() + 1, " World");
		final ContentRange range = childElement.getRange().resizeBy(-2, 2);
		final IDocumentFragment fragment = document.getFragment(range);
		assertEquals(11, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes(), range.getStartOffset());
	}

	@Test
	public void createFragmentWithExactlyOneChild() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		final IElement childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Child");
		final ContentRange range = childElement.getRange();
		final IDocumentFragment fragment = document.getFragment(range);
		assertEquals(7, fragment.getLength());
		assertNodesEqual(document.getNodes(range), fragment.getNodes(), range.getStartOffset());
	}

	@Test
	public void givenElementWithText_whenRangeBeginsFromStartOffset_shouldProvideParentAsCommenNode() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		final IElement childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Hello World");

		final INode commonNode = document.findCommonNode(childElement.getStartOffset(), childElement.getEndOffset() - 5);

		assertSame(document.getRootElement(), commonNode);
	}

	@Test
	public void givenElementWithText_whenRangeWithinText_shouldProvideElementAsCommonNode() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		final IElement childElement = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(childElement.getEndOffset(), "Hello World");

		final INode commonNode = document.findCommonNode(childElement.getStartOffset() + 2, childElement.getEndOffset() - 5);

		assertSame(childElement, commonNode);
	}

	@Test
	public void insertFragmentWithChildGrandChildAndText() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		final IElement child = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(child.getEndOffset(), "Hello ");
		final IElement grandChild = document.insertElement(child.getEndOffset(), new QualifiedName(null, "grandchild"));
		document.insertText(grandChild.getEndOffset(), "Grandchild");
		document.insertText(child.getEndOffset(), " World");

		final IDocumentFragment expectedFragment = document.getFragment(child.getRange());
		document.insertFragment(document.getRootElement().getEndOffset(), expectedFragment);
		final IDocumentFragment actualFragment = document.getFragment(new ContentRange(child.getEndOffset() + 1, document.getRootElement().getEndOffset() - 1));

		assertNodeEquals(expectedFragment, actualFragment, 0);
	}

	@Test
	public void insertFragment_shouldDeclareUndeclaredNamespaces() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		document.getRootElement().declareNamespace("ns1", "http://ns1");
		final IElement sourceParent = document.insertElement(2, new QualifiedName("http://ns1", "parent"));
		sourceParent.declareNamespace("ns2", "http://ns2");
		final IElement child = document.insertElement(sourceParent.getEndOffset(), new QualifiedName("http://ns2", "child"));

		final IElement targetParent = document.insertElement(document.getRootElement().getEndOffset(), new QualifiedName("http://ns1", "parent"));
		final IDocumentFragment fragment = document.getFragment(child.getRange());
		document.insertFragment(targetParent.getEndOffset(), fragment);

		assertEquals(1, targetParent.getDeclaredNamespacePrefixes().size());
		assertEquals("http://ns2", targetParent.getNamespaceURI(targetParent.getDeclaredNamespacePrefixes().iterator().next()));
	}

	@Test
	public void insertFragment_shouldNotOverrideDeclaredNamespacePrefixes() throws Exception {
		final IDocument document = new Document(new QualifiedName(null, "root"));
		document.getRootElement().declareNamespace("ns1", "http://ns1");
		final IElement sourceParent = document.insertElement(2, new QualifiedName("http://ns1", "parent"));
		sourceParent.declareNamespace("ns2", "http://ns2");
		final IElement child = document.insertElement(sourceParent.getEndOffset(), new QualifiedName("http://ns2", "child"));

		final IElement targetParent = document.insertElement(document.getRootElement().getEndOffset(), new QualifiedName("http://ns1", "parent"));
		final IDocumentFragment fragment = document.getFragment(child.getRange());
		document.insertFragment(targetParent.getEndOffset(), fragment);

		assertEquals("http://ns1", targetParent.getNamespaceURI("ns1"));
		assertEquals("http://ns2", targetParent.getNamespaceURI("ns2"));
	}

	private static void assertNodesEqual(final Iterable<? extends INode> expected, final Iterable<? extends INode> actual, final int rangeOffsetExpected) {
		final Iterator<? extends INode> expectedIterator = expected.iterator();
		final Iterator<? extends INode> actualIterator = actual.iterator();
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			assertNodeEquals(expectedIterator.next(), actualIterator.next(), rangeOffsetExpected);
		}
		assertFalse("more elements expected", expectedIterator.hasNext());
		assertFalse("less elements expected", actualIterator.hasNext());
	}

	private static void assertNodeEquals(final INode expected, final INode actual, final int rangeOffsetExpected) {
		assertSame("node class", expected.getClass(), actual.getClass());
		assertEquals("node range", expected.getRange(), actual.getRange().moveBy(rangeOffsetExpected));
		assertEquals("node text", expected.getText(), actual.getText());
		if (expected instanceof IParent) {
			assertNodesEqual(((IParent) expected).children(), ((IParent) actual).children(), rangeOffsetExpected);
		}
	}

	private static void assertDocumentConnectedToRootElement(final IElement rootElement, final Document document) {
		assertNotNull(document.getContent());
		assertTrue(document.isAssociated());
		assertTrue(rootElement.isAssociated());
		assertSame(document, rootElement.getParent());
		assertTrue(rootElement.getStartOffset() >= document.getStartOffset());
		assertTrue(rootElement.getEndOffset() <= document.getEndOffset());
	}

}
