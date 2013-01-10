/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - use XML reader/writer for serialization
 *******************************************************************************/
package org.eclipse.vex.ui.internal.swt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Transfer object that handles Vex DocumentFragments.
 */
public class DocumentFragmentTransfer extends ByteArrayTransfer {

	private static final String MIME_TYPE = "application/x-vex-document-fragment";

	private static final String[] typeNames = { MIME_TYPE };
	private static final int[] typeIds = { ByteArrayTransfer.registerType(MIME_TYPE) };

	private static DocumentFragmentTransfer instance;

	/**
	 * @return the singleton instance of the DocumentFragmentTransfer.
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

	// Writing

	@Override
	public void javaToNative(final Object object, final TransferData transferData) {
		if (object == null || !(object instanceof DocumentFragment)) {
			return;
		}

		if (!isSupportedType(transferData)) {
			return;
		}

		final DocumentFragment fragment = (DocumentFragment) object;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writeFragmentToStream(fragment, out);
		} catch (final IOException e) {
		}
		super.javaToNative(out.toByteArray(), transferData);
	}

	public void writeFragmentToStream(final DocumentFragment fragment, final OutputStream out) throws IOException {
		new DocumentWriter().write(fragment, out);
	}

	// Reading

	@Override
	public Object nativeToJava(final TransferData transferData) {
		if (isSupportedType(transferData)) {
			final byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}
			final ByteArrayInputStream in = new ByteArrayInputStream(buffer);

			try {
				return readFragmentFromStream(in);
			} catch (final IOException ex) {
				return null;
			}
		}

		return null;
	}

	public DocumentFragment readFragmentFromStream(final InputStream in) throws IOException {
		try {
			final Document document = new DocumentReader().read(new InputSource(in));
			return document.getFragment(document.getRootElement().getRange().resizeBy(1, -1));
		} catch (final ParserConfigurationException e) {
			// TODO shoult never happen - log this exception?
			e.printStackTrace();
			return null;
		} catch (final SAXException e) {
			// TODO shoult never happen - log this exception?
			e.printStackTrace();
			return null;
		}
	}
}
