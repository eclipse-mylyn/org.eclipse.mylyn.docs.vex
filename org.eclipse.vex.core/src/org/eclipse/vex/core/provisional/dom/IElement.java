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
package org.eclipse.vex.core.provisional.dom;

import java.util.Collection;

import org.eclipse.core.runtime.QualifiedName;

/**
 * A representation of an XML element in the DOM. Elements have attributes, namespace declarations and children, as well
 * as a start and an end tag. The textual content of an element is represented by its child text nodes.
 *
 * @author Florian Thienel
 */
public interface IElement extends IParent {

	/**
	 * This element is the same kind as the given node if the node is also an element and both have the same qualified
	 * name.
	 *
	 * @see INode#isKindOf(INode)
	 * @return true if this element and the given node are of the same kind
	 */
	@Override
	boolean isKindOf(INode node);

	/**
	 * Set the base URI of this element. The xml:base attribute re-defines the base URI for a part of an XML document,
	 * according to the XML Base Recommendation.
	 *
	 * @param baseURI
	 *            the base URI of this element
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	void setBaseURI(String baseURI);

	/**
	 * @return the qualified name of this element
	 */
	QualifiedName getQualifiedName();

	/**
	 * @return the local name of this element
	 */
	String getLocalName();

	/**
	 * @return the declared namespace prefix for this element
	 */
	String getPrefix();

	/**
	 * @return the declared namespace prefix and the local name of this element separated by a colon (e.g.
	 *         "prefix:localName")
	 */
	String getPrefixedName();

	/**
	 * Qualify the given local name with the namespace qualifier of this element.
	 *
	 * @param localName
	 *            the local name to be qualified
	 * @return the qualified variant of the given local name
	 */
	QualifiedName qualify(String localName);

	/**
	 * @return the attribute with the given local name, or null if the attribute is not set
	 */
	IAttribute getAttribute(String localName);

	/**
	 * @return the attribute with the given qualified name, or null if the attribute is not set
	 */
	IAttribute getAttribute(QualifiedName name);

	/**
	 * @return the attribute with the given local name, or null if the attribute is not set
	 */
	String getAttributeValue(String localName);

	/**
	 * @return the value of the attribute with the given qualified name, or null if the attribute is not set
	 */
	String getAttributeValue(QualifiedName name);

	/**
	 * @param localName
	 *            the local name of the attribute to remove
	 * @return true if it is valid to remove the attribute with the given local name
	 */
	boolean canRemoveAttribute(String localName);

	/**
	 * @param name
	 *            the qualified name of the attribute to remove
	 * @return true if it is valid to remove the attribute with the given qualified name
	 */
	boolean canRemoveAttribute(QualifiedName name);

	/**
	 * Remove the attribute with the given local name.
	 *
	 * @param localName
	 *            the local name of the attribute to be removed
	 * @throws DocumentValidationException
	 *             if the removal of the attribute would make the document invalid
	 */
	void removeAttribute(String localName) throws DocumentValidationException;

	/**
	 * Remove the attribute with the given qualified name.
	 *
	 * @param name
	 *            the qualified name of the attribute to be removed
	 * @throws DocumentValidationException
	 *             if the removal of the attribute would make the document invalid
	 */
	void removeAttribute(QualifiedName name) throws DocumentValidationException;

	/**
	 * @param localName
	 *            the local name of the attribute
	 * @param value
	 *            the new attribute value
	 * @return true if the given value is valid for the attribute with the given local name
	 */
	boolean canSetAttribute(String localName, String value);

	/**
	 * @param name
	 *            the qualified name of the attribute
	 * @param value
	 *            the new attribute value
	 * @return true if the given value is valid for the attribute with the given qualified name
	 */
	boolean canSetAttribute(QualifiedName name, String value);

	/**
	 * Set the attribute with the given local name to the given value.
	 *
	 * @param localName
	 *            the local name of the attribute
	 * @param value
	 *            the new attribute value
	 * @throws DocumentValidationException
	 *             if the new attribute value would make the document invalid
	 */
	void setAttribute(String localName, String value) throws DocumentValidationException;

	/**
	 * Set the attribute with the given qualified name to the given value.
	 *
	 * @param name
	 *            the qualified name of the attribute
	 * @param value
	 *            the new attribute value
	 * @throws DocumentValidationException
	 *             if the new attribute value would make the document invalid
	 */
	void setAttribute(QualifiedName name, String value) throws DocumentValidationException;

	/**
	 * @return all attributes currently set on this element
	 */
	Collection<IAttribute> getAttributes();

	/**
	 * @return the qualified names of all attributes currently set on this element
	 */
	Collection<QualifiedName> getAttributeNames();

	/**
	 * @return the parent element of this element
	 */
	IElement getParentElement();

	/**
	 * @return the child elements of this element in form of an axis
	 * @see IAxis
	 */
	IAxis<? extends IElement> childElements();

	/**
	 * @return the namespace URI of the given namespace prefix, or null if no namespace with the given prefix is
	 *         declared
	 */
	String getNamespaceURI(String namespacePrefix);

	/**
	 * @return the default namespace URI or null, if no default namespace is declared
	 */
	String getDefaultNamespaceURI();

	/**
	 * @return the URI of the default namespace declared on this element, or null if no default namespace is declared on
	 *         this element
	 */
	String getDeclaredDefaultNamespaceURI();

	/**
	 * @return the declared namespace prefix for the given namespace URI, or null if the namespace with the given URI is
	 *         not declared in the document
	 */
	String getNamespacePrefix(String namespaceURI);

	/**
	 * @return the prefixes of the namespaces that are declared on this element
	 */
	Collection<String> getDeclaredNamespacePrefixes();

	/**
	 * @return the prefixes of the declared namespaces
	 */
	Collection<String> getNamespacePrefixes();

	/**
	 * Declare a namespace with the given prefix and URI on this element.
	 *
	 * @param namespacePrefix
	 *            the prefix of the namespace to be declared
	 * @param namespaceURI
	 *            the URI of the namespace to be declared
	 */
	void declareNamespace(String namespacePrefix, String namespaceURI);

	/**
	 * Remove the namespace declaration with the given prefix from this element.
	 *
	 * @param namespacePrefix
	 *            the prefix of the namespace to be removed
	 */
	void removeNamespace(String namespacePrefix);

	/**
	 * Declare the default namespace with the given URI on this element.
	 *
	 * @param namespaceURI
	 *            the URI of the default namespace
	 */
	void declareDefaultNamespace(String namespaceURI);

	/**
	 * Remove the declaration of the default namespace from this element.
	 */
	void removeDefaultNamespace();

}