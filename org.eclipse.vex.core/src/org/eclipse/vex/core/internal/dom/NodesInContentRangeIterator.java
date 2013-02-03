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

import org.eclipse.vex.core.internal.core.FilterIterator;
import org.eclipse.vex.core.internal.core.IFilter;

/**
 * @author Florian Thienel
 */
public class NodesInContentRangeIterator extends FilterIterator<Node> {

	public NodesInContentRangeIterator(final Iterable<Node> nodes, final ContentRange contentRange) {
		super(nodes.iterator(), new IFilter<Node>() {
			public boolean matches(final Node node) {
				return !node.isAssociated() || contentRange.contains(node.getRange());
			}
		});
	}

}