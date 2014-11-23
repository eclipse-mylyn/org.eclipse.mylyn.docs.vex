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

import java.util.List;

import org.eclipse.core.runtime.QualifiedName;

/**
 * A representation of an XML document in the DOM.
 *
 * @author Florian Thienel
 */
public interface IDocument extends IParent {

	/**
	 * @return the base URI of this document
	 * @see INode#getBaseURI()
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	String getDocumentURI();

	/**
	 * Set the root base URI of this document, which is used to resolve resources with relative location information.
	 *
	 * @param documentURI
	 *            the base URI of this document
	 * @see INode#getBaseURI()
	 * @see http://www.w3.org/TR/xmlbase/
	 */
	void setDocumentURI(String documentURI);

	/**
	 * @return the encoding of this document
	 */
	String getEncoding();

	/**
	 * Set the encoding of this document.
	 *
	 * @param encoding
	 *            the encoding of this document
	 */
	void setEncoding(String encoding);

	/**
	 * @return the public identifier of this document
	 */
	String getPublicID();

	/**
	 * Set the public identifier of this document
	 *
	 * @param publicID
	 *            the public identifier of this document
	 */
	void setPublicID(String publicID);

	/**
	 * @return the system identifier of this document
	 */
	String getSystemID();

	/**
	 * Set the system identifier of this document.
	 *
	 * @param systemID
	 *            the system identifier of this document
	 */
	void setSystemID(String systemID);

	/**
	 * @return the validator of this document
	 */
	IValidator getValidator();

	/**
	 * Set the validator of this document.
	 *
	 * @param validator
	 *            the validator of this document
	 */
	void setValidator(IValidator validator);

	/**
	 * A document can have only one element node as child. This element is called the 'root element'. While the root
	 * element is the only child element of an document, the document can have further child nodes of other types (e.g.
	 * comments or processing instructions) before or (less common but legal) after the root element.
	 *
	 * @return the root element of this document
	 */
	IElement getRootElement();

	/**
	 * @return the length of the textual content of this document plus 1 for each opening or closing XML tag (element
	 *         tags, comment tags, PI tags and entity references).
	 */
	int getLength();

	/**
	 * Create a Position for the given offset. A position is automatically updated if it "moves" due to content
	 * modifications.
	 *
	 * <p>
	 * All created positions are referenced by this document. <b>Make sure to remove positions you don't need
	 * anymore.</b>
	 *
	 * @see IPosition
	 * @see IDocument#removePosition(IPosition)
	 * @return the Position for the given offset
	 */
	IPosition createPosition(int offset);

	/**
	 * Remove the given Position. A removed position is not updated anymore.
	 *
	 * @param position
	 *            the position to remove
	 * @see IDocument#createPosition(int)
	 */
	void removePosition(IPosition position);

	/**
	 * @return true if text can be inserted at the given offset
	 */
	boolean canInsertText(int offset);

	/**
	 * Insert the given text at the given offset.
	 *
	 * @param offset
	 *            the offset at which the text should be inserted
	 * @param text
	 *            The text to insert. Control characters are automatically converted to \s (except for \n).
	 * @throws DocumentValidationException
	 *             if text is not allowed at the given offset
	 */
	void insertText(int offset, String text) throws DocumentValidationException;

	/**
	 * @return true if a comment can be inserted a the given offset
	 */
	boolean canInsertComment(int offset);

	/**
	 * Insert a new processing instruction at the given offset.
	 *
	 * @see IProcessingInstruction
	 * @param offset
	 *            the offset at which the processing instruction should be inserted
	 * @return the new processing instruction
	 * @throws DocumentValidationException
	 *             if a processing instruction is not allowed at the given offset (e.g. within an existing processing
	 *             instr.)
	 */
	IProcessingInstruction insertProcessingInstruction(int offset, String target) throws DocumentValidationException;

	/**
	 * Update the processing instruction at the given offset with a new target.
	 *
	 * @see IProcessingInstruction
	 * @param offset
	 *            the offset at which the processing instruction should be inserted.
	 * @param target
	 *            The new target to set.
	 * @throws DocumentValidationException
	 *             if the given String is not a valid target.
	 */
	void setProcessingInstructionTarget(int offset, String target) throws DocumentValidationException;

