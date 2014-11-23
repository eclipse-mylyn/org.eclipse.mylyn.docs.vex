/*******************************************************************************
 * Copyright (c) 2010 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import org.eclipse.core.resources.IProject;

/**
 * Registry of configuration sources and listeners.
 *
 * @author Florian Thienel
 */
public interface ConfigurationRegistry {

	/**
	 * Dispose the registry and clean-up free acquired resources.
	 */
	void dispose();

	/**
	 * Load all configurations from installed bundles and plug-in projects in the workspace.
	 */
	void loadConfigurations();

	/**
	 * @return true if the configurations have been loaded.
	 */
	boolean isLoaded();

	/**
	 * The document type configuration for the given identifier.<br />
	 * This method tries to resolve by PublicId or namespace first. If this does not yield as result, a doctype with a
	 * matching SystemId is returned.
	 *
	 * @param id
	 *            the public/system identifier or namespace
	 * @param systemId
	 *            the system id, only used when resolving the public id fails. May be null.
	 * @return the document type configuration for the identifier, or null if there is no doctype found.
	 */
	DocumentType getDocumentType(final String id, final String systemId);

	/**
	 * @return all document type configurations
	 */
	DocumentType[] getDocumentTypes();

	/**
	 * @return all document type configurations for which there is at least one registered style.
	 */
	DocumentType[] getDocumentTypesWithStyles();

	/**
	 * All styles for the given document type. The returned styles are ordered by the way they are resolved:
	 * <ul>
	 * <li>1. document type id</li>
	 * <li>2. Public ID (DTD) or Namespace (XML-Schema)</li>
	 * <li>3. System ID (DTD)</li>
	 * </ul>
	 *
	 * @param doctype
	 *            the document type
	 * @return all styles for the given document type
	 */
	Style[] getStyles(final DocumentType doctype);

	/**
	 * The style with the given id, or null if there is no style with this id.
	 *
	 * @param styleId
	 *            the style's id
	 * @return the style with the given id, or null if there is no style with this id.
	 */
	Style getStyle(final String styleId);

	/**
	 * An arbitrary style for the given document type. If available, the style with the given style id is preferred.
	 *
	 * @param pdoctype
	 *            the document type
	 * @param preferredStyleId
	 *            the preferred style's id
	 * @return a style for the given document type, or null if no style is configured for the given document type
	 */
	Style getStyle(final DocumentType doctype, final String preferredStyleId);

	/**
	 * The representation of the given plug-in project in the workspace.
	 *
	 * @param project
	 *            the project
	 * @return the representation of the given plug-in project in the workspace.
	 */
	PluginProject getPluginProject(final IProject project);

	/**
	 * Adds a ConfigChangeListener to the notification list.
	 *
	 * @param listener
	 *            Listener to be added.
	 */
	void addConfigListener(IConfigListener listener);

	/**
	 * Removes a ConfigChangeListener from the notification list.
	 *
	 * @param listener
	 *            Listener to be removed.
	 */
	void removeConfigListener(IConfigListener listener);
}
