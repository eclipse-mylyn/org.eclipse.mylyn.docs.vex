/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Florian Thienel - support for XML namespaces (bug 253753)
 *******************************************************************************/
package org.eclipse.vex.core.dom;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Represents an object that can validate the structure of a document.
 */
public interface IValidator {

	/**
	 * QualifiedName indicating that character data is allowed at the given point in the document.
	 */
	final QualifiedName PCDATA = new QualifiedName(null, "#PCDATA");

	/**
	 * Returns the AttributeDefinition for a particular attribute.
	 * 
	 * @param element
	 *            Name of the element.
	 * @param attribute
	 *            Name of the attribute.
	 * @model
	 */
	AttributeDefinition getAttributeDefinition(IAttribute attribute);

	/**
	 * Returns the attribute definitions that apply to the given element.
	 * 
	 * @param element
	 *            the element to check.
	 */
	List<AttributeDefinition> getAttributeDefinitions(IElement element);

	/**
	 * Returns a set of QualifiedNames representing items that are valid at point in the child nodes of a given element.
	 * Each string is either an element name or Validator.PCDATA.
	 * 
	 * @param element
	 *            the parent element.
	 */
	Set<QualifiedName> getValidItems(final IElement element);

	/**
	 * Returns true if the given sequence is valid for the given element. Accepts three sequences, which will be
	 * concatenated before doing the check.
	 * 
	 * @param element
	 *            Name of the element being tested.
	 * @param nodes
	 *            Array of element names and Validator.PCDATA.
	 * @param partial
	 *            If true, an valid but incomplete sequence is acceptable.
	 */
	boolean isValidSequence(QualifiedName element, List<QualifiedName> nodes, boolean partial);

	/**
	 * Returns true if the given sequence is valid for the given element. Accepts three sequences, which will be
	 * concatenated before doing the check.
	 * 
	 * @param element
	 *            Name of the element being tested.
	 * @param seq1
	 *            List of element names and Validator.PCDATA.
	 * @param seq2
	 *            List of element names and Validator.PCDATA. May be null or empty.
	 * @param seq3
	 *            List of element names and Validator.PCDATA. May be null or empty.
	 * @param partial
	 *            If true, an valid but incomplete sequence is acceptable.
	 */
	boolean isValidSequence(QualifiedName element, List<QualifiedName> seq1, List<QualifiedName> seq2, List<QualifiedName> seq3, boolean partial);

	/**
	 * Returns a set of QualifiedNames representing valid root elements for the given document type.
	 */
	Set<QualifiedName> getValidRootElements();

	/**
	 * @return the namespaces which are used and hence required in the document type represented by this validator
	 */
	Set<String> getRequiredNamespaces();

}
