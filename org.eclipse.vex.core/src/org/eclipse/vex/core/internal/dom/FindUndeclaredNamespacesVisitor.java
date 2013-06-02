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
package org.eclipse.vex.core.internal.dom;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * This visitor implements a deep search for undeclared namespace URIs. The result is returned as a set of strings
 * containing the undeclared URIs.
 * 
 * @author Florian Thienel
 */
public class FindUndeclaredNamespacesVisitor implements INodeVisitorWithResult<Set<String>> {

	public Set<String> visit(final IDocument document) {
		return visitAll(document.children());
	}

	public Set<String> visit(final IDocumentFragment fragment) {
		return visitAll(fragment.children());
	}

	public Set<String> visit(final IElement element) {
		final Set<String> result = new HashSet<String>();

		final String namespaceUri = element.getQualifiedName().getQualifier();
		if (!isNamespaceDeclared(element, namespaceUri)) {
			result.add(namespaceUri);
		}

		result.addAll(visitAll(element.children()));
		return result;
	}

	public Set<String> visit(final IText text) {
		return Collections.emptySet();
	}

	public Set<String> visit(final IComment comment) {
		return Collections.emptySet();
	}

	private Set<String> visitAll(final Iterable<INode> iterable) {
		final Set<String> result = new HashSet<String>();
		for (final INode node : iterable) {
			result.addAll(node.accept(this));
		}
		return result;
	}

	private static boolean isNamespaceDeclared(final IElement element, final String namespaceUri) {
		if (namespaceUri == null) {
			return true;
		}

		if (namespaceUri.equals(element.getDefaultNamespaceURI())) {
			return true;
		}

		if (element.getNamespacePrefix(namespaceUri) != null) {
			return true;
		}
		return false;
	}
}
