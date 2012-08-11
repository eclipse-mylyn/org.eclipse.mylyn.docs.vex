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
package org.eclipse.vex.ui.internal.namespace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.vex.core.internal.dom.Element;

/**
 * @author Florian Thienel
 */
public class EditNamespacesController {

	private final Element element;

	private String defaultNamespaceURI;

	private final List<EditableNamespaceDefinition> namespaceDefinitions;

	public EditNamespacesController(final Element element) {
		this.element = element;
		defaultNamespaceURI = getDefaultNamespaceURI(element);
		namespaceDefinitions = getNamespaceDefinitions(element);
	}

	private static String getDefaultNamespaceURI(final Element element) {
		final String result = element.getDeclaredDefaultNamespaceURI();
		if (result == null) {
			return "";
		}
		return result;
	}

	private static List<EditableNamespaceDefinition> getNamespaceDefinitions(final Element element) {
		final ArrayList<EditableNamespaceDefinition> result = new ArrayList<EditableNamespaceDefinition>();
		for (final String prefix : element.getDeclaredNamespacePrefixes()) {
			result.add(new EditableNamespaceDefinition(prefix, element.getNamespaceURI(prefix)));
		}
		return result;
	}

	public String getDefaultNamespaceURI() {
		return defaultNamespaceURI;
	}

	public void setDefaultNamespaceURI(final String defaultNamespaceURI) {
		this.defaultNamespaceURI = defaultNamespaceURI;
	}

	public List<EditableNamespaceDefinition> getNamespaceDefinitions() {
		return namespaceDefinitions;
	}

	public EditableNamespaceDefinition addNamespaceDefinition() {
		final EditableNamespaceDefinition result = new EditableNamespaceDefinition();
		namespaceDefinitions.add(result);
		return result;
	}

	public void removeNamespaceDefinition(final EditableNamespaceDefinition namespaceDefinition) {
		namespaceDefinitions.remove(namespaceDefinition);
	}

	public void applyToElement() {
		if (defaultNamespaceURI == null || "".equals(defaultNamespaceURI)) {
			element.removeDefaultNamespace();
		} else {
			element.declareDefaultNamespace(defaultNamespaceURI);
		}

		final HashSet<String> declaredPrefixes = new HashSet<String>();
		for (final EditableNamespaceDefinition definition : namespaceDefinitions) {
			element.declareNamespace(definition.getPrefix(), definition.getUri());
			declaredPrefixes.add(definition.getPrefix());
		}

		for (final String prefix : element.getDeclaredNamespacePrefixes()) {
			if (!declaredPrefixes.contains(prefix)) {
				element.removeNamespace(prefix);
			}
		}
	}

}
