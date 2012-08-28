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

/**
 * This class represents one node within the XML structure which is associated to a region of the textual content. It is
 * the base class for all representatives of the XML structure in the document object model (DOM).
 */
public abstract class Node {

	private Parent parent;
	private Content content;
	private Position startPosition = Position.NULL;
	private Position endPosition = Position.NULL;

	public Parent getParent() {
		return parent;
	}

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
		this.content = content;
		startPosition = content.createPosition(startOffset);
		endPosition = content.createPosition(endOffset);
	}

	/**
	 * Dissociates this node from its associated content region.
	 */
	public void dissocate() {
		content.removePosition(startPosition);
		content.removePosition(endPosition);
		startPosition = Position.NULL;
		endPosition = Position.NULL;
		content = null;
	}

	public Content getContent() {
		return content;
	}

	public int getEndOffset() {
		return endPosition.getOffset();
	}

	public int getStartOffset() {
		return startPosition.getOffset();
	}

	public String getText() {
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
}
