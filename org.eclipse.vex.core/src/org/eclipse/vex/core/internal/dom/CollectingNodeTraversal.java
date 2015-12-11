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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

public class CollectingNodeTraversal<T> extends BaseNodeVisitorWithResult<T> {

	public CollectingNodeTraversal() {
		this(null);
	}

	public CollectingNodeTraversal(final T defaultValue) {
		super(defaultValue);
	}

	protected final Collection<T> traverseChildren(final IParent parent) {
		final ArrayList<T> result = new ArrayList<T>();
		for (final INode child : parent.children()) {
			final T childResult = child.accept(this);

			if (childResult != null) {
				result.add(childResult);
			}
		}
		return result;
	}
}
