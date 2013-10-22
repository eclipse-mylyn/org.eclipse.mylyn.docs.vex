/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.io.InputStream;

import org.eclipse.jface.text.BadLocationException;

/**
 * This class wraps a {@link org.eclipse.jface.text.IDocument JFace Document} in an InputStream.
 */
public class DocumentInputStream extends InputStream {

	private final org.eclipse.jface.text.IDocument document;
	private int position;
	private final int length;

	public DocumentInputStream(final org.eclipse.jface.text.IDocument document) {
		this.document = document;
		length = document.getLength();
		position = 0;
	}

	/**
	 * Reads the next byte of data from this input stream. The value byte is returned as an int in the range 0 to 255.
	 * If no byte is available because the end of the stream has been reached, the value -1 is returned.
	 * 
	 * @returns the next byte of data, or -1 if the end of the stream has been reached.
	 */

	@Override
	public synchronized int read() {
		try {
			return position < length ? document.get(position++, 1).getBytes()[0] & 0xff : -1;
		} catch (final BadLocationException e) {
			return -1;
		}
	}

}
