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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.vex.core.internal.dom.DocumentContentModel;
import org.eclipse.vex.core.internal.dom.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.RootElement;
import org.eclipse.vex.core.internal.widget.CssWhitespacePolicy;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;

/**
 * @author Florian Thienel
 */
public class VexDocumentContentModel extends DocumentContentModel {

	private final Shell shell;
	
	private DocumentType documentType;

	private Style style;
	
	private boolean shouldAssignInferredDocumentType;
	
	public VexDocumentContentModel(final Shell shell) {
		this.shell = shell;
	}

	@Override
	public void initialize(final String baseUri, final String publicId, final String systemId, final RootElement rootElement) {
		super.initialize(baseUri, publicId, systemId, rootElement);
		final String mainDocumentTypeIdentifier = getMainDocumentTypeIdentifier();
		documentType = getRegisteredDocumentType();
		if (documentType == null)
			documentType = queryUserForDocumentType();
		
		if (documentType == null)
			throw new NoRegisteredDoctypeException(mainDocumentTypeIdentifier);

		// TODO verify documentType URL???
//		final URL url = documentType.getResourceUrl();
//		if (url == null) {
//			final String message = MessageFormat.format(Messages.getString("VexEditor.noUrlForDoctype"), mainDocumentTypeIdentifier);
//			throw new RuntimeException(message);
//		}

		style = VexPlugin.getDefault().getPreferences().getPreferredStyle(documentType.getPublicId());
		if (style == null)
			throw new NoStyleForDoctypeException();
	}

	private DocumentType getRegisteredDocumentType() {
		return VexPlugin.getDefault().getConfigurationRegistry().getDocumentType(getMainDocumentTypeIdentifier());
	}
	
	private DocumentType queryUserForDocumentType() {
		final DocumentTypeSelectionDialog dialog = DocumentTypeSelectionDialog.create(shell, getMainDocumentTypeIdentifier());
		dialog.open();
		if (dialog.alwaysUseThisDoctype())
			shouldAssignInferredDocumentType = true;
		return dialog.getDoctype();
	}

	@Override
	public IWhitespacePolicy getWhitespacePolicy() {
		if (style == null)
			return super.getWhitespacePolicy();
		return new CssWhitespacePolicy(style.getStyleSheet());
	}
	
	public DocumentType getDocumentType() {
		return documentType;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public boolean shouldAssignInferredDocumentType() {
		return shouldAssignInferredDocumentType;
	}
}
