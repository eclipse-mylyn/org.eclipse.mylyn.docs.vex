/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.vex.core.tests.TestResources;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentReaderTest {

	@Test
	public void readDocumentWithDtdPublic() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final Document document = reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertEquals("-//Eclipse Foundation//DTD Vex Test//EN", document.getPublicID());
		assertEquals("test1.dtd", document.getSystemID());
	}

	@Test
	public void readDocumentWithDtdSystem() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final URL documentUrl = TestResources.get("documentWithDtdSystem.xml");
		final Document document = reader.read(documentUrl);
		assertNull(document.getPublicID());
		assertEquals("test1.dtd", document.getSystemID());
	}

	@Test
	public void resolveDtdWithSystemId() throws Exception {
		final URL documentUrl = TestResources.get("documentWithDtdSystem.xml");
		final ContentModelManager modelManager = ContentModelManager.getInstance();
		final URL dtdUrl = new URL(documentUrl, "test1.dtd");
		final CMDocument dtd = modelManager.createCMDocument(dtdUrl.toString(), null);
		assertNotNull(dtd.getElements().getNamedItem("section"));
	}

	@Test
	public void useDocumentContentModelAsEntityResolver() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final boolean[] called = new boolean[1];
		reader.setDocumentContentModel(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					called[0] = true;
				}
				return super.resolveEntity(publicId, systemId);
			}
		});
		reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertTrue(called[0]);
	}

	@Test
	public void preferEntityResolver() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final boolean[] documentContentModelCalled = new boolean[1];
		final boolean[] entityResolverCalled = new boolean[1];
		reader.setDocumentContentModel(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					documentContentModelCalled[0] = true;
				}
				return super.resolveEntity(publicId, systemId);
			}
		});
		reader.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					entityResolverCalled[0] = true;
				}
				return new InputSource(TestResources.get("test1.dtd").toString());
			}
		});
		reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertFalse(documentContentModelCalled[0]);
		assertTrue(entityResolverCalled[0]);
	}

	@Test
	public void useDocumentContentModelAsEntityResolverBackup() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final int[] callPosition = new int[1];
		final int[] documentContentModelPosition = new int[1];
		final int[] entityResolverPosition = new int[1];
		reader.setDocumentContentModel(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					documentContentModelPosition[0] = ++callPosition[0];
				}
				return super.resolveEntity(publicId, systemId);
			}
		});
		reader.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					entityResolverPosition[0] = ++callPosition[0];
				}
				return null;
			}
		});
		reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertEquals(2, documentContentModelPosition[0]);
		assertEquals(1, entityResolverPosition[0]);
	}

	@Test
	public void readDocumentWithComments() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final Document document = reader.read(TestResources.get("documentWithComments.xml"));
		final Element rootElement = document.getRootElement();
		final List<Node> rootChildNodes = rootElement.getChildNodes();
		assertEquals(4, rootChildNodes.size());

		final Comment comment1 = (Comment) rootChildNodes.get(0);
		assertEquals("A comment within the root element.", comment1.getText());

		final Comment comment2 = (Comment) ((Element) rootChildNodes.get(1)).getChildNodes().get(1);
		assertEquals("A comment within text.", comment2.getText());

		final Comment comment3 = (Comment) rootChildNodes.get(2);
		assertEquals("Another comment between two child elements.", comment3.getText());
	}
}
