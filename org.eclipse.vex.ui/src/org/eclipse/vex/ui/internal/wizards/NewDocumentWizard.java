/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
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
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.DocumentContentModel;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.editor.DocumentFileCreationPage;
import org.eclipse.vex.ui.internal.editor.DocumentTypeSelectionPage;
import org.eclipse.vex.ui.internal.editor.Messages;
import org.eclipse.vex.ui.internal.editor.VexEditor;

/**
 * Wizard for creating a new Vex document.
 */
public class NewDocumentWizard extends BasicNewResourceWizard {

	private DocumentTypeSelectionPage typePage;
	private DocumentFileCreationPage filePage;

	@Override
	public void addPages() {
		typePage = new DocumentTypeSelectionPage();
		filePage = new DocumentFileCreationPage("filePage", getSelection()); //$NON-NLS-1$
		addPage(typePage);
		addPage(filePage);
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {

		super.init(workbench, currentSelection);
		setWindowTitle(Messages.getString("NewDocumentWizard.title")); //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
		try {
			final IDocument doc = createDocument(typePage.getDocumentType(), typePage.getRootElementName());

			final Style style = VexPlugin.getDefault().getPreferences().getPreferredStyle(typePage.getDocumentType().getPublicId());
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
			this.selectAndReveal(file);

			registerEditorForFilename("*." + file.getFileExtension(), VexEditor.ID); //$NON-NLS-1$

			// Open editor on new file.
			final IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
			if (dw != null) {
				final IWorkbenchPage page = dw.getActivePage();
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
		final String defaultNamespaceUri = documentType.getPublicId();
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

	/**
	 * Register an editor to use for files with the given filename.
	 * 
	 * NOTE: this method uses internal, undocumented Eclipse functionality. It may therefore break in a future version
	 * of Eclipse.
	 * 
	 * @param fileName
	 *            Filename to be registered. Use the form "*.ext" to register all files with a given extension.
	 * @param editorId
	 *            ID of the editor to use for the given filename.
	 */
	private static void registerEditorForFilename(final String fileName, final String editorId) {

		final EditorDescriptor ed = getEditorDescriptor(editorId);
		if (ed == null) {
			return;
		}

		final IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();
		final EditorRegistry ereg = (EditorRegistry) reg;
		final FileEditorMapping[] mappings = (FileEditorMapping[]) ereg.getFileEditorMappings();
		FileEditorMapping mapping = null;
		for (final FileEditorMapping fem : mappings) {
			if (fem.getLabel().equals(fileName)) {
				mapping = fem;
				break;
			}
		}

		if (mapping != null) {
			// found mapping for fileName
			// make sure it includes our editor
			for (final IEditorDescriptor editor : mapping.getEditors()) {
				if (editor.getId().equals(editorId)) {
					// already mapped
					return;
				}
			}

			// editor not in the list, so add it
			mapping.addEditor(ed);
			ereg.setFileEditorMappings(mappings);
			ereg.saveAssociations();

		} else {
			// no mapping found for the filename
			// let's add one
			String name = null;
			String ext = null;
			final int iDot = fileName.lastIndexOf('.');
			if (iDot == -1) {
				name = fileName;
			} else {
				name = fileName.substring(0, iDot);
				ext = fileName.substring(iDot + 1);
			}

			mapping = new FileEditorMapping(name, ext);
			final FileEditorMapping[] newMappings = new FileEditorMapping[mappings.length + 1];
			mapping.addEditor(ed);

			System.arraycopy(mappings, 0, newMappings, 0, mappings.length);
			newMappings[mappings.length] = mapping;
			ereg.setFileEditorMappings(newMappings);
			ereg.saveAssociations();
		}

	}

	/**
	 * Return the IEditorDescriptor for the given editor ID.
	 */
	private static EditorDescriptor getEditorDescriptor(final String editorId) {
		final EditorRegistry reg = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
		for (final IEditorDescriptor editor : reg.getSortedEditorsFromPlugins()) {
			if (editor.getId().equals(editorId)) {
				return (EditorDescriptor) editor;
			}
		}

		return null;
	}
}
