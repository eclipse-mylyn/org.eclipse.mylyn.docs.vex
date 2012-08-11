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
package org.eclipse.vex.core.internal.validator;

import java.util.Iterator;

import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamespace;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;

/**
 * @author Florian Thienel
 */
public class UnknownCMDocument implements CMDocument {

	private static final String TARGET_NAMESPACE_PROPERTY = "http://org.eclipse.wst/cm/properties/targetNamespaceURI";

	private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	private static final CMNamedNodeMap EMPTY_NODE_MAP = new CMNamedNodeMap() {
		public Iterator<?> iterator() {
			return EMPTY_ITERATOR;
		}

		public CMNode item(final int index) {
			return null;
		}

		public CMNode getNamedItem(final String name) {
			return null;
		}

		public int getLength() {
			return 0;
		}
	};

	private final String targetNamespace;

	public UnknownCMDocument(final String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getNodeName() {
		return "";
	}

	public int getNodeType() {
		return DOCUMENT;
	}

	public boolean supports(final String propertyName) {
		if (TARGET_NAMESPACE_PROPERTY.equals(propertyName)) {
			return true;
		}
		return false;
	}

	public Object getProperty(final String propertyName) {
		if (propertyName.equals(TARGET_NAMESPACE_PROPERTY)) {
			return targetNamespace;
		}
		return null;
	}

	public CMNamedNodeMap getElements() {
		return EMPTY_NODE_MAP;
	}

	public CMNamedNodeMap getEntities() {
		return EMPTY_NODE_MAP;
	}

	public CMNamespace getNamespace() {
		return null;
	}

}
