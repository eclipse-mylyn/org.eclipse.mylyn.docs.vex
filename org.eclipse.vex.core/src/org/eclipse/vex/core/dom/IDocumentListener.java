/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - promoted to the public API
 *******************************************************************************/
package org.eclipse.vex.core.dom;

/**
 * Receives notifications of document changes.
 */
public interface IDocumentListener extends java.util.EventListener {

	/**
	 * Called when an attribute is changed in one of the document's elements.
	 * 
	 * @param event
	 *            the document event.
	 */
	void attributeChanged(DocumentEvent event);

	/**
	 * Called when a namespace delcaration is changed in one of the document's elements.
	 * 
	 * @param event
	 *            the document event.
	 */
	void namespaceChanged(DocumentEvent event);

	/**
	 * Called before content is deleted from a document.
	 * 
	 * @param event
	 *            the document event
	 */
	void beforeContentDeleted(DocumentEvent event);

	/**
	 * Called before content is inserted into a document.
	 * 
	 * @param event
	 *            the document event
	 */
	void beforeContentInserted(DocumentEvent event);

	/**
	 * Called when content is deleted from a document.
	 * 
	 * @param event
	 *            the document event
	 */
	void contentDeleted(DocumentEvent event);

	/**
	 * Called when content is inserted into a document.
	 * 
	 * @param event
	 *            the document event
	 */
	void contentInserted(DocumentEvent event);

}
