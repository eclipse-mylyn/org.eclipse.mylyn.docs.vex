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
package org.eclipse.vex.core.provisional.dom;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
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

	public DocumentContentModel() {
	}

	public DocumentContentModel(final String baseUri, final String publicId, final String systemId, final IElement rootElement) {
		initialize(baseUri, publicId, systemId, rootElement);
	}

	public void initialize(final String baseUri, final String publicId, final String systemId, final IElement rootElement) {
		this.baseUri = baseUri;
		this.publicId = publicId;
		this.systemId = systemId;
		if (rootElement != null) {
			schemaId = rootElement.getQualifiedName().getQualifier();
		} else {
			schemaId = null;
		}
	}

	public String getMainDocumentTypeIdentifier() {
		if (publicId != null) {
			return publicId;
		}
		if (systemId != null) {
			return systemId;
		}
		return schemaId;
	}

	public String getPublicId() {
		return publicId;
	}

	public String getSystemId() {
		return systemId;
	}

	public boolean isDtdAssigned() {
		return publicId != null || systemId != null;
	}

	public CMDocument getDTD() {
		final URL resolvedPublicId = resolveSchemaIdentifier(publicId);
		if (resolvedPublicId != null) {
			return createCMDocument(resolvedPublicId);
		}
		return createCMDocument(resolveSystemId(systemId));
	}

	private CMDocument createCMDocument(final URL resolvedDtdUrl) {
		if (resolvedDtdUrl == null) {
			return null;
		}
		final ContentModelManager modelManager = ContentModelManager.getInstance();
		return modelManager.createCMDocument(resolvedDtdUrl.toString(), null);
	}

	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		final String resolved = URI_RESOLVER.resolve(baseUri, publicId, systemId);
		System.out.println("Resolved " + publicId + " " + systemId + " -> " + resolved);
		if (resolved == null) {
			return null;
		}

		final InputSource result = new InputSource(resolved);
		result.setPublicId(publicId);
		return result;
	}

	public URL resolveSchemaIdentifier(final String schemaIdentifier) {
		if (schemaIdentifier == null) {
			return null;
		}
		final String schemaLocation = URI_RESOLVER.resolve(baseUri, schemaIdentifier, null);
		if (schemaLocation == null) {
			/*
			 * TODO this is a common case that should be handled somehow - a hint should be shown: the schema is not
			 * available, the schema should be added to the catalog by the user - an inferred schema should be used, to
			 * allow to at least display the document in the editor - this is not the right place to either check or
			 * handle this
			 */
			return null;
		}
		try {
			return new URL(schemaLocation);
		} catch (final MalformedURLException e) {
			throw new AssertionError(MessageFormat.format("Resolution of schema ''{0}'' resulted in a malformed URL: ''{1}''. {2}", schemaIdentifier, schemaLocation, e.getMessage()));
		}
	}

	public URL resolveSystemId(final String systemId) {
		final String schemaLocation = URI_RESOLVER.resolve(baseUri, null, systemId);
		if (schemaLocation == null) {
			return null;
		}
		try {
			return new URL(schemaLocation);
		} catch (final MalformedURLException e) {
			throw new AssertionError(MessageFormat.format("Resolution of systemId ''{0}'' resulted in a malformed URL: ''{1}''. {2}", systemId, schemaLocation, e.getMessage()));
		}
	}
}
