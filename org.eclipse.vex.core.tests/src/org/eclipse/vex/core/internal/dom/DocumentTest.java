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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
}
