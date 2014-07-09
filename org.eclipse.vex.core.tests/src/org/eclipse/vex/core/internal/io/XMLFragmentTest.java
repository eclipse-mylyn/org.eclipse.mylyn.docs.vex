/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IText;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class XMLFragmentTest {

	@Test
	public void createFromXmlString() throws Exception {
		final XMLFragment xmlFragment = new XMLFragment("prefix<child>inner text</child>suffix");
		assertEquals("prefix<child>inner text</child>suffix", xmlFragment.getXML());
	}

	@Test
	public void createFromDocumentFragment() throws Exception {
		final Document doc = new Document(new QualifiedName(null, "root"));
		doc.insertText(doc.getRootElement().getEndOffset(), "prefix");
		final IElement child = doc.insertElement(doc.getRootElement().getEndOffset(), new QualifiedName(null, "child"));
		doc.insertText(child.getEndOffset(), "inner text");
		doc.insertText(doc.getRootElement().getEndOffset(), "suffix");
		final DocumentFragment documentFragment = doc.getFragment(doc.getRootElement().getRange().resizeBy(1, -1));

		final XMLFragment xmlFragment = new XMLFragment(documentFragment);
		assertEquals("prefix<child>inner text</child>suffix", xmlFragment.getXML());
	}

	@Test
	public void provideDocumentFragmentForXmlString() throws Exception {
		final XMLFragment xmlFragment = new XMLFragment("prefix<child>inner text</child>suffix");
		final IDocumentFragment documentFragment = xmlFragment.getDocumentFragment();

		assertEquals(3, documentFragment.children().count());
		assertTrue(documentFragment.children().get(0) instanceof IText);
		assertTrue(documentFragment.children().get(1) instanceof IElement);
		assertTrue(documentFragment.children().get(2) instanceof IText);

		assertEquals(new QualifiedName(null, "child"), ((IElement) documentFragment.children().get(1)).getQualifiedName());

		assertEquals("prefix", documentFragment.children().get(0).getText());
		assertEquals("inner text", documentFragment.children().get(1).getText());
		assertEquals("suffix", documentFragment.children().get(2).getText());
	}

	@Test
	public void canCompressWhitespaceOfXmlString() throws Exception {
		final XMLFragment xmlFragment = new XMLFragment("prefix\n<child>\n   inner  \t text</child>\r suffix");
		final XMLFragment compressedXmlFragment = xmlFragment.compressWhitespace();

		assertEquals("prefix <child> inner text</child> suffix", compressedXmlFragment.getXML());
	}

	@Test
	public void shouldPreserveWhitespaceByDefault() throws Exception {
		final XMLFragment xmlFragment = new XMLFragment("prefix\n<child>\n   inner  \t text</child>\n suffix");
		final IDocumentFragment documentFragment = xmlFragment.getDocumentFragment();

		assertEquals("prefix\n", documentFragment.children().get(0).getText());
		assertEquals("\n   inner  \t text", documentFragment.children().get(1).getText());
		assertEquals("\n suffix", documentFragment.children().get(2).getText());
	}
}
