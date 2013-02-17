/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.eclipse.vex.core.internal.io.RoundTrip.assertDocumentsEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.widget.CssWhitespacePolicy;
import org.eclipse.vex.core.provisional.dom.IDocument;
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
