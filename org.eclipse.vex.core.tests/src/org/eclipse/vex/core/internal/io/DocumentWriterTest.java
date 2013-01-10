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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.BaseNodeVisitor;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.dom.Parent;
import org.eclipse.vex.core.internal.dom.Text;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.core.internal.widget.CssWhitespacePolicy;
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

		final Document expectedDocument = readDocument(new InputSource(documentUrl.toString()));

		final DocumentWriter documentWriter = new DocumentWriter();
		documentWriter.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		documentWriter.write(expectedDocument, buffer);

		final InputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());
		final InputSource inputSource = new InputSource(inputStream);
		inputSource.setSystemId(documentUrl.toString());
		final Document actualDocument = readDocument(inputSource);

		assertDocumentsEqual(expectedDocument, actualDocument);
	}

	private static Document readDocument(final InputSource inputSource) throws IOException, ParserConfigurationException, SAXException {
		final DocumentReader documentReader = new DocumentReader();
		return documentReader.read(inputSource);
	}

	private static void assertDocumentsEqual(final Document expected, final Document actual) {
		assertEquals(expected.getPublicID(), actual.getPublicID());
		assertEquals(expected.getSystemID(), actual.getSystemID());
		assertContentEqual(expected, actual);
	}

	private static void assertContentEqual(final Parent expected, final Parent actual) {
		final List<Node> expectedContent = expected.getChildNodes();
		final List<Node> actualContent = actual.getChildNodes();
		assertEquals("children of " + expected, expectedContent.size(), actualContent.size());
		for (int i = 0; i < expectedContent.size(); i++) {
			final Node expectedNode = expectedContent.get(i);
			final Node actualNode = actualContent.get(i);
			assertEquals(expectedNode.getClass(), actualNode.getClass());
			expectedNode.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Element element) {
					assertElementsEqual((Element) expectedNode, (Element) actualNode);
				}

				@Override
				public void visit(final Comment comment) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}

				@Override
				public void visit(final Text text) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}
			});
		}
	}

	private static void assertElementsEqual(final Element expected, final Element actual) {
		assertEquals("qualified name of " + expected, expected.getQualifiedName(), actual.getQualifiedName());
		assertAttributesEqual(expected, actual);
		assertNamespacesEqual(expected, actual);
		assertContentEqual(expected, actual);
	}

	private static void assertAttributesEqual(final Element expected, final Element actual) {
		final List<QualifiedName> expectedAttrs = expected.getAttributeNames();
		final List<QualifiedName> actualAttrs = actual.getAttributeNames();

		assertEquals("attributes of " + expected, expectedAttrs.size(), actualAttrs.size());
		for (int i = 0; i < expectedAttrs.size(); i++) {
			assertEquals(expectedAttrs.get(i), actualAttrs.get(i));
		}
	}

	private static void assertNamespacesEqual(final Element expected, final Element actual) {
		assertEquals("declared default namespace of " + expected, expected.getDeclaredDefaultNamespaceURI(), actual.getDeclaredDefaultNamespaceURI());

		final Collection<String> expectedNamespacePrefixes = expected.getDeclaredNamespacePrefixes();
		final Collection<String> actualNamespacePrefixes = actual.getDeclaredNamespacePrefixes();
		assertEquals("declared namespaces of " + expected, expectedNamespacePrefixes.size(), actualNamespacePrefixes.size());
		for (final String prefix : expectedNamespacePrefixes) {
			assertTrue("namespace not declared: " + prefix, actualNamespacePrefixes.contains(prefix));
			assertEquals("namespace URI of prefix " + prefix, expected.getNamespaceURI(prefix), actual.getNamespaceURI(prefix));
		}
	}
}
