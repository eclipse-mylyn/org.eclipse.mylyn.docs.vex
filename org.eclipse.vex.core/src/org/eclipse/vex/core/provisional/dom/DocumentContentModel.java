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

	/**
	 * Initialize the content model.
	 *
	 * @param baseUri
	 *            The base uri of the loaded document
	 * @param publicId
	 *            The PublicID of the DocumentTypeDefinition (DTD)
	 * @param systemId
	 *            The SystemID of the DocumentTypeDefinition (DTD)
	 * @param rootElement
	 *            If no DTD is defined (publicId and systemId are both null) the namespace of the root element is used
	 *            to define the document type.
	 */
	public void initialize(final String baseUri, final String publicId, final String systemId, final IElement rootElement) {
		this.baseUri = baseUri;
		this.publicId = publicId;
		this.systemId = systemId;
		if (publicId == null && systemId == null && rootElement != null) {
			schemaId = rootElement.getQualifiedName().getQualifier();
		} else {
			schemaId = null;
		}
	}

	/**
	 * Sets an explicit namespace.
	 *
	 * @param baseUri
	 *            The base uri of the loaded document
	 * @param rootNamespace
	 *            The default namespace to resolve the XML-Schema for validation.
	 */
	public void setSchemaId(final String baseUri, final String rootNamespace) {
		this.baseUri = baseUri;
		publicId = null;
		systemId = null;
		schemaId = rootNamespace;
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

	public String getSchemaId() {
		return schemaId;
	}

	public boolean isDtdAssigned() {
		return publicId != null || systemId != null;
	}

	/**
	 * Create and return the WTP CMDocument for the DTD or schema defined by this ContentModel.
	 *
	 * @return The resolved CMDocument.
	 */
	public CMDocument getContentModelDocument() {
		if (schemaId != null) {
			return getContentModelDocument(schemaId);
		}

		String resolvedURI = null;
		try {
			resolvedURI = resolveResourceURI(publicId, systemId);
			if (resolvedURI == null) {
				return null;
			}
			return createCMDocument(resolvedURI);
		} catch (final Exception e) {
			throw new AssertionError(MessageFormat.format("Resolution of systemId ''{0}'' resulted in a exception:{1}. {2}", systemId, resolvedURI, e.getMessage()));
		}
	}

	/**
	 * Create and return the WTP CMDocument for the XML-Schema at the given URI or namespace. The XML catalog is used to
	 * resolve the schema URI.
	 *
	 * @param schemaID
	 *            The URI or the namespace of the XML-Schema.
	 * @return The resolved CMDocument.
	 */
	public CMDocument getContentModelDocument(final String schemaId) {
		String resolvedUri = null;
		try {
			resolvedUri = resolveResourceURI(null, schemaId);
			if (resolvedUri == null) {
				return null;
			}
			return createCMDocument(resolvedUri);
		} catch (final Exception e) {
			throw new AssertionError(MessageFormat.format("Resolution of resource URI ''{0}'' resulted in a exception:{1}. {2}", schemaId, resolvedUri, e.getMessage()));
		}
	}

	/**
	 * Create a new CMDocument from the DTD or XML-Schema at the given URI.
	 *
	 * @param resourceURI
	 *            The URI containing the schema or dtd.
	 * @return
	 */
	private CMDocument createCMDocument(final String resourceURI) {
		if (resourceURI == null) {
			return null;
		}
		final ContentModelManager modelManager = ContentModelManager.getInstance();
		final CMDocument cmDocument = modelManager.createCMDocument(resourceURI, null);
		return cmDocument;
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		final String resolved = resolveResourceURI(publicId, systemId);

		if (resolved == null) {
			return null;
		}

		final InputSource result = new InputSource(resolved);
		result.setPublicId(publicId);
		return result;
	}

	/**
	 * Resolve the URI for a given Resource with the XML-Catalog.
	 *
	 * @param publicId
	 *            The public identifier (DTD) of the resource being referenced, may be null
	 * @param systemId
	 *            The system identifier (DTD) or the namespace (XML-Schema) of the resource being referenced, may be
	 *            null.
	 * @return A String containing the resolved URI.
	 * @throws IOException
	 */
	public String resolveResourceURI(final String publicId, final String systemId) throws IOException {
		final String resolved = URI_RESOLVER.resolve(baseUri, publicId, systemId);
		return resolved;
	}
}
