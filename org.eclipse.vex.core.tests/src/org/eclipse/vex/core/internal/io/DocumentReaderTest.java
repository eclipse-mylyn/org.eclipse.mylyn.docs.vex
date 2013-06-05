/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *      Carsten Hiesserich - do not add text nodes containing only whitespace when reading the document (bug 407803)
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.internal.dom.DummyValidator;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
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
		final IDocument document = reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertEquals("-//Eclipse Foundation//DTD Vex Test//EN", document.getPublicID());
		assertEquals("test1.dtd", document.getSystemID());
	}

	@Test
	public void readDocumentWithDtdSystem() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final URL documentUrl = TestResources.get("documentWithDtdSystem.xml");
		final IDocument document = reader.read(documentUrl);
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
		reader.setValidator(new DummyValidator(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					called[0] = true;
				}
				return super.resolveEntity(publicId, systemId);
			}
		}));
		reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertTrue(called[0]);
	}

	@Test
	public void preferEntityResolver() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final boolean[] documentContentModelCalled = new boolean[1];
		final boolean[] entityResolverCalled = new boolean[1];
		reader.setValidator(new DummyValidator(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					documentContentModelCalled[0] = true;
				}
				return super.resolveEntity(publicId, systemId);
			}
		}));
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
		reader.setValidator(new DummyValidator(new DocumentContentModel() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (TestResources.TEST_DTD.equals(publicId)) {
					documentContentModelPosition[0] = ++callPosition[0];
				}
				return super.resolveEntity(publicId, systemId);
			}
		}));
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

		final IDocument document = reader.read(TestResources.get("documentWithComments.xml"));
		final Iterator<INode> documentChildren = document.children().iterator();

		final IComment documentComment1 = (IComment) documentChildren.next();
		assertEquals("A comment before the root element.", documentComment1.getText());
		assertSame(document.getRootElement(), documentChildren.next());
		final IComment documentComment2 = (IComment) documentChildren.next();
		assertEquals("A final comment after the root element.", documentComment2.getText());
		assertFalse(documentChildren.hasNext());

		final IElement rootElement = document.getRootElement();
		final Iterator<INode> rootChildren = rootElement.children().iterator();

		final IComment comment1 = (IComment) rootChildren.next();
		assertEquals("A comment within the root element.", comment1.getText());

		final IComment comment2 = (IComment) ((IElement) rootChildren.next()).children().get(1);
		assertEquals("A comment within text.", comment2.getText());

		final IComment comment3 = (IComment) rootChildren.next();
		assertEquals("Another comment between two child elements.", comment3.getText());

		rootChildren.next();
		assertFalse(rootChildren.hasNext());
	}

	@Test
	public void shouldNotAddWhitespaceTextNodesInDocumentWithoutDTD() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final IDocument document = reader.read(TestResources.get("documentWithoutDTD.xml"));
		final IElement rootElement = document.getRootElement();

		final IFilter<INode> recursiveWhitespaceTextNodeFilter = new IFilter<INode>() {
			public boolean matches(final INode node) {
				return node.accept(new BaseNodeVisitorWithResult<Boolean>(false) {
					@Override
					public Boolean visit(final IElement element) {
						return containsEmptyTextNodes(element);
					}

					@Override
					public Boolean visit(final IText text) {
						return text.getText().trim().length() == 0;
					}
				});
			}

			private boolean containsEmptyTextNodes(final IElement element) {
				return !element.children().matching(this).isEmpty();
			}
		};

		final IAxis<? extends INode> nodesWithEmptyTextNodes = rootElement.children().matching(recursiveWhitespaceTextNodeFilter);

		assertEquals("whitespace text nodes", 0, nodesWithEmptyTextNodes.count());
	}
}
