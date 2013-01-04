/*******************************************************************************
 * Copyright (c) 2010, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.core.runtime.QualifiedName;

/**
 * An immutable representation of an attribute within the start tag of an element. An attribute consists of a qualified
 * name and a value. It is Comparable by its qualified name, which is the natural order of attributes.
 * 
 * @author Florian Thienel
 */
public class Attribute implements Comparable<Attribute> {

	private final Element parent;

	private final QualifiedName name;

	private final String value;

	/**
	 * Create an attribute within the namespace of the parent element, i.e. only the local name without a qualifier is
	 * given.
	 * 
	 * @param parent
	 *            the element containing the attribute
	 * @param localName
	 *            the local name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public Attribute(final Element parent, final String localName, final String value) {
		this(parent, new QualifiedName(null, localName), value);
	}

	/**
	 * Create an attribute within an arbitrary namespace.
	 * 
	 * @param parent
	 *            the element containing the attribute
	 * @param name
	 *            the qualified name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public Attribute(final Element parent, final QualifiedName name, final String value) {
		this.parent = parent;
		this.name = name;
		this.value = value;
	}

	public Element getParent() {
		return parent;
	}

	public String getLocalName() {
		return name.getLocalName();
	}

	public String getValue() {
		return value;
	}

	public QualifiedName getQualifiedName() {
		return name;
	}

	/**
	 * @return prefix:localName, or localName if prefix is null or this attribute is in the same namespace as the parent
	 *         element.
	 */
	public String getPrefixedName() {
		final String attributeQualifier = name.getQualifier();
		if (parent == null || attributeQualifier == null) {
			return getLocalName();
		}
		final String elementQualifier = parent.getQualifiedName().getQualifier();
		if (attributeQualifier.equals(elementQualifier)) {
			return getLocalName();
		}
		final String prefix = parent.getNamespacePrefix(attributeQualifier);
		return (prefix == null ? "" : prefix + ":") + getLocalName();
	}

	/**
	 * Compares two attributes by their qualified name.
	 * 
	 * @param otherAttribute
	 *            the other attribute
	 * @see Comparable
	 */
	public int compareTo(final Attribute otherAttribute) {
		return name.toString().compareTo(otherAttribute.name.toString());
	}
}
