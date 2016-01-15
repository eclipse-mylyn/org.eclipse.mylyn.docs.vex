/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;

public interface IClipboard {

	void dispose();

	/**
	 * Cuts the current selection to the clipboard.
	 *
	 * @param editor
	 *            TODO
	 */
	void cutSelection(IDocumentEditor editor);

	/**
	 * Copy the current selection to the clipboard.
	 *
	 * @param editor
	 *            TODO
	 */
	void copySelection(IDocumentEditor editor);

	/**
	 * Returns true if the clipboard has content that can be pasted. Used to enable/disable the paste action of a
	 * containing application.
	 */
	boolean hasContent();

	/**
	 * Paste the current clipboard contents into the document at the current caret position.
	 *
	 * @param editor
	 *            TODO
	 */
	void paste(IDocumentEditor editor) throws DocumentValidationException;

	/**
	 * Returns true if the clipboard has plain text content that can be pasted. Used to enable/disable the "paste text"
	 * action of a containing application.
	 */
	boolean hasTextContent();

	/**
	 * Paste the current clipboard contents as plain text into the document at the current caret position.
	 *
	 * @param editor
	 *            TODO
	 */
	void pasteText(IDocumentEditor editor) throws DocumentValidationException;

}
