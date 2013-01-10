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
package org.eclipse.vex.ui.internal.swt.tests;

import static org.eclipse.vex.core.internal.io.RoundTrip.assertContentEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.ui.internal.swt.DocumentFragmentTransfer;
import org.junit.Before;
import org.junit.Test;

public class DocumentFragmentTransferTest {

	private Document document;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
	}

	@Test
	public void shouldTransferSimpleText() throws Exception {
		document.insertText(document.getRootElement().getEndOffset(), "Hello World");

		assertRoundTripWorks(getExpectedFragment());
	}

	@Test
	public void shouldTransferElementAndText() throws Exception {
		document.insertText(document.getRootElement().getEndOffset(), "Hello");
		final Element child = addChild();
		document.insertText(child.getEndOffset(), "New");
		document.insertText(document.getRootElement().getEndOffset(), "World");

		assertRoundTripWorks(getExpectedFragment());
	}

	@Test
	public void shouldTransferElementWithNamespace() throws Exception {
		final Element child = addChild();
		child.declareNamespace("ns1", "http://namespaceUri/1");

		assertRoundTripWorks(getExpectedFragment());
	}

	@Test
	public void shouldTransferElementWithDefaultNamespace() throws Exception {
		final Element child = addChild(new QualifiedName("http://namespaceUri/default", "child"));
		child.declareDefaultNamespace("http://namespaceUri/default");

		assertRoundTripWorks(getExpectedFragment());
	}

	@Test
	public void shouldTransferComment() throws Exception {
		final Comment comment = document.insertComment(document.getRootElement().getEndOffset());
		document.insertText(comment.getEndOffset(), "Hello World");
		assertRoundTripWorks(getExpectedFragment());
	}

	private Element addChild() {
		return addChild(new QualifiedName(null, "child"));
	}

	private Element addChild(final QualifiedName elementName) {
		return document.insertElement(document.getRootElement().getEndOffset(), elementName);
	}

	private DocumentFragment getExpectedFragment() {
		return document.getFragment(document.getRootElement().getRange().resizeBy(1, -1));
	}

	private static void assertRoundTripWorks(final DocumentFragment expectedFragment) throws Exception {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final DocumentFragmentTransfer transfer = new DocumentFragmentTransfer();
		transfer.writeFragmentToStream(expectedFragment, buffer);
		final DocumentFragment actualFragment = transfer.readFragmentFromStream(new ByteArrayInputStream(buffer.toByteArray()));
		assertContentEqual(expectedFragment, actualFragment);
	}

}
