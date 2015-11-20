/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

public class DepthFirstNodeTraversal<T> extends BaseNodeVisitorWithResult<T> {

	public DepthFirstNodeTraversal() {
		this(null);
	}

	public DepthFirstNodeTraversal(final T defaultValue) {
		super(defaultValue);
	}

	@Override
	public T visit(final IDocument document) {
		return traverseChildren(document);
	}

	@Override
	public T visit(final IDocumentFragment fragment) {
		return traverseChildren(fragment);
	}

	@Override
	public T visit(final IElement element) {
		return traverseChildren(element);
	}

	protected final T traverseChildren(final IParent parent) {
		for (final INode child : parent.children()) {
			final T childResult = child.accept(this);

			if (childResult != null) {
				return childResult;
			}
		}
		return null;
	}
}
