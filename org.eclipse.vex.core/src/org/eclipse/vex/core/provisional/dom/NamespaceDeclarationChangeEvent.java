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
package org.eclipse.vex.core.provisional.dom;

/**
 * Notification about a change of the namespace delclarations of an element.
 * 
 * @author Florian Thienel
 */
public class NamespaceDeclarationChangeEvent extends DocumentEvent {

	private static final long serialVersionUID = 1L;

	public NamespaceDeclarationChangeEvent(final IDocument document, final IParent parent) {
		super(document, parent);
	}

}
