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

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.vex.core.internal.dom.DocumentFragment;

/**
 * Transfer object that handles Vex DocumentFragments.
 */
public class DocumentFragmentTransfer extends ByteArrayTransfer {

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
				oos.writeObject(frag);
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
				final Object object = ois.readObject();
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

	private static final String[] typeNames = { DocumentFragment.MIME_TYPE };
	private static final int[] typeIds = { ByteArrayTransfer.registerType(DocumentFragment.MIME_TYPE) };

	private static DocumentFragmentTransfer instance;

	private DocumentFragmentTransfer() {
	}
}
