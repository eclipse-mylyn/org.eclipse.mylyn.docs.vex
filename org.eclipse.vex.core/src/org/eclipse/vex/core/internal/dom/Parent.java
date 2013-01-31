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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;

/**
 * A Parent node is a Node which can contain other nodes as children. This class defines the tree-like structure of the
 * DOM. It handles the merging of the child nodes and the textual content of one node within the structure of the
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
	 * @return the child node of this parent following the given offset
	 */
	public int getIndexOfChildNextTo(final int offset) {
		final ContentRange insertionRange = getRange().resizeBy(1, 0);
		Assert.isTrue(insertionRange.contains(offset), MessageFormat.format("The offset must be within {0}.", insertionRange));
		int i = 0;
		for (final Node child : children()) {
			if (offset <= child.getStartOffset()) {
				return i;
			}
			i++;
		}
		return children.size();
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
	 * @see Axis
	 * @return the iterable children Axis of this parent.
	 */
	public Axis children() {
		return new Axis(this) {
			@Override
			public Iterator<Node> iterator(final ContentRange range, final boolean includeText) {
				return new ChildrenAndText(range, includeText);
			}
		};
	}

	/**
	 * Returns the child node which contains the given offset, or this node, if no child contains the offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the node at the given offset
	 */
	public Node getChildNodeAt(final int offset) {
		Assert.isTrue(containsOffset(offset), MessageFormat.format("Offset must be within {0}.", getRange()));
		for (final Node child : children()) {
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
	 * Indicates whether this parent node has child nodes, including text nodes.
	 * 
	 * @return true if this parent node has any child nodes
	 */
	public boolean hasChildren() {
		return children().iterator().hasNext();
	}

	private class ChildrenAndText implements Iterator<Node> {

		private final ContentRange trimmedRange;
		private final boolean includeText;
		private final Iterator<Node> childIterator;

		private int textCursor;
		private Node currentChild;
		private ContentRange nextTextGap;

		public ChildrenAndText(final ContentRange range, final boolean includeText) {
			this.includeText = includeText;
			trimmedRange = range.intersection(getRange());
			childIterator = children.iterator();
			initialize();
		}

		private void initialize() {
			currentChild = null;
			nextTextGap = trimmedRange;
			textCursor = trimmedRange.getStartOffset();
			nextStep();
		}

		private void nextStep() {
			while (childIterator.hasNext()) {
				currentChild = childIterator.next();
				if (!currentChild.isAssociated()) {
					nextTextGap = trimmedRange;
					return;
				} else if (currentChild.isInRange(trimmedRange)) {
					nextTextGap = currentChild.getRange();
					textCursor = findNextTextStart(textCursor, nextTextGap.getStartOffset());
					return;
				} else if (trimmedRange.contains(currentChild.getStartOffset())) {
					nextTextGap = trimmedRange.intersection(currentChild.getRange());
					textCursor = findNextTextStart(textCursor, nextTextGap.getStartOffset());
					currentChild = null; // we can bail out here because we are behind the trimmed range now
					return;
				} else if (trimmedRange.contains(currentChild.getEndOffset())) {
					textCursor = currentChild.getEndOffset() + 1;
				}
			}

			currentChild = null;
			nextTextGap = new ContentRange(trimmedRange.getEndOffset(), trimmedRange.getEndOffset());
			textCursor = findNextTextStart(textCursor, trimmedRange.getEndOffset());
		}

		private int findNextTextStart(int currentOffset, final int maximumOffset) {
			while (currentOffset < maximumOffset && getContent().isTagMarker(currentOffset)) {
				currentOffset++;
			}
			return currentOffset;
		}

		private int findNextTextEnd(int currentOffset, final int minimumOffset) {
			while (currentOffset > minimumOffset && getContent().isTagMarker(currentOffset)) {
				currentOffset--;
			}
			return currentOffset;
		}

		public boolean hasNext() {
			return hasMoreChildrenInRange() || hasMoreText();
		}

		private boolean hasMoreChildrenInRange() {
			return currentChild != null;
		}

		private boolean hasMoreText() {
			return includeText && textCursor < nextTextGap.getStartOffset();
		}

		public Node next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			if (currentChild != null && !currentChild.isAssociated()) {
				return nextChild();
			}

			if (includeText) {
				final int textStart = findNextTextStart(textCursor, nextTextGap.getStartOffset());
				final int textEnd = findNextTextEnd(nextTextGap.getStartOffset(), textStart);
				textCursor = nextTextGap.getEndOffset() + 1;

				if (textStart < textEnd) {
					return nextText(textStart, textEnd);
				}
				if (textStart == textEnd && !getContent().isTagMarker(textStart)) {
					return nextText(textStart, textEnd);
				}
			}

			Assert.isNotNull(currentChild, "No text and no child makes Homer go crazy!");

			return nextChild();
		}

		private Node nextChild() {
			final Node child = currentChild;
			nextStep();
			return child;
		}

		private Node nextText(final int textStart, final int textEnd) {
			return new Text(Parent.this, getContent(), new ContentRange(textStart, textEnd));
		}

		public void remove() {
			throw new UnsupportedOperationException("Cannot remove children.");
		}
	}

}
