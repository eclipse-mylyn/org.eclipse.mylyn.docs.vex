/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - a NULL object
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * Factory for returning a WhitespacePolicy object given a document type public ID. This is required by DocumentBuilder,
 * since we don't know what WhitespacePolicy we need before we begin parsing the document.
 */
public interface IWhitespacePolicyFactory {

	/**
	 * A factory that always returns the NULL whitespace policy.
	 */
	IWhitespacePolicyFactory NULL = new IWhitespacePolicyFactory() {
		@Override
		public IWhitespacePolicy createPolicy(final IValidator validator, final DocumentContentModel documentContentModel, final StyleSheet style) {
			return IWhitespacePolicy.NULL;
		}
	};

	/**
	 * Return a WhitespacePolicy for documents with the given public ID.
	 *
	 * @param publicId
	 *            Public ID of the document type associated with the document.
	 */
	IWhitespacePolicy createPolicy(IValidator validator, DocumentContentModel documentContentModel, StyleSheet styleSheet);
}
