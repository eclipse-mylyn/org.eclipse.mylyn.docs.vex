/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - fixed insertion of elements to nodes with text (bug 408731)
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * A Parent node is a Node which can contain other nodes as children. This class defines the tree-like structure of the
 * DOM. It handles the merging of the child nodes and the textual content of one node within the structure of the
 * document.
 * 
 * @author Florian Thienel
 */
public abstract class Parent extends Node implements IParent {

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
	 * Insert the given child node into the list of children at the given offset. The parent attribute of the child is
	 * set to this node.
	 * 
	 * @param offset
	 *            the offset within the associated content at which the new child should be inserted
	 * @param child
	 *            the child node to insert
	 */
	public void insertChildAt(final int offset, final Node child) {
		final int index = indexOfChildNextTo(offset);
		insertChildAtIndex(index, child);
	}

	private int indexOfChildNextTo(final int offset) {
		final ContentRange insertionRange = getRange().resizeBy(1, 0);
		Assert.isTrue(insertionRange.contains(offset), MessageFormat.format("The offset must be within {0}.", insertionRange));

		if (children.isEmpty()) {
			return 0;
		}

		int minIndex = -1;
		int maxIndex = children.size();

		while (minIndex <= maxIndex) {
			final int pivotIndex = (maxIndex + minIndex) / 2;
			final INode pivot = children.get(pivotIndex);

			if (pivot.containsOffset(offset)) {
				return pivotIndex;
			} else if (maxIndex - minIndex == 1) {
				return maxIndex;
			}

			if (pivot.getStartOffset() > offset) {
				maxIndex = Math.min(pivotIndex, maxIndex);
			} else if (pivot.getEndOffset() < offset) {
				minIndex = Math.max(pivotIndex, minIndex);
			}
		}

		throw new AssertionError("No child found at offset " + offset);
	}

	private void insertChildAtIndex(final int index, final Node child) {
		children.add(index, child);
		child.setParent(this);
	}

	/**
	 * Returns the child node which contains the given offset, or this node, if no child contains the offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the node at the given offset
	 */
	public INode getChildAt(final int offset) {
		Assert.isTrue(containsOffset(offset), MessageFormat.format("Offset must be within {0}.", getRange()));

		if (offset == getStartOffset() || offset == getEndOffset()) {
			return this;
		}

		if (children.isEmpty()) {
			return new Text(this, getContent(), getRange().resizeBy(1, -1));
		}

		int minIndex = -1;
		int maxIndex = children.size();

		while (minIndex <= maxIndex) {
			final int pivotIndex = (maxIndex + minIndex) / 2;
			final INode pivot = children.get(pivotIndex);

			if (pivot.containsOffset(offset)) {
				return getChildIn(pivot, offset);
			} else if (maxIndex - minIndex == 1) {
				return createTextBetween(minIndex, maxIndex);
			}

			if (pivot.getStartOffset() > offset) {
				maxIndex = Math.min(pivotIndex, maxIndex);
			} else if (pivot.getEndOffset() < offset) {
				minIndex = Math.max(pivotIndex, minIndex);
			}
		}

		throw new AssertionError("No child found at offset " + offset);
	}

	private INode getChildIn(final INode child, final int offset) {
		if (child instanceof IParent) {
			return ((IParent) child).getChildAt(offset);
		}
		return child;
	}

	private Text createTextBetween(final int childIndex1, final int childIndex2) {
		Assert.isTrue(childIndex1 < childIndex2);

		final int startOffset;
		if (childIndex1 < 0) {
			startOffset = getStartOffset() + 1;
		} else {
			startOffset = children.get(childIndex1).getEndOffset() + 1;
		}

		final int endOffset;
		if (childIndex2 >= children.size()) {
			endOffset = getEndOffset() - 1;
		} else {
			endOffset = children.get(childIndex2).getStartOffset() - 1;
		}

		return new Text(this, getContent(), new ContentRange(startOffset, endOffset));
	}

	/**
	 * Insert the given child into the list of children before the given node. The parent attribute of the child is set
	 * to this node.
	 * 
	 * @param beforeNode
	 *            the node before which the new child should be inserted
	 * @param child
	 *            the child node to insert
	 */
	public void insertChildBefore(final INode beforeNode, final Node child) {
		final int index = children.indexOf(beforeNode);
		Assert.isTrue(index > -1, MessageFormat.format("{0} must be a child of this parent.", beforeNode));
		insertChildAtIndex(index, child);
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
	 * @return the children axis of this parent.
	 * @see IAxis
	 */
	public IAxis<INode> children() {
		return new Axis<INode>(this) {
			@Override
			public Iterator<? extends INode> createRootIterator(final ContentRange contentRange, final boolean includeText) {
				if (includeText) {
					return new MergeNodesWithTextIterator(Parent.this, children, getContent(), contentRange);
				}
				return NodesInContentRangeIterator.iterator(children, contentRange);
			}
		};
	}

	/**
	 * Indicates whether this parent node has child nodes, including text nodes.
	 * 
	 * @return true if this parent node has any child nodes
	 */
	public boolean hasChildren() {
		return !children().isEmpty();
	}

}
