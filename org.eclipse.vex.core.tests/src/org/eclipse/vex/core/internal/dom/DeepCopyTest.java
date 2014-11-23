/*******************************************************************************
 * Copyright (c) 2012, 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *		Carsten Hiesserich - additional test for namespace inheritance
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DeepCopyTest {

	@Test
	public void givenOneElement_shouldCopyElement() throws Exception {
		final Element element = new Element("element");

		final DeepCopy deepCopy = new DeepCopy(element);
		final List<Node> copiedNodes = deepCopy.getNodes();

		assertEquals(1, copiedNodes.size());
		assertTrue("copy should be of same type: " + copiedNodes.get(0), copiedNodes.get(0) instanceof Element);
		assertNotSame(element, copiedNodes.get(0));
	}

	@Test
	public void givenOneElementWithContent_shouldCopyAssociatedContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element element = new Element("element");
		element.associate(content, new ContentRange(0, 1));
		content.insertText(1, "Hello World");

		final DeepCopy deepCopy = new DeepCopy(element);
		final IContent copiedContent = deepCopy.getContent();

		assertNotNull(copiedContent);
		assertNotSame(content, copiedContent);
		assertEquals(content.length(), copiedContent.length());
		assertEquals(content.getRawText(), copiedContent.getRawText());
	}

	@Test
	public void givenOneElementWithHugeContent_shouldOnlyCopyRelevantContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element element = new Element("element");
		element.associate(content, new ContentRange(0, 1));
		content.insertText(2, "World");
		content.insertText(1, " New ");
		content.insertText(0, "Hello");

		final DeepCopy deepCopy = new DeepCopy(element);
		final IContent copiedContent = deepCopy.getContent();

		assertEquals(7, copiedContent.length());
		assertEquals(" New ", copiedContent.getText());
	}

	@Test
	public void givenOneElementWithHugeContent_shouldAssociateCopiedElementWithCopiedContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element element = new Element("element");
		element.associate(content, new ContentRange(0, 1));
		content.insertText(2, "World");
		content.insertText(1, " New ");
		content.insertText(0, "Hello");

		final DeepCopy deepCopy = new DeepCopy(element);
		final Element copiedElement = (Element) deepCopy.getNodes().get(0);

		assertTrue(copiedElement.isAssociated());
		assertEquals(element.getText(), copiedElement.getText());
	}

	@Test
	public void givenOneParentWithTwoChildren_shouldCopyParentAndChildren() throws Exception {
		final Element parent = new Element("parent");
		final Element child1 = new Element("child");
		child1.setAttribute("order", "1");
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.setAttribute("order", "2");
		parent.addChild(child2);

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final Iterator<INode> copiedChildren = copiedParent.children().iterator();

		assertEquals("1", ((IElement) copiedChildren.next()).getAttribute("order").getValue());
		assertEquals("2", ((IElement) copiedChildren.next()).getAttribute("order").getValue());
		assertFalse(copiedChildren.hasNext());
	}

	@Test
	public void givenOneParentWithTwoChildrenAndContent_shouldCopyParentChildrenAndContent() throws Exception {
		final IContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new ContentRange(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new ContentRange(3, 4));
		parent.addChild(child2);
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final Iterator<INode> copiedChildren = copiedParent.children().iterator();

		assertNodeIsAssociatedElementWithText("Hello", copiedChildren.next());
		assertNodeIsAssociatedText(" New ", copiedChildren.next());
		assertNodeIsAssociatedElementWithText("World", copiedChildren.next());
		assertFalse(copiedChildren.hasNext());
	}

	@Test
	public void givenOneParentWithTwoChildrenInHugeContent_shouldCopyOnlyRelevantContent() throws Exception {
		final IContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new ContentRange(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new ContentRange(3, 4));
		parent.addChild(child2);
		content.insertText(parent.getStartOffset(), "Prefix Content");
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");
		content.insertText(parent.getEndOffset() + 1, "Suffix Content");

		final DeepCopy deepCopy = new DeepCopy(parent);

		assertEquals(21, deepCopy.getContent().length());
	}

	@Test
	public void givenOneParentWithTwoChildrenAndContent_whenGivenRange_shouldOnlyCopyChildrenAndContentWithinRange() throws Exception {
		final IContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new ContentRange(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new ContentRange(3, 4));
		parent.addChild(child2);
		content.insertText(child1.getStartOffset(), "Prefix Content");
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");
		content.insertText(parent.getEndOffset(), "Suffix Content");

		final DeepCopy deepCopy = new DeepCopy(parent, new ContentRange(8, 39));

		assertEquals(32, deepCopy.getContent().length());
		assertEquals("Content\0Hello\0 New \0World\0Suffix", deepCopy.getContent().getRawText());
		assertEquals(2, deepCopy.getNodes().size());
	}

	@Test
	public void givenOneParentWithTwoCommentChildren_shouldCopyParentAndChildren() throws Exception {
		final IContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Comment child1 = new Comment();
		child1.associate(content, new ContentRange(1, 2));
		parent.addChild(child1);
		final Comment child2 = new Comment();
		child2.associate(content, new ContentRange(3, 4));
		parent.addChild(child2);
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getEndOffset(), "World");

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final Iterator<INode> copiedChildren = copiedParent.children().iterator();

		assertEquals("Hello", copiedChildren.next().getText());
		assertEquals("World", copiedChildren.next().getText());
		assertFalse(copiedChildren.hasNext());
	}

	@Test
	public void givenAnElement_shouldCopyOnlyDeclaredNamespaces() throws Exception {
		final Element parent = new Element("parent");
		parent.declareDefaultNamespace("http://namespace/default");
		parent.declareNamespace("ns1", "http://namespace/ns1");
		final Element child = new Element("child");
		child.declareNamespace("ns2", "http://namespace/ns2");
		parent.addChild(child);

		final DeepCopy deepCopy = new DeepCopy(child);
		final Element copiedChild = (Element) deepCopy.getNodes().get(0);
		assertNull(copiedChild.getDeclaredDefaultNamespaceURI());
		assertEquals(1, copiedChild.getDeclaredNamespacePrefixes().size());
		assertEquals("ns2", copiedChild.getDeclaredNamespacePrefixes().iterator().next());
	}

	@Test
	public void givenOneParentWithProcessingInstruction_shouldCopyParentAndProcessingInstruction() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());

		final ProcessingInstruction pi = new ProcessingInstruction("PI");
		pi.associate(content, new ContentRange(1, 2));
		content.insertText(2, "test=  1");

		parent.addChild(pi);

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final List<? extends INode> copiedChildren = copiedParent.children().asList();

		assertEquals(1, copiedChildren.size());
		assertTrue("Expecting the copied processing instruction", copiedChildren.get(0) instanceof ProcessingInstruction);
		assertEquals("Data should be copied", pi.getText(), copiedChildren.get(0).getText());
		assertEquals("Target should be copied", pi.getTarget(), ((ProcessingInstruction) copiedChildren.get(0)).getTarget());
	}

	@Test
	public void givenOneParentWithXInclude_shouldCopyParentAndXInclude() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());

		final QualifiedName includeName = new QualifiedName("http://www.w3.org/2001/XInclude", "include");
		final Element reference = new Element(includeName);
		reference.setAttribute("href", "testHRef");
		reference.setAttribute("parse", "xml");
		reference.setParent(parent);
		reference.associate(content, new ContentRange(1, 2));
		final IncludeNode include = new IncludeNode(reference);

		parent.addChild(include);

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final List<? extends INode> copiedChildren = copiedParent.children().asList();

		assertEquals(1, copiedChildren.size());
		assertTrue("Expecting the copied IncludeNode", copiedChildren.get(0) instanceof IncludeNode);
		final IncludeNode copiedInclude = (IncludeNode) copiedChildren.get(0);
		assertEquals("Parent of IncludeNode should be set", copiedParent, copiedInclude.getParent());
		assertEquals("Parent of reference should be set", copiedParent, copiedInclude.getReference().getParent());
		assertEquals(includeName, copiedInclude.getReference().getQualifiedName());

		assertEquals("Attribute 'href' should be copied", "testHRef", copiedInclude.getReference().getAttribute("href").getValue());
		assertEquals("Attribute 'parse' should be copied", "xml", copiedInclude.getReference().getAttribute("parse").getValue());
	}

	@Test
	public void givenXIncludeWithContent_shouldCopyContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());

		final QualifiedName includeName = new QualifiedName("http://www.w3.org/2001/XInclude", "include");
		final QualifiedName fallbackName = new QualifiedName("http://www.w3.org/2001/XInclude", "fallback");

		final Element reference = new Element(includeName);
		reference.setAttribute("href", "testHRef");
		reference.setAttribute("parse", "xml");
		reference.setParent(parent);
		reference.associate(content, new ContentRange(1, 4));
		final IncludeNode include = new IncludeNode(reference);

		final Element fallback = new Element(fallbackName);
		fallback.associate(content, new ContentRange(2, 3));
		reference.addChild(fallback);

		content.insertText(3, "fallbackText");

		parent.addChild(include);

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final List<? extends INode> copiedChildren = copiedParent.children().asList();

		assertEquals(1, copiedChildren.size());
		assertTrue("Expecting the copied IncludeNode", copiedChildren.get(0) instanceof IncludeNode);
		final IncludeNode copiedInclude = (IncludeNode) copiedChildren.get(0);

		final Element copiedFallback = (Element) copiedInclude.getReference().children().get(0);
		assertEquals("Expecting the copied fallback element", fallbackName, copiedFallback.getQualifiedName());
		assertEquals("Parent of fallback should be set", copiedInclude.getReference(), copiedFallback.getParent());
		assertEquals("Copied text content", "fallbackText", copiedFallback.getText());
	}

	private static void assertNodeIsAssociatedElementWithText(final String expectedText, final INode actualNode) {
		assertTrue(actualNode.isAssociated());
		assertTrue(actualNode instanceof Element);
		assertEquals(expectedText, actualNode.getText());
	}

	private static void assertNodeIsAssociatedText(final String expectedText, final INode actualNode) {
		assertTrue(actualNode.isAssociated());
		assertTrue(actualNode instanceof IText);
		assertEquals(expectedText, actualNode.getText());
	}
}
