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

import org.eclipse.core.runtime.QualifiedName;

/**
 * Encapsulation of the details of a document change.
 */
public class DocumentEvent extends EventObject {

	private static final long serialVersionUID = -9028980559838712720L;

	private final IDocument document;
	private final IParent parent;
	private final ContentRange range;
	private final QualifiedName attributeName;
	private final String oldAttributeValue;
	private final String newAttributeValue;

	/**
	 * Create an event.
	 * 
	 * @param document
	 *            the document that changed
	 * @param parent
	 *            the parent node containing the change
	 * @param range
	 *            the range which was changed
	 */
	public DocumentEvent(final IDocument document, final IParent parent, final ContentRange range) {
		super(document);
		this.document = document;
		this.parent = parent;
		this.range = range;
		attributeName = null;
		oldAttributeValue = null;
		newAttributeValue = null;
	}

	/**
	 * Create an event with attribute information.
	 * 
	 * @param document
	 *            Document that changed.
	 * @param parent
	 *            Parent containing the attribute that changed
	 * @param attributeName
	 *            name of the attribute that changed
	 * @param oldAttributeValue
	 *            value of the attribute before the change.
	 * @param newAttributeValue
	 *            value of the attribute after the change.
	 */
	public DocumentEvent(final IDocument document, final IParent parent, final QualifiedName attributeName, final String oldAttributeValue, final String newAttributeValue) {
		super(document);
		this.document = document;
		this.parent = parent;
		range = parent.getRange();
		this.attributeName = attributeName;
		this.oldAttributeValue = oldAttributeValue;
		this.newAttributeValue = newAttributeValue;
	}

	/**
	 * @return the range which was changed
	 */
	public ContentRange getRange() {
		return range;
	}

	/**
	 * @return the Parent containing the change
	 */
	public IParent getParent() {
		return parent;
	}

	/**
	 * @return the value of the attribute before the change. If null, indicates that the attribute was removed
	 */
	public String getNewAttributeValue() {
		return newAttributeValue;
	}

	/**
	 * @return the value of the attribute after the change. If null, indicates the attribute did not exist before the
	 *         change
	 */
	public String getOldAttributeValue() {
		return oldAttributeValue;
	}

	/**
	 * @return the qualified name of the attribute that was changed
	 */
	public QualifiedName getAttributeName() {
		return attributeName;
	}

	/**
	 * @return the document for which this event was generated
	 */
	public IDocument getDocument() {
		return document;
	}

}
