/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.dom;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Notification about the change of an attribute.
 * 
 * @author Florian Thienel
 */
public class AttributeChangeEvent extends DocumentEvent {

	private static final long serialVersionUID = 1L;

	private final QualifiedName attributeName;
	private final String oldAttributeValue;
	private final String newAttributeValue;

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
	public AttributeChangeEvent(final IDocument document, final IParent parent, final QualifiedName attributeName, final String oldAttributeValue, final String newAttributeValue) {
		super(document, parent);
		this.attributeName = attributeName;
		this.oldAttributeValue = oldAttributeValue;
		this.newAttributeValue = newAttributeValue;
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

}
