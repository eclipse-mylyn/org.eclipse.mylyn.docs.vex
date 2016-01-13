/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - insertXML (bug 408501)
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * Methods implemented by implementations of the Vex widget on all platforms. This interface is more important as a
 * place to gather common Javadoc than as a way to enforce a contract.
 */
public interface IVexWidget extends IDocumentEditor {

	/*
	 * Configuration
	 */

	/**
	 * Sets a new document for this control.
	 *
	 * @param document
	 *            new Document to display
	 * @param styleSheet
	 *            StyleSheet to use for formatting
	 */
	void setDocument(IDocument document, StyleSheet styleSheet);

	/**
	 * Returns the style sheet used to format the document while editing.
	 */
	StyleSheet getStyleSheet();

	/**
	 * Sets the style sheet to be applied to the current document during editing. If no resolver has been set, the style
	 * sheet will also be used for any subsequently loaded documents. If a resolver has been set, the style sheet
	 * returned by the resolver will be used for subsequently loaded documents.
	 *
	 * @param styleSheet
	 *            the new StyleSheet to use
	 */
	void setStyleSheet(StyleSheet styleSheet);

	public void setWhitespacePolicy(IWhitespacePolicy whitespacePolicy);

	public IWhitespacePolicy getWhitespacePolicy();

	/**
	 * Returns the value of the debugging flag.
	 */
	boolean isDebugging();

	/**
	 * Sets the value of the debugging flag. When debugging, copious information is dumped to stdout.
	 *
	 * @param debugging
	 *            true if debugging is to be enabled.
	 */
	void setDebugging(boolean debugging);

	/*
	 * Layout
	 */

	/**
	 * Returns the width to which the document was layed out.
	 */
	int getLayoutWidth();

	/**
	 * Sets the width to which the document should be layed out. The actual resulting width may be different due to
	 * overflowing boxes.
	 */
	void setLayoutWidth(int width);

	/**
	 * Return the offset into the document for the given coordinates.
	 *
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 */
	ContentPosition viewToModel(int x, int y);

}
