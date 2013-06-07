/*******************************************************************************
 * Copyright (c) 2010, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - test for handling of attribute namespaces in FindUndeclaredNamespacesVisitor
 * 
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Florian Thienel
 */
public class NamespaceTest {

	@Test
	public void attributeQualifiedName() throws Exception {
		final IAttribute attributeWithoutNamespace = new Attribute(null, "localName", "value");
		assertEquals("localName", attributeWithoutNamespace.getQualifiedName().toString());
		final IAttribute attributeWithNamespace = new Attribute(null, new QualifiedName("http://namespace/uri", "localName"), "value");
		assertEquals("http://namespace/uri:localName", attributeWithNamespace.getQualifiedName().toString());
	}

	@Test
	public void declareNamespace() throws Exception {
		final IElement element = new Element("element");
		assertNull(element.getNamespaceURI("nsPrefix"));
		assertNull(element.getNamespacePrefix("http://namespace/uri"));

		element.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("http://namespace/uri", element.getNamespaceURI("nsPrefix"));
		assertEquals("nsPrefix", element.getNamespacePrefix("http://namespace/uri"));
	}

	@Test
	public void transitiveNamespaceDeclaration() throws Exception {
		final Element parent = new Element("parent");
		final Element child = new Element("child");
		child.setParent(parent);
		assertNull(child.getNamespaceURI("nsPrefix"));
		assertNull(child.getNamespacePrefix("http://namespace/uri"));

		parent.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("http://namespace/uri", child.getNamespaceURI("nsPrefix"));
		assertEquals("nsPrefix", child.getNamespacePrefix("http://namespace/uri"));
	}

	@Test
	public void removeNamespaceDeclaration() throws Exception {
		final IElement element = new Element("element");
		element.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("http://namespace/uri", element.getNamespaceURI("nsPrefix"));
		assertEquals("nsPrefix", element.getNamespacePrefix("http://namespace/uri"));

		element.removeNamespace("nsPrefix");
		assertNull(element.getNamespaceURI("nsPrefix"));
		assertNull(element.getNamespacePrefix("http://namespace/uri"));
	}

	@Test
	public void overloadNamespaceDeclaration() throws Exception {
		final IElement element = new Element("element");
		element.declareNamespace("nsPrefix", "http://namespace/uri");
		element.declareNamespace("nsPrefix2", "http://namespace/uri");
		assertEquals("http://namespace/uri", element.getNamespaceURI("nsPrefix"));
		assertEquals("http://namespace/uri", element.getNamespaceURI("nsPrefix2"));
		assertNotNull(element.getNamespacePrefix("http://namespace/uri"));
	}

	@Test
	public void overrideNamespaceDeclaration() throws Exception {
		final Element parent = new Element("parent");
		final Element child = new Element("child");
		child.setParent(parent);
		parent.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("http://namespace/uri", child.getNamespaceURI("nsPrefix"));
		assertEquals("nsPrefix", child.getNamespacePrefix("http://namespace/uri"));

		child.declareNamespace("nsPrefix", "http://namespace2/uri");
		assertNull(child.getNamespacePrefix("http://namespace/uri"));
		assertEquals("http://namespace2/uri", child.getNamespaceURI("nsPrefix"));
		assertEquals("nsPrefix", child.getNamespacePrefix("http://namespace2/uri"));
	}

	@Test
	public void ignoreEmptyNamespaceURI() throws Exception {
		final IElement element = new Element("element");
		element.declareNamespace("nsPrefix", null);
		assertNull(element.getNamespaceURI("nsPrefix"));
		assertNull(element.getNamespacePrefix(null));

		element.declareNamespace("nsPrefix", "");
		assertNull(element.getNamespaceURI("nsPrefix"));
		assertNull(element.getNamespacePrefix(""));

		element.declareNamespace("nsPrefix", " ");
		assertNull(element.getNamespaceURI("nsPrefix"));
		assertNull(element.getNamespacePrefix(" "));
		assertNull(element.getNamespacePrefix(""));
	}

	@Test
	public void handleDefaultNamespace() throws Exception {
		final IElement element = new Element("element");
		assertNull(element.getDefaultNamespaceURI());
		element.declareDefaultNamespace("http://namespace/uri");
		assertEquals("http://namespace/uri", element.getDefaultNamespaceURI());
	}

