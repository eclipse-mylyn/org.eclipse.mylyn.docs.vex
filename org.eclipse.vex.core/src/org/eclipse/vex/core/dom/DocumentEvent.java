/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - support for attribute changes
 *******************************************************************************/
package org.eclipse.vex.core.dom;

import java.util.EventObject;

/**
 * This is the base class for notifications about a document change.
 */
public abstract class DocumentEvent extends EventObject {

	private static final long serialVersionUID = -9028980559838712720L;

	private final IDocument document;
	private final IParent parent;

	/**
	 * Create an event.
	 * 
	 * @param document
	 *            the document that changed
	 * @param parent
	 *            the parent node containing the change
	 */
	public DocumentEvent(final IDocument document, final IParent parent) {
		super(document);
		this.document = document;
		this.parent = parent;
	}

	/**
	 * @return the Parent containing the change
	 */
	public IParent getParent() {
		return parent;
	}

	/**
	 * @return the document for which this event was generated
	 */
	public IDocument getDocument() {
		return document;
	}

}
