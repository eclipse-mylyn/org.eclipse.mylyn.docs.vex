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
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.QualifiedName;
import org.junit.Before;
import org.junit.Test;

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
		model.initialize("publicId", "systemId", new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("publicId", model.getMainDocumentTypeIdentifier());
		assertTrue(model.isDtdAssigned());
	}

	@Test
	public void initializeWithSystemId() throws Exception {
		model.initialize(null, "systemId", new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("systemId", model.getMainDocumentTypeIdentifier());
		assertTrue(model.isDtdAssigned());
	}
	
	@Test
	public void initializeWithNamespace() throws Exception {
		model.initialize(null, null, new RootElement(new QualifiedName("schemaId", "rootElement")));
		assertEquals("schemaId", model.getMainDocumentTypeIdentifier());
		assertFalse(model.isDtdAssigned());
	}
	
	@Test
	public void createWhitespacePolicy() throws Exception {
		assertNotNull(model.getWhitespacePolicy());
	}
}
