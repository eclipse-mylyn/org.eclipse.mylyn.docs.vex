/*******************************************************************************
 * Copyright (c) 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

import java.text.MessageFormat;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * A wrapper for a pair of {@link ContentPosition} that mark the start and end position of a range inside the content.
 */
public class ContentPositionRange {

	private final ContentPosition startPosition;
	private final ContentPosition endPosition;

	public ContentPositionRange(final ContentPosition start, final ContentPosition end) {
		if (!start.getNodeAtOffset().getDocument().equals(end.getNodeAtOffset().getDocument())) {
			throw new AssertionFailedException(MessageFormat.format(
					"assertion failed: start node (%s) and end node (%s) of a ContentPositionRange have to be in the same document", start.getNodeAtOffset(), end.getNodeAtOffset())); //$NON-NLS-1$

		}
		startPosition = start;
		endPosition = end;
	}

	/**
	 * @return The start position of thios range
	 */
	public ContentPosition getStartPosition() {
		return startPosition;
	}

	/**
	 * @return The end position of thios range
	 */
	public ContentPosition getEndPosition() {
		return endPosition;
	}

	/**
	 * Resize this range by the given delta. Since ContentPositionRange is immutable, a new resized range is returned.
	 * 
	 * @return the resized range
	 */
	public ContentPositionRange resizeBy(final int deltaStart, final int deltaEnd) {
		return new ContentPositionRange(startPosition.moveBy(deltaStart), endPosition.moveBy(deltaEnd));
	}

	/**
	 * Checks if this range is completely inside the insertion range of the given node.
	 * 
	 * @param node
	 */
	public boolean isInsertionPointIn(final INode node) {
		if (startPosition.getNodeAtOffset().getContent() != node.getContent()) {
			// The node has to use the same content as this range
			return false;
		}
		return startPosition.isAfterOrEquals(node.getStartPosition().moveBy(1)) && endPosition.isBeforeOrEquals(node.getEndPosition());
	}

	/**
	 * Indicate whether this range contains the given position.
	 * 
	 * @return true if this range contains the given position
	 */
	public boolean contains(final ContentPosition position) {
		if (startPosition.getNodeAtOffset().getContent() != position.getNodeAtOffset().getContent()) {
			// The position has to use the same content as this range
			return false;
		}
		return startPosition.isBeforeOrEquals(position) && endPosition.isAfterOrEquals(position);
	}

}
