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
 *     Florian Thienel - extracted responsibility for serialization, refactoring to full fledged DOM  
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Represents a wellformed fragment of an XML document.
 */
public class DocumentFragment extends Parent {

	/**
	 * Create a new fragment with based on the given content and nodes.
	 * 
	 * @param content
	 *            the Content holding the fragment's content
	 * @param nodes
	 *            the nodes that make up the structure of this fragment
	 */
	public DocumentFragment(final Content content, final List<Node> nodes) {
		Assert.isTrue(content.length() > 0);
		associate(content, content.getRange());
		for (final Node node : nodes) {
			addChild(node);
		}
	}

	/**
	 * @return the length of the textual content of this fragment plus 1 for each opening or closing XML tag (element
	 *         tags, comment tags, PI tags and entity references)
	 */
	public int getLength() {
		return getContent().length();
	}

	/**
	 * @return a list with the qualified names off all nodes on the root level of this fragment
	 */
	public List<QualifiedName> getNodeNames() {
		return Node.getNodeNames(children());
	}

	/**
	 * @return all nodes on the root level of this fragment
	 */
	public List<Node> getNodes() {
		final List<Node> result = new ArrayList<Node>();
		for (final Node node : children()) {
			result.add(node);
		}
		return result;
	}

	/**
	 * The base URI of a fragment is always null because a fragment has no persistent representation.
	 * 
	 * @see Node#getBaseURI()
	 * @return null
	 */
	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isKindOf(final Node node) {
		return false;
	}
}
