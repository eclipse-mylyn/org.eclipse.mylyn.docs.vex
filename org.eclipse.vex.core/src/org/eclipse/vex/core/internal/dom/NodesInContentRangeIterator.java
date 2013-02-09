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

import java.util.Iterator;

import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.dom.ContentRange;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.core.FilterIterator;

/**
 * @author Florian Thienel
 */
public class NodesInContentRangeIterator extends FilterIterator<INode> {

	@SuppressWarnings("unchecked")
	public static <T extends INode> Iterator<T> iterator(final Iterable<T> nodes, final ContentRange contentRange) {
		return (Iterator<T>) new NodesInContentRangeIterator(nodes, contentRange);
	}

	private NodesInContentRangeIterator(final Iterable<? extends INode> nodes, final ContentRange contentRange) {
		super(nodes.iterator(), new IFilter<INode>() {
			public boolean matches(final INode node) {
				return !node.isAssociated() || contentRange.contains(node.getRange());
			}
		});
	}

}