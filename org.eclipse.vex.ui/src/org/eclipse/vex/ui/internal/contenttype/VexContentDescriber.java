/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.contenttype;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.xml.sax.InputSource;

/**
 * @author Florian Thienel
 */
public class VexContentDescriber extends XMLContentDescriber {

	@Override
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		contents.reset();
		return checkCriteria(new InputSource(contents), description);
	}

	@Override
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		contents.reset();
		return checkCriteria(new InputSource(contents), description);
	}

	private int checkCriteria(final InputSource contents, final IContentDescription description) {
		final InferXmlContentTypeHandler handler = new InferXmlContentTypeHandler();
		if (!handler.parseContents(contents)) {
			return INVALID;
		}

		final DocumentType documentType = VexPlugin.getDefault().getConfigurationRegistry().getDocumentType(handler.getMainDocumentTypeIdentifier(), "");
		if (documentType != null) {
			return VALID;
		}
		return INDETERMINATE;
	}
}
