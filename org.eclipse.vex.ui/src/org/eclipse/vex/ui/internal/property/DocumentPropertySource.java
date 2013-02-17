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
package org.eclipse.vex.ui.internal.property;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * @author Florian Thienel
 */
public class DocumentPropertySource implements IPropertySource2 {

	private static final String SYSTEM_ID = "systemId";
	private static final String PUBLIC_ID = "systemId";
	private static final String DOCUMENT_URI = "documentUri";
	private static final String ENCODING = "encoding";

	private final IDocument document;

	public DocumentPropertySource(final IDocument document) {
		this.document = document;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { new PropertyDescriptor(SYSTEM_ID, "System Identifier"), new PropertyDescriptor(PUBLIC_ID, "Public Identifier"),
				new PropertyDescriptor(DOCUMENT_URI, "Document URI"), new PropertyDescriptor(ENCODING, "Encoding") };
	}

	public Object getPropertyValue(final Object id) {
		if (id == SYSTEM_ID) {
			return document.getSystemID();
		}
		if (id == PUBLIC_ID) {
			return document.getPublicID();
		}
		if (id == DOCUMENT_URI) {
			return document.getDocumentURI();
		}
		if (id == ENCODING) {
			return document.getEncoding();
		}
		return null;
	}

	public boolean isPropertySet(final Object id) {
		return false;
	}

	public Object getEditableValue() {
		// this property source is read-only
		return null;
	}

	public void resetPropertyValue(final Object id) {
		// this property source is read-only
	}

	public void setPropertyValue(final Object id, final Object value) {
		// this property source is read-only
	}

	public boolean isPropertyResettable(final Object id) {
		return false;
	}

}
