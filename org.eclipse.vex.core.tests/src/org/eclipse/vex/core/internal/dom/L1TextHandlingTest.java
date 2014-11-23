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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L1TextHandlingTest {

	private IDocument document;
	private IElement titleElement;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		titleElement = document.insertElement(2, new QualifiedName(null, "title"));
		document.setValidator(new DummyValidator() {
			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
				return "title".equals(element.getLocalName());
			}

			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3,
					final boolean partial) {
				return "title".equals(element.getLocalName());
			}
		});
	}

	@Test
	public void shouldIndicateValidTextInsertionPoints() throws Exception {
		assertFalse("the root element may not contain textual contant", document.canInsertText(titleElement.getStartOffset() - 1));
		assertFalse("the start offset is the last insertion point of the root element before the title element", document.canInsertText(titleElement.getStartOffset()));
		assertTrue("append new content before the end offset of the title element", document.canInsertText(titleElement.getEndOffset()));
		assertFalse("the root element may not contain textual contant", document.canInsertText(titleElement.getEndOffset() + 1));
	}

	@Test
	public void insertTextAtValidInsertionPoint() throws Exception {
		document.insertText(titleElement.getEndOffset(), "Hello World");
		assertEquals("Hello World", titleElement.getText());
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotInsertTextBeforeDocumentStart() throws Exception {
		document.insertText(-1, "Hello World");
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotInsertTextAtDocumentStartOffset() throws Exception {
		document.insertText(0, "Hello World");
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotInsertTextAfterDocumentEndOffset() throws Exception {
		document.insertText(document.getEndOffset() + 1, "Hello World");
	}

	@Test(expected = DocumentValidationException.class)
	public void cannotInsertTextAtInvalidInsertionPoint() throws Exception {
		// titleElement.startOffset is the last insertion point in root before title; root may not contain text according to the validator
		document.insertText(titleElement.getStartOffset(), "Hello World");
	}

	@Test
	public void shouldConvertControlCharactersToSpaces() throws Exception {
		document.insertText(titleElement.getEndOffset(), "\0\u001F\n");
		assertEquals("control characters except \\n are converted to spaces", "  \n", titleElement.getText());
	}
}
