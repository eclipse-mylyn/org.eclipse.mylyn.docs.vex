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
		if (this.content != null) {
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
		Assert.isNotNull(content, "Node must be associated to a Content region before it can be dissociated.");

		content.removePosition(startPosition);
		content.removePosition(endPosition);
		startPosition = Position.NULL;
		endPosition = Position.NULL;
		content = null;
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
		Assert.isNotNull(content, "Node must be associated to a Content region to have an start offset.");
		return startPosition.getOffset();
	}

	/**
	 * The end offset of this node, which eventually also includes the position of an element marker.
	 * 
	 * @return the end offset of this node within the textual content
	 */
	public int getEndOffset() {
		Assert.isNotNull(content, "Node must be associated to a Content region to have an end offset.");
		return endPosition.getOffset();
	}

	/**
	 * The textual content, which does not inlude any element markers.
	 * 
	 * @return the textual content of this node
	 */
	public String getText() {
		Assert.isNotNull(content, "Node must be associated to a Content region to have textual content.");
		return content.getText(getStartOffset(), getEndOffset() - getStartOffset());
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
