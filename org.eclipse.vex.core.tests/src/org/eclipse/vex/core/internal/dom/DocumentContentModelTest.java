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

import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.tests.TestResources;
import org.eclipse.vex.core.tests.VEXCoreTestPlugin;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Florian Thienel
 */
public class DocumentContentModelTest {

	private DocumentContentModel model;

	@Before
	public void setUp() throws Exception {
		model = new DocumentContentModel();
	}

	@Test
	public void initializeWithPublicId() throws Exception {
		model.initialize(null, "publicId", "systemId", new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("publicId", model.getMainDocumentTypeIdentifier());
		assertTrue(model.isDtdAssigned());
	}

	@Test
	public void initializeWithSystemId() throws Exception {
		model.initialize(null, null, "systemId", new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("systemId", model.getMainDocumentTypeIdentifier());
		assertTrue(model.isDtdAssigned());
	}

	@Test
	public void initializeWithNamespace() throws Exception {
		model.initialize(null, null, null, new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("schemaId", model.getMainDocumentTypeIdentifier());
		assertFalse(model.isDtdAssigned());
	}

	@Test
	public void createWhitespacePolicy() throws Exception {
		assertNotNull(model.getWhitespacePolicy());
	}

	@Test
	public void isEntityResolver() throws Exception {
		assertTrue(model instanceof EntityResolver);
	}

	@Test
	public void resolveEntityFromXMLCatalog() throws Exception {
		final InputSource resolvedEntity = model.resolveEntity(TestResources.TEST_DTD, "test.dtd");
		assertNotNull(resolvedEntity);
		assertTrue(resolvedEntity.getSystemId().contains(VEXCoreTestPlugin.PLUGIN_ID));
		assertEquals(TestResources.TEST_DTD, resolvedEntity.getPublicId());
	}

	@Test
	public void resolveUnknownEntityWithSystemId() throws Exception {
		final InputSource resolvedEntity = model.resolveEntity("UnknownPublicId", "UnknownSystemId");
		assertNotNull(resolvedEntity);
		assertEquals("UnknownSystemId", resolvedEntity.getSystemId());
		assertEquals("UnknownPublicId", resolvedEntity.getPublicId());
		assertNull(resolvedEntity.getByteStream());
	}

	@Test
	public void resolveUnknownEntityWithoutSystemId() throws Exception {
		assertNull(model.resolveEntity("UnknownPublicId", null));
	}

	@Test
	public void useBaseUriForResolving() throws Exception {
		model.initialize("file://base/uri/document.xml", null, null, new RootElement(new QualifiedName("schemaId", "rootElement")));
		final InputSource resolvedEntity = model.resolveEntity("UnknownPublicId", "UnknownSystemId.dtd");
		assertNotNull(resolvedEntity);
		assertEquals("file://base/uri/UnknownSystemId.dtd", resolvedEntity.getSystemId());
		assertEquals("UnknownPublicId", resolvedEntity.getPublicId());
	}

	@Test
	public void resolveSchemaIdentifier() throws Exception {
		final URL resolvedUrl = model.resolveSchemaIdentifier(TestResources.TEST_DTD);
		assertNotNull(resolvedUrl);
		assertTrue(resolvedUrl.toString().contains(VEXCoreTestPlugin.PLUGIN_ID));
	}
	
	@Test
	public void onlySystemId() throws Exception {
		model.initialize(null, null, TestResources.get("test1.dtd").toString(), null);
		assertTrue(model.isDtdAssigned());
		assertNotNull(model.getDTD());
	}
	
	@Test
	public void onlyRelativeSystemId() throws Exception {
		final String baseUri = TestResources.get("test.css").toString();
		model.initialize(baseUri, null, "test1.dtd", null);
		assertTrue(model.isDtdAssigned());
		final CMDocument dtd = model.getDTD();
		assertNotNull(dtd);
		assertEquals(10, dtd.getElements().getLength());
	}
}
