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
 *     Carsten Hiesserich - writeNoWrap(DocumentFragment) method
 *     Carsten Hiesserich - added processing instructions support
 *     Carsten Hiesserich - use org.eclipse.jface.text.IDOcument as intermediate
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.DocumentTextPosition;
import org.eclipse.vex.core.provisional.dom.AttributeDefinition;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * Writes a document to an output stream, using a stylesheet to provide formatting hints.
 *
 * <ul>
 * <li>Children of an element are indented by a configurable amount.</li>
 * <li>Text is wrapped to fit within a configurable width.
 * <li>
 * </ul>
 *
 * <p>
 * Documents are currently saved UTF-8 encoding, with no encoding specified in the XML declaration.
 * </p>
 */
public class DocumentWriter {

	private final String newLine = System.getProperty("line.separator");

	private IWhitespacePolicy whitespacePolicy;
	private String indent;
	private int wrapColumn;

	/** The INode that contains the editing caret */
	private INode nodeAtCaret;
	/** Stores the start offset when writing the node */
	private int startOffsetOfCaretNode = 0;

	public DocumentWriter() {
		indent = "  ";
		wrapColumn = 72;
		whitespacePolicy = IWhitespacePolicy.NULL;
	}

	/**
	 * Escapes special XML characters. Changes '<', '>', and '&' to
	 * '&lt;', '&gt;' and '&amp;', respectively.
	 *
	 * @param s the string to be escaped.
	 * @return the escaped string
	 */
	public static String escape(final String s) {
		final StringBuilder result = new StringBuilder(s.length());

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '<') {
				result.append("&lt;");
			} else if (c == '>') {
				result.append("&gt;");
			} else if (c == '&') {
				result.append("&amp;");
			} else if (c == '"') {
				result.append("&quot;");
			} else if (c == '\'') {
				result.append("&apos;");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * @return the indent string. By default this is two spaces.
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the value of the indent string.
	 *
	 * @param indent
	 *            new value for the indent string.
	 */
	public void setIndent(final String indent) {
		this.indent = indent;
	}

	/**
	 * @return the whitespace policy used by this writer.
	 */
	public IWhitespacePolicy getWhitespacePolicy() {
		return whitespacePolicy;
	}

	/**
	 * Sets the whitespace policy for this writer. The whitespace policy tells the writer which elements are
	 * block-formatted and which are pre-formatted.
	 *
	 * @param whitespacePolicy
	 *            The whitespacePolicy to set.
	 */
	public void setWhitespacePolicy(final IWhitespacePolicy whitespacePolicy) {
		if (whitespacePolicy == null) {
			this.whitespacePolicy = IWhitespacePolicy.NULL;
		} else {
			this.whitespacePolicy = whitespacePolicy;
		}
	}

	/**
	 * @return the column at which text should be wrapped. By default this is 72.
	 */
	public int getWrapColumn() {
		return wrapColumn;
	}

	/**
	 * Sets the value of the wrap column.
	 *
	 * @param wrapColumn
	 *            new value for the wrap column.
	 */
	public void setWrapColumn(final int wrapColumn) {
		this.wrapColumn = wrapColumn;
	}

	public void write(final IDocument document, final OutputStream out) throws IOException {
		final org.eclipse.jface.text.Document doc = new org.eclipse.jface.text.Document();
		docPrintln(doc, "<?xml version='1.0' encoding='UTF-8'?>");
		writeNode(document, doc, "");

		writeToOutputStream(out, doc);
	}

	public void write(final IDocumentFragment fragment, final OutputStream out) throws IOException {
		final org.eclipse.jface.text.Document doc = new org.eclipse.jface.text.Document();
		docPrintln(doc, "<?xml version='1.0' encoding='UTF-8'?>");
		writeNode(fragment, doc, "");

		writeToOutputStream(out, doc);
	}

	public void writeNoWrap(final IDocumentFragment fragment, final OutputStream out) throws IOException {
		final org.eclipse.jface.text.Document doc = new org.eclipse.jface.text.Document();

		for (final INode child : fragment.children()) {
			writeNodeNoWrap(child, doc);
		}

		writeToOutputStream(out, doc);
	}

	/**
	 * Write the document to the given {@link org.eclipse.jface.text.IDocument}. The document is cleaed before the
	 * content written.<br />
	 * While writing the document a Position is created to track the caret when external changes occur.
	 *
	 * @param document
	 * @param doc
	 * @param nodeAtCaret
	 *            The node that currently contains the editing caret.
	 * @return The Position of the given <code>nodeAtCaret</code>. (not added to the doc)
	 */
	public DocumentTextPosition write(final IDocument document, final org.eclipse.jface.text.IDocument doc, final INode nodeAtCaret) {
		this.nodeAtCaret = nodeAtCaret;
		doc.set("");
		writeNode(document, doc, "");
		return new DocumentTextPosition(startOffsetOfCaretNode);
	}

	// ====================================================== PRIVATE

	private void writeToOutputStream(final OutputStream out, final org.eclipse.jface.text.Document doc) throws IOException {
		final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		printWriter.print(doc.get());
		printWriter.flush();
	}

	private void writeNode(final INode node, final org.eclipse.jface.text.IDocument doc, final String indent) {
		checkCaretPosition(node, doc);

		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final IDocument document) {
				if (document.getSystemID() != null) {
					final StringBuilder buffer = new StringBuilder();
					buffer.append("<!DOCTYPE ");
					buffer.append(document.getRootElement().getPrefixedName());
					if (document.getPublicID() != null) {
						buffer.append(" PUBLIC");
						buffer.append(" \"");
						buffer.append(document.getPublicID());
						buffer.append("\"");
					} else {
						buffer.append(" SYSTEM");
					}
					buffer.append(" \"");
					buffer.append(document.getSystemID());
					buffer.append("\">");
					docPrintln(doc, buffer.toString());
				}

				for (final INode child : document.children()) {
					writeNode(child, doc, indent);
				}
			}

			@Override
			public void visit(final IDocumentFragment fragment) {
				docPrint(doc, "<vex_fragment>");
				for (final INode child : fragment.children()) {
					writeNodeNoWrap(child, doc);
				}
				docPrintln(doc, "</vex_fragment>");
			}

			@Override
			public void visit(final IElement element) {
				if (whitespacePolicy.isPre(element)) {
					docPrint(doc, indent);
					writeNodeNoWrap(node, doc);
					docPrintln(doc, "");
					return;
				}

				boolean hasBlockChild = false;
				for (final INode child : element.childElements()) {
					if (whitespacePolicy.isBlock(child)) {
						hasBlockChild = true;
						break;
					}
				}

				if (hasBlockChild) {
					docPrint(doc, indent);
					docPrint(doc, "<");
					docPrint(doc, element.getPrefixedName());

					final TextWrapper wrapper = new TextWrapper();
					wrapper.addNoSplit(getNamespaceDeclarationsString(element));
					wrapper.addNoSplit(getAttributeString(element));
					final int outdent = indent.length() + 1 + element.getPrefixedName().length();
					final String[] lines = wrapper.wrap(wrapColumn - outdent);
					final char[] bigIndent = new char[outdent];
					Arrays.fill(bigIndent, ' ');
					final String bigIndentString = new String(bigIndent);
					for (int i = 0; i < lines.length; i++) {
						if (i > 0) {
							docPrint(doc, new String(bigIndentString));
						}
						docPrint(doc, lines[i]);
						if (i < lines.length - 1) {
							docPrintln(doc, "");
						}
					}
					docPrintln(doc, ">");

					final String childIndent = indent + DocumentWriter.this.indent;
					for (final INode child : element.children()) {
						writeNode(child, doc, childIndent);
					}
					docPrint(doc, indent);
					docPrint(doc, "</");
					docPrint(doc, element.getPrefixedName());
					docPrintln(doc, ">");
				} else {
					final TextWrapper wrapper = new TextWrapper();
					addNode(element, wrapper, doc);
					final String[] lines = wrapper.wrap(wrapColumn - indent.length());
					for (final String line : lines) {
						docPrint(doc, indent);
						docPrintln(doc, line);
					}
				}
			}

			@Override
			public void visit(final IComment comment) {
				docPrint(doc, indent);
				docPrintln(doc, "<!--");

				final String childIndent = indent + DocumentWriter.this.indent;
				final TextWrapper wrapper = new TextWrapper();
				wrapper.add(escape(node.getText()));
				final String[] lines = wrapper.wrap(wrapColumn - childIndent.length());

				for (final String line : lines) {
					docPrint(doc, childIndent);
					docPrintln(doc, line);
				}

				docPrint(doc, indent);
				docPrintln(doc, "-->");
			}

			@Override
			public void visit(final IProcessingInstruction pi) {
				// Text in PI's is written as is with no wrapping
				docPrint(doc, indent);
				docPrint(doc, "<?");
				docPrint(doc, pi.getTarget() + " " + node.getText());
				docPrintln(doc, "?>");
			}

			@Override
			public void visit(final IText text) {
				final TextWrapper wrapper = new TextWrapper();
				wrapper.add(escape(node.getText()));

				final String[] lines = wrapper.wrap(wrapColumn - indent.length());

				for (final String line : lines) {
					docPrint(doc, indent);
					docPrintln(doc, line);
				}
			}
		});
	}

	private void writeNodeNoWrap(final INode node, final org.eclipse.jface.text.IDocument doc) {

		checkCaretPosition(node, doc);

		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final IElement element) {
				docPrint(doc, "<");
				docPrint(doc, element.getPrefixedName());
				docPrint(doc, getNamespaceDeclarationsString(element));
				docPrint(doc, getAttributeString(element));
				docPrint(doc, ">");

				for (final INode child : element.children()) {
					writeNodeNoWrap(child, doc);
				}

				docPrint(doc, "</");
				docPrint(doc, element.getPrefixedName());
				docPrint(doc, ">");
			}

			@Override
			public void visit(final IComment comment) {
				docPrint(doc, "<!--");
				docPrint(doc, escape(node.getText()));
				docPrint(doc, "-->");
			}

			@Override
			public void visit(final IProcessingInstruction pi) {
				docPrint(doc, "<?");
				docPrint(doc, pi.getTarget() + " " + node.getText());
				docPrint(doc, "?>");
			}

			@Override
			public void visit(final IText text) {
				docPrint(doc, escape(node.getText()));
			}
		});
	}

	private static String getNamespaceDeclarationsString(final IElement element) {
		final StringBuilder result = new StringBuilder();
		final String declaredNamespaceURI = element.getDeclaredDefaultNamespaceURI();
		if (declaredNamespaceURI != null) {
			result.append(" xmlns=\"").append(declaredNamespaceURI).append("\"");
		}
		for (final String prefix : element.getDeclaredNamespacePrefixes()) {
			result.append(" xmlns:").append(prefix).append("=\"").append(element.getNamespaceURI(prefix)).append("\"");
		}
		return result.toString();
	}

	private void addNode(final INode node, final TextWrapper wrapper, final org.eclipse.jface.text.IDocument doc) {

		checkCaretPosition(node, doc);

		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final IElement element) {
				final boolean elementHasChildren = element.hasChildren();

				final StringBuilder buffer = new StringBuilder();
				buffer.append("<").append(element.getPrefixedName());
				buffer.append(getNamespaceDeclarationsString(element));
				buffer.append(getAttributeString(element));

				if (elementHasChildren) {
					buffer.append(">");
				} else {
					buffer.append("/>");
				}
				wrapper.addNoSplit(buffer.toString());

				for (final INode child : element.children()) {
					addNode(child, wrapper, doc);
				}

				if (elementHasChildren) {
					wrapper.add("</" + element.getPrefixedName() + ">");
				}
			}

			@Override
			public void visit(final IComment comment) {
				wrapper.addNoSplit("<!--");
				wrapper.add(escape(node.getText()));
				wrapper.addNoSplit("-->");
			}

			@Override
			public void visit(final IProcessingInstruction pi) {
				// Text in PI's is written as is with no wrapping
				wrapper.addNoSplit("<?" + pi.getTarget() + " " + node.getText() + "?>");
			}

			@Override
			public void visit(final IText text) {
				wrapper.add(escape(node.getText()));
			}
		});
	}

	private static String getAttributeString(final IElement element) {
		final IDocument document = element.getDocument();
		final IValidator validator = document != null ? document.getValidator() : null;

		final StringBuffer result = new StringBuffer();
		for (final IAttribute attribute : element.getAttributes()) {
			if (!isAttributeDefaultValueSet(validator, attribute)) {
				result.append(" ");
				result.append(attribute.getPrefixedName());
				result.append("=\"");
				result.append(escape(attribute.getValue()));
				result.append("\"");
			}
		}
		return result.toString();
	}

	private static boolean isAttributeDefaultValueSet(final IValidator validator, final IAttribute attribute) {
		if (validator != null) {
			final AttributeDefinition attributeDefinition = validator.getAttributeDefinition(attribute);
			if (attributeDefinition != null) {
				final String currentValue = attribute.getValue();
				final String defaultValue = attributeDefinition.getDefaultValue();
				return currentValue.equals(defaultValue);
			}
		}
		return false;
	}

	private void checkCaretPosition(final INode node, final org.eclipse.jface.text.IDocument doc) {
		if (startOffsetOfCaretNode > 0) {
			// Offset already found
			return;
		}

		INode nodeToCheck;
		if (node instanceof IText) {
			nodeToCheck = node.getParent();
		} else {
			nodeToCheck = node;
		}

		if (nodeToCheck.equals(nodeAtCaret)) {
			// Store the start offset of the found node
			startOffsetOfCaretNode = doc.getLength();
		}
	}

	private void docPrint(final org.eclipse.jface.text.IDocument doc, final String text) {
		try {
			doc.replace(doc.getLength(), 0, text);
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void docPrintln(final org.eclipse.jface.text.IDocument doc, final String text) {
		try {
			doc.replace(doc.getLength(), 0, text + newLine);
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}
}
