/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.EventObject;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.undo.IUndoableEdit;

/**
 * Encapsulation of the details of a document change
 * 
 * @model
 */
public class DocumentEvent extends EventObject {

	private static final long serialVersionUID = -9028980559838712720L;

	private final Document document;
	private final Element parentElement;
	private int offset;
	private int length;
	private QualifiedName attributeName;
	private String oldAttributeValue;
	private String newAttributeValue;
	private final IUndoableEdit undoableEdit;

	/**
	 * Class constructor.
	 * 
	 * @param document
	 *            Document that changed.
	 * @param e1
	 *            Element containing the change.
	 * @param offset
	 *            offset at which the change occurred.
	 * @param length
	 *            length of the change.
	 * @param undoableEdit
	 *            IUndoableEdit that can be used to undo the change.
	 */
	public DocumentEvent(final Document document, final Element e1, final int offset, final int length, final IUndoableEdit undoableEdit) {

		super(document);
		this.document = document;
		parentElement = e1;
		this.offset = offset;
		this.length = length;
		this.undoableEdit = undoableEdit;
	}

	/**
	 * Class constructor used when firing an attributeChanged event.
	 * 
	 * @param document
	 *            Document that changed.
	 * @param parentElement
	 *            element containing the attribute that changed
	 * @param attributeName
	 *            name of the attribute that changed
	 * @param oldAttributeValue
	 *            value of the attribute before the change.
	 * @param newAttributeValue
	 *            value of the attribute after the change.
	 * @param undoableEdit
	 *            IUndoableEdit that can be used to undo the change.
	 */
	public DocumentEvent(final Document document, final Element parentElement, final QualifiedName attributeName, final String oldAttributeValue, final String newAttributeValue,
			final IUndoableEdit undoableEdit) {

		super(document);
		this.document = document;
		this.parentElement = parentElement;
		this.attributeName = attributeName;
		this.oldAttributeValue = oldAttributeValue;
		this.newAttributeValue = newAttributeValue;
		this.undoableEdit = undoableEdit;
	}

	/**
	 * Returns the length of the change.
	 * 
	 * @model
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns the offset at which the change occurred.
	 * 
	 * @model
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the element containing the change.
	 * 
	 * @model
	 */
	public Element getParentElement() {
		return parentElement;
	}

	/**
	 * @return the value of the attribute before the change. If null, indicates that the attribute was removed.
	 * @model
	 */
	public String getNewAttributeValue() {
		return newAttributeValue;
	}

	/**
	 * @return the value of the attribute after the change. If null, indicates the attribute did not exist before the
	 *         change.
	 * @model
	 */
	public String getOldAttributeValue() {
		return oldAttributeValue;
	}

	/**
	 * @return the name of the attribute that was changed.
	 * @model
	 */
	public QualifiedName getAttributeName() {
		return attributeName;
	}

	/**
	 * @return the document for which this event was generated
	 * @model
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the undoable edit that can be used to undo the action. May be null, in which case the action cannot be
	 * undone.
	 * 
	 * @model
	 */
	public IUndoableEdit getUndoableEdit() {
		return undoableEdit;
	}
}
