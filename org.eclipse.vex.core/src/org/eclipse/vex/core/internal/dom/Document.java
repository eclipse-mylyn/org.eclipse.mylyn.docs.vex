/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ListenerList;

/**
 * Represents an XML document.
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
	 * Class constructor.
	 * 
	 * @param rootElement
	 *            root element of the document. The document property of this RootElement is set by this constructor.
	 * 
	 */
	public Document(final Element rootElement) {
		final GapContent content = new GapContent(100);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		associate(content, content.getRange());

		this.rootElement = rootElement;
		addChild(rootElement);
		rootElement.associate(content, content.getRange());
	}

	/**
	 * Class constructor. This constructor is used by the document builder and assumes that the content and root element
	 * have bee properly set up and is already associated with the given content.
	 * 
	 * @param content
	 *            Content object used to store the document's content.
	 * @param rootElement
	 *            RootElement of the document.
	 * 
	 */
	public Document(final Content content, final Element rootElement) {
		Assert.isTrue(content == rootElement.getContent(), "The given root element must already be associated with the given content.");
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

	public int getLength() {
		return getContent().length();
	}

	public Position createPosition(final int offset) {
		return getContent().createPosition(offset);
	}

	/*
	 * L1 Operations
	 */

	private boolean canInsertAt(final Element insertionParent, final int offset, final QualifiedName... nodeNames) {
		return canInsertAt(insertionParent, offset, Arrays.asList(nodeNames));
	}

	private boolean canInsertAt(final Element insertionParent, final int offset, final List<QualifiedName> nodeNames) {
		if (validator == null) {
			return true;
		}

		if (insertionParent == null) {
			return false;
		}

		final List<QualifiedName> prefix = getNodeNames(insertionParent.getChildNodesBefore(offset));
		final List<QualifiedName> insertionCandidates = nodeNames;
		final List<QualifiedName> suffix = getNodeNames(insertionParent.getChildNodesAfter(offset));

		return validator.isValidSequence(insertionParent.getQualifiedName(), prefix, insertionCandidates, suffix, true);
	}

	private Element getInsertionParentAt(final int offset) {
		final Element parent = getElementAt(offset);
		if (offset == parent.getStartOffset()) {
			return parent.getParentElement();
		}
		return parent;
	}

	private Node getInsertionNodeAt(final int offset) {
		final Node node = getChildNodeAt(offset);
		if (offset == node.getStartOffset()) {
			return node.getParent();
		}
		return node;
	}

	public boolean canInsertText(final int offset) {
		return canInsertAt(getInsertionParentAt(offset), offset, Validator.PCDATA);
	}

	public void insertText(final int offset, final String text) throws DocumentValidationException {
		Assert.isTrue(offset > getStartOffset() && offset <= getEndOffset(), MessageFormat.format("Offset must be in [{0}, {1}]", getStartOffset() + 1, getEndOffset()));

		final String adjustedText = convertControlCharactersToSpaces(text);
		final Node insertionNode = getInsertionNodeAt(offset);
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

				fireBeforeContentInserted(new DocumentEvent(Document.this, element, offset, 2, null));

				getContent().insertText(offset, adjustedText);

				fireContentInserted(new DocumentEvent(Document.this, element, offset, adjustedText.length(), null));
			}

			public void visit(final Text text) {
				getContent().insertText(offset, adjustedText);
			}

			public void visit(final Comment comment) {
				getContent().insertText(offset, adjustedText);
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

	public boolean canInsertComment(final int offset) {
		// TODO Currently comments can only be inserted within the root element.
		return offset > rootElement.getStartOffset() && offset <= rootElement.getEndOffset();
	}

	public Comment insertComment(final int offset) {
		Assert.isTrue(canInsertComment(offset));

		final Element parent = getInsertionParentAt(offset);

		// TODO fire events
		final Comment comment = new Comment();
		getContent().insertElementMarker(offset);
		getContent().insertElementMarker(offset);
		comment.associate(getContent(), new Range(offset, offset + 1));

		parent.insertChild(parent.getInsertionIndex(offset), comment);

		return comment;
	}

	public boolean canInsertElement(final int offset, final QualifiedName elementName) {
		return canInsertAt(getInsertionParentAt(offset), offset, elementName);
	}

	public Element insertElement(final int offset, final QualifiedName elementName) throws DocumentValidationException {
		Assert.isTrue(offset > rootElement.getStartOffset() && offset <= rootElement.getEndOffset(), MessageFormat.format("Offset must be in [{0}, {1}]", getStartOffset() + 1, getEndOffset()));

		final Element parent = getInsertionParentAt(offset);
		if (!canInsertAt(parent, offset, elementName)) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert element {0} at offset {1}.", elementName, offset));
		}

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		final Element element = new Element(elementName);
		getContent().insertElementMarker(offset);
		getContent().insertElementMarker(offset);
		element.associate(getContent(), new Range(offset, offset + 1));

		parent.insertChild(parent.getInsertionIndex(offset), element);

		fireContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		return element;
	}

	public boolean canInsertFragment(final int offset, final DocumentFragment fragment) {
		return canInsertAt(getInsertionParentAt(offset), offset, fragment.getNodeNames());
	}

	public void insertFragment(final int offset, final DocumentFragment fragment) throws DocumentValidationException {
		if (offset < 1 || offset >= getLength()) {
			throw new IllegalArgumentException("Error inserting document fragment");
		}

		if (!canInsertAt(getInsertionParentAt(offset), offset, fragment.getNodeNames())) {
			throw new DocumentValidationException("Cannot insert document fragment");
		}

		final Element parent = getElementAt(offset);

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		getContent().insertContent(offset, fragment.getContent());

		final DeepCopy deepCopy = new DeepCopy(fragment);
		final List<Node> newNodes = deepCopy.getNodes();
		int index = parent.getInsertionIndex(offset);
		for (final Node newNode : newNodes) {
			parent.insertChild(index, newNode);
			newNode.associate(getContent(), newNode.getRange().moveBounds(offset));
			index++;
		}

		fireContentInserted(new DocumentEvent(this, parent, offset, fragment.getContent().length(), null));
	}

	public void delete(final Range range) throws DocumentValidationException {
		final Element surroundingElement = getElementAt(range.getStartOffset() - 1);
		final Element elementAtEndOffset = getElementAt(range.getEndOffset() + 1);
		if (surroundingElement != elementAtEndOffset) {
			throw new IllegalArgumentException("Deletion in " + range + " is unbalanced");
		}

		final Validator validator = getValidator();
		if (validator != null) {
			final List<QualifiedName> seq1 = getNodeNames(surroundingElement.getChildNodesBefore(range.getStartOffset()));
			final List<QualifiedName> seq2 = getNodeNames(surroundingElement.getChildNodesAfter(range.getEndOffset()));

			if (!validator.isValidSequence(surroundingElement.getQualifiedName(), seq1, seq2, null, true)) {
				throw new DocumentValidationException("Unable to delete " + range);
			}
		}

		fireBeforeContentDeleted(new DocumentEvent(this, surroundingElement, range.getStartOffset(), range.length(), null));

		final Iterator<Node> iter = surroundingElement.getChildIterator();
		while (iter.hasNext()) {
			final Node child = iter.next();
			if (child.isInRange(range)) {
				surroundingElement.removeChild(child);
			}
		}

		getContent().remove(range);

		fireContentDeleted(new DocumentEvent(this, surroundingElement, range.getStartOffset(), range.length(), null));
	}

	/*
	 * Miscellaneous
	 */

	public char getCharacterAt(final int offset) {
		final String text = getContent().getText(new Range(offset, offset));
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

	public Element findCommonElement(final int offset1, final int offset2) {
		Element element = rootElement;
		for (;;) {
			boolean tryAgain = false;
			final List<Element> children = element.getChildElements();
			for (int i = 0; i < children.size(); i++) {
				final Element child = children.get(i);
				if (child.containsOffset(offset1) && child.containsOffset(offset2)) {
					element = child;
					tryAgain = true;
					break;
				}
			}
			if (!tryAgain) {
				break;
			}
		}
		return element;
	}

	public Element getElementAt(final int offset) {
		return getParentElement(getChildNodeAt(offset));
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

	public boolean isElementAt(final int offset) {
		return getContent().isElementMarker(offset);
	}

	public DocumentFragment getFragment(final Range range) {
		final Parent parent = getParentOfRange(range);
		final DeepCopy deepCopy = new DeepCopy(parent, range);
		return new DocumentFragment(deepCopy.getContent(), deepCopy.getNodes());
	}

	private Parent getParentOfRange(final Range range) {
		Assert.isTrue(getRange().contains(range));

		final Node startNode = getChildNodeAt(range.getStartOffset());
		final Node endNode = getChildNodeAt(range.getEndOffset());
		final Parent parent = startNode.getParent();
		Assert.isTrue(parent == endNode.getParent(), MessageFormat.format("The fragment in {0} is unbalanced.", range));
		Assert.isNotNull(parent, MessageFormat.format("No balanced parent found for {0}", range));

		return parent;
	}

	public List<Node> getNodes(final Range range) {
		return getParentOfRange(range).getChildNodes(range);
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
