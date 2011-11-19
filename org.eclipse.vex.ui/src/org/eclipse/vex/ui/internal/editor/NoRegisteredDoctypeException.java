/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

/**
 * Indicates that no document type is registered for the public ID in the
 * document, or that the document does not have a PUBLIC DOCTYPE decl, in
 * which case publicId is null.
 */
public class NoRegisteredDoctypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NoRegisteredDoctypeException(final String publicId) {
		this.publicId = publicId;
	}

	public String getPublicId() {
		return publicId;
	}

	private final String publicId;
}