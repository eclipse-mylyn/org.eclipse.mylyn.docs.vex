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

import org.eclipse.core.runtime.Assert;

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
		return getChildNodes(getStartOffset(), getEndOffset());
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
		final int rangeStart = Math.max(startOffset, getStartOffset());
		final int rangeEnd = Math.min(endOffset, getEndOffset());
		int textStart = rangeStart;
		final List<Node> result = new ArrayList<Node>();
		for (final Node child : children) {
			if (!child.isAssociated()) {
				result.add(child);
			} else if (child.isInRange(rangeStart, rangeEnd)) {
				mergeTextIntoResult(textStart, child.getStartOffset(), result);
				result.add(child);
				textStart = child.getEndOffset() + 1;
			}
		}

		mergeTextIntoResult(textStart, rangeEnd, result);
		return Collections.unmodifiableList(result);
	}

	private void mergeTextIntoResult(final int startOffset, final int endOffset, final List<Node> result) {
		final int textStart = findNextTextStart(startOffset, endOffset);
		final int textEnd = findNextTextEnd(endOffset, textStart);
		if (textStart < textEnd) {
			result.add(new Text(this, getContent(), textStart, textEnd));
		} else if (textStart == textEnd && !getContent().isElementMarker(textStart)) {
			result.add(new Text(this, getContent(), textStart, textEnd));
		}
	}

	private int findNextTextStart(int currentOffset, final int maximumOffset) {
		while (currentOffset < maximumOffset && getContent().isElementMarker(currentOffset)) {
			currentOffset++;
		}
		return currentOffset;
	}

	private int findNextTextEnd(int currentOffset, final int minimumOffset) {
		while (currentOffset > minimumOffset && getContent().isElementMarker(currentOffset)) {
			currentOffset--;
		}
		return currentOffset;
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
	 * Returns the node at the given offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the node at the given offset
	 */
	public Node getChildNodeAt(final int offset) {
		Assert.isTrue(containsOffset(offset));
		final List<Node> childNodes = getChildNodes();
		for (final Node child : childNodes) {
			if (child.containsOffset(offset)) {
				if (child instanceof Parent) {
					return ((Parent) child).getChildNodeAt(offset);
				} else {
					return child;
				}
			}
		}
		return this;
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
