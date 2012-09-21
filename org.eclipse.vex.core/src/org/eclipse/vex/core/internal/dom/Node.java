/*******************************************************************************
 * Copyright (c) 2004, 2012 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.core.runtime.Assert;

/**
 * This class represents one node within the XML structure, which is also associated to a region of the textual content.
 * It is the base class for all representatives of the XML structure in the document object model (DOM).
 */
public abstract class Node {

	private Parent parent;
	private Content content;
	private Position startPosition = Position.NULL;
	private Position endPosition = Position.NULL;

	/**
	 * @see Parent
	 * @return the parent of this node, maybe null if this node has no parent
	 */
	public Parent getParent() {
		return parent;
	}

	/**
	 * @see Parent
	 * @param parent
	 *            the parent of this node, maybe null if this node should not have a parent
	 */
	public void setParent(final Parent parent) {
		this.parent = parent;
	}

	/**
	 * Associates this node to a region within the given content.
	 * 
	 * @param content
	 *            Content object holding the node's content
	 * @param startOffset
	 *            offset at which the node's content starts
	 * @param endOffset
	 *            offset at which the node's content ends
	 */
	public void associate(final Content content, final int startOffset, final int endOffset) {
		if (isAssociated()) {
			dissociate();
		}

		this.content = content;
		startPosition = content.createPosition(startOffset);
		endPosition = content.createPosition(endOffset);
	}

	/**
	 * Dissociates this node from its associated content region.
	 */
	public void dissociate() {
		Assert.isTrue(isAssociated(), "Node must be associated to a Content region before it can be dissociated.");

		content.removePosition(startPosition);
		content.removePosition(endPosition);
		startPosition = Position.NULL;
		endPosition = Position.NULL;
		content = null;
	}

	/**
	 * @return if this node is associated to content
	 */
	public boolean isAssociated() {
		return content != null;
	}

	/**
	 * @return the content to which this node is associated or null if this node is not associated to any content yet
	 */
	public Content getContent() {
		return content;
	}

	/**
	 * The start offset of this node, which eventually also includes the position of an element marker.
	 * 
	 * @return the start offset of this node within the textual content
	 */
	public int getStartOffset() {
		Assert.isTrue(isAssociated(), "Node must be associated to a Content region to have a start offset.");
		return startPosition.getOffset();
	}

	/**
	 * The end offset of this node, which eventually also includes the position of an element marker.
	 * 
	 * @return the end offset of this node within the textual content
	 */
	public int getEndOffset() {
		Assert.isTrue(isAssociated(), "Node must be associated to a Content region to have an end offset.");
		return endPosition.getOffset();
	}

	/**
	 * Indicates if the given offset is within the boundaries of this node. A node contains its end offset, but not its
	 * start offset. If this node is not associated with textual content, this method returns false.
	 * 
	 * @return true if the given offset is withing ]startOffset; endOffset], or false if not associated
	 */
	public boolean containsOffset(final int offset) {
		if (!isAssociated()) {
			return false;
		}
		return offset > getStartOffset() && offset <= getEndOffset();
	}

	/**
	 * The textual content, not inluding any element markers.
	 * 
	 * @return the textual content of this node
	 */
	public String getText() {
		return getText(getStartOffset(), getEndOffset());
	}

	/**
	 * The textual content in the given range, not including any element markers.
	 * 
	 * @param startOffset
	 *            the start offset
	 * @param endOffset
	 *            the end offset
	 * @return the textual content in the given range
	 */
	public String getText(final int startOffset, final int endOffset) {
		Assert.isTrue(isAssociated(), "Node must be associated to a Content region to have textual content.");
		Assert.isTrue(startOffset >= getStartOffset(), "Invalid range start.");
		Assert.isTrue(containsOffset(endOffset), "Invalid range end.");

		return content.getText(startOffset, endOffset - startOffset + 1);
	}

	public Document getDocument() {
		return getDocument(this);
	}

	private static Document getDocument(final Node node) {
		if (node instanceof Document) {
			return (Document) node;
		}
		final Parent parent = node.getParent();
		if (parent == null) {
			return null;
		}
		if (parent instanceof Document) {
			return (Document) parent;
		}
		return getDocument(parent);
	}

	public abstract String getBaseURI();

	/**
	 * Accept the given visitor.
	 * 
	 * @see INodeVisitor
	 * @param visitor
	 *            the visitor
	 */
	public abstract void accept(final INodeVisitor visitor);
}
