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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.vex.core.tests.TestResources;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DocumentReaderTest {
	
	@Test
	public void readDocumentWithDtdPublic() throws Exception {
		final DocumentReader reader = new DocumentReader();
		final Document document = reader.read(TestResources.get("documentWithDtdPublic.xml"));
		assertEquals("-//Eclipse Foundation//DTD Vex Test//EN", document.getPublicID());
		assertEquals("test1.dtd", document.getSystemID());
	}

	@Test
	public void testname() throws Exception {
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
}