	/**
	 * @param offset
	 * @param target
	 *            The target of the pi to insert. If null, validity of target is not checked.
	 * @return true if a processing instruction can be inserted a the given offset
	 */
	boolean canInsertProcessingInstruction(int offset, String target);

	/**
	 * Insert a new comment at the given offset.
	 *
	 * @see IComment
	 * @param offset
	 *            the offset at which the comment should be inserted
	 * @return the new comment
	 * @throws DocumentValidationException
	 *             if a comment is not allowed a the given offset (e.g. within an existing comment)
	 */
	IComment insertComment(int offset) throws DocumentValidationException;

	/**
	 * @return true if a new element with the given qualified name can be inserted at the given offset
	 */
	boolean canInsertElement(int offset, QualifiedName elementName);

	/**
	 * Insert a new element with the given qualified name a the given offset.
	 *
	 * @see IElement
	 * @see QualifiedName
	 * @param offset
	 *            the offset at which the element should be inserted
	 * @param elementName
	 *            the qualified name of the new element
	 * @return the new element
	 * @throws DocumentValidationException
	 *             if an element with the given qualified name is not allowed a the given offset
	 */
	IElement insertElement(int offset, QualifiedName elementName) throws DocumentValidationException;

	/**
	 * @return true if the given DocumentFragment can be inserted at the given offset
	 */
	boolean canInsertFragment(int offset, IDocumentFragment fragment);

	/**
	 * Insert the given DocumentFragment at the given offset.
	 *
	 * @see IDocumentFragment
	 * @param offset
	 *            the offset at which the fragment should be inserted
	 * @param fragment
	 *            the fragment to insert
	 * @throws DocumentValidationException
	 *             if the given fragment may not be inserted at the given offset
	 */
	void insertFragment(int offset, IDocumentFragment fragment) throws DocumentValidationException;

	/**
	 * @param range
	 *            the range to delete
	 * @return true if the given range can be deleted
	 */
	boolean canDelete(ContentRange range);

	/**
	 * Delete everything in the given range. The range must be balanced i.e. it must start in the same node as it ends.
	 *
	 * @param range
	 *            the range to delete
	 * @throws DocumentValidationException
	 *             if the deletion would lead to an invalid document
	 */
	void delete(ContentRange range) throws DocumentValidationException;

	/**
	 * @return the character at the given offset. If there is an XML tag at the given offset \0 is returned.
	 */
	char getCharacterAt(int offset);

	/**
	 * Find the nearest common node of the given offsets going from each offset to the root element.
	 *
	 * @param offset1
	 *            the first offset
	 * @param offset2
	 *            the second offset
	 * @return the nearest common node for both offsets
	 */
	INode findCommonNode(int offset1, int offset2);

	/**
	 * @return the node in which an insertion at the given offset will end
	 */
	INode getNodeForInsertionAt(int offset);

	/**
	 * @return the node in which an insertion at the given offset will end
	 */
	INode getNodeForInsertionAt(ContentPosition position);

	/**
	 * @return the element in which an insertion at the given offset will end
	 */
	IElement getElementForInsertionAt(int offset);

	/**
	 * @return true if there is an XML tag at the given offset (element tags, comment tags, PI tags and entity
	 *         references)
	 */
	boolean isTagAt(int offset);

	/**
	 * Create a new DocumentFragment with a deep copy of the given range in this document. The range must be balanced
	 * i.e. it must start in the same node as it ends.
	 *
	 * @see IDocumentFragment
	 * @param range
	 *            the range to copy into the fragment
	 * @return a new fragment with a deep copy of the given range
	 */
	IDocumentFragment getFragment(ContentRange range);

	/**
	 * @return all nodes in the given range in this document
	 */
	List<? extends INode> getNodes(ContentRange range);

	/**
	 * Add a listener that is notified about modifications to this document.
	 *
	 * @param listener
	 *            the listener
	 */
	void addDocumentListener(IDocumentListener listener);

	/**
	 * Remove a listener that has been added before.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	void removeDocumentListener(IDocumentListener listener);

}