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

import java.io.IOException;

import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Florian Thienel
 */
public class DocumentContentModel implements EntityResolver {

	private static final URIResolver URI_RESOLVER = URIResolverPlugin.createResolver();

	private String baseUri;
	private String publicId;
	private String systemId;
	private String schemaId;

	public void initialize(final String publicId, final String systemId, final RootElement rootElement) {
		this.publicId = publicId;
		this.systemId = systemId;
		schemaId = rootElement.getQualifiedName().getQualifier();
	}

	public String getMainDocumentTypeIdentifier() {
		if (publicId != null)
			return publicId;
		if (systemId != null)
			return systemId;
		return schemaId;
	}

	public boolean isDtdAssigned() {
		return publicId != null || systemId != null;
	}

	public IWhitespacePolicy getWhitespacePolicy() {
		return IWhitespacePolicy.NULL;
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		final String resolved = URI_RESOLVER.resolve(baseUri, publicId, systemId);
		System.out.println("Resolved " + publicId + " " + systemId + " -> " + resolved);
		if (resolved == null)
			return null;
		
		final InputSource result = new InputSource(resolved);
		result.setPublicId(publicId);
		return result;
	}

}
