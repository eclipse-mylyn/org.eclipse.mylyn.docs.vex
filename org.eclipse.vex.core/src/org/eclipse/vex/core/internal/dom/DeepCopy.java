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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class creates a deep copy of a single Node or the child nodes of a Parent within a given Range. The copy is made
 * instantly when the constructor of DeepCopy is called.
 * <p>
 * DeepCopy means a full copy of all nodes, their children down to the leaf level and the associated content.
 * 
 * @author Florian Thienel
 */
public class DeepCopy {

	private final Content content;
	private final List<Node> nodes;

	/**
	 * Creates a deep copy of the given node.
	 * 
	 * @param node
	 *            the node to copy
	 */
	public DeepCopy(final Node node) {
		final int delta;
		if (node.isAssociated()) {
			final ContentRange range = node.getRange();
			delta = -range.getStartOffset();
			content = node.getContent().getContent(range);
		} else {
			delta = 0;
			content = null;
		}
		nodes = new ArrayList<Node>();

		copyNodes(Collections.singletonList(node), delta);
	}

	/**
	 * Creates a deep copy of the child nodes of the given parent within the given range.
	 * 
	 * @param parent
	 *            the parent
	 * @param range
	 *            the range to copy
	 */
	public DeepCopy(final Parent parent, final ContentRange range) {
		final int delta;
		if (parent.isAssociated()) {
			delta = -range.getStartOffset();
			content = parent.getContent().getContent(range);
		} else {
			delta = 0;
			content = null;
		}
		nodes = new ArrayList<Node>();

		copyNodes(parent.children().in(range), delta);
	}

	private void copyNodes(final Iterable<Node> sourceNodes, final int delta) {
		final DeepCopyVisitor deepCopyVisitor = new DeepCopyVisitor(nodes, content, delta);
		for (final Node sourceNode : sourceNodes) {
			sourceNode.accept(deepCopyVisitor);
		}
	}

	/**
	 * @return the copied nodes on the root level.
	 */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * @return the copied content.
	 */
	public Content getContent() {
		return content;
	}

}
