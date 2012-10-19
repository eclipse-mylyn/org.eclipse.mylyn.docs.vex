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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.ListenerList;
import org.eclipse.vex.core.internal.undo.CannotRedoException;
import org.eclipse.vex.core.internal.undo.CannotUndoException;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;

/**
 * Represents an XML document.
 */
public class Document extends Parent {

	private final Element rootElement;
	private final ListenerList<DocumentListener, DocumentEvent> listeners = new ListenerList<DocumentListener, DocumentEvent>(DocumentListener.class);
	private boolean undoEnabled = true;

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
		associate(content, 0, 1);

		this.rootElement = rootElement;
		addChild(rootElement);
		rootElement.associate(content, 0, 1);
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
		associate(content, 0, content.length() - 1);
		this.rootElement = rootElement;
		addChild(rootElement);
	}

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
	public String getBaseURI() {
		return getDocumentURI();
	}

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

	public boolean isUndoEnabled() {
		return undoEnabled;
	}

	public void setUndoEnabled(final boolean undoEnabled) {
		this.undoEnabled = undoEnabled;
	}

	public void addDocumentListener(final DocumentListener listener) {
		listeners.add(listener);
	}

	public void removeDocumentListener(final DocumentListener listener) {
		listeners.remove(listener);
	}

	public int getLength() {
		return getContent().length();
	}

	private boolean canInsertAt(final int offset, final QualifiedName... nodeNames) {
		return canInsertAt(offset, Arrays.asList(nodeNames));
	}

	private boolean canInsertAt(final int offset, final List<QualifiedName> nodeNames) {
		if (validator == null) {
			return true;
		}

		final Element parent = getElementAt(offset);
		final List<QualifiedName> seq1 = getNodeNames(parent.getChildNodesBefore(offset));
		final List<QualifiedName> seq2 = nodeNames;
		final List<QualifiedName> seq3 = getNodeNames(parent.getChildNodesAfter(offset));

		return validator.isValidSequence(parent.getQualifiedName(), seq1, seq2, seq3, true);
	}

	public boolean canInsertFragment(final int offset, final DocumentFragment fragment) {
		return canInsertAt(offset, fragment.getNodeNames());
	}

	public boolean canInsertText(final int offset) {
		return canInsertAt(offset, Validator.PCDATA);
	}

	public Position createPosition(final int offset) {
		return getContent().createPosition(offset);
	}

	public void delete(final int startOffset, final int endOffset) throws DocumentValidationException {
		final Element surroundingElement = getElementAt(startOffset - 1);
		final Element elementAtEndOffset = getElementAt(endOffset + 1);
		if (surroundingElement != elementAtEndOffset) {
			throw new IllegalArgumentException("Deletion from " + startOffset + " to " + endOffset + " is unbalanced");
		}

		final Validator validator = getValidator();
		if (validator != null) {
			final List<QualifiedName> seq1 = getNodeNames(surroundingElement.getChildNodesBefore(startOffset));
			final List<QualifiedName> seq2 = getNodeNames(surroundingElement.getChildNodesAfter(endOffset));

			if (!validator.isValidSequence(surroundingElement.getQualifiedName(), seq1, seq2, null, true)) {
				throw new DocumentValidationException("Unable to delete from " + startOffset + " to " + endOffset);
			}
		}

		// Grab the fragment for the undoable edit while it's still here
		final DocumentFragment frag = getFragment(startOffset, endOffset);

		fireBeforeContentDeleted(new DocumentEvent(this, surroundingElement, startOffset, endOffset - startOffset + 1, null));

		final Iterator<Node> iter = surroundingElement.getChildIterator();
		while (iter.hasNext()) {
			final Node child = iter.next();
			if (startOffset <= child.getStartOffset() && child.getEndOffset() <= endOffset) {
				surroundingElement.removeChild(child);
			}
		}

		getContent().remove(startOffset, endOffset - startOffset + 1);

		final IUndoableEdit edit = undoEnabled ? new DeleteEdit(startOffset, endOffset, frag) : null;

		fireContentDeleted(new DocumentEvent(this, surroundingElement, startOffset, endOffset - startOffset + 1, edit));
	}

	public char getCharacterAt(final int offset) {
		final String text = getContent().getText(offset, offset);
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
				if (offset1 >= child.getStartOffset() && offset2 >= child.getStartOffset() && offset1 <= child.getEndOffset() && offset2 <= child.getEndOffset()) {

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
		return getContainerElement(getChildNodeAt(offset));
	}

	private static Element getContainerElement(final Node node) {
		if (node == null) {
			return null;
		}
		if (node instanceof Element) {
			return (Element) node;
		}
		return getContainerElement(node.getParent());
	}

	public boolean isElementAt(final int offset) {
		return getContent().isElementMarker(offset);
	}

	public DocumentFragment getFragment(final int startOffset, final int endOffset) {
		final List<Node> childNodes = getParentOfRange(startOffset, endOffset).getChildNodes();

		final Content cloneContent = getContent().getContent(startOffset, endOffset);
		final List<Node> cloneChildNodes = new ArrayList<Node>();

		for (final Node child : childNodes) {
			if (child.getEndOffset() <= startOffset) {
				continue;
			} else if (child.getStartOffset() >= endOffset) {
				break;
			} else {
				final Node cloneChildNode = cloneNode(child, cloneContent, -startOffset, null);
				if (cloneChildNode != null) {
					cloneChildNodes.add(cloneChildNode);
				}
			}
		}

		return new DocumentFragment(cloneContent, cloneChildNodes);
	}

	private Parent getParentOfRange(final int startOffset, final int endOffset) {
		Assert.isTrue(containsOffset(startOffset));
		Assert.isTrue(containsOffset(endOffset));
		Assert.isTrue(startOffset <= endOffset, MessageFormat.format("Invalid range [{0};{1}].", startOffset, endOffset));

		final Node startNode = getChildNodeAt(startOffset);
		final Node endNode = getChildNodeAt(endOffset);
		final Parent parent = startNode.getParent();
		Assert.isTrue(parent == endNode.getParent(), MessageFormat.format("The fragment from {0} to {1} is unbalanced.", startOffset, endOffset));
		Assert.isNotNull(parent, MessageFormat.format("No balanced parent found for range [{0};{1}].", startOffset, endOffset));

		return parent;
	}

	private Node cloneNode(final Node original, final Content content, final int shift, final Parent parent) {
		final Node result[] = new Node[1];
		original.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final Element element) {
				result[0] = cloneElement(element, content, shift, parent);
			}
		});

		return result[0];
	}

	private Element cloneElement(final Element original, final Content content, final int shift, final Parent parent) {
		final Element clone = original.clone();
		clone.associate(content, original.getStartOffset() + shift, original.getEndOffset() + shift);
		clone.setParent(parent);

		final List<Node> children = original.getChildNodes();
		for (final Node child : children) {
			final Node cloneChild = cloneNode(child, content, shift, clone);
			if (cloneChild != null) {
				clone.addChild(cloneChild);
			}
		}

		return clone;
	}

	public static List<QualifiedName> getNodeNames(final Collection<Node> nodes) {
		final List<QualifiedName> names = new ArrayList<QualifiedName>(nodes.size());

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

	public List<Node> getNodes(final int startOffset, final int endOffset) {
		return getParentOfRange(startOffset, endOffset).getChildNodes(startOffset, endOffset);
	}

	public void insertElement(final int offset, final Element element) throws DocumentValidationException {

		if (offset < 1 || offset >= getLength()) {
			throw new IllegalArgumentException("Error inserting element <" + element.getPrefixedName() + ">: offset is " + offset + ", but it must be between 1 and " + (getLength() - 1));
		}

		if (!canInsertAt(offset, element.getQualifiedName())) {
			throw new DocumentValidationException("Cannot insert element " + element.getPrefixedName() + " at offset " + offset);
		}

		// find the parent, and the index into its children at which
		// this element should be inserted
		Element parent = rootElement;
		int childIndex = -1;
		while (childIndex == -1) {
			boolean tryAgain = false;
			final List<Element> children = parent.getChildElements();
			for (int i = 0; i < children.size(); i++) {
				final Element child = children.get(i);
				if (offset <= child.getStartOffset()) {
					childIndex = i;
					break;
				} else if (offset <= child.getEndOffset()) {
					parent = child;
					tryAgain = true;
					break;
				}
			}
			if (!tryAgain && childIndex == -1) {
				childIndex = children.size();
				break;
			}
		}

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		getContent().insertElementMarker(offset);
		getContent().insertElementMarker(offset);

		element.associate(getContent(), offset, offset + 1);
		element.setParent(parent);
		parent.insertChild(childIndex, element);

		final IUndoableEdit edit = undoEnabled ? new InsertElementEdit(offset, element) : null;

		fireContentInserted(new DocumentEvent(this, parent, offset, 2, edit));
	}

	public void insertFragment(final int offset, final DocumentFragment fragment) throws DocumentValidationException {
		if (offset < 1 || offset >= getLength()) {
			throw new IllegalArgumentException("Error inserting document fragment");
		}

		if (!canInsertAt(offset, fragment.getNodeNames())) {
			throw new DocumentValidationException("Cannot insert document fragment");
		}

		final Element parent = getElementAt(offset);

		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		getContent().insertContent(offset, fragment.getContent());

		final List<Element> children = parent.getChildElements();
		int index = 0;
		while (index < children.size() && children.get(index).getEndOffset() < offset) {
			index++;
		}

		final List<Element> elements = fragment.getElements();
		for (int i = 0; i < elements.size(); i++) {
			final Element newElement = cloneElement(elements.get(i), getContent(), offset, parent);
			parent.insertChild(index, newElement);
			index++;
		}

		final IUndoableEdit edit = undoEnabled ? new InsertFragmentEdit(offset, fragment) : null;

		fireContentInserted(new DocumentEvent(this, parent, offset, fragment.getContent().length(), edit));
	}

	public void insertText(final int offset, final String text) throws DocumentValidationException {
		if (offset < 1 || offset >= getLength()) {
			throw new IllegalArgumentException("Offset must be between 1 and n-1");
		}

		final boolean isValid;
		if (!getContent().isElementMarker(offset - 1)) {
			isValid = true;
		} else if (!getContent().isElementMarker(offset)) {
			isValid = true;
		} else {
			isValid = canInsertAt(offset, Validator.PCDATA);
		}

		if (!isValid) {
			throw new DocumentValidationException("Cannot insert text '" + text + "' at offset " + offset);
		}

		// Convert control chars to spaces
		final StringBuffer sb = new StringBuffer(text);
		for (int i = 0; i < sb.length(); i++) {
			if (Character.isISOControl(sb.charAt(i)) && sb.charAt(i) != '\n') {
				sb.setCharAt(i, ' ');
			}
		}
		final String s = sb.toString();

		final Element parent = getElementAt(offset);
		fireBeforeContentInserted(new DocumentEvent(this, parent, offset, 2, null));

		getContent().insertText(offset, s);

		final IUndoableEdit edit = undoEnabled ? new InsertTextEdit(offset, s) : null;

		fireContentInserted(new DocumentEvent(this, parent, offset, s.length(), edit));
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

	// TODO move undoable edits int L2

	private class DeleteEdit implements IUndoableEdit {

		private final int startOffset;
		private final int endOffset;
		private final DocumentFragment frag;

		public DeleteEdit(final int startOffset, final int endOffset, final DocumentFragment frag) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
			this.frag = frag;
		}

		public boolean combine(final IUndoableEdit edit) {
			return false;
		}

		public void undo() throws CannotUndoException {
			try {
				setUndoEnabled(false);
				insertFragment(startOffset, frag);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			try {
				setUndoEnabled(false);
				delete(startOffset, endOffset);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

	}

	private class InsertElementEdit implements IUndoableEdit {

		private final int offset;
		private final Element element;

		public InsertElementEdit(final int offset, final Element element2) {
			this.offset = offset;
			element = element2;
		}

		public boolean combine(final IUndoableEdit edit) {
			return false;
		}

		public void undo() throws CannotUndoException {
			try {
				setUndoEnabled(false);
				delete(offset, offset + 2);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			try {
				setUndoEnabled(false);
				insertElement(offset, element);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

	}

	private class InsertFragmentEdit implements IUndoableEdit {

		private final int offset;
		private final DocumentFragment frag;

		public InsertFragmentEdit(final int offset, final DocumentFragment frag) {
			this.offset = offset;
			this.frag = frag;
		}

		public boolean combine(final IUndoableEdit edit) {
			return false;
		}

		public void undo() throws CannotUndoException {
			try {
				setUndoEnabled(false);
				final int length = frag.getContent().length();
				delete(offset, offset + length - 1);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			try {
				setUndoEnabled(false);
				insertFragment(offset, frag);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

	}

	private class InsertTextEdit implements IUndoableEdit {

		private final int offset;
		private String text;

		public InsertTextEdit(final int offset, final String text) {
			this.offset = offset;
			this.text = text;
		}

		public boolean combine(final IUndoableEdit edit) {
			if (edit instanceof InsertTextEdit) {
				final InsertTextEdit ite = (InsertTextEdit) edit;
				if (ite.offset == offset + text.length()) {
					text = text + ite.text;
					return true;
				}
			}
			return false;
		}

		public void undo() throws CannotUndoException {
			try {
				setUndoEnabled(false);
				delete(offset, offset + text.length());
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

		public void redo() throws CannotRedoException {
			try {
				setUndoEnabled(false);
				insertText(offset, text);
			} catch (final DocumentValidationException ex) {
				throw new CannotUndoException();
			} finally {
				setUndoEnabled(true);
			}
		}

	}

}
