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
 *     Carsten Hiesserich - bug fixes (bug 407801, 410659)
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ListenerList;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentEvent;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * A representation of an XML document in the DOM.
 */
public class Document extends Parent implements IDocument {

	private static final String DEFAULT_NAMESPACE_PREFIX = "ns";

	private final Element rootElement;
	private final ListenerList<IDocumentListener, DocumentEvent> listeners = new ListenerList<IDocumentListener, DocumentEvent>(IDocumentListener.class);

	private String publicID;
	protected String systemID;
	private String documentURI;

	private String encoding;
	private IValidator validator;

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
	public Document(final IContent content, final Element rootElement) {
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

	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String getBaseURI() {
		return getDocumentURI();
	}

	public boolean isKindOf(final INode node) {
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

	public IValidator getValidator() {
		return validator;
	}

	public void setValidator(final IValidator validator) {
		this.validator = validator;
	}

	public Element getRootElement() {
		return rootElement;
	}

	public int getLength() {
		return getContent().length();
	}

	public IPosition createPosition(final int offset) {
		return getContent().createPosition(offset);
	}

	public void removePosition(final IPosition position) {
		getContent().removePosition(position);
	}

	/*
	 * L1 Operations
	 */

	private boolean canInsertAt(final INode insertionNode, final int offset, final QualifiedName... nodeNames) {
		return canInsertAt(insertionNode, offset, Arrays.asList(nodeNames));
	}

	private boolean canInsertAt(final INode insertionNode, final int offset, final List<QualifiedName> nodeNames) {
		if (insertionNode == null) {
			return false;
		}
		return insertionNode.accept(new BaseNodeVisitorWithResult<Boolean>(false) {
			@Override
			public Boolean visit(final IElement element) {
				if (validator == null) {
					return true;
				}

				final List<QualifiedName> prefix = getNodeNames(element.children().before(offset));
				final List<QualifiedName> insertionCandidates = nodeNames;
				final List<QualifiedName> suffix = getNodeNames(element.children().after(offset));

				return validator.isValidSequence(element.getQualifiedName(), prefix, insertionCandidates, suffix, true);
			}

			@Override
			public Boolean visit(final IComment comment) {
				for (final QualifiedName nodeName : nodeNames) {
					if (!nodeName.equals(IValidator.PCDATA)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public Boolean visit(final IText text) {
				return true;
			}
		});
	}

	public boolean canInsertText(final int offset) {
		return canInsertAt(getNodeForInsertionAt(offset), offset, IValidator.PCDATA);
	}

	public void insertText(final int offset, final String text) throws DocumentValidationException {
		Assert.isTrue(offset > getStartOffset() && offset <= getEndOffset(), MessageFormat.format("Offset must be in [{0}, {1}]", getStartOffset() + 1, getEndOffset()));

		final String adjustedText = convertControlCharactersToSpaces(text);
		final INode insertionNode = getNodeForInsertionAt(offset);
		insertionNode.accept(new INodeVisitor() {
			public void visit(final IDocument document) {
				Assert.isTrue(false, "Cannot insert text directly into Document.");
			}

			public void visit(final IDocumentFragment fragment) {
				Assert.isTrue(false, "DocumentFragment is never a child of Document.");
			}

			public void visit(final IElement element) {
				if (!canInsertAt(element, offset, IValidator.PCDATA)) {
					throw new DocumentValidationException(MessageFormat.format("Cannot insert text ''{0}'' at offset {1}.", text, offset));
				}

				fireBeforeContentInserted(new ContentChangeEvent(Document.this, element, new ContentRange(offset, offset + adjustedText.length() - 1)));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new ContentChangeEvent(Document.this, element, new ContentRange(offset, offset + adjustedText.length() - 1)));
			}

			public void visit(final IText text) {
				fireBeforeContentInserted(new ContentChangeEvent(Document.this, text.getParent(), new ContentRange(offset, offset + adjustedText.length() - 1)));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new ContentChangeEvent(Document.this, text.getParent(), new ContentRange(offset, offset + adjustedText.length() - 1)));
			}

			public void visit(final IComment comment) {
				fireBeforeContentInserted(new ContentChangeEvent(Document.this, comment.getParent(), new ContentRange(offset, offset + adjustedText.length() - 1)));
				getContent().insertText(offset, adjustedText);
				fireContentInserted(new ContentChangeEvent(Document.this, comment.getParent(), new ContentRange(offset, offset + adjustedText.length() - 1)));
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
		if (!(offset > getStartOffset() && offset <= getEndOffset())) {
			return false;
		}
		final INode node = getNodeForInsertionAt(offset);
		if (node instanceof IComment) {
			return false;
		}
		return true;
	}

	public IComment insertComment(final int offset) throws DocumentValidationException {
		if (!canInsertComment(offset)) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert a comment at offset {0}.", offset));
		}

		final Parent parent = getParentForInsertionAt(offset);

		fireBeforeContentInserted(new ContentChangeEvent(this, parent, new ContentRange(offset, offset + 1)));

		final Comment comment = new Comment();
		getContent().insertTagMarker(offset);
		getContent().insertTagMarker(offset);
		comment.associate(getContent(), new ContentRange(offset, offset + 1));

		parent.insertChildAt(offset, comment);

		fireContentInserted(new ContentChangeEvent(this, parent, comment.getRange()));

		return comment;
	}

	public boolean canInsertElement(final int offset, final QualifiedName elementName) {
		return canInsertAt(getNodeForInsertionAt(offset), offset, elementName);
	}

	public Element insertElement(final int offset, final QualifiedName elementName) throws DocumentValidationException {
		Assert.isTrue(offset > rootElement.getStartOffset() && offset <= rootElement.getEndOffset(),
				MessageFormat.format("Offset must be in [{0}, {1}]", rootElement.getStartOffset() + 1, rootElement.getEndOffset()));

		final Element parent = getElementForInsertionAt(offset);
		final INode node = getNodeForInsertionAt(offset);
		if (!canInsertAt(node, offset, elementName)) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert element {0} at offset {1}.", elementName, offset));
		}

		fireBeforeContentInserted(new ContentChangeEvent(this, parent, new ContentRange(offset, offset + 1)));

		final Element element = new Element(elementName);
		getContent().insertTagMarker(offset);
		getContent().insertTagMarker(offset);
		element.associate(getContent(), new ContentRange(offset, offset + 1));

		parent.insertChildAt(offset, element);

		fireContentInserted(new ContentChangeEvent(this, parent, element.getRange()));

		return element;
	}

	public boolean canInsertFragment(final int offset, final IDocumentFragment fragment) {
		return canInsertAt(getNodeForInsertionAt(offset), offset, fragment.getNodeNames());
	}

	public void insertFragment(final int offset, final IDocumentFragment fragment) throws DocumentValidationException {
		Assert.isTrue(isInsertionPointIn(this, offset), "Cannot insert fragment outside of the document range.");

		final Element parent = getElementForInsertionAt(offset);
		final INode node = getNodeForInsertionAt(offset);
		if (!canInsertAt(node, offset, fragment.getNodeNames())) {
			throw new DocumentValidationException(MessageFormat.format("Cannot insert document fragment at offset {0}.", offset));
		}

		fireBeforeContentInserted(new ContentChangeEvent(this, parent, new ContentRange(offset, offset + 1)));

		getContent().insertContent(offset, fragment.getContent());

		final Set<String> undeclaredNamespaces = new HashSet<String>();

		final DeepCopy deepCopy = new DeepCopy(fragment);
		final List<Node> newNodes = deepCopy.getNodes();
		int nextOffset = offset;
		for (final Node newNode : newNodes) {
			parent.insertChildAt(nextOffset, newNode);
			associateDeeply(newNode, offset);
			nextOffset = newNode.getEndOffset() + 1;

			undeclaredNamespaces.addAll(newNode.accept(new FindUndeclaredNamespacesVisitor()));
		}

		declareNamespaces(undeclaredNamespaces, parent);

		fireContentInserted(new ContentChangeEvent(this, parent, new ContentRange(offset, offset + fragment.getContent().length() - 1)));
	}

	private void associateDeeply(final Node node, final int offset) {
		if (node instanceof Parent) {
			final Parent parent = (Parent) node;
			for (final INode child : parent.children()) {
				associateDeeply((Node) child, offset);
			}
		}
		node.associate(getContent(), node.getRange().moveBy(offset));
	}

	private void declareNamespaces(final Collection<String> namespaceURIs, final IElement element) {
		final NamespacePrefixGenerator namespacePrefixGenerator = new NamespacePrefixGenerator(element, DEFAULT_NAMESPACE_PREFIX);
		for (final String namespaceURI : namespaceURIs) {
			final String namespacePrefix = namespacePrefixGenerator.next();
			element.declareNamespace(namespacePrefix, namespaceURI);
		}
	}

	public boolean canDelete(final ContentRange range) {
		final IParent surroundingParent = getParentAt(range.getStartOffset());
		final IParent parentAtEndOffset = getParentAt(range.getEndOffset());
		if (surroundingParent != parentAtEndOffset) {
			return false; // the range is unbalanced
		}

		final Parent parentForDeletion;
		if (range.equals(surroundingParent.getRange())) {
			parentForDeletion = (Parent) surroundingParent.getParent();
		} else {
			parentForDeletion = (Parent) surroundingParent;
		}

		final boolean deletionIsValid = parentForDeletion.accept(new BaseNodeVisitorWithResult<Boolean>(true) {
			@Override
			public Boolean visit(final IDocument document) {
				if (range.intersects(document.getRootElement().getRange())) {
					return false;
				}
				return true;
			}

			@Override
			public Boolean visit(final IElement element) {
				final IValidator validator = getValidator();
				if (validator == null) {
					return true;
				}
				final List<QualifiedName> prefix = getNodeNames(element.children().before(range.getStartOffset()));
				final List<QualifiedName> suffix = getNodeNames(element.children().after(range.getEndOffset()));
				return validator.isValidSequence(element.getQualifiedName(), prefix, suffix, null, true);
			}
		});

		return deletionIsValid;
	}

	public void delete(final ContentRange range) throws DocumentValidationException {
		IParent surroundingParent = getParentAt(range.getStartOffset());
		if (range.getStartOffset() == surroundingParent.getStartOffset()) {
			if (surroundingParent.getEndOffset() > range.getEndOffset()) {
				throw new DocumentValidationException(MessageFormat.format("Range for deletion is unbalanced (Node {1} is not completely enclosed)", surroundingParent));
			}
			surroundingParent = surroundingParent.getParent();
		}

		if (surroundingParent.getEndOffset() <= range.getEndOffset()) {
			throw new DocumentValidationException(MessageFormat.format("Range for deletion is unbalanced (Range exceeds end offset of node {1})", surroundingParent));
		}

		final Parent parentForDeletion = (Parent) surroundingParent;

		final boolean deletionIsValid = parentForDeletion.accept(new BaseNodeVisitorWithResult<Boolean>(true) {
			@Override
			public Boolean visit(final IDocument document) {
				if (range.intersects(document.getRootElement().getRange())) {
					return false;
				}
				return true;
			}

			@Override
			public Boolean visit(final IElement element) {
				final IValidator validator = getValidator();
				if (validator == null) {
					return true;
				}
				final List<QualifiedName> prefix = getNodeNames(element.children().before(range.getStartOffset()));
				final List<QualifiedName> suffix = getNodeNames(element.children().after(range.getEndOffset()));
				return validator.isValidSequence(element.getQualifiedName(), prefix, suffix, null, true);
			}
		});
		if (!deletionIsValid) {
			throw new DocumentValidationException(MessageFormat.format("Cannot delete {0}", range));
		}

		fireBeforeContentDeleted(new ContentChangeEvent(this, parentForDeletion, range));

		for (final INode child : parentForDeletion.children().in(range)) {
			parentForDeletion.removeChild((Node) child);
			((Node) child).dissociate();
		}

		getContent().remove(range);

		fireContentDeleted(new ContentChangeEvent(this, parentForDeletion, range));
	}

	/*
	 * Miscellaneous
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

	public INode findCommonNode(final int offset1, final int offset2) {
		Assert.isTrue(containsOffset(offset1) && containsOffset(offset2));
		return findCommonNodeIn(this, offset1, offset2);
	}

	private static INode findCommonNodeIn(final IParent parent, final int offset1, final int offset2) {
		for (final INode child : parent.children().withoutText()) {
			if (isCommonNodeFor(child, offset1, offset2)) {
				if (child instanceof IParent) {
					return findCommonNodeIn((IParent) child, offset1, offset2);
				}
				return child;
			}
		}
		return parent;
	}

	private static boolean isCommonNodeFor(final INode node, final int offset1, final int offset2) {
		return isInsertionPointIn(node, offset1) && isInsertionPointIn(node, offset2);
	}

	public static boolean isInsertionPointIn(final INode node, final int offset) {
		return node.getRange().resizeBy(1, 0).contains(offset);
	}

	public INode getNodeForInsertionAt(final int offset) {
		final INode node = getChildAt(offset);
		if (node instanceof IText) {
			return node.getParent();
		}
		if (offset == node.getStartOffset()) {
			return node.getParent();
		}
		return node;
	}

	private Parent getParentForInsertionAt(final int offset) {
		final INode node = getChildAt(offset);
		if (!(node instanceof Parent)) {
			return (Parent) node.getParent();
		}
		if (offset == node.getStartOffset()) {
			return (Parent) node.getParent();
		}
		// this cast is save because if we got here node is a Parent
		return (Parent) node;
	}

	public Element getElementForInsertionAt(final int offset) {
		final Element parent = getParentElement(getChildAt(offset));
		if (parent == null) {
			return null;
		}
		if (offset == parent.getStartOffset()) {
			return parent.getParentElement();
		}
		return parent;
	}

	private static Element getParentElement(final INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof Element) {
			return (Element) node;
		}
		return getParentElement(node.getParent());
	}

	private IParent getParentAt(final int offset) {
		final INode child = getChildAt(offset);
		if (child instanceof IParent) {
			return (IParent) child;
		}
		return child.getParent();
	}

	public boolean isTagAt(final int offset) {
		return getContent().isTagMarker(offset);
	}

	public DocumentFragment getFragment(final ContentRange range) {
		final IParent parent = getParentOfRange(range);
		final DeepCopy deepCopy = new DeepCopy(parent, range);
		return new DocumentFragment(deepCopy.getContent(), deepCopy.getNodes());
	}

	private IParent getParentOfRange(final ContentRange range) {
		Assert.isTrue(getRange().contains(range));

		final INode startNode = getChildAt(range.getStartOffset());
		final INode endNode = getChildAt(range.getEndOffset());
		final IParent parent = startNode.getParent();
		Assert.isTrue(parent == endNode.getParent(), MessageFormat.format("The fragment in {0} is unbalanced.", range));
		Assert.isNotNull(parent, MessageFormat.format("No balanced parent found for {0}", range));

		return parent;
	}

	public List<? extends INode> getNodes(final ContentRange range) {
		return getParentOfRange(range).children().in(range).asList();
	}

	/*
	 * Events
	 */

	public void addDocumentListener(final IDocumentListener listener) {
		listeners.add(listener);
	}

	public void removeDocumentListener(final IDocumentListener listener) {
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

	/*
	 * Internal Helper Classes
	 */

	private static class NamespacePrefixGenerator implements Iterator<String> {
		final IElement element;
		final String prefix;
		int namespaceIndex = 1;

		public NamespacePrefixGenerator(final IElement element, final String prefix) {
			this.element = element;
			this.prefix = prefix;
		}

		public boolean hasNext() {
			return true;
		}

		public String next() {
			final String result = prefix + namespaceIndex++;
			if (!element.getNamespacePrefixes().contains(result)) {
				return result;
			}
			return next();
		}

		public void remove() {
			throw new UnsupportedOperationException("remove not supported");
		}

	}

}
