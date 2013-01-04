/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

/**
 * Interface for classes that manage a string of characters representing the textual content of a document.
 */
public interface Content extends CharSequence {

	/**
	 * Create a new Position object at the given initial offset.
	 * 
	 * @param offset
	 *            initial offset of the position
	 */
	public Position createPosition(int offset);

	/**
	 * Remove the given Position from the list of positions. A removed position is not updated anymore when this content
	 * is modified.
	 * 
	 * @param position
	 *            the position to remove
	 */
	public void removePosition(Position position);

	/**
	 * Insert given text into the content.
	 * 
	 * @param offset
	 *            Offset at which to insert the string.
	 * @param text
	 *            Text to insert.
	 */
	public void insertText(int offset, String text);

	/**
	 * Get the plain text of a region of this content. The plain text does not contain any information about the element
	 * markers in this content.
	 * 
	 * @param startOffset
	 *            Offset at which the substring begins.
	 * @param endOffset
	 *            Offset at which the substring ends. The number of the returned characters may be less, since the
	 *            element markers are removed.
	 * @return the plain text of the given region without element markers
	 */
	public String getText(final ContentRange range);

	/**
	 * Get the whole plain text of this content. The plain text does not contain any information about the element
	 * markers in this content.
	 * 
	 * @return the whole plain text without element markers
	 */
	public String getText();

	/**
	 * Get the raw text of a region of this content. The plain text does also contain the element markers in this
	 * content.
	 * 
	 * @param startOffset
	 *            Offset at which the substring begins.
	 * @param endOffset
	 *            Offset at which the substring ends.
	 * @return the text of the given region including element markers
	 */
	public String getRawText(final ContentRange range);

	/**
	 * Get the whole raw text of this content. The raw text does also contain the element markers in this content.
	 * 
	 * @return the whole text including element markers
	 */
	public String getRawText();

	/**
	 * Insert the given content into this content at the given offset.
	 * 
	 * @param offset
	 *            Offset at which to insert the given content
	 * @param content
	 *            Content to insert
	 */
	public void insertContent(final int offset, final Content content);

	/**
	 * Get a copy of a region of this content.
	 * 
	 * @param startOffset
	 *            Offset at which the region to copy begins.
	 * @param endOffset
	 *            Offset at which the region to copy ends.
	 * @return the copy of the given region
	 */
	public Content getContent(final ContentRange range);

	/**
	 * Get a full copy of this content.
	 * 
	 * @return a full copy of this content
	 */
	public Content getContent();

	/**
	 * Insert an element marker into the content.
	 * 
	 * @param offset
	 *            Offset at which to insert the element marker.
	 */
	public void insertElementMarker(int offset);

	/**
	 * Indicate if the character at the given offset is an element marker.
	 * 
	 * @param offset
	 *            Offset at which to check if an element marker is present.
	 */
	public boolean isElementMarker(int offset);

	/**
	 * Delete the given range of characters.
	 * 
	 * @param offset
	 *            Offset from which characters should be deleted.
	 * @param length
	 *            Number of characters to delete.
	 */
	public void remove(ContentRange range);

	/**
	 * @return the length of the content.
	 */
	public int length();

	public ContentRange getRange();
}
