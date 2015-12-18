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
package org.eclipse.vex.core.provisional.dom;

/**
 * Interface for classes that manage a string of characters representing the textual content of a document.
 */
public interface IContent extends CharSequence {

	/**
	 * Create a new Position object at the given initial offset.
	 *
	 * @param offset
	 *            initial offset of the position
	 */
	IPosition createPosition(int offset);

	/**
	 * Remove the given Position from the list of positions. A removed position is not updated anymore when this content
	 * is modified.
	 *
	 * @param position
	 *            the position to remove
	 */
	void removePosition(IPosition position);

	/**
	 * Insert the given text at the given offset into this content.
	 *
	 * @param offset
	 *            offset at which to insert the text
	 * @param text
	 *            the text to insert
	 */
	void insertText(int offset, String text);

	/**
	 * Get the plain text of a range in this content. The plain text does not contain any tag markers in this content.
	 * The length of the returned text may be less then the length of the given range since the returned text does not
	 * contain tag markers.
	 *
	 * @param range
	 *            the range of the text to return
	 * @return the plain text of the given range, not including tag markers
	 */
	String getText(final ContentRange range);

	/**
	 * Get the whole plain text of this content. The plain text does not contain any tag markers.
	 *
	 * @return the whole plain text, not including tag markers
	 */
	String getText();

	/**
	 * Get the raw text of a range in this content. The plain text does also contain the tag markers in this content.
	 *
	 * @param range
	 *            the range of the text to return
	 * @return the text of the given range, including element markers
	 */
	String getRawText(final ContentRange range);

	/**
	 * Get the whole raw text of this content. The raw text does also contain the tag markers in this content.
	 *
	 * @return the whole text, including tag markers
	 */
	String getRawText();

	void insertLineBreak(final int offset);

	MultilineText getMultilineText(final ContentRange range);

	/**
	 * Insert the given content into this content at the given offset.
	 *
	 * @param offset
	 *            offset at which to insert the given content
	 * @param content
	 *            content to insert
	 */
	void insertContent(final int offset, final IContent content);

	/**
	 * Get a copy of a range in this content.
	 *
	 * @param range
	 *            the range to copy
	 * @return the copy of the given range
	 */
	IContent getContent(final ContentRange range);

	/**
	 * @return a full copy of this content
	 */
	IContent getContent();

	/**
	 * Insert a tag marker at the given offset into this content.
	 *
	 * @param offset
	 *            offset at which to insert the tag marker.
	 */
	void insertTagMarker(int offset);

	/**
	 * Indicate if there is a tag marker at the given offset.
	 *
	 * @param offset
	 *            offset at which to check if a tag marker is present.
	 */
	boolean isTagMarker(int offset);

	/**
	 * Delete the given range of characters.
	 *
	 * @param range
	 *            the range to delete from this content
	 */
	void remove(ContentRange range);

	/**
	 * @return the length of the content including tag markers
	 */
	@Override
	int length();

	/**
	 * @return the range of this content = [0, length - 1].
	 */
	ContentRange getRange();
}
