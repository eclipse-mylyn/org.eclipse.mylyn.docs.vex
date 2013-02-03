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
package org.eclipse.vex.core.internal.io;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Content;
import org.eclipse.vex.core.internal.dom.ContentRange;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentContentModel;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.GapContent;
import org.eclipse.vex.core.internal.dom.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A SAX handler that builds a Vex document. This builder collapses whitespace as it goes, according to the following
 * rules.
 * 
 * <ul>
 * <li>Elements with style white-space: pre are left alone.</li>
 * <li>Runs of whitespace are replaced with a single space.</li>
 * <li>Space just inside the start and end of elements is removed.</li>
 * <li>Space just outside the start and end of block-formatted elements is removed.</li>
 * </ul>
 */
public class DocumentBuilder implements ContentHandler, LexicalHandler {

	private final DocumentContentModel documentContentModel;

	private IWhitespacePolicy policy;

	// Holds pending characters until we see another element boundary.
	// This is (a) so we can collapse spaces in multiple adjacent character
	// blocks, and (b) so we can trim trailing whitespace, if necessary.
	private final StringBuilder pendingChars = new StringBuilder();

	// If true, trim the leading whitespace from the next received block of
	// text.
	private boolean trimLeading = false;

	// Content object to hold document content
	private final Content content = new GapContent(100);

	// Stack of StackElement objects
	private final LinkedList<StackEntry> stack = new LinkedList<StackEntry>();

	private final NamespaceStack namespaceStack = new NamespaceStack();

	private final List<Node> nodesBeforeRoot = new ArrayList<Node>();
	private final List<Node> nodesAfterRoot = new ArrayList<Node>();

	private boolean inDTD = false;

	private Element rootElement;

	private final String baseUri;
	private String dtdPublicID;
	private String dtdSystemID;
	private Document document;
	private Locator locator;

	public DocumentBuilder(final String baseUri, final DocumentContentModel documentContentModel) {
		this.baseUri = baseUri;
		this.documentContentModel = documentContentModel;
	}

	/**
	 * Returns the newly built <code>Document</code> object.
	 */
	public Document getDocument() {
		return document;
	}