	@Test
	public void getDeclaredDefaultNamespaceURI() throws Exception {
		final Element element = new Element("element");
		final Element child = new Element("child");
		element.addChild(child);

		assertNull(element.getDeclaredDefaultNamespaceURI());
		element.declareDefaultNamespace("http://namespace/default/element");
		assertEquals("http://namespace/default/element", element.getDeclaredDefaultNamespaceURI());

		assertNull(child.getDeclaredDefaultNamespaceURI());
		child.declareDefaultNamespace("http://namespace/default/child");
		assertEquals("http://namespace/default/child", child.getDeclaredDefaultNamespaceURI());
	}

	@Test
	public void getDeclaredNamespacePrefixes() throws Exception {
		final Element element = new Element("element");
		final Element child = new Element("child");
		element.addChild(child);

		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());
		element.declareDefaultNamespace("http://namespace/default/element");
		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());

		element.declareNamespace("ns1", "http://namespace/uri/1");
		assertEquals(1, element.getDeclaredNamespacePrefixes().size());
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns1"));
		element.declareNamespace("ns2", "http://namespace/uri/2");
		assertEquals(2, element.getDeclaredNamespacePrefixes().size());
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns1"));
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns2"));

		assertTrue(child.getDeclaredNamespacePrefixes().isEmpty());
		child.declareNamespace("ns3", "http://namespace/uri/3");
		assertEquals(1, child.getDeclaredNamespacePrefixes().size());
		assertTrue(child.getDeclaredNamespacePrefixes().contains("ns3"));
	}

	@Test
	public void elementLocalName() throws Exception {
		final IElement elementWithoutNamespace = new Element("localName");
		assertEquals("localName", elementWithoutNamespace.getLocalName());
		final IElement elementWithNamespace = new Element(new QualifiedName("http://namespace/uri", "localName"));
		assertEquals("localName", elementWithNamespace.getLocalName());
	}

	@Test
	public void elementQualifiedName() throws Exception {
		final IElement elementWithoutNamespace = new Element("localName");
		assertEquals("localName", elementWithoutNamespace.getQualifiedName().toString());
		final IElement elementWithNamespace = new Element(new QualifiedName("http://namespace/uri", "localName"));
		assertEquals("http://namespace/uri:localName", elementWithNamespace.getQualifiedName().toString());
	}

	@Test
	public void elementPrefix() throws Exception {
		final IElement elementWithoutNamespace = new Element("element");
		assertNull(elementWithoutNamespace.getPrefix());

		final IElement elementWithNamespace = new Element(new QualifiedName("http://namespace/uri", "element"));
		elementWithNamespace.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("nsPrefix", elementWithNamespace.getPrefix());

		final IElement elementWithDefaultNamespace = new Element(new QualifiedName("http://namespace/uri", "element"));
		elementWithDefaultNamespace.declareDefaultNamespace("http://namespace/uri");
		assertNull(elementWithDefaultNamespace.getPrefix());
	}

	@Test
	public void elementPrefixedName() throws Exception {
		final IElement elementWithoutNamespace = new Element("element");
		assertEquals("element", elementWithoutNamespace.getPrefixedName());

		final IElement elementWithNamespace = new Element(new QualifiedName("http://namespace/uri", "element"));
		elementWithNamespace.declareNamespace("nsPrefix", "http://namespace/uri");
		assertEquals("nsPrefix:element", elementWithNamespace.getPrefixedName());

		final IElement elementWithDefaultNamespace = new Element(new QualifiedName("http://namespace/uri", "element"));
		elementWithDefaultNamespace.declareDefaultNamespace("http://namespace/uri");
		assertEquals("element", elementWithDefaultNamespace.getPrefixedName());
	}

	@Test
	public void attributePrefixedName() throws Exception {
		final IElement element = new Element(new QualifiedName("http://namespace/uri/1", "element"));
		element.declareDefaultNamespace("http://namespace/uri/default");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		element.declareNamespace("ns2", "http://namespace/uri/2");
		element.setAttribute("attribute1", "value1");
		element.setAttribute(new QualifiedName("http://namespace/uri/1", "attribute2"), "value2");
		element.setAttribute(new QualifiedName("http://namespace/uri/2", "attribute3"), "value3");

		assertEquals("attribute1", element.getAttribute("attribute1").getPrefixedName());
		assertEquals("attribute2", element.getAttribute(new QualifiedName("http://namespace/uri/1", "attribute2")).getPrefixedName());
		assertEquals("ns2:attribute3", element.getAttribute(new QualifiedName("http://namespace/uri/2", "attribute3")).getPrefixedName());
	}

	@Test
	public void readNamespaceDeclarations() throws Exception {
		final IDocument document = readDocumentFromString("<ns1:a xmlns=\"http://namespace/default\" xmlns:ns1=\"http://namespace/uri/1\"/>");
		final IElement rootElement = document.getRootElement();

		assertEquals("http://namespace/default", rootElement.getDefaultNamespaceURI());
		assertEquals("http://namespace/uri/1", rootElement.getNamespaceURI("ns1"));
	}

	private IDocument readDocumentFromString(final String documentContent) throws IOException, ParserConfigurationException, SAXException {
		return new DocumentReader().read(documentContent);
	}

	@Test
	public void readNestedNamespaceDeclarations() throws Exception {
		final IDocument document = readDocumentFromString("<ns1:a xmlns=\"http://namespace/default\" xmlns:ns1=\"http://namespace/uri/1\">" + "<ns2:b xmlns:ns2=\"http://namespace/uri/2\"/>"
				+ "</ns1:a>");
		final IElement rootElement = document.getRootElement();
		final IElement nestedElement = rootElement.childElements().first();
		assertEquals("http://namespace/default", nestedElement.getDefaultNamespaceURI());
		assertEquals("http://namespace/uri/1", nestedElement.getNamespaceURI("ns1"));
		assertEquals("http://namespace/uri/2", nestedElement.getNamespaceURI("ns2"));
		assertNull(rootElement.getNamespaceURI("ns2"));
	}

	@Test
	public void evaluateElementPrefix() throws Exception {
		final IDocument document = readDocumentFromString("<ns1:a xmlns=\"http://namespace/default\" xmlns:ns1=\"http://namespace/uri/1\">" + "<ns2:b xmlns:ns2=\"http://namespace/uri/2\"/>" + "<c />"
				+ "</ns1:a>");
		final IElement rootElement = document.getRootElement();
		assertEquals("http://namespace/uri/1", rootElement.getQualifiedName().getQualifier());
		assertEquals("a", rootElement.getLocalName());
		assertEquals("ns1:a", rootElement.getPrefixedName());

		final IElement firstNestedElement = rootElement.childElements().first();
		assertEquals("http://namespace/uri/2", firstNestedElement.getQualifiedName().getQualifier());
		assertEquals("b", firstNestedElement.getLocalName());
		assertEquals("ns2:b", firstNestedElement.getPrefixedName());

		final IElement secondNestedElement = rootElement.childElements().get(1);
		assertEquals("http://namespace/default", secondNestedElement.getQualifiedName().getQualifier());
		assertEquals("c", secondNestedElement.getLocalName());
		assertEquals("c", secondNestedElement.getPrefixedName());
	}

	@Test
	public void evaluateAttributePrefix() throws Exception {
		final IDocument document = readDocumentFromString("<ns1:a xmlns=\"http://namespace/default\" xmlns:ns1=\"http://namespace/uri/1\" attr1=\"value1\">"
				+ "<ns2:b xmlns:ns2=\"http://namespace/uri/2\" ns1:attr2=\"value2\" ns2:attr3=\"value3\" attr4=\"value4\" />" + "<c ns1:attr5=\"value5\" attr6=\"value6\" />" + "</ns1:a>");
		final IElement rootElement = document.getRootElement();
		assertTrue(rootElement.getAttributeNames().contains(new QualifiedName("http://namespace/uri/1", "attr1")));
		assertFalse(rootElement.getAttributeNames().contains(new QualifiedName("http://namespace/default", "attr1")));
		assertFalse(rootElement.getAttributeNames().contains(new QualifiedName("", "attr1")));
		assertFalse(rootElement.getAttributeNames().contains(new QualifiedName(null, "attr1")));

		final IElement firstNestedElement = rootElement.childElements().first();
		assertTrue(firstNestedElement.getAttributeNames().contains(new QualifiedName("http://namespace/uri/1", "attr2")));
		assertTrue(firstNestedElement.getAttributeNames().contains(new QualifiedName("http://namespace/uri/2", "attr3")));
		assertTrue(firstNestedElement.getAttributeNames().contains(new QualifiedName("http://namespace/uri/2", "attr4")));

		final IElement secondNestedElement = rootElement.childElements().get(1);
		assertTrue(secondNestedElement.getAttributeNames().contains(new QualifiedName("http://namespace/uri/1", "attr5")));
		assertTrue(secondNestedElement.getAttributeNames().contains(new QualifiedName("http://namespace/default", "attr6")));
	}

	@Test
	public void readWriteCycle() throws Exception {
		final String inputContent = "<?xml version='1.0' encoding='UTF-8'?> <ns1:a xmlns=\"http://namespace/default\" xmlns:ns1=\"http://namespace/uri/1\" attr1=\"value1\"> "
				+ "<ns2:b xmlns:ns2=\"http://namespace/uri/2\" ns1:attr2=\"value2\" attr3=\"value3\"/> " + "<c attr4=\"value4\" ns1:attr5=\"value5\"/>" + "</ns1:a> ";
		final IDocument document = readDocumentFromString(inputContent);

		final DocumentWriter documentWriter = new DocumentWriter();
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		documentWriter.write(document, buffer);
		final String outputContent = new String(buffer.toByteArray()).replaceAll("\\s+", " ");

		assertEquals(inputContent, outputContent);
	}

	@Test
	public void allVisibleNamespacePrefixes() throws Exception {
		final Element parent = new Element("parent");
		final Element element = new Element("element");
		final Element child = new Element("child");
		parent.addChild(element);
		parent.declareDefaultNamespace("http://namespace/uri/parent/default");
		parent.declareNamespace("ns1", "http://namespace/uri/1");
		parent.declareNamespace("ns2", "http://namespace/uri/2");
		element.addChild(child);
		element.declareNamespace("ns3", "http://namespace/uri/3");
		element.declareNamespace("ns1", "http://namespace/uri/1a");
		child.declareDefaultNamespace("http://namespace/uri/child/default");

		assertEquals(2, parent.getNamespacePrefixes().size());
		assertTrue(parent.getNamespacePrefixes().contains("ns1"));
		assertTrue(parent.getNamespacePrefixes().contains("ns2"));

		assertEquals(3, element.getNamespacePrefixes().size());
		assertTrue(element.getNamespacePrefixes().contains("ns1"));
		assertTrue(element.getNamespacePrefixes().contains("ns2"));
		assertTrue(element.getNamespacePrefixes().contains("ns3"));

		assertEquals(3, child.getNamespacePrefixes().size());
		assertTrue(child.getNamespacePrefixes().contains("ns1"));
		assertTrue(child.getNamespacePrefixes().contains("ns2"));
		assertTrue(child.getNamespacePrefixes().contains("ns3"));
	}

	@Test
	public void findUndeclaredNamespaces() throws Exception {
		final Element parent = new Element(new QualifiedName("http://namespace/default", "parent"));
		parent.declareDefaultNamespace("http://namespace/default");
		parent.declareNamespace("ns2", "http://namespace/uri/2");

		parent.addChild(new Element(new QualifiedName("http://namespace/uri/1", "child")));
		parent.addChild(new Element(new QualifiedName("http://namespace/uri/2", "child")));
		((Parent) parent.children().get(0)).addChild(new Element(new QualifiedName("http://namespace/uri/1", "child")));
		((Parent) parent.children().get(0)).addChild(new Element(new QualifiedName("http://namespace/uri/2", "child")));
		((Parent) parent.children().get(0)).addChild(new Element(new QualifiedName("http://namespace/uri/3", "child")));
		((Parent) parent.children().get(0)).addChild(new Element(new QualifiedName("http://namespace/default", "child")));

		final Set<String> undeclaredNamespaces = parent.accept(new FindUndeclaredNamespacesVisitor());
		assertTrue(undeclaredNamespaces.contains("http://namespace/uri/1"));
		assertFalse(undeclaredNamespaces.contains("http://namespace/uri/2"));
		assertTrue(undeclaredNamespaces.contains("http://namespace/uri/3"));
		assertFalse(undeclaredNamespaces.contains("http://namespace/default"));
	}

	@Test
	public void findUndeclaredAttributeNamespaces() throws Exception {
		final Element parent = new Element(new QualifiedName("http://namespace/default", "parent"));
		parent.declareDefaultNamespace("http://namespace/default");
		parent.declareNamespace("ns2", "http://namespace/uri/2");

		parent.addChild(new Element(new QualifiedName(null, "child")));

		((IElement) parent.children().get(0)).setAttribute(new QualifiedName("http://namespace/uri/1", "attr1"), "val1");
		((IElement) parent.children().get(0)).setAttribute(new QualifiedName("http://namespace/uri/2", "attr2"), "val12");

		final Set<String> undeclaredNamespaces = parent.accept(new FindUndeclaredNamespacesVisitor());
		assertTrue(undeclaredNamespaces.contains("http://namespace/uri/1"));
		assertFalse(undeclaredNamespaces.contains("http://namespace/uri/2"));
	}
}
