/*******************************************************************************
 * Copyright (c) 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * A ContentPosition wraps an offset in a document's content. The position knows the document and the node it is in.
 * <br />
 *
 */
public class ContentPosition implements Comparable<ContentPosition> {
	private final int offset;
	private INode nodeAtOffset;
	private final IDocument parentDocument;

	/**
	 * Create a new position at the given document offset.
	 *
	 * @param node
	 *            The node at the given offset
	 * @param offset
	 *            The offset in the document's content
	 */
	public ContentPosition(final INode node, final int offset) {
		if (node == null) {
			throw new AssertionFailedException("Node must not be null");
		}

		this.offset = offset;
		nodeAtOffset = node instanceof IText ? node.getParent() : node;
		if (offset < node.getStartOffset()) {
			throw new AssertionFailedException("Offset of content position has to be inside the node.");
		}

		parentDocument = nodeAtOffset.getDocument();
	}

	/**
	 * Create a new position at the given offset in the given document.<br />
	 * This method has to search the document to find the node at the given offset, so when the node is known, it is
	 * more efficient to use {@link ContentPosition#ContentPosition(INode, int)} instead.
	 *
	 * @param document
	 * @param offset
	 */
	public ContentPosition(final IDocument document, final int offset) {
		this(document.getChildAt(offset), offset);
	}

	private ContentPosition(final ContentPosition other) {
		offset = other.offset;
		nodeAtOffset = other.nodeAtOffset;
		parentDocument = nodeAtOffset.getDocument();
		if (nodeAtOffset == null) {
			throw new AssertionFailedException("Node must not be null");
		}
	}

	/**
	 * @return A copy of this position.
	 */
	public ContentPosition copy() {
		return new ContentPosition(this);
	}

	/**
	 * @return The INode at this position.
	 */
	public INode getNodeAtOffset() {
		if (!nodeAtOffset.isAssociated()) {
			// Node is not associated - it has probably been deletet
			nodeAtOffset = parentDocument.getChildAt(offset);
		}
		return nodeAtOffset;
	}

	/**
	 * @return The INode in which an insertion at this position will end.
	 */
	public INode getNodeForInsertion() {
		final INode node = getNodeAtOffset();
		if (offset == node.getStartOffset()) {
			return node.getParent();
		} else {
			return node;
		}
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 * @return A new Position that is moved by the offset.
	 */
	public ContentPosition moveBy(final int offset) {

		int newOffset = this.offset + offset;

		if (offset < 0) {
			final int contentStart = getNodeAtOffset().getContent().getRange().getStartOffset() + 1;
			if (newOffset < contentStart) {
				// New position would be before content
				newOffset = contentStart;
			}
		} else if (offset > 0) {
			final int contentEnd = getNodeAtOffset().getContent().getRange().getEndOffset();
			if (newOffset > contentEnd) {
				// New position would be before content
				newOffset = contentEnd;
			}
		}

		INode newNode = getNodeAtOffset().getDocument().getChildAt(newOffset);
		if (newNode instanceof IText) {
			newNode = newNode.getParent();
		}

		final ContentPosition moved = new ContentPosition(newNode, newOffset);
		if (newNode instanceof IIncludeNode) {
			// TODO: There's currently no support for editing the content of an include, so we skip it here.
			if (offset > 0 && moved.isAfter(newNode.getStartPosition())) {
				return newNode.getEndPosition();
			}
			if (offset < 0 && moved.isBefore(newNode.getEndPosition())) {
				return newNode.getStartPosition();
			}
		}
		return moved;
	}

	/**
	 * @return The previous sibling or the parent of the node at this position
	 */
	public ContentPosition moveToPreviousNode() {
		final IParent parent = nodeAtOffset.getParent();
		if (parent == null) {
			// No parent, so we have no previous node
			return nodeAtOffset.getStartPosition();
		}

		final IAxis<? extends INode> siblings = parent.children().before(nodeAtOffset.getStartOffset());
		if (!siblings.isEmpty()) {
			return siblings.last().getEndPosition();
		} else {
			return parent.getStartPosition();
		}
	}

	/**
	 * @return A new ContentPosition at the start of the next sibling or the parent of the node at this position
	 */
	public ContentPosition moveToNextNode() {
		final IParent parent = nodeAtOffset.getParent();
		if (parent == null) {
			// No parent, so return the end of the current node
			return nodeAtOffset.getEndPosition();
		}
		final IAxis<? extends INode> siblings = parent.children().after(offset);
		if (!siblings.isEmpty()) {
			return siblings.first().getStartPosition();
		} else {
			return parent.getEndPosition();
		}
	}

	/**
	 * @param other
	 * @return <code>true</code> if the content offset of this position is before the other position.
	 */
	public boolean isBefore(final ContentPosition other) {
		return offset < other.getOffset();
	}

	/**
	 * @param other
	 * @return <code>true</code> if the content offset of this position is before or equals the other position.
	 */
	public boolean isBeforeOrEquals(final ContentPosition other) {
		return offset <= other.getOffset();
	}

	/**
	 * @param other
	 * @return <code>true</code> if the content offset of this position is after the other position.
	 */
	public boolean isAfter(final ContentPosition other) {
		return offset > other.getOffset();
	}

	/**
	 * @param other
	 * @return <code>true</code> if the content offset of this position is after or equals the other position.
	 */
	public boolean isAfterOrEquals(final ContentPosition other) {
		return offset >= other.getOffset();
	}

	@Override
	public int compareTo(final ContentPosition other) {
		return offset - other.getOffset();
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof ContentPosition) {
			return offset == ((ContentPosition) other).getOffset() && nodeAtOffset == ((ContentPosition) other).nodeAtOffset;
		}
		return false;
	}

	public static ContentPosition smallest(final ContentPosition p1, final ContentPosition p2) {
		return p1.isBefore(p2) ? p1 : p2;
	}

	public static ContentPosition greatest(final ContentPosition p1, final ContentPosition p2) {
		return p1.isAfter(p2) ? p1 : p2;
	}

	/**
	 * Returns the first ContentPosition after the given one that inserts into the given node.
	 *
	 * @param position
	 * @param balancedNode
	 * @return
	 */
	public static ContentPosition balanceForward(final ContentPosition position, final INode balancedNode) {

		if (position.getNodeForInsertion() == balancedNode) {
			return position;
		}

		// Move the position to the start of the next node, this will insert in the parent
		ContentPosition balancedPosition = position.moveToNextNode();
		while (balancedPosition.getNodeForInsertion() != balancedNode) {
			balancedPosition = balancedPosition.getNodeAtOffset().getParent().getEndPosition();
		}
		return balancedPosition;
	}

	/**
	 * Returns the first ContentPosition before the given one that inserts into the given node.
	 *
	 * @param position
	 * @param balancedNode
	 * @return
	 */
	public static ContentPosition balanceBackward(final ContentPosition position, final INode balancedNode) {

		if (position.getNodeForInsertion() == balancedNode) {
			return position;
		}

		// Insertion at the start position of a node inserts into the parent
		ContentPosition balancedPosition = position.getNodeAtOffset().getStartPosition();
		while (balancedPosition.getNodeAtOffset().getParent() != balancedNode) {
			balancedPosition = balancedPosition.getNodeAtOffset().getParent().getStartPosition();
		}
		return balancedPosition;
	}

	@Override
	public String toString() {
		return "ContentPosition at " + offset + " in node " + nodeAtOffset;
	}
}
