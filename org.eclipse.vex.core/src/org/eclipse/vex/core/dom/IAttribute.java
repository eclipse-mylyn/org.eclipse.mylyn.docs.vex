/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.dom;

import org.eclipse.core.runtime.QualifiedName;

/**
 * A representation of an attribute within the start tag of an element. An attribute consists of a qualified name and a
 * value. It is comparable by its qualified name, which is the natural order of attributes.
 * 
 * @author Florian Thienel
 */
public interface IAttribute extends Comparable<IAttribute> {

	/**
	 * @return the element to which this attribute belongs
	 */
	IElement getParent();

	/**
	 * @return the qualified name of this attribute
	 */
	QualifiedName getQualifiedName();

	/**
	 * @return prefix:localName, or localName if prefix is null or this attribute is in the same namespace as the parent
	 *         element.
	 */
	String getPrefixedName();

	/**
	 * @return the local name of this attribute
	 */
	String getLocalName();

	/**
	 * @return the value of this attribute .
	 */
	String getValue();

	/**
	 * Compares two attributes by their qualified name.
	 * 
	 * @param otherAttribute
	 *            the other attribute
	 * @see Comparable
	 */
	int compareTo(IAttribute otherAttribute);

}