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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Represents a fragment of an XML document.
 */
public class DocumentFragment extends Parent {

	/**
	 * @param content
	 *            Content holding the fragment's content.
	 * @param nodes
	 *            Elements that make up this fragment.
	 */
	public DocumentFragment(final Content content, final List<Node> nodes) {
		Assert.isTrue(content.length() > 0);
		associate(content, content.getRange());
		for (final Node node : nodes) {
			addChild(node);
		}
	}

	public int getLength() {
		return getContent().length();
	}

	public List<Element> getElements() {
		final List<Element> elements = new ArrayList<Element>();
		for (final Node node : getNodes()) {
			node.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Element element) {
					elements.add(element);
				}
			});
		}
		return elements;
	}

	public List<QualifiedName> getNodeNames() {
		return Node.getNodeNames(getChildNodes());
	}

	public List<Node> getNodes() {
		return getChildNodes();
	}

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

}
