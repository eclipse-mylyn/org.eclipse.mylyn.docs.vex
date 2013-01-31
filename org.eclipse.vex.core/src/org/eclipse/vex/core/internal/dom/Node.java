/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;

/**
 * A representation of one node in the XML structure. A node is associated to a range of the textual content.
 * <p>
 * This is the base class for all representatives of the XML structure in the document object model (DOM).
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
	 * Associate this node to a range within the given content.
	 * 
	 * @param content
	 *            Content object holding the node's content
	 * @param range
	 *            the range of this node's content
	 */
	public void associate(final Content content, final ContentRange range) {
		if (isAssociated()) {
			dissociate();
		}

		this.content = content;
		startPosition = content.createPosition(range.getStartOffset());
		endPosition = content.createPosition(range.getEndOffset());
	}

	/**
	 * Dissociates this node from its associated content range.
	 */
	public void dissociate() {
		Assert.isTrue(isAssociated(), "This node must be associated to a ContentRange before it can be dissociated.");

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
	 * The start offset of this node, which eventually also includes the position of a tag marker.
	 * 
	 * @return the start offset of this node within the textual content
	 */
	public int getStartOffset() {
		Assert.isTrue(isAssociated(), "Node must be associated to a ContentRange to have a start offset.");
		return startPosition.getOffset();
	}

	/**
	 * The end offset of this node, which eventually also includes the position of a tag marker.
	 * 
	 * @return the end offset of this node within the textual content
	 */
	public int getEndOffset() {
		Assert.isTrue(isAssociated(), "Node must be associated to a ContentRange to have an end offset.");
		return endPosition.getOffset();
	}

	/**
	 * @return the range in the content to which this node is associated, eventually including tag markers
	 */
	public ContentRange getRange() {
		if (!isAssociated()) {
			return ContentRange.NULL;
		}
		return new ContentRange(getStartOffset(), getEndOffset());
	}

	/**
	 * Indicate whether this node has no content beside its tag markers. If this node is not associated with textual
	 * content, this method returns false.
	 * 
	 * @return true if this node has no content beside its tag markers
	 */
	public boolean isEmpty() {
		if (!isAssociated()) {
			return false;
		}
		return getEndOffset() - getStartOffset() == 1;
	}

	/**
	 * Indicate whether the given offset is within the boundaries of this node. If this node is not associated with
	 * textual content, this method returns false.
	 * 
	 * @param offset
	 *            the offset
	 * @return true if the given offset is withing [startOffset; endOffset], or false if not associated
	 */
	public boolean containsOffset(final int offset) {
		if (!isAssociated()) {
			return false;
		}
		return getRange().contains(offset);
	}

	/**
	 * Indicate whether this node is fully within the given range. If this node is not associated with textual content,
	 * this method returns false.
	 * 
	 * @param range
	 *            the range
	 * @return true if this node is fully within the given range
	 */
	public boolean isInRange(final ContentRange range) {
		if (!isAssociated()) {
			return false;
		}
		return range.contains(getRange());
	}

	/**
	 * @return the textual content of this node, not including any tag markers
	 */
	public String getText() {
		return getText(getRange());
	}

	/**
	 * The textual content in the given range, not including any element markers.
	 * 
	 * @param range
	 *            the range of the textual content
	 * @return the textual content in the given range
	 */
	public String getText(final ContentRange range) {
		Assert.isTrue(isAssociated(), "Node must be associated to a Content region to have textual content.");
		return content.getText(range.intersection(getRange()));
	}

	/**
	 * @return the document to which this node belongs, or null if this node does not belong to any document
	 */
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

	/**
	 * Indicate whether this and the given node are of the same kind (e.g. elements with the same qualified name).
	 * 
	 * @return true if this and the given node are of the same kind
	 */
	public abstract boolean isKindOf(final Node node);

	/**
	 * @see Element#setBaseURI(String)
	 * @return the base URI of this node
	 */
	public String getBaseURI() {
		if (getParent() != null) {
			return getParent().getBaseURI();
		}
		if (getDocument() != null) {
			return getDocument().getBaseURI();
		}
		return null;
	}

	/**
	 * Accept the given visitor.
	 * 
	 * @see INodeVisitor
	 * @param visitor
	 *            the visitor
	 */
	public abstract void accept(final INodeVisitor visitor);

	/**
	 * Accept the given visitor.
	 * 
	 * @see INodeVisitorWithResult
	 * @param visitor
	 *            the visitor
	 */
	public abstract <T> T accept(final INodeVisitorWithResult<T> visitor);

	/**
	 * @return the qualified names of the given nodes
	 */
	public static List<QualifiedName> getNodeNames(final Iterable<Node> nodes) {
		final List<QualifiedName> names = new ArrayList<QualifiedName>();

		for (final Node node : nodes) {
			node.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Text text) {
					names.add(Validator.PCDATA);
				}

				@Override
				public void visit(final Element element) {
					names.add(element.getQualifiedName());
				}
			});
		}

		return names;
	}

}
