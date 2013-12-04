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
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IElement;

/**
 * An immutable representation of an attribute within the start tag of an element. An attribute consists of a qualified
 * name and a value. It is Comparable by its qualified name, which is the natural order of attributes.
 * 
 * @author Florian Thienel
 */
public class Attribute implements IAttribute {

	private final IElement parent;

	private final QualifiedName name;

	private final String value;

	public Attribute(final IElement parent, final String localName, final String value) {
		this(parent, new QualifiedName(null, localName), value);
	}

	public Attribute(final IElement parent, final QualifiedName name, final String value) {
		this.parent = parent;
		this.name = name;
		this.value = value;
	}

	public IElement getParent() {
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

	public String getPrefixedName() {
		final String attributeQualifier = name.getQualifier();
		if (parent == null || attributeQualifier == null) {
			return getLocalName();
		}
		final String prefix = parent.getNamespacePrefix(attributeQualifier);
		return (prefix == null ? "" : prefix + ":") + getLocalName();
	}

	public int compareTo(final IAttribute otherAttribute) {
		return name.toString().compareTo(otherAttribute.getQualifiedName().toString());
	}
}
