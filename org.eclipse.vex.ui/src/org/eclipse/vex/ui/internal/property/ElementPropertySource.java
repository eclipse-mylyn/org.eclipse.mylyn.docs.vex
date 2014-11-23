/*******************************************************************************
 * Copyright (c) 2004, 2011 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay    - initial API and implementation
 *     Florian Thienel - namespace support (bug 253753)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.eclipse.vex.core.provisional.dom.AttributeDefinition;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.ui.internal.Messages;

public class ElementPropertySource implements IPropertySource2 {

	private static final String ATTR_ID = "id"; //$NON-NLS-1$

	private static final String ELEMENT_NAME_PROPERTY = "elementName"; //$NON-NLS-1$
	private static final String ELEMENT_NAMESPACE_URI_PROPERTY = "elementNsUri"; //$NON-NLS-1$
	private static final String ELEMENT_NAMESPACE_PREFIX_PROPERTY = "elementNsPrefix"; //$NON-NLS-1$

	private static final String ELEMENT_CATEGORY = "Element";
	private static final String ATTRIBUTES_CATEGORY = "Attributes";
	private static final String NAMESPACES_CATEGORY = "Namespaces";

	private final IElement element;
	private final IValidator validator;
	private final boolean multipleElementsSelected;

	public ElementPropertySource(final IElement element, final IValidator validator, final boolean multipleElementsSelected) {
		this.element = element;
		this.validator = validator;
		this.multipleElementsSelected = multipleElementsSelected;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		final List<IPropertyDescriptor> result = new ArrayList<IPropertyDescriptor>();

		result.add(createExpertPropertyDescriptor(ELEMENT_NAME_PROPERTY, "Element Name", ELEMENT_CATEGORY));

		if (element.getQualifiedName().getQualifier() != null) {
			result.add(createExpertPropertyDescriptor(ELEMENT_NAMESPACE_URI_PROPERTY, "Namespace URI", ELEMENT_CATEGORY));
			result.add(createExpertPropertyDescriptor(ELEMENT_NAMESPACE_PREFIX_PROPERTY, "Namespace Prefix", ELEMENT_CATEGORY));
		}

		/*
		 * Note that elements from DocumentFragments don't have access to their original document, so we get it from the
		 * VexWidget.
		 */
		final List<AttributeDefinition> attributeDefinitions = validator.getAttributeDefinitions(element);
		for (final AttributeDefinition attributeDefinition : attributeDefinitions) {
			final PropertyDescriptor propertyDescriptor;

			final String nsPrefix = element.getNamespacePrefix(attributeDefinition.getQualifiedName().getQualifier());
			String attributeName = attributeDefinition.getName();
			if (nsPrefix != null) {
				attributeName = nsPrefix + ":" + attributeName;
			}

			if (multipleElementsSelected && attributeDefinition.getName().equals(ATTR_ID)) {
				propertyDescriptor = new PropertyDescriptor(attributeDefinition, attributeName);
			} else if (attributeDefinition.isFixed()) {
				propertyDescriptor = new PropertyDescriptor(attributeDefinition, attributeName);
			} else if (attributeDefinition.getType() == AttributeDefinition.Type.ENUMERATION) {
				propertyDescriptor = new ComboBoxPropertyDescriptor(attributeDefinition, attributeName, getEnumValues(attributeDefinition));
			} else {
				propertyDescriptor = new TextPropertyDescriptor(attributeDefinition, attributeName);
			}
			if (nsPrefix != null) {
				propertyDescriptor.setCategory(ATTRIBUTES_CATEGORY + " " + nsPrefix);
			} else {
				propertyDescriptor.setCategory(ATTRIBUTES_CATEGORY);
			}
			result.add(propertyDescriptor);
		}

		for (final String namespacePrefix : element.getNamespacePrefixes()) {
			final String namespaceUri = element.getNamespaceURI(namespacePrefix);
			final NamespaceUri namespaceDeclaration = new NamespaceUri(namespaceUri);
			result.add(createExpertPropertyDescriptor(namespaceDeclaration, Namespace.XMLNS_NAMESPACE_PREFIX + ":" + namespacePrefix, NAMESPACES_CATEGORY));
		}

		final String defaultNamespaceUri = element.getDefaultNamespaceURI();
		if (defaultNamespaceUri != null) {
			final NamespaceUri namespaceDeclaration = new NamespaceUri(defaultNamespaceUri);
			result.add(createExpertPropertyDescriptor(namespaceDeclaration, Namespace.XMLNS_NAMESPACE_PREFIX, NAMESPACES_CATEGORY));
		}

		return result.toArray(new IPropertyDescriptor[result.size()]);
	}

	private IPropertyDescriptor createExpertPropertyDescriptor(final Object id, final String displayName, final String category) {
		final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(id, displayName);
		propertyDescriptor.setCategory(category);
		propertyDescriptor.setFilterFlags(new String[] { IPropertySheetEntry.FILTER_ID_EXPERT });
		return propertyDescriptor;
	}

	@Override
	public Object getPropertyValue(final Object id) {
		if (id == ELEMENT_NAME_PROPERTY) {
			return element.getLocalName();
		}
		if (id == ELEMENT_NAMESPACE_URI_PROPERTY) {
			return element.getQualifiedName().getQualifier();
		}
		if (id == ELEMENT_NAMESPACE_PREFIX_PROPERTY) {
			return element.getNamespacePrefix(element.getQualifiedName().getQualifier());
		}

		if (id instanceof AttributeDefinition) {
			final AttributeDefinition attributeDefinition = (AttributeDefinition) id;
			if (multipleElementsSelected && id.equals(ATTR_ID)) {
				return Messages.getString("ElementPropertySource.multiple"); //$NON-NLS-1$
			}

			final IAttribute attribute = element.getAttribute(attributeDefinition.getQualifiedName());
			final String value;
			if (attribute != null) {
				value = attribute.getValue();
			} else {
				value = nullToEmpty(attributeDefinition.getDefaultValue());
			}

			if (attributeDefinition.getType() == AttributeDefinition.Type.ENUMERATION) {
				final String[] values = getEnumValues(attributeDefinition);
				for (int i = 0; i < values.length; i++) {
					if (values[i].equals(value)) {
						return Integer.valueOf(i);
					}
				}
				return Integer.valueOf(0);
				// TODO: If the actual value is not in the list, we should probably add it.
			}
			return value;
		}

		if (id instanceof NamespaceUri) {
			return ((NamespaceUri) id).uri;
		}

		return "";
	}

	private static String nullToEmpty(final String string) {
		if (string == null) {
			return "";
		}
		return string;
	}

	@Override
	public boolean isPropertySet(final Object id) {
		if (id == ELEMENT_NAME_PROPERTY) {
			return true;
		}
		if (id == ELEMENT_NAMESPACE_URI_PROPERTY) {
			return true;
		}
		if (id == ELEMENT_NAMESPACE_PREFIX_PROPERTY) {
			return true;
		}

		if (id instanceof AttributeDefinition) {
			final AttributeDefinition attributeDefinition = (AttributeDefinition) id;
			final IAttribute attribute = element.getAttribute(attributeDefinition.getName());
			if (attribute == null) {
				return false;
			}
			return true;
		}

		if (id instanceof NamespaceUri) {
			return true;
		}

		return false;
	}

	@Override
	public void resetPropertyValue(final Object id) {
		if (!(id instanceof AttributeDefinition)) {
			return;
		}
		final AttributeDefinition attributeDefinition = (AttributeDefinition) id;
		element.removeAttribute(attributeDefinition.getName());
	}

	@Override
	public void setPropertyValue(final Object id, final Object value) {
		if (!(id instanceof AttributeDefinition)) {
			return;
		}
		final AttributeDefinition attributeDefinition = (AttributeDefinition) id;

		try {
			if (attributeDefinition.getType() == AttributeDefinition.Type.ENUMERATION) {
				final int i = ((Integer) value).intValue();
				final String enumValue = getEnumValues(attributeDefinition)[i];
				if (!attributeDefinition.isRequired() && enumValue.equals("")) {
					element.removeAttribute(attributeDefinition.getName());
				} else {
					element.setAttribute(attributeDefinition.getQualifiedName(), enumValue);
				}
			} else {
				final String s = (String) value;
				if (s.equals("")) {
					element.removeAttribute(attributeDefinition.getQualifiedName());
				} else {
					element.setAttribute(attributeDefinition.getQualifiedName(), s);
				}
			}
		} catch (final DocumentValidationException e) {
		}
	}

	private static String[] getEnumValues(final AttributeDefinition attributeDefinition) {
		String[] values = attributeDefinition.getValues();
		if (attributeDefinition.isRequired()) {
			return values;
		} else {
			if (values == null) {
				values = new String[1];
				values[0] = "";
			}
			final String[] values2 = new String[values.length + 1];
			values2[0] = ""; //$NON-NLS-1$
			System.arraycopy(values, 0, values2, 1, values.length);
			return values2;
		}
	}

	@Override
	public boolean isPropertyResettable(final Object id) {
		if (!(id instanceof AttributeDefinition)) {
			return false;
		}
		return true;
	}

	private static class NamespaceUri {
		public final String uri;

		public NamespaceUri(final String uri) {
			this.uri = uri;
		}
	}

}