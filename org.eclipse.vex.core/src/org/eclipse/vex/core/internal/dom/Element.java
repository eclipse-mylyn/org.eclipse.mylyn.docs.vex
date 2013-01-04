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
 *     Florian Thienel - namespace handling (bug 253753), refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.QualifiedNameComparator;

/**
 * A representation of an XML element in the DOM. Elements have attributes, namespace declarations and children, as well
 * as a start and an end tag. The textual content of an element is represented by its child Text nodes.
 */
public class Element extends Parent {

	/*
	 * The xml:base attribute re-defines the base URI for a part of an XML document, according to the XML Base
	 * Recommendation.
	 * 
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	private static final QualifiedName XML_BASE_ATTRIBUTE = new QualifiedName(Namespace.XML_NAMESPACE_URI, "base");

	private final QualifiedName name;

	private final Map<QualifiedName, Attribute> attributes = new HashMap<QualifiedName, Attribute>();
	private final Map<String, String> namespaceDeclarations = new HashMap<String, String>();

	/**
	 * Create an element with the given name in the default namespace (only the local name without the qualifier is
	 * given).
	 * 
	 * @param localName
	 *            the local name of the element
	 */
	public Element(final String localName) {
		this(new QualifiedName(null, localName));
	}

	/**
	 * Create an element with the given qualified name.
	 * 
	 * @param qualifiedName
	 *            the qualified name of the element
	 */
	public Element(final QualifiedName qualifiedName) {
		name = qualifiedName;
	}

	/*
	 * Node
	 */

	@Override
	public String getBaseURI() {
		final Attribute baseAttribute = getAttribute(XML_BASE_ATTRIBUTE);
		if (baseAttribute != null) {
			return baseAttribute.getValue();
		}
		return super.getBaseURI();
	}

