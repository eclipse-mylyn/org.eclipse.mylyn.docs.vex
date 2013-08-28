/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - writeNoWrap(DocumentFragment)
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.eclipse.vex.core.internal.io.RoundTrip.assertDocumentsEqual;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentWriterTest {

	@Test
	public void testHtmlWithAttributes() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("DocumentWriterTest1.xml"));
	}

	@Test
	public void testDocumentWithDtdPublic() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("documentWithDtdPublic.xml"));
	}

	@Test
	public void testDocumentWithDtdSystem() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("documentWithDtdSystem.xml"));
	}

	@Test
	public void testDocumentWithSchema() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("document.xml"));
	}

	@Test
	public void testDocumentWithComments() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("documentWithComments.xml"));
	}

	@Test
	public void testDocumentWithProcessingInstructions() throws Exception {
		assertWriteReadCycleWorks(TestResources.get("documentWithProcessingInstr.xml"));
	}

	@Test
	public void writeDocumentFragmentNoWrap() throws Exception {
		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement child1 = doc.insertElement(doc.getRootElement().getEndOffset(), new QualifiedName(null, "child"));
		final IElement child2 = doc.insertElement(doc.getRootElement().getEndOffset(), new QualifiedName(null, "child"));
		final IElement child3 = doc.insertElement(doc.getRootElement().getEndOffset(), new QualifiedName(null, "child"));
		doc.insertText(child1.getEndOffset(), "a   b");
		doc.insertText(child2.getEndOffset(), "c   d");
		doc.insertText(child3.getEndOffset(), "e   f");
		final IDocumentFragment fragment = doc.getFragment(new ContentRange(child1.getStartOffset(), child3.getEndOffset()));

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final DocumentWriter writer = new DocumentWriter();
		writer.writeNoWrap(fragment, buffer);
		final String writtenFragment = new String(buffer.toByteArray());

		assertEquals("<child>a   b</child><child>c   d</child><child>e   f</child>", writtenFragment);
	}

	private static void assertWriteReadCycleWorks(final URL documentUrl) throws IOException, ParserConfigurationException, SAXException, Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read(TestResources.get("test.css"));

		final IDocument expectedDocument = readDocument(new InputSource(documentUrl.toString()));

		final DocumentWriter documentWriter = new DocumentWriter();
		documentWriter.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		documentWriter.write(expectedDocument, buffer);

		final InputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());
		final InputSource inputSource = new InputSource(inputStream);
		inputSource.setSystemId(documentUrl.toString());
		final IDocument actualDocument = readDocument(inputSource);

		assertDocumentsEqual(expectedDocument, actualDocument);
	}

	private static IDocument readDocument(final InputSource inputSource) throws IOException, ParserConfigurationException, SAXException {
		final DocumentReader documentReader = new DocumentReader();
		return documentReader.read(inputSource);
	}

}
