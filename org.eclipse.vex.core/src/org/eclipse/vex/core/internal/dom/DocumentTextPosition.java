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
package org.eclipse.vex.core.internal.dom;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * This class extends {@link Position} with additional methods to track a position in the source file.
 */
public class DocumentTextPosition extends Position {
	private int line;
	private int column;
	private int offsetInNode;

	/**
	 * Creates a new position with the given offset and length 0.
	 *
	 * @param offset
	 *            the position offset, must be >= 0
	 */
	public DocumentTextPosition(final int offset) {
		super(offset);
	}

	/**
	 * Creates a new position with the given offset and length.
	 *
	 * @param offset
	 *            the position offset, must be >= 0
	 * @param length
	 *            the position length, must be >= 0
	 */
	public DocumentTextPosition(final int offset, final int length) {
		super(offset, length);
	}

	/**
	 * Compute line and column offset of this position
	 */
	public void computePosition(final org.eclipse.jface.text.IDocument doc) {
		try {
			setLine(doc.getLineOfOffset(getOffset()));
			final IRegion region = doc.getLineInformation(line);
			setColumn(getOffset() + 1 - region.getOffset());
		} catch (final BadLocationException e) {
			setLine(0);
			setColumn(0);
		}
	}

	public int getLine() {
		return line;
	}

	public void setLine(final int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(final int column) {
		this.column = column;
	}

	/**
	 * Return the previously stored offset.
	 *
	 * @return The stored offset
	 */
	public int getOffsetInNode() {
		return offsetInNode;
	}

	/**
	 * Stores an additional offset in this position
	 *
	 * @param offsetInNode
	 *            The offset to store.
	 */
	public void setOffsetInNode(final int offsetInNode) {
		this.offsetInNode = offsetInNode;
	}
}
