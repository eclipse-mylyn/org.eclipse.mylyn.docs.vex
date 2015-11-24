/*******************************************************************************
 * Copyright (c) 2004, 2010 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;

/**
 * Factory for DocumentType objects.
 */
public class DoctypeFactory implements IConfigItemFactory {

	private static final String[] EXTS = new String[] { "dtd" }; //$NON-NLS-1$

	private static final String ELT_DOCTYPE = "doctype"; //$NON-NLS-1$
	private static final String ATTR_OUTLINE_PROVIDER = "outlineProvider"; //$NON-NLS-1$
	private static final String ATTR_SYSTEM_ID = "systemId"; //$NON-NLS-1$
	private static final String ATTR_PUBLIC_ID = "publicId"; //$NON-NLS-1$
	private static final String ATTR_NAMESPACE_NAME = "namespaceName"; //$NON-NLS-1$
	private static final String ATTR_URI = "uri"; //$NON-NLS-1$

	private static final String ELT_ROOT_ELEMENT = "rootElement"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	@Override
	public IConfigElement[] createConfigurationElements(final ConfigItem item) {
		final DocumentType doctype = (DocumentType) item;
		final ConfigurationElement doctypeElement = new ConfigurationElement(ELT_DOCTYPE);
		doctypeElement.setAttribute(ATTR_URI, doctype.getResourceUri().toString());
		doctypeElement.setAttribute(ATTR_PUBLIC_ID, doctype.getPublicId());
		doctypeElement.setAttribute(ATTR_SYSTEM_ID, doctype.getSystemId());
		doctypeElement.setAttribute(ATTR_OUTLINE_PROVIDER, doctype.getOutlineProvider());

		for (final String name : doctype.getRootElements()) {
			final ConfigurationElement rootElement = new ConfigurationElement(ELT_ROOT_ELEMENT);
			rootElement.setAttribute(ATTR_NAME, name);
			doctypeElement.addChild(rootElement);
		}

		return new IConfigElement[] { doctypeElement };
	}

	@Override
	public ConfigItem createItem(final ConfigSource config, final IConfigElement[] configElements) throws IOException {
		if (configElements.length < 1) {
			return null;
		}
		final DocumentType doctype = new DocumentType(config);
		final IConfigElement configElement = configElements[0];
		if (configElement.getName() == null) {
			System.out.println("configElement:" + configElement.getName());
		}
		if (configElement.getName().equals(ELT_DOCTYPE)) {
			final String publicId = configElement.getAttribute(ATTR_PUBLIC_ID);
			final String systemId = configElement.getAttribute(ATTR_SYSTEM_ID);
			doctype.setPublicId(publicId);
			doctype.setSystemId(systemId);
			final String resourceUri = configElement.getAttribute(ATTR_URI);
			if (resourceUri != null && !resourceUri.trim().isEmpty()) {
				// Use the URI given in the Plugin configuration
				doctype.setResourceUri(newUri(resourceUri));
			} else {
				// Try to resolve the URI
				doctype.setResourceUri(newUri(config.resolve(publicId, systemId)));
			}
		} else {
			final String namespaceName = configElement.getAttribute(ATTR_NAMESPACE_NAME);
			doctype.setNamespaceName(namespaceName);
			doctype.setResourceUri(newUri(config.resolve(null, namespaceName)));
		}

		doctype.setOutlineProvider(configElement.getAttribute(ATTR_OUTLINE_PROVIDER));

		final IConfigElement[] rootElementRefs = configElement.getChildren();
		final String[] rootElements = new String[rootElementRefs.length];
		for (int i = 0; i < rootElementRefs.length; i++) {
			rootElements[i] = rootElementRefs[i].getAttribute("name"); //$NON-NLS-1$
		}
		doctype.setRootElements(rootElements);

		return doctype;
	}

	private static URI newUri(final String uriString) {
		try {

			// TODO remove ".replaceAll(" ", "%20")" as soon this bug is
			// fixed in org.eclipse.wst.xml.core
			return new URI(uriString.replaceAll(" ", "%20"));

		} catch (final URISyntaxException e) {
			return null;
		}
	}

	@Override
	public String getExtensionPointId() {
		return DocumentType.EXTENSION_POINT;
	}

	@Override
	public String[] getFileExtensions() {
		return EXTS;
	}

	@Override
	public String getPluralName() {
		return Messages.getString("DoctypeFactory.pluralName"); //$NON-NLS-1$
	}

	@Override
	public Object parseResource(final ConfigItem item, final URL baseUrl, final String resourcePath, final IBuildProblemHandler problemHandler) throws IOException {
		final DocumentType documentType = (DocumentType) item;
		final DocumentContentModel documentContentModel = new DocumentContentModel(baseUrl.toString(), documentType.getPublicId(), documentType.getSystemId(), null);
		return new WTPVEXValidator(documentContentModel);
	}
}