	// ============================================= ContentHandler methods

	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		appendPendingCharsFiltered(ch, start, length);
	}

	private void appendPendingCharsFiltered(final char[] ch, final int start, final int length) {
		// Convert control characters to spaces, since we use nulls for element delimiters
		for (int i = start; i < start + length; i++) {
			if (isControlCharacter(ch[i])) {
				pendingChars.append(' ');
			} else {
				pendingChars.append(ch[i]);
			}
		}
	}

	private static boolean isControlCharacter(final char ch) {
		return Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t';
	}

	public void endDocument() {
		if (rootElement == null) {
			return;
		}

		document = new Document(content, rootElement);
		document.setPublicID(dtdPublicID);
		document.setSystemID(dtdSystemID);

		for (final Node node : nodesBeforeRoot) {
			document.insertChildBefore(document.getRootElement(), node);
		}

		for (final Node node : nodesAfterRoot) {
			document.addChild(node);
		}
	}

	public void endElement(final String namespaceURI, final String localName, final String qName) {
		appendChars(true);

		final StackEntry entry = stack.removeLast();

		// we must insert the trailing sentinel first, else the insertion
		// pushes the end position of the element to after the sentinel
		content.insertTagMarker(content.length());
		entry.element.associate(content, new ContentRange(entry.offset, content.length() - 1));

		if (isBlock(entry.element)) {
			trimLeading = true;
		}
	}

	public void endPrefixMapping(final String prefix) {
	}

	public void ignorableWhitespace(final char[] ch, final int start, final int length) {
	}

	public void processingInstruction(final String target, final String data) {
	}

	public void setDocumentLocator(final Locator locator) {
		this.locator = locator;
	}

	public void skippedEntity(final java.lang.String name) {
	}

	public void startDocument() {
	}

	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) throws SAXException {
		final QualifiedName elementName;
		if ("".equals(namespaceURI)) {
			elementName = new QualifiedName(null, qName);
		} else {
			elementName = new QualifiedName(namespaceURI, localName);
		}
		Element element;
		if (stack.isEmpty()) {
			rootElement = new Element(elementName);
			element = rootElement;
		} else {
			element = new Element(elementName);

			final Element parent = stack.getLast().element;
			parent.addChild(element);
		}

		final String defaultNamespaceUri = namespaceStack.peekDefault();
		if (defaultNamespaceUri != null) {
			element.declareDefaultNamespace(defaultNamespaceUri);
		}

		for (final String prefix : namespaceStack.getPrefixes()) {
			element.declareNamespace(prefix, namespaceStack.peek(prefix));
		}

		final int n = attrs.getLength();
		for (int i = 0; i < n; i++) {
			final QualifiedName attributeName;
			if ("".equals(attrs.getLocalName(i))) {
				attributeName = new QualifiedName(null, attrs.getQName(i));
			} else if ("".equals(attrs.getURI(i))) {
				attributeName = new QualifiedName(elementName.getQualifier(), attrs.getLocalName(i));
			} else {
				attributeName = new QualifiedName(attrs.getURI(i), attrs.getLocalName(i));
			}
			try {
				element.setAttribute(attributeName, attrs.getValue(i));
			} catch (final DocumentValidationException e) {
				throw new SAXParseException("DocumentValidationException", locator, e);
			}
		}

		if (stack.isEmpty() && documentContentModel != null) {
			documentContentModel.initialize(baseUri, dtdPublicID, dtdSystemID, rootElement);
			policy = documentContentModel.getWhitespacePolicy();
		}

		appendChars(isBlock(element));

		stack.add(new StackEntry(element, content.length(), isPre(element)));
		content.insertTagMarker(content.length());

		trimLeading = true;

		namespaceStack.clear();
	}

	public void startPrefixMapping(final String prefix, final String uri) {
		checkPrefix(prefix);
		if (isDefaultPrefix(prefix)) {
			namespaceStack.pushDefault(uri);
		} else {
			namespaceStack.push(prefix, uri);
		}
	}

	private static void checkPrefix(final String prefix) {
		Assert.isNotNull(prefix, "null is not a valid namespace prefix.");
	}

	private static boolean isDefaultPrefix(final String prefix) {
		return "".equals(prefix);
	}

	// ============================================== LexicalHandler methods

	public void comment(final char[] ch, final int start, final int length) {
		if (inDTD) {
			return;
		}

		if (isBeforeRoot()) {
			final Comment comment = new Comment();
			final int startOffset = content.length();
			content.insertTagMarker(content.length());

			trimLeading = true;
			appendPendingCharsFiltered(ch, start, length);
			appendChars(true);

			content.insertTagMarker(content.length());
			comment.associate(content, new ContentRange(startOffset, content.length() - 1));
			if (isBlock(comment)) {
				trimLeading = true;
			}

			nodesBeforeRoot.add(comment);
		} else if (isAfterRoot()) {
			final Comment comment = new Comment();
			final int startOffset = content.length();
			content.insertTagMarker(content.length());

			trimLeading = true;
			appendPendingCharsFiltered(ch, start, length);
			appendChars(true);

			content.insertTagMarker(content.length());
			comment.associate(content, new ContentRange(startOffset, content.length() - 1));
			if (isBlock(comment)) {
				trimLeading = true;
			}

			nodesAfterRoot.add(comment);
		} else {
			final Comment comment = new Comment();
			final Element parent = stack.getLast().element;
			parent.addChild(comment);

			appendChars(isBlock(comment));
			final int startOffset = content.length();
			content.insertTagMarker(content.length());

			trimLeading = true;
			appendPendingCharsFiltered(ch, start, length);
			appendChars(true);

			content.insertTagMarker(content.length());
			comment.associate(content, new ContentRange(startOffset, content.length() - 1));
			if (isBlock(comment)) {
				trimLeading = true;
			}
		}
	}

	private boolean isBeforeRoot() {
		return stack.isEmpty() && rootElement == null;
	}

	private boolean isAfterRoot() {
		return stack.isEmpty() && rootElement != null;
	}

	public void endCDATA() {
	}

	public void endDTD() {
		inDTD = false;
	}

	public void endEntity(final String name) {
	}

	public void startCDATA() {
	}

	public void startDTD(final String name, final String publicId, final String systemId) {
		dtdPublicID = publicId;
		dtdSystemID = systemId;
		inDTD = true;
	}

	public void startEntity(final String name) {
	}

	// ======================================================== PRIVATE

	// Append any pending characters to the content
	private void appendChars(final boolean trimTrailing) {

		StringBuilder sb;

		sb = cleanUpTextContent(trimTrailing);

		content.insertText(content.length(), sb.toString());

		pendingChars.setLength(0);
		trimLeading = false;
	}

	private StringBuilder cleanUpTextContent(final boolean trimTrailing) {
		StringBuilder sb;
		final StackEntry entry = stack.isEmpty() ? null : stack.getLast();

		if (entry != null && entry.pre) {
			sb = pendingChars;
		} else {

			// collapse the space in the pending characters
			sb = new StringBuilder(pendingChars.length());
			boolean ws = false; // true if we're in a run of whitespace
			for (int i = 0; i < pendingChars.length(); i++) {
				final char c = pendingChars.charAt(i);
				if (Character.isWhitespace(c)) {
					ws = true;
				} else {
					if (ws) {
						sb.append(' ');
						ws = false;
					}
					sb.append(c);
				}
			}
			if (ws) {
				sb.append(' ');
			}
			// trim leading and trailing space, if necessary
			if (trimLeading && sb.length() > 0 && sb.charAt(0) == ' ') {
				sb.deleteCharAt(0);
			}
			if (trimTrailing && sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
				sb.setLength(sb.length() - 1);
			}
		}

		normalizeNewlines(sb);
		return sb;
	}

	private boolean isBlock(final Node node) {
		return policy != null && policy.isBlock(node);
	}

	private boolean isPre(final Node node) {
		return policy != null && policy.isPre(node);
	}

	/**
	 * Convert lines that end in CR and CRLFs to plain newlines.
	 * 
	 * @param sb
	 *            StringBuffer to be normalized.
	 */
	private void normalizeNewlines(final StringBuilder sb) {

		// State machine states
		final int START = 0;
		final int SEEN_CR = 1;

		int state = START;
		int i = 0;
		while (i < sb.length()) {
			// No simple 'for' here, since we may delete chars

			final char c = sb.charAt(i);

			switch (state) {
			case START:
				if (c == '\r') {
					state = SEEN_CR;
				}
				i++;
				break;

			case SEEN_CR:
				if (c == '\n') {
					// CR-LF, just delete the previous CR
					sb.deleteCharAt(i - 1);
					state = START;
					// no need to advance i, since it's done implicitly
				} else if (c == '\r') {
					// CR line ending followed by another
					// Replace the first with a newline...
					sb.setCharAt(i - 1, '\n');
					i++;
					// ...and stay in the SEEN_CR state
				} else {
					// CR line ending, replace it with a newline
					sb.setCharAt(i - 1, '\n');
					i++;
					state = START;
				}
			}
		}

		if (state == SEEN_CR) {
			// CR line ending, replace it with a newline
		}
	}

	private static class StackEntry {
		public Element element;
		public int offset;
		public boolean pre;

		public StackEntry(final Element element, final int offset, final boolean pre) {
			this.element = element;
			this.offset = offset;
			this.pre = pre;
		}
	}
}
