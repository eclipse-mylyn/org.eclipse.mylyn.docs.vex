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
 * @author Florian Thienel
 */
public class NodeTraversal {

	private final INodeVisitor nodeVisitor;
	private final TraversalVisitor traversalVisitor;

	public NodeTraversal(final INodeVisitor nodeVisitor) {
		this.nodeVisitor = nodeVisitor;
		traversalVisitor = new TraversalVisitor();
	}

	public void traverse(final Node node) {
		node.accept(traversalVisitor);
	}

	private class TraversalVisitor implements INodeVisitor {

		public void visit(final Document document) {
			visitParent(document);
		}

		public void visit(final DocumentFragment fragment) {
			visitParent(fragment);
		}

		public void visit(final Element element) {
			visitParent(element);
		}

		private void visitParent(final Parent parent) {
			parent.accept(nodeVisitor);
			for (final Node child : parent.getChildNodes()) {
				child.accept(traversalVisitor);
			}
		}

		public void visit(final Text text) {
			text.accept(nodeVisitor);
		}

	}

}
