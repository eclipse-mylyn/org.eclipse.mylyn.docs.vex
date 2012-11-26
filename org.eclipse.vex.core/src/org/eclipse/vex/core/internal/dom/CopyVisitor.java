/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

/**
 * This visitor creates a simple copy of the visited node. I.e. only the node itself, not its content neither its
 * children are copied. The copy is provided through the getCopy() method.
 * 
 * @author Florian Thienel
 */
public class CopyVisitor implements INodeVisitorWithResult<Node> {

	public Document visit(final Document document) {
		throw new UnsupportedOperationException("Document cannot be copied");
	}

	public DocumentFragment visit(final DocumentFragment fragment) {
		throw new UnsupportedOperationException("DocumentFragment cannot be copied");
	}

	public Element visit(final Element element) {
		final Element copyElement = new Element(element.getQualifiedName());

		for (final Attribute attribute : element.getAttributes()) {
			copyElement.setAttribute(attribute.getQualifiedName(), attribute.getValue());
		}

		copyElement.declareDefaultNamespace(element.getDeclaredDefaultNamespaceURI());

		for (final String prefix : element.getNamespacePrefixes()) {
			copyElement.declareNamespace(prefix, element.getNamespaceURI(prefix));
		}

		return copyElement;
	}

	public Text visit(final Text text) {
		// ignore Text nodes because they are created dynamically in Element.getChildNodes()
		return null;
	}

	public Comment visit(final Comment comment) {
		return new Comment();
	}

}