	/**
	 * Set the base URI of this element. The xml:base attribute re-defines the base URI for a part of an XML document,
	 * according to the XML Base Recommendation.
	 * 
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	public void setBaseURI(final String baseURI) {
		setAttribute(XML_BASE_ATTRIBUTE, baseURI);
	}

	/**
	 * This element is the same kind as the given node if the node is also an element and both have the same qualified
	 * name.
	 * 
	 * @see Node#isKindOf(Node)
	 * @return true if this element and the given node are of the same kind
	 */
	@Override
	public boolean isKindOf(final Node other) {
		if (!(other instanceof Element)) {
			return false;
		}
		return getQualifiedName().equals(((Element) other).getQualifiedName());
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	/*
	 * Element Name
	 */

	public QualifiedName getQualifiedName() {
		return name;
	}

	public String getLocalName() {
		return name.getLocalName();
	}

	/**
	 * @return the declared namespace prefix for this element
	 */
	public String getPrefix() {
		return getNamespacePrefix(name.getQualifier());
	}

	/**
	 * @return the declared namespace prefix and the local name of this element separated by a colon (e.g.
	 *         "prefix:localName")
	 */
	public String getPrefixedName() {
		final String prefix = getPrefix();
		if (prefix == null) {
			return getLocalName();
		}
		return prefix + ":" + getLocalName();
	}

	/**
	 * Qualify the given local name with the namespace qualifier of this element.
	 * 
	 * @param localName
	 *            the local name to be qualified
	 * @return the qualified variant of the given local name
	 */
	public QualifiedName qualify(final String localName) {
		return new QualifiedName(name.getQualifier(), localName);
	}

	/*
	 * Attributes
	 */

	/**
	 * @return the attribute with the given local name, or null if the attribute is not set
	 */
	public Attribute getAttribute(final String localName) {
		return getAttribute(qualify(localName));
	}

	/**
	 * @return the attribute with the given qualified name, or null if the attribute is not set
	 */
	public Attribute getAttribute(final QualifiedName name) {
		return attributes.get(name);
	}

	public String getAttributeValue(final String localName) {
		return getAttributeValue(qualify(localName));
	}

	/**
	 * @return the value of the attribute with the given qualified name, or null if the attribute is not set
	 */
	public String getAttributeValue(final QualifiedName name) {
		final Attribute attribute = getAttribute(name);
		if (attribute == null || "".equals(attribute.getValue().trim())) {
			return null;
		}
		return attribute.getValue();
	}

	/**
	 * Remove the attribute with the given local name.
	 * 
	 * @param localName
	 *            the local name of the attribute to be removed
	 * @throws DocumentValidationException
	 *             if the removal of the attribute would make the document invalid
	 */
	public void removeAttribute(final String localName) throws DocumentValidationException {
		removeAttribute(qualify(localName));
	}

	/**
	 * Remove the attribute with the given qualified name.
	 * 
	 * @param name
	 *            the qualified name of the attribute to be removed
	 * @throws DocumentValidationException
	 *             if the removal of the attribute would make the document invalid
	 */
	public void removeAttribute(final QualifiedName name) throws DocumentValidationException {
		final Attribute attribute = this.getAttribute(name);
		if (attribute == null) {
			return;
		}
		final String oldValue = attribute.getValue();
		final String newValue = null;
		if (oldValue != null) {
			attributes.remove(name);
		}

		final Document document = getDocument();
		if (document == null) {
			return;
		}

		document.fireAttributeChanged(new DocumentEvent(document, this, name, oldValue, newValue, null));
	}

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
	public void setAttribute(final String localName, final String value) throws DocumentValidationException {
		setAttribute(qualify(localName), value);
	}

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
	public void setAttribute(final QualifiedName name, final String value) throws DocumentValidationException {
		final Attribute oldAttribute = attributes.get(name);
		final String oldValue = oldAttribute != null ? oldAttribute.getValue() : null;

		if (value == null && oldValue == null) {
			return;
		}

		if (value == null) {
			this.removeAttribute(name);
		} else {
			if (value.equals(oldValue)) {
				return;
			} else {
				final Attribute newAttribute = new Attribute(this, name, value);
				attributes.put(name, newAttribute);

				final Document document = getDocument();
				if (document == null) {
					return;
				}

				document.fireAttributeChanged(new DocumentEvent(document, this, name, oldValue, value, null));
			}
		}
	}

	/**
	 * @return all attributes currently set on this element
	 */
	public Collection<Attribute> getAttributes() {
		final ArrayList<Attribute> result = new ArrayList<Attribute>(attributes.values());
		Collections.sort(result);
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * @return the qualified names of all attributes currently set on this element
	 */
	public List<QualifiedName> getAttributeNames() {
		final ArrayList<QualifiedName> result = new ArrayList<QualifiedName>();
		for (final Attribute attribute : attributes.values()) {
			result.add(attribute.getQualifiedName());
		}
		Collections.sort(result, new QualifiedNameComparator());
		return result;
	}

	/*
	 * Element Structure
	 */

	/**
	 * @return the parent element of this element
	 */
	public Element getParentElement() {
		return getParentElement(this);
	}

	private static Element getParentElement(final Node node) {
		final Node parent = node.getParent();
		if (parent == null) {
			return null;
		}
		if (parent instanceof Element) {
			return (Element) parent;
		}
		return getParentElement(parent);
	}

	/**
	 * @return the child elements of this element
	 */
	public List<Element> getChildElements() {
		final List<Node> nodes = getChildNodes();
		final List<Element> elements = new ArrayList<Element>();
		for (final Node node : nodes) {
			node.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Element element) {
					elements.add(element);
				}
			});
		}
		return elements;
	}

	/*
	 * Namespaces
	 */

	/**
	 * @return the namespace URI of the given namespace prefix, or null if no namespace with the given prefix is
	 *         declared
	 */
	public String getNamespaceURI(final String namespacePrefix) {
		if (namespaceDeclarations.containsKey(namespacePrefix)) {
			return namespaceDeclarations.get(namespacePrefix);
		}
		final Element parent = getParentElement();
		if (parent != null) {
			return parent.getNamespaceURI(namespacePrefix);
		}
		return null;
	}

	/**
	 * @return the default namespace URI or null, if no default namespace is declared
	 */
	public String getDefaultNamespaceURI() {
		return getNamespaceURI(null);
	}

	/**
	 * @return the URI of the default namespace declared on this element, or null if no default namespace is declared on
	 *         this element
	 */
	public String getDeclaredDefaultNamespaceURI() {
		return namespaceDeclarations.get(null);
	}

	/**
	 * @return the declared namespace prefix for the given namespace URI, or null if the namespace with the given URI is
	 *         not declared in the document
	 */
	public String getNamespacePrefix(final String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		}
		if (Namespace.XML_NAMESPACE_URI.equals(namespaceURI)) {
			return Namespace.XML_NAMESPACE_PREFIX;
		}
		if (Namespace.XMLNS_NAMESPACE_URI.equals(namespaceURI)) {
			return Namespace.XMLNS_NAMESPACE_PREFIX;
		}
		for (final Entry<String, String> entry : namespaceDeclarations.entrySet()) {
			if (entry.getValue().equals(namespaceURI)) {
				return entry.getKey();
			}
		}
		final Element parent = getParentElement();
		if (parent != null) {
			final String parentPrefix = parent.getNamespacePrefix(namespaceURI);
			if (!namespaceDeclarations.containsKey(parentPrefix)) {
				return parentPrefix;
			}
		}
		return null;
	}

	/**
	 * @return the prefixes of the namespaces that are declared on this element
	 */
	public Collection<String> getDeclaredNamespacePrefixes() {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String prefix : namespaceDeclarations.keySet()) {
			if (prefix != null) {
				result.add(prefix);
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * @return the prefixes of the declared namespaces
	 */
	public Collection<String> getNamespacePrefixes() {
		final HashSet<String> result = new HashSet<String>();
		result.addAll(getDeclaredNamespacePrefixes());
		final Element parent = getParentElement();
		if (parent != null) {
			result.addAll(parent.getNamespacePrefixes());
		}
		return result;
	}

	/**
	 * Declare a namespace with the given prefix and URI on this element.
	 * 
	 * @param namespacePrefix
	 *            the prefix of the namespace to be declared
	 * @param namespaceURI
	 *            the URI of the namespace to be declared
	 */
	public void declareNamespace(final String namespacePrefix, final String namespaceURI) {
		if (namespaceURI == null || "".equals(namespaceURI.trim())) {
			return;
		}
		final String oldNamespaceURI = namespaceDeclarations.put(namespacePrefix, namespaceURI);
		final Document document = getDocument();
		if (document == null) {
			return;
		}

		if (namespaceURI.equals(oldNamespaceURI)) {
			return;
		}

		document.fireNamespaceChanged(new DocumentEvent(document, this, getStartOffset(), 0, null));
	}

	/**
	 * Remove the namespace declaration with the given prefix from this element.
	 * 
	 * @param namespacePrefix
	 *            the prefix of the namespace to be removed
	 */
	public void removeNamespace(final String namespacePrefix) {
		final String oldNamespaceURI = namespaceDeclarations.remove(namespacePrefix);
		final Document document = getDocument();
		if (document == null) {
			return;
		}

		if (oldNamespaceURI == null) {
			return; // we have actually removed nothing, so we should not tell anybody about it
		}

		document.fireNamespaceChanged(new DocumentEvent(document, this, getStartOffset(), 0, null));
	}

	/**
	 * Declare the default namespace with the given URI on this element.
	 * 
	 * @param namespaceURI
	 *            the URI of the default namespace
	 */
	public void declareDefaultNamespace(final String namespaceURI) {
		declareNamespace(null, namespaceURI);
	}

	/**
	 * Remove the declaration of the default namespace from this element.
	 */
	public void removeDefaultNamespace() {
		removeNamespace(null);
	}

	/*
	 * Miscellaneous
	 */

	@Override
	public String toString() {

		final StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(getPrefixedName().toString());

		for (final Attribute attribute : getAttributes()) {
			sb.append(" ");
			sb.append(attribute.getPrefixedName());
			sb.append("=\"");
			sb.append(attribute.getValue());
			sb.append("\"");
		}

		sb.append("> (");
		if (isAssociated()) {
			sb.append(getStartOffset());
			sb.append(",");
			sb.append(getEndOffset());
		} else {
			sb.append("n/a");
		}
		sb.append(")");

		return sb.toString();
	}

}
