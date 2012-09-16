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
import java.util.Iterator;
import java.util.List;

/**
 * A Parent node is a Node which can contain other nodes as children. This class defines the tree-like structure of the
 * DOM. It handles the mergin of the child nodes and the textual content of one node within the structure of the
 * document.
 * 
 * @author Florian Thienel
 */
public abstract class Parent extends Node {

	private final List<Node> children = new ArrayList<Node>();

	/**
	 * Append the given child node to the end of the list of children. The parent attribute of the child is set to this
	 * node.
	 * 
	 * @param child
	 *            the new child node
	 */
	public void addChild(final Node child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Insert the given child node into the list of children at the given index. The children from the child with the
	 * given index until the last child will be moved forward by one position. The parent attribute of the child is set
	 * to this node.
	 * 
	 * @param index
	 *            the index at which the child should be inserted.
	 * @param child
	 *            the child node to insert
	 */
	public void insertChild(final int index, final Node child) {
		children.add(index, child);
		child.setParent(this);
	}

	/**
	 * Remove the given child node from the list of children. The parent attribute of the child will be set to null.
	 * 
	 * @param child
	 *            the child node to remove
	 */
	public void removeChild(final Node child) {
		children.remove(child);
		child.setParent(null);
	}

	/**
	 * Returns a list of all child nodes of this parent node, including Text nodes for the textual content.
	 * 
	 * @return all child nodes including Text nodes
	 */
	public List<Node> getChildNodes() {
		return getChildNodes(getStartOffset() + 1, getEndOffset() - 1);
	}

	/**
	 * Returns a list of all child nodes (including Text nodes) in the given range. The Text nodes are cut at the edges,
	 * all other nodes must be fully contained in the range (i.e. the start tag and the end tag). The returned list is
	 * not modifyable.
	 * 
	 * @param startOffset
	 *            the start offset of the range
	 * @param endOffset
	 *            the end offset of the range
	 * @return all child nodes which are completely within the given range plus the textual content
	 */
	public List<Node> getChildNodes(final int startOffset, final int endOffset) {
		final int rangeStart = Math.max(startOffset, getStartOffset() + 1);
		final int rangeEnd = Math.min(endOffset, getEndOffset());

		final List<Node> result = new ArrayList<Node>();
		int offset = rangeStart;
		for (final Node child : children) {
			if (child.isAssociated()) {
				final int childStart = child.getStartOffset();
				final int childEnd = child.getEndOffset();
				if (offset < childStart) {
					final int textEnd = Math.min(childStart, rangeEnd);
					result.add(new Text(this, getContent(), offset, textEnd));
					offset = textEnd + 1;
				}
				if (childStart >= rangeStart && childStart <= rangeEnd && childEnd <= rangeEnd) {
					result.add(child);
					offset = childEnd + 1;
				} else if (childEnd >= rangeStart) {
					offset = childEnd + 1;
				}
			} else {
				result.add(child);
			}
		}

		if (offset < rangeEnd) {
			result.add(new Text(this, getContent(), offset, rangeEnd));
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * An Iterator of all child nodes. The underlying collection is not modifyable.
	 * 
	 * @see Parent#getChildNodes()
	 * @see Iterator
	 * @return an Iterator of all child nodes
	 */
	public Iterator<Node> getChildIterator() {
		return getChildNodes().iterator();
	}

	/**
	 * Returns the child node at the given index. This index is based on the list of children without the Text nodes, so
	 * the child node might have a different index in the list returned by getChildNodes().
	 * 
	 * @see Parent#getChildNodes()
	 * @return the child node at the given index
	 */
	public Node getChildNode(final int index) {
		return children.get(index);
	}

	/**
	 * Returns the number of child nodes (<b>not</b> including Text nodes) of this parent node.
	 * 
	 * @return the number of child nodes
	 */
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Indicates if this parent node has child nodes. Text nodes are ignored, i.e. this method will return false if this
	 * parent node contains only text.
	 * 
	 * @return true if this parent node has child nodes
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
