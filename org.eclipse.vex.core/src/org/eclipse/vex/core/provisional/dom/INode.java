/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * A representation of one node in the XML structure. A node is associated to a range of the textual content.
 * <p>
 * This is the base for all representatives of the XML structure in the document object model (DOM).
 *
 * @author Florian Thienel
 */
public interface INode {

	/**
	 * @see IParent
	 * @return the parent of this node, maybe null if this node has no parent
	 */
	IParent getParent();

	/**
	 * The ancestors axis contains all subsequent parents of this node.
	 *
	 * @return the ancestors axis of this node
	 * @see IAxis
	 */
	IAxis<IParent> ancestors();

	/**
	 * @return if this node is associated to content
	 */
	boolean isAssociated();

	/**
	 * @return the content to which this node is associated or null if this node is not associated to any content yet
	 */
	IContent getContent();

	/**
	 * The start offset of this node, which eventually also includes the position of a tag marker.
	 *
	 * @return the start offset of this node within the textual content
	 */
	int getStartOffset();

	/**
	 * The start position of this node, which eventually also includes the position of a tag marker.
	 *
	 * @return the start position of this node within the textual content
	 */
	ContentPosition getStartPosition();

	/**
	 * The end offset of this node, which eventually also includes the position of a tag marker.
	 *
	 * @return the end offset of this node within the textual content
	 */
	int getEndOffset();

	/**
	 * The end position of this node, which eventually also includes the position of a tag marker.
	 *
	 * @return the end position of this node within the textual content
	 */
	ContentPosition getEndPosition();

	/**
	 * @return the in the content to which this node is associated, eventually including tag markers
	 */
	ContentRange getRange();

	/**
	 * @return the {@link ContentPositionRange} to which this node is associated, eventually including tag markers
	 */
	ContentPositionRange getPositionRange();

	/**
	 * Indicate whether this node has no content beside its tag markers. If this node is not associated with textual
	 * content, this method returns false.
	 *
	 * @return true if this node has no content beside its tag markers
	 */
	boolean isEmpty();

	/**
	 * Indicate whether the given offset is within the boundaries of this node. If this node is not associated with
	 * textual content, this method returns false.
	 *
	 * @param offset
	 *            the offset
	 * @return true if the given offset is withing [startOffset; endOffset], or false if not associated
	 */
	boolean containsOffset(int offset);

	/**
	 * Indicate whether the given position is within the boundaries of this node. If this node is not associated with
	 * textual content, this method returns false.
	 *
	 * @param position
	 *            the position
	 * @return true if the given position is withing [startPosition; endPosition], or false if not associated
	 */
	boolean containsPosition(ContentPosition position);

	/**
	 * Indicate whether this node is fully within the given range. If this node is not associated with textual content,
	 * this method returns false.
	 *
	 * @param range
	 *            the range
	 * @return true if this node is fully within the given range
	 */
	boolean isInRange(ContentRange range);

	/**
	 * @return the textual content of this node, not including any tag markers
	 */
	String getText();

	/**
	 * The textual content in the given range, not including any element markers.
	 *
	 * @param range
	 *            the range of the textual content
	 * @return the textual content in the given range
	 */
	String getText(ContentRange range);

	/**
	 * @return the document to which this node belongs, or null if this node does not belong to any document
	 */
	IDocument getDocument();

	/**
	 * Indicate whether this and the given node are of the same kind (e.g. elements with the same qualified name).
	 *
	 * @return true if this and the given node are of the same kind
	 */
	boolean isKindOf(INode node);

	/**
	 * The xml:base attribute re-defines the base URI for a part of an XML document, according to the XML Base
	 * Recommendation.
	 *
	 * @see http://www.w3.org/TR/xmlbase/
	 * @return the base URI of this node
	 */
	String getBaseURI();

	/**
	 * Accept the given visitor.
	 *
	 * @see INodeVisitor
	 * @param visitor
	 *            the visitor
	 */
	void accept(INodeVisitor visitor);

	/**
	 * Accept the given visitor.
	 *
	 * @see INodeVisitorWithResult
	 * @param visitor
	 *            the visitor
	 */
	<T> T accept(INodeVisitorWithResult<T> visitor);

}