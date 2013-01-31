/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Florian Thienel - refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ListenerList;

/**
 * A representation of an XML document in the DOM.
 */
public class Document extends Parent {

	private final Element rootElement;
	private final ListenerList<DocumentListener, DocumentEvent> listeners = new ListenerList<DocumentListener, DocumentEvent>(DocumentListener.class);

	private String publicID;
	protected String systemID;
	private String documentURI;

	private String encoding;
	private Validator validator;

	/**
	 * Create a new document with the given root element. This constructor creates a Content object and associates both
	 * the root element and the document with it.
	 * 
	 * @param rootElementName
	 *            the name of the root element of the document
	 */
	public Document(final QualifiedName rootElementName) {
		final GapContent content = new GapContent(100);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		associate(content, content.getRange());

		rootElement = new Element(rootElementName);
		addChild(rootElement);
		content.insertTagMarker(1);
		content.insertTagMarker(1);
		rootElement.associate(content, getRange().resizeBy(1, -1));
	}

	/**
	 * Create a new document with the given content and root element. This constructor assumes that the content and root
	 * element have bee properly set up and are already associated. It associates the document with the given content.
	 * 
	 * @param content
	 *            Content object used to store the document's content
	 * @param rootElement
	 *            root element of the document
	 * 
	 */
	public Document(final Content content, final Element rootElement) {
		Assert.isTrue(content == rootElement.getContent(), "The given root element must already be associated with the given content.");
		content.insertTagMarker(0);
		content.insertTagMarker(content.length());
		associate(content, content.getRange());

		this.rootElement = rootElement;
		addChild(rootElement);
	}

	/*
	 * Node
	 */

	@Override
	public int getStartOffset() {
		return 0;
	}

