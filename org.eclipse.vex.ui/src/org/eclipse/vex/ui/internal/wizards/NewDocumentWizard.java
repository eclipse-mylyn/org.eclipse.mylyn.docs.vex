/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Florian Thienel - fix illegal inheritance from BasicNewResourceWizard
 *******************************************************************************/
package org.eclipse.vex.ui.internal.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.editor.DocumentFileCreationPage;
import org.eclipse.vex.ui.internal.editor.DocumentTypeSelectionPage;
import org.eclipse.vex.ui.internal.editor.VexEditor;

public class NewDocumentWizard extends Wizard implements INewWizard {

	private IWorkbench workbench;
	private IStructuredSelection selection;

	private DocumentTypeSelectionPage typePage;
	private DocumentFileCreationPage filePage;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		this.workbench = workbench;
		selection = currentSelection;

		setWindowTitle(Messages.getString("NewDocumentWizard.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		typePage = new DocumentTypeSelectionPage();
		filePage = new DocumentFileCreationPage("filePage", selection); //$NON-NLS-1$
		addPage(typePage);
		addPage(filePage);
	}

	@Override
	public boolean performFinish() {
		try {
			final IDocument doc = createDocument(typePage.getDocumentType(), typePage.getRootElementName());

			final Style style = VexPlugin.getDefault().getPreferences().getPreferredStyle(typePage.getDocumentType());
			if (style == null) {
				MessageDialog.openError(getShell(), Messages.getString("NewDocumentWizard.noStyles.title"), Messages.getString("NewDocumentWizard.noStyles.message")); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
				// TODO: don't allow selection of types with no stylesheets
			}

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final DocumentWriter writer = new DocumentWriter();
			writer.setWhitespacePolicy(new CssWhitespacePolicy(style.getStyleSheet()));
			writer.write(doc, baos);
			baos.close();
			final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

			filePage.setInitialContents(bais);
			final IFile file = filePage.createNewFile();
			IDE.setDefaultEditor(file, VexEditor.ID);

			// Open editor on new file.
			final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				BasicNewResourceWizard.selectAndReveal(file, activeWorkbenchWindow);
				final IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
				if (page != null) {
					IDE.openEditor(page, file, true);
				}
			}

			typePage.saveSettings();

			return true;

		} catch (final Exception ex) {
			final String message = MessageFormat.format(Messages.getString("NewDocumentWizard.errorLoading.message"), new Object[] { filePage.getFileName(), ex.getMessage() });
			VexPlugin.getDefault().log(IStatus.ERROR, message, ex);
			MessageDialog.openError(getShell(), Messages.getString("NewDocumentWizard.errorLoading.title"), "Unable to create " + filePage.getFileName()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	private static IDocument createDocument(final DocumentType documentType, final String rootElementName) {
		if (isDTD(documentType)) {
			return createDocumentWithDTD(documentType, rootElementName);
		}
		return createDocumentWithSchema(documentType, rootElementName);
	}

	private static boolean isDTD(final DocumentType documentType) {
		final String systemId = documentType.getSystemId();
		return systemId != null && systemId.toLowerCase().endsWith(".dtd");
	}

	private static IDocument createDocumentWithDTD(final DocumentType documentType, final String rootElementName) {
		final IDocument result = new Document(new QualifiedName(null, rootElementName));
		result.setPublicID(documentType.getPublicId());
		result.setSystemID(documentType.getSystemId());
		return result;
	}

	private static IDocument createDocumentWithSchema(final DocumentType documentType, final String rootElementName) {
		final String defaultNamespaceUri = documentType.getNamespaceName();
		final Document document = new Document(new QualifiedName(defaultNamespaceUri, rootElementName));

		final IElement root = document.getRootElement();
		root.declareDefaultNamespace(defaultNamespaceUri);

		final WTPVEXValidator validator = new WTPVEXValidator(new DocumentContentModel(null, null, null, root));
		int namespaceIndex = 1;
		for (final String namespaceUri : validator.getRequiredNamespaces()) {
			if (!defaultNamespaceUri.equals(namespaceUri)) {
				root.declareNamespace("ns" + namespaceIndex++, namespaceUri);
			}
		}

		return document;
	}

}
