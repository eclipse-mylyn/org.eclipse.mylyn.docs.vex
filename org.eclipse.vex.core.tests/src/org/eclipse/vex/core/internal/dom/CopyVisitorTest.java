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
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.eclipse.core.runtime.QualifiedName;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class CopyVisitorTest {

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotCopyDocument() throws Exception {
		final Document document = new Document(new Element("root"));

		final CopyVisitor copyVisitor = new CopyVisitor();
		document.accept(copyVisitor);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotCopyDocumentFragment() throws Exception {
		final GapContent content = new GapContent(1);
		content.insertText(0, "abc");
		final DocumentFragment documentFragment = new DocumentFragment(content, Collections.<Node> emptyList());

		final CopyVisitor copyVisitor = new CopyVisitor();
		documentFragment.accept(copyVisitor);
	}

	@Test
	public void givenAnElement_shouldCopyNameAndAttributesAndNamespaceDeclaration() throws Exception {
		final Element element = new Element("element");
		element.setAttribute("attribute1", "value1");
		element.setAttribute("attribute2", "value2");
		element.declareDefaultNamespace("defaultNamespaceUri");
		element.declareNamespace("ns1", "additionalNamespaceUri1");
		element.declareNamespace("ns2", "additionalNamespaceUri2");

		final CopyVisitor copyVisitor = new CopyVisitor();
		element.accept(copyVisitor);
		final Element copy = copyVisitor.getCopy();

		assertEquals(new QualifiedName(null, "element"), copy.getQualifiedName());
		assertEquals(2, copy.getAttributes().size());
		assertEquals("value1", copy.getAttributeValue(new QualifiedName(null, "attribute1")));
		assertEquals("value2", copy.getAttributeValue(new QualifiedName(null, "attribute2")));
		assertEquals("defaultNamespaceUri", copy.getDeclaredDefaultNamespaceURI());
		assertEquals(2, copy.getNamespacePrefixes().size());
		assertEquals("ns1", copy.getNamespacePrefix("additionalNamespaceUri1"));
		assertEquals("ns2", copy.getNamespacePrefix("additionalNamespaceUri2"));
	}

	@Test
	public void shouldIgnoreText() throws Exception {
		final Text text = new Text(null, new GapContent(1), new Range(0, 0));

		final CopyVisitor copyVisitor = new CopyVisitor();
		text.accept(copyVisitor);

		assertNull(copyVisitor.getCopy());
	}
}
