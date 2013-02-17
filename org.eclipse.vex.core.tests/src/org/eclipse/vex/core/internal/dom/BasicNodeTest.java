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

import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;

/**
 * @author Florian Thienel
 */
public class BasicNodeTest extends NodeTest {

	@Override
	protected Node createNode() {
		return new Node() {
			public void accept(final INodeVisitor visitor) {
				throw new UnsupportedOperationException();
			}

			public <T> T accept(final INodeVisitorWithResult<T> visitor) {
				throw new UnsupportedOperationException();
			}

			public boolean isKindOf(final INode node) {
				return false;
			}
		};
	}

}
