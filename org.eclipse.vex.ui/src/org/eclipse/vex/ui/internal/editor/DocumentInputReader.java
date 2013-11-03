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

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jface.text.BadLocationException;

/**
 * This class wraps a {@link org.eclipse.jface.text.IDocument JFace Document} in an Reader. The Jface document already
 * contains an encoded String, so we can simply return the chars here.
 */
public class DocumentInputReader extends Reader {

	private final org.eclipse.jface.text.IDocument document;
	private int position;
	private final int documentLength;

	public DocumentInputReader(final org.eclipse.jface.text.IDocument document) {
		this.document = document;
		documentLength = document.getLength();
		position = 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * Reads characters into a portion of an array. This method will block until some input is available, an I/O error
	 * occurs, or the end of the stream is reached.
	 * 
	 * @param cbuf
	 *            - Destination buffer
	 * @param off
	 *            - Offset at which to start storing characters
	 * @param length
	 *            - Maximum number of characters to read
	 * 
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 */
	@Override
	public int read(final char[] cbuf, final int off, final int length) throws IOException {

		int charsRead = 0;
		while (charsRead < length && position < documentLength) {
			try {
				final char c = document.getChar(position++);
				cbuf[off + charsRead++] = c;
			} catch (final BadLocationException e) {
				return -1;
			}
		}

		if (charsRead == 0) {
			return -1;
		}

		return charsRead;
	}

}
