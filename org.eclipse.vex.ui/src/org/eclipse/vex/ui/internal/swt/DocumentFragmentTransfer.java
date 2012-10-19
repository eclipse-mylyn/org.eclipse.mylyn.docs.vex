/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.swt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.vex.core.internal.dom.Attribute;
import org.eclipse.vex.core.internal.dom.BaseNodeVisitor;
import org.eclipse.vex.core.internal.dom.Content;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.DocumentValidationException;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.GapContent;
import org.eclipse.vex.core.internal.dom.Node;

/**
 * Transfer object that handles Vex DocumentFragments.
 */
public class DocumentFragmentTransfer extends ByteArrayTransfer {

	private static final String MIME_TYPE = "application/x-vex-document-fragment";

	/**
	 * Returns the singleton instance of the DocumentFragmentTransfer.
	 */
	public static DocumentFragmentTransfer getInstance() {
		if (instance == null) {
			instance = new DocumentFragmentTransfer();
		}
		return instance;
	}

	@Override
	protected String[] getTypeNames() {
		return typeNames;
	}

	@Override
	protected int[] getTypeIds() {
		return typeIds;
	}

	@Override
	public void javaToNative(final Object object, final TransferData transferData) {
		if (object == null || !(object instanceof DocumentFragment)) {
			return;
		}

		if (isSupportedType(transferData)) {
			final DocumentFragment frag = (DocumentFragment) object;
			try {
				// write data to a byte array and then ask super to convert to
				// pMedium
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final ObjectOutputStream oos = new ObjectOutputStream(out);
				writeFragment(frag, oos);
				final byte[] buffer = out.toByteArray();
				oos.close();
				super.javaToNative(buffer, transferData);
			} catch (final IOException e) {
			}
		}
	}

	@Override
	public Object nativeToJava(final TransferData transferData) {

		if (isSupportedType(transferData)) {
			final byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}

			try {
				final ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				final ObjectInputStream ois = new ObjectInputStream(in);
				final Object object = readFragment(ois);
				ois.close();
				return object;
			} catch (final ClassNotFoundException ex) {
				return null;
			} catch (final IOException ex) {
				return null;
			}
		}

		return null;
	}

	// =================================================== PRIVATE

	private static final String[] typeNames = { MIME_TYPE };
	private static final int[] typeIds = { ByteArrayTransfer.registerType(MIME_TYPE) };

	private static DocumentFragmentTransfer instance;

	private DocumentFragmentTransfer() {
	}

	private void writeFragment(final DocumentFragment fragment, final ObjectOutputStream out) throws IOException {
		writeContent(fragment.getContent(), out);
		for (final Node node : fragment.getNodes()) {
			node.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Element element) {
					try {
						writeElement(element, out);
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private static void writeContent(final Content content, final ObjectOutputStream out) throws IOException {
		final int contentLength = content.length();
		out.write(contentLength);
		for (int i = 0; i < contentLength; i++) {
			if (content.isElementMarker(i)) {
				out.writeUTF("\0"); // This internal representation of element markers has nothing to do with the internal representation in GapContent.
			} else {
				out.writeUTF(content.getText(i, i));
			}
		}
	}

	private static void writeElement(final Element element, final ObjectOutputStream out) throws IOException {
		out.writeObject(element.getQualifiedName());
		out.writeInt(element.getStartOffset());
		out.writeInt(element.getEndOffset());
		final Collection<Attribute> attributes = element.getAttributes();
		out.writeInt(attributes.size());
		for (final Attribute attribute : attributes) {
			out.writeObject(attribute.getQualifiedName());
			out.writeObject(attribute.getValue());
		}
		final List<Element> children = element.getChildElements();
		out.writeInt(children.size());
		for (int i = 0; i < children.size(); i++) {
			writeElement(children.get(i), out);
		}
	}

	private DocumentFragment readFragment(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		final Content content = readContent(in);
		final int n = in.readInt();
		final ArrayList<Node> nodes = new ArrayList<Node>(n);
		for (int i = 0; i < n; i++) {
			nodes.add(readElement(in, content));
		}
		return new DocumentFragment(content, nodes);
	}

	private static Content readContent(final ObjectInputStream in) throws IOException {
		final int contentLength = in.readInt();
		final Content result = new GapContent(contentLength);
		for (int i = 0; i < contentLength; i++) {
			final String input = in.readUTF();
			if ("\0".equals(input)) { // This internal representation of element markers has nothing to do with the internal representation in GapContent.
				result.insertElementMarker(i);
			} else {
				result.insertText(i, input);
			}
		}
		return result;
	}

	private static Element readElement(final ObjectInputStream in, final Content content) throws IOException, ClassNotFoundException {
		final QualifiedName elementName = createQualifiedName(in.readObject());
		final int startOffset = in.readInt();
		final int endOffset = in.readInt();
		final Element element = new Element(elementName);
		element.associate(content, startOffset, endOffset);

		final int attrCount = in.readInt();
		for (int i = 0; i < attrCount; i++) {
			final QualifiedName attributeName = createQualifiedName(in.readObject());
			final String value = (String) in.readObject();
			try {
				element.setAttribute(attributeName, value);
			} catch (final DocumentValidationException e) {
				// Should never happen; there ain't no document
				e.printStackTrace();
			}
		}

		final int childCount = in.readInt();
		for (int i = 0; i < childCount; i++) {
			final Element child = readElement(in, content);
			child.setParent(element);
			element.insertChild(i, child);
		}

		return element;
	}

	private static QualifiedName createQualifiedName(final Object object) {
		final String serializedQualifiedName = object.toString();
		final int localNameStartIndex = serializedQualifiedName.lastIndexOf(':') + 1;
		if (localNameStartIndex == 0) {
			return new QualifiedName(null, serializedQualifiedName);
		}
		final String qualifier = serializedQualifiedName.substring(0, localNameStartIndex - 1);
		final String localName = serializedQualifiedName.substring(localNameStartIndex);
		return new QualifiedName(qualifier, localName);
	}

}