	@Override
	public int getEndOffset() {
		return getContent().length() - 1;
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String getBaseURI() {
		return getDocumentURI();
	}

	@Override
	public boolean isKindOf(final Node node) {
		return false;
	}

	/*
	 * Document
	 */

	public void setDocumentURI(final String documentURI) {
		this.documentURI = documentURI;
	}

	public String getDocumentURI() {
		return documentURI;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	public String getPublicID() {
		return publicID;
	}

	public void setPublicID(final String publicID) {
		this.publicID = publicID;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(final String systemID) {
		this.systemID = systemID;
	}

	public Validator getValidator() {
		return validator;
	}

	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	public Element getRootElement() {
		return rootElement;
	}

	/**
	 * @return the length of the textual content of this document plus 1 for each opening or closing XML tag (element
	 *         tags, comment tags, PI tags and entity references).
	 */
	public int getLength() {
		return getContent().length();
	}

	/**
	 * Create a Position for the given offset. A position is automatically updated if it "moves" due to content
	 * modifications.
	 * 
	 * <p>
	 * All created positions are referenced by this document. <b>Make sure to remove positions you don't need
	 * anymore.</b>
	 * 
	 * @see Position
	 * @see Document#removePosition(Position)
	 * @return the Position for the given offset
	 */
	public Position createPosition(final int offset) {
		return getContent().createPosition(offset);
	}

	/**
	 * Remove the given Position. A removed position is not updated anymore.
	 */
	public void removePosition(final Position position) {
		getContent().removePosition(position);
	}

	/*
	 * L1 Operations
	 */

	private boolean canInsertAt(final Node insertionNode, final int offset, final QualifiedName... nodeNames) {
		return canInsertAt(insertionNode, offset, Arrays.asList(nodeNames));
	}

	private boolean canInsertAt(final Node insertionNode, final int offset, final List<QualifiedName> nodeNames) {
		if (insertionNode == null) {
			return false;
		}
		return insertionNode.accept(new BaseNodeVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final Element element) {
				if (validator == null) {
					return true;
				}

				final List<QualifiedName> prefix = getNodeNames(element.getChildNodesBefore(offset));
				final List<QualifiedName> insertionCandidates = nodeNames;
				final List<QualifiedName> suffix = getNodeNames(element.getChildNodesAfter(offset));

				return validator.isValidSequence(element.getQualifiedName(), prefix, insertionCandidates, suffix, true);
			}

			@Override
			public Boolean visit(final Comment comment) {
				return true;
			}

			@Override
			public Boolean visit(final Text text) {
				return true;
			}
		});
	}

	/**
	 * @return true if text can be inserted at the given offset
	 */
	public boolean canInsertText(final int offset) {
		return canInsertAt(getNodeForInsertionAt(offset), offset, Validator.PCDATA);
	}

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
	public void insertText(final int offset, final String text) throws DocumentValidationException {
		Assert.isTrue(offset > getStartOffset() && offset <= getEndOffset(), MessageFormat.format("Offset must be in [{0}, {1}]", getStartOffset() + 1, getEndOffset()));

		final String adjustedText = convertControlCharactersToSpaces(text);
		final Node insertionNode = getNodeForInsertionAt(offset);
		insertionNode.accept(new INodeVisitor() {
			public void visit(final Document document) {
				Assert.isTrue(false, "Cannot insert text directly into Document.");
			}

			public void visit(final DocumentFragment fragment) {
				Assert.isTrue(false, "DocumentFragment is never a child of Document.");
			}

			public void visit(final Element element) {
				if (!canInsertAt(element, offset, Validator.PCDATA)) {
					throw new DocumentValidationException(MessageFormat.format("Cannot insert text ''{0}'' at offset {1}.", text, offset));
				}

				fireBeforeContentInserted(new DocumentEvent(Document.this, element, offset, adjustedText.length(), null));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new DocumentEvent(Document.this, element, offset, adjustedText.length(), null));
			}

			public void visit(final Text text) {
				fireBeforeContentInserted(new DocumentEvent(Document.this, text.getParent(), offset, adjustedText.length(), null));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new DocumentEvent(Document.this, text.getParent(), offset, adjustedText.length(), null));
			}

			public void visit(final Comment comment) {
				fireBeforeContentInserted(new DocumentEvent(Document.this, comment.getParent(), offset, adjustedText.length(), null));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new DocumentEvent(Document.this, comment.getParent(), offset, adjustedText.length(), null));
			}
		});
	}

	private String convertControlCharactersToSpaces(final String text) {
		final char[] characters = text.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			if (Character.isISOControl(characters[i]) && characters[i] != '\n') {
				characters[i] = ' ';
			}
		}
		return new String(characters);
	}

	/**
	 * @return true if a comment can be inserted a the given offset
	 */
	public boolean canInsertComment(final int offset) {
		if (!(offset > getStartOffset() && offset <= getEndOffset())) {
			return false;
		}
		final Node node = getNodeForInsertionAt(offset);
		if (node instanceof Comment) {
			return false;
		}
		return true;
	}

	/**
	 * Insert a new comment at the given offset.
	 * 
	 * @see Comment
	 * @param offset
	 *            the offset at which the comment should be inserted
	 * @return the new comment
	 * @throws DocumentValidationException
	 *             if a comment is not allowed a the given offset (e.g. within an existing comment)
	 */
	public Comment insertComment(final int offset) throws DocumentValidationException {
		if (!canInsertComment(offset)) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert a comment at offset {0}.", offset));
		}

		final Parent parent = getParentForInsertionAt(offset);

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		final Comment comment = new Comment();
		getContent().insertTagMarker(offset);
		getContent().insertTagMarker(offset);
		comment.associate(getContent(), new ContentRange(offset, offset + 1));

		parent.insertChild(parent.getIndexOfChildNextTo(offset), comment);

		fireContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		return comment;
	}

	/**
	 * @return true if a new element with the given qualified name can be inserted at the given offset
	 */
	public boolean canInsertElement(final int offset, final QualifiedName elementName) {
		return canInsertAt(getElementForInsertionAt(offset), offset, elementName);
	}

	/**
	 * Insert a new element with the given qualified name a the given offset.
	 * 
	 * @see Element
	 * @see QualifiedName
	 * @param offset
	 *            the offset at which the element should be inserted
	 * @param elementName
	 *            the qualified name of the new element
	 * @return the new element
	 * @throws DocumentValidationException
	 *             if an element with the given qualified name is not allowed a the given offset
	 */
	public Element insertElement(final int offset, final QualifiedName elementName) throws DocumentValidationException {
		Assert.isTrue(offset > rootElement.getStartOffset() && offset <= rootElement.getEndOffset(),
				MessageFormat.format("Offset must be in [{0}, {1}]", rootElement.getStartOffset() + 1, rootElement.getEndOffset()));

		final Element parent = getElementForInsertionAt(offset);
		if (!canInsertAt(parent, offset, elementName)) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert element {0} at offset {1}.", elementName, offset));
		}

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		final Element element = new Element(elementName);
		getContent().insertTagMarker(offset);
		getContent().insertTagMarker(offset);
		element.associate(getContent(), new ContentRange(offset, offset + 1));

		parent.insertChild(parent.getIndexOfChildNextTo(offset), element);

		fireContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		return element;
	}

	/**
	 * @return true if the given DocumentFragment can be inserted at the given offset
	 */
	public boolean canInsertFragment(final int offset, final DocumentFragment fragment) {
		return canInsertAt(getElementForInsertionAt(offset), offset, fragment.getNodeNames());
	}

	/**
	 * Insert the given DocumentFragment at the given offset.
	 * 
	 * @see DocumentFragment
	 * @param offset
	 *            the offset at which the fragment should be inserted
	 * @param fragment
	 *            the fragment to insert
	 * @throws DocumentValidationException
	 *             if the given fragment may not be inserted at the given offset
	 */
	public void insertFragment(final int offset, final DocumentFragment fragment) throws DocumentValidationException {
		Assert.isTrue(isInsertionPointIn(this, offset), "Cannot insert fragment outside of the document range.");

		final Element parent = getElementForInsertionAt(offset);
		if (!canInsertAt(parent, offset, fragment.getNodeNames())) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert document fragment at offset {0}.", offset));
		}

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		getContent().insertContent(offset, fragment.getContent());

		final DeepCopy deepCopy = new DeepCopy(fragment);
		final List<Node> newNodes = deepCopy.getNodes();
		int index = parent.getIndexOfChildNextTo(offset);
		for (final Node newNode : newNodes) {
			parent.insertChild(index, newNode);
			associateDeeply(newNode, offset);
			index++;
		}

		fireContentInserted(new DocumentEvent(this, parent, offset, fragment.getContent().length(), null));
	}

	private void associateDeeply(final Node node, final int offset) {
		if (node instanceof Parent) {
			final Parent parent = (Parent) node;
			for (final Node child : parent.children()) {
				associateDeeply(child, offset);
			}
		}
		node.associate(getContent(), node.getRange().moveBy(offset));
	}

	/**
	 * Delete everything in the given range. The range must be balanced i.e. it must start in the same node as it ends.
	 * 
	 * @param range
	 *            the range to delete
	 * @throws DocumentValidationException
	 *             if the deletion would lead to an invalid document
	 */
	public void delete(final ContentRange range) throws DocumentValidationException {
		final Parent surroundingParent = getParentAt(range.getStartOffset());
		final Parent parentAtEndOffset = getParentAt(range.getEndOffset());
		Assert.isTrue(surroundingParent == parentAtEndOffset, MessageFormat.format("Range {0} for deletion is unbalanced: {1} -> {2}", range, surroundingParent, parentAtEndOffset));

		final Parent parentForDeletion;
		if (range.equals(surroundingParent.getRange())) {
			parentForDeletion = surroundingParent.getParent();
		} else {
			parentForDeletion = surroundingParent;
		}

		final boolean deletionIsValid = parentForDeletion.accept(new BaseNodeVisitorWithResult<Boolean>(true) {
			@Override
			public Boolean visit(final Document document) {
				if (range.intersects(document.getRootElement().getRange())) {
					return false;
				}
				return true;
			}

			@Override
			public Boolean visit(final Element element) {
				final Validator validator = getValidator();
				if (validator == null) {
					return true;
				}
				final List<QualifiedName> prefix = getNodeNames(element.getChildNodesBefore(range.getStartOffset()));
				final List<QualifiedName> suffix = getNodeNames(element.getChildNodesAfter(range.getEndOffset()));
				return validator.isValidSequence(element.getQualifiedName(), prefix, suffix, null, true);
			}
		});
		if (!deletionIsValid) {
			throw new DocumentValidationException(MessageFormat.format("Cannot delete {0}", range));
		}

		fireBeforeContentDeleted(new DocumentEvent(this, parentForDeletion, range.getStartOffset(), range.length(), null));

		for (final Node child : parentForDeletion.children()) {
			if (child.isInRange(range)) {
				parentForDeletion.removeChild(child);
				child.dissociate();
			}
		}

		getContent().remove(range);

		fireContentDeleted(new DocumentEvent(this, parentForDeletion, range.getStartOffset(), range.length(), null));
	}

	/*
	 * Miscellaneous
	 */

	/**
	 * @return the character at the given offset. If there is an XML tag at the given offset \0 is returned.
	 */
	public char getCharacterAt(final int offset) {
		final String text = getContent().getText(new ContentRange(offset, offset));
		if (text.length() == 0) {
			/*
			 * XXX This is used in VexWidgetImpl.deleteNextChar/deletePreviousChar to find out if there is an element
			 * marker at the given offset. VexWidgetImpl has no access to Content, so there should be a method in
			 * Document to find out if there is an element at a given offset.
			 */
			return '\0';
		}
		return text.charAt(0);
	}

	/**
	 * Find the nearest common node of the given offsets going from each offset to the root element.
	 * 
	 * @param offset1
	 *            the first offset
	 * @param offset2
	 *            the second offset
	 * @return the nearest common node for both offsets
	 */
	public Node findCommonNode(final int offset1, final int offset2) {
		Assert.isTrue(containsOffset(offset1) && containsOffset(offset2));
		return findCommonNodeIn(this, offset1, offset2);
	}

	private static Node findCommonNodeIn(final Parent parent, final int offset1, final int offset2) {
		for (final Node child : parent.children()) {
			if (child instanceof Text) {
				continue;
			}
			if (isCommonNodeFor(child, offset1, offset2)) {
				if (child instanceof Parent) {
					return findCommonNodeIn((Parent) child, offset1, offset2);
				}
				return child;
			}
		}
		return parent;
	}

	private static boolean isCommonNodeFor(final Node node, final int offset1, final int offset2) {
		return isInsertionPointIn(node, offset1) && isInsertionPointIn(node, offset2);
	}

	/**
	 * @return true if the given node would contain an insertion at the given offset
	 */
	public static boolean isInsertionPointIn(final Node node, final int offset) {
		return node.getRange().resizeBy(1, 0).contains(offset);
	}

	/**
	 * @return the node in which an insertion at the given offset will end
	 */
	public Node getNodeForInsertionAt(final int offset) {
		final Node node = getChildNodeAt(offset);
		if (node instanceof Text) {
			return node.getParent();
		}
		if (offset == node.getStartOffset()) {
			return node.getParent();
		}
		return node;
	}

	private Parent getParentForInsertionAt(final int offset) {
		final Node node = getChildNodeAt(offset);
		if (!(node instanceof Parent)) {
			return node.getParent();
		}
		if (offset == node.getStartOffset()) {
			return node.getParent();
		}
		// this cast is save because if we got here node is a Parent
		return (Parent) node;
	}

	/**
	 * @return the element in which an insertion at the given offset will end
	 */
	public Element getElementForInsertionAt(final int offset) {
		final Element parent = getParentElement(getChildNodeAt(offset));
		if (parent == null) {
			return null;
		}
		if (offset == parent.getStartOffset()) {
			return parent.getParentElement();
		}
		return parent;
	}

	private static Element getParentElement(final Node node) {
		if (node == null) {
			return null;
		}
		if (node instanceof Element) {
			return (Element) node;
		}
		return getParentElement(node.getParent());
	}

	private Parent getParentAt(final int offset) {
		final Node child = getChildNodeAt(offset);
		if (child instanceof Parent) {
			return (Parent) child;
		}
		return child.getParent();
	}

	/**
	 * @return true if there is an XML tag at the given offset (element tags, comment tags, PI tags and entity
	 *         references)
	 */
	public boolean isTagAt(final int offset) {
		return getContent().isTagMarker(offset);
	}

	/**
	 * Create a new DocumentFragment with a deep copy of the given range in this document. The range must be balanced
	 * i.e. it must start in the same node as it ends.
	 * 
	 * @see DocumentFragment
	 * @param range
	 *            the range to copy into the fragment
	 * @return a new fragment with a deep copy of the given range
	 */
	public DocumentFragment getFragment(final ContentRange range) {
		final Parent parent = getParentOfRange(range);
		final DeepCopy deepCopy = new DeepCopy(parent, range);
		return new DocumentFragment(deepCopy.getContent(), deepCopy.getNodes());
	}

	private Parent getParentOfRange(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));

		final Node startNode = getChildNodeAt(range.getStartOffset());
		final Node endNode = getChildNodeAt(range.getEndOffset());
		final Parent parent = startNode.getParent();
		Assert.isTrue(parent == endNode.getParent(), MessageFormat.format("The fragment in {0} is unbalanced.", range));
		Assert.isNotNull(parent, MessageFormat.format("No balanced parent found for {0}", range));

		return parent;
	}

	/**
	 * @return all nodes in the given range in this document
	 */
	public List<Node> getNodes(final ContentRange range) {
		final List<Node> result = new ArrayList<Node>();
		for (final Node node : getParentOfRange(range).children(range)) {
			result.add(node);
		}
		return result;
	}

	/*
	 * Events
	 */

	public void addDocumentListener(final DocumentListener listener) {
		listeners.add(listener);
	}

	public void removeDocumentListener(final DocumentListener listener) {
		listeners.remove(listener);
	}

	public void fireAttributeChanged(final DocumentEvent e) {
		listeners.fireEvent("attributeChanged", e);
	}

	public void fireNamespaceChanged(final DocumentEvent e) {
		listeners.fireEvent("namespaceChanged", e);
	}

	private void fireBeforeContentDeleted(final DocumentEvent e) {
		listeners.fireEvent("beforeContentDeleted", e);
	}

	private void fireBeforeContentInserted(final DocumentEvent e) {
		listeners.fireEvent("beforeContentInserted", e);
	}

	private void fireContentDeleted(final DocumentEvent e) {
		listeners.fireEvent("contentDeleted", e);
	}

	private void fireContentInserted(final DocumentEvent e) {
		listeners.fireEvent("contentInserted", e);
	}

}
