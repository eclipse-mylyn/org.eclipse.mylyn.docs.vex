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
 *     Carsten Hiesserich - moved serialization to XMLFragment
 *******************************************************************************/
package org.eclipse.vex.ui.internal.swt;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

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
		if (object == null || !(object instanceof IDocumentFragment)) {
			return;
		}

		if (!isSupportedType(transferData)) {
			return;
		}

		final IDocumentFragment fragment = (IDocumentFragment) object;

		super.javaToNative(writeFragmentToStream(fragment), transferData);
	}

	public byte[] writeFragmentToStream(final IDocumentFragment fragment) {
		final XMLFragment wrapper = new XMLFragment(fragment);
		try {
			return wrapper.getXML().getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			// This should not happen with UTF-8
			return new byte[0];
		}
	}

	// Reading

	@Override
	public Object nativeToJava(final TransferData transferData) {
		if (isSupportedType(transferData)) {
			final byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}

			return readFragmentFromStream(buffer);
		}

		return null;
	}

	public IDocumentFragment readFragmentFromStream(final byte[] in) {
		try {
			final XMLFragment xmlFragment = new XMLFragment(new String(in, "UTF-8"));
			return xmlFragment.getDocumentFragment();
		} catch (final UnsupportedEncodingException e) {
			// This should not happen with UTF-8
			return null;
		}
	}

}
