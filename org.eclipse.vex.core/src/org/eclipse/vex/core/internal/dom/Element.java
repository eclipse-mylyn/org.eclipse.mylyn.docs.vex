/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.QualifiedNameComparator;
import org.eclipse.vex.core.internal.undo.CannotRedoException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;

/**
 * Represents a tag in an XML document. Methods are available for managing the element's attributes and children.
 */
public class Element extends Node implements Cloneable {

	private static final QualifiedName XML_BASE_ATTRIBUTE = new QualifiedName(Namespace.XML_NAMESPACE_URI, "base");

	private final QualifiedName name;

	private Element parent = null;
	private final List<Node> childNodes = new ArrayList<Node>();
	private final Map<QualifiedName, Attribute> attributes = new HashMap<QualifiedName, Attribute>();
	private final Map<String, String> namespaceDeclarations = new HashMap<String, String>();

	public Element(final String localName) {
		this(new QualifiedName(null, localName));
	}

	public Element(final QualifiedName qualifiedName) {
		name = qualifiedName;
	}

	public void addChild(final Element child) {
		childNodes.add(child);
		child.setParent(this);
	}

	@Override
	public Element clone() {
		try {
			final Element element = new Element(getQualifiedName());
			//add the attributes to the element instance to be cloned
			for (final Map.Entry<QualifiedName, Attribute> attr : attributes.entrySet()) {
				element.setAttribute(attr.getKey(), attr.getValue().getValue());
			}
			for (final Map.Entry<String, String> namespaceDeclaration : namespaceDeclarations.entrySet()) {
				if (namespaceDeclaration.getKey() == null) {
					element.declareDefaultNamespace(namespaceDeclaration.getValue());
				} else {
					element.declareNamespace(namespaceDeclaration.getKey(), namespaceDeclaration.getValue());
				}
			}
			return element;
		} catch (final DocumentValidationException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Attribute getAttribute(final String localName) {
		return getAttribute(qualify(localName));
	}

	public Attribute getAttribute(final QualifiedName name) {
		return attributes.get(name);
	}

	public String getAttributeValue(final String localName) {
		return getAttributeValue(qualify(localName));
	}

	public String getAttributeValue(final QualifiedName name) {
		final Attribute attribute = getAttribute(name);
		if (attribute == null || "".equals(attribute.getValue().trim())) {
			return null;
		}
		return attribute.getValue();
	}

	public void removeAttribute(final String localName) throws DocumentValidationException {
		removeAttribute(qualify(localName));
	}

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

		final IUndoableEdit edit = document.isUndoEnabled() ? new AttributeChangeEdit(name, oldValue, newValue) : null;
		document.fireAttributeChanged(new DocumentEvent(document, this, name, oldValue, newValue, edit));
	}

	public void setAttribute(final String name, final String value) throws DocumentValidationException {
		setAttribute(qualify(name), value);
	}

	private QualifiedName qualify(final String localName) {
		return new QualifiedName(name.getQualifier(), localName);
	}

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

				final IUndoableEdit edit = document.isUndoEnabled() ? new AttributeChangeEdit(name, oldValue, value) : null;
				document.fireAttributeChanged(new DocumentEvent(document, this, name, oldValue, value, edit));
			}
		}
	}

	public Collection<Attribute> getAttributes() {
		final ArrayList<Attribute> result = new ArrayList<Attribute>(attributes.values());
		Collections.sort(result);
		return Collections.unmodifiableCollection(result);
	}

	public List<QualifiedName> getAttributeNames() {
		final ArrayList<QualifiedName> result = new ArrayList<QualifiedName>();
		for (final Attribute attribute : attributes.values()) {
			result.add(attribute.getQualifiedName());
		}
		Collections.sort(result, new QualifiedNameComparator());
		return result;
	}

	public Iterator<Node> getChildIterator() {
		return childNodes.iterator();
	}

	public List<Element> getChildElements() {
		final List<Node> nodes = getChildNodes();
		final Iterator<Node> iter = nodes.iterator();
		final List<Element> elements = new ArrayList<Element>();
		while (iter.hasNext()) {
			final Node node = iter.next();
			if (node.getNodeType().equals("Element")) {
				elements.add((Element) node);
			}
		}
		return elements;
	}

	public List<Node> getChildNodes() {
		return Document.createNodeList(getContent(), getStartOffset() + 1, getEndOffset(), childNodes);
	}

	public Document getDocument() {
		Element root = this;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		if (root instanceof RootElement) {
			return root.getDocument();
		} else {
			return null;
		}
	}

	public String getLocalName() {
		return name.getLocalName();
	}

	public QualifiedName getQualifiedName() {
		return name;
	}

	public String getPrefix() {
		return getNamespacePrefix(name.getQualifier());
	}

	public String getPrefixedName() {
		final String prefix = getPrefix();
		if (prefix == null) {
			return getLocalName();
		}
		return prefix + ":" + getLocalName();
	}

	public Element getParent() {
		return parent;
	}

	@Override
	public String getText() {
		final String s = super.getText();
		final StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (!getContent().isElementMarker(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Inserts the given element as a child at the given child index. Sets the parent attribute of the given element to
	 * this element.
	 */
	public void insertChild(final int index, final Element child) {
		childNodes.add(index, child);
		child.setParent(this);
	}

	public boolean isEmpty() {
		return getStartOffset() + 1 == getEndOffset();
	}

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
		sb.append(getStartPosition());
		sb.append(",");
		sb.append(getEndPosition());
		sb.append(")");

		return sb.toString();
	}

	public void setParent(final Element parent) {
		this.parent = parent;
	}

	public String getNamespaceURI(final String namespacePrefix) {
		if (namespaceDeclarations.containsKey(namespacePrefix)) {
			return namespaceDeclarations.get(namespacePrefix);
		}
		if (parent != null) {
			return parent.getNamespaceURI(namespacePrefix);
		}
		return null;
	}

	public String getDefaultNamespaceURI() {
		return getNamespaceURI(null);
	}

	public String getDeclaredDefaultNamespaceURI() {
		return namespaceDeclarations.get(null);
	}

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
		if (parent != null) {
			final String parentPrefix = parent.getNamespacePrefix(namespaceURI);
			if (!namespaceDeclarations.containsKey(parentPrefix)) {
				return parentPrefix;
			}
		}
		return null;
	}

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

	public Collection<String> getNamespacePrefixes() {
		final HashSet<String> result = new HashSet<String>();
		result.addAll(getDeclaredNamespacePrefixes());
		if (parent != null) {
			result.addAll(parent.getNamespacePrefixes());
		}
		return result;
	}

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

		final IUndoableEdit edit = document.isUndoEnabled() ? new NamespaceChangeEdit(namespacePrefix, oldNamespaceURI, namespaceURI) : null;
		document.fireNamespaceChanged(new DocumentEvent(document, this, getStartOffset(), 0, edit));
	}

	public void removeNamespace(final String namespacePrefix) {
		final String oldNamespaceURI = namespaceDeclarations.remove(namespacePrefix);
		final Document document = getDocument();
		if (document == null) {
			return;
		}

		if (oldNamespaceURI == null) {
			return; // we have actually removed nothing, so we should not tell anybody about it
		}

		final IUndoableEdit edit = document.isUndoEnabled() ? new NamespaceChangeEdit(namespacePrefix, oldNamespaceURI, null) : null;
		document.fireNamespaceChanged(new DocumentEvent(document, this, getStartOffset(), 0, edit));
	}

	public void declareDefaultNamespace(final String namespaceURI) {
		declareNamespace(null, namespaceURI);
	}

	public void removeDefaultNamespace() {
		removeNamespace(null);
	}

	@Override
	public String getNodeType() {
		return "Element";
	}

	@Override
	public void setContent(final Content content, final int startOffset, final int endOffset) {
		super.setContent(content, startOffset, endOffset);
	}

	@Override
	public String getBaseURI() {
		final Attribute baseAttribute = getAttribute(XML_BASE_ATTRIBUTE);
		if (baseAttribute != null) {
			return baseAttribute.getValue();
		}
		if (getParent() != null) {
			return getParent().getBaseURI();
		}
		if (getDocument() != null) {
			return getDocument().getBaseURI();
		}
		return null;
	}

	private class AttributeChangeEdit implements IUndoableEdit {

		private final QualifiedName name;
		private final String oldValue;
		private final String newValue;

		public AttributeChangeEdit(final QualifiedName name, final String oldValue, final String newValue) {
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public boolean combine(final IUndoableEdit edit) {
			return false;
		}

		public void undo() throws CannotUndoException {
			final Document doc = getDocument();
			try {
				doc.setUndoEnabled(false);
				setAttribute(name, oldValue);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				doc.setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			final Document doc = getDocument();
			try {
				doc.setUndoEnabled(false);
				setAttribute(name, newValue);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				doc.setUndoEnabled(true);
			}
		}
	}

	private class NamespaceChangeEdit implements IUndoableEdit {

		private final String prefix;
		private final String oldUri;
		private final String newUri;

		public NamespaceChangeEdit(final String prefix, final String oldUri, final String newUri) {
			this.prefix = prefix;
			this.oldUri = oldUri;
			this.newUri = newUri;
		}

		public boolean combine(final IUndoableEdit edit) {
			return false;
		}

		public void undo() throws CannotUndoException {
			final Document doc = getDocument();
			try {
				doc.setUndoEnabled(false);
				if (oldUri == null) {
					removeNamespace(prefix);
				} else {
					declareNamespace(prefix, oldUri);
				}
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				doc.setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			final Document doc = getDocument();
			try {
				doc.setUndoEnabled(false);
				if (newUri == null) {
					removeNamespace(prefix);
				} else {
					declareNamespace(prefix, newUri);
				}
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				doc.setUndoEnabled(true);
			}
		}
	}

}
