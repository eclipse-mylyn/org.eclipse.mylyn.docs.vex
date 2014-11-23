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

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;

public class DocumentFragment extends Parent implements IDocumentFragment {

	public DocumentFragment(final IContent content, final List<Node> nodes) {
		Assert.isTrue(content.length() > 0);
		associate(content, content.getRange());
		for (final Node node : nodes) {
			addChild(node);
		}
	}

	@Override
	public int getLength() {
		return getContent().length();
	}

	@Override
	public List<QualifiedName> getNodeNames() {
		return Node.getNodeNames(children());
	}

	@Override
	public List<? extends INode> getNodes() {
		return children().asList();
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

	@Override
	public boolean isKindOf(final INode node) {
		return false;
	}
}
