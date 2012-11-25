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
 * This class provides default implementations for the methods defined by the <code>INodeVisitor</code> interface.
 * 
 * @see INodeVisitor
 * @author Florian Thienel
 */
public class BaseNodeVisitor implements INodeVisitor {

	public void visit(final Document document) {
		// ignore
	}

	public void visit(final DocumentFragment fragment) {
		// ignore
	}

	public void visit(final Element element) {
		// ignore
	}

	public void visit(final Text text) {
		// ignore
	}

	public void visit(final Comment comment) {
		// ignore
	}
}
