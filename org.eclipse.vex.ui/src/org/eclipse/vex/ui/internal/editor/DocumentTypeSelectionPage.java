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
package org.eclipse.vex.ui.internal.editor;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;

/**
 * Wizard page for selecting the document type and root element for the new document.
 */
public class DocumentTypeSelectionPage extends WizardPage {

	private static final String SETTINGS_PUBLIC_ID = "publicId"; //$NON-NLS-1$
	private static final String SETTINGS_ROOT_ELEMENT_PREFIX = "root."; //$NON-NLS-1$

	/**
	 * Class constructor.
	 */
	public DocumentTypeSelectionPage() {
		super(Messages.getString("DocumentTypeSelectionPage.pageName")); //$NON-NLS-1$
		setPageComplete(false);

		final IDialogSettings rootSettings = VexPlugin.getDefault().getDialogSettings();
		settings = rootSettings.getSection("newDocument"); //$NON-NLS-1$
		if (settings == null) {
			settings = rootSettings.addNewSection("newDocument"); //$NON-NLS-1$
		}

		doctypes = VexPlugin.getDefault().getConfigurationRegistry().getDocumentTypesWithStyles();
		Arrays.sort(doctypes);
	}

	public void createControl(final Composite parent) {

		final Composite pane = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		pane.setLayout(layout);
		GridData gd;

		Label label = new Label(pane, SWT.NONE);
		label.setText(Messages.getString("DocumentTypeSelectionPage.doctype")); //$NON-NLS-1$

		typeCombo = new Combo(pane, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		typeCombo.setLayoutData(gd);
		typeCombo.addSelectionListener(typeComboSelectionListener);

		label = new Label(pane, SWT.NONE);
		label.setText(Messages.getString("DocumentTypeSelectionPage.rootElement")); //$NON-NLS-1$
		setControl(pane);

		elementCombo = new Combo(pane, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		elementCombo.setLayoutData(gd);
		elementCombo.addSelectionListener(elementComboSelectionListener);

		final String publicId = settings.get(SETTINGS_PUBLIC_ID);
		int initSelection = -1;
		final String[] typeNames = new String[doctypes.length];
		for (int i = 0; i < doctypes.length; i++) {
			typeNames[i] = doctypes[i].getName();
			if (doctypes[i].getPublicId().equals(publicId)) {
				initSelection = i;
			}
		}

		typeCombo.setItems(typeNames);

		if (initSelection != -1) {
			typeCombo.select(initSelection);
			// calling select() does not fire the selection listener,
			// so we update it manually
			updateRootElementCombo();
		}

		setTitle(Messages.getString("DocumentTypeSelectionPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("DocumentTypeSelectionPage.desc")); //$NON-NLS-1$
	}

	/**
	 * Returns the selected document type.
	 */
	public DocumentType getDocumentType() {
		final int i = typeCombo.getSelectionIndex();
		if (i == -1) {
			return null;
		} else {
			return doctypes[i];
		}
	}

	/**
	 * Returns the selected name of the root element.
	 */
	public String getRootElementName() {
		return elementCombo.getText();
	}

	/**
	 * Called from the wizard's performFinal method to save the settings for this page.
	 */
	public void saveSettings() {
		final DocumentType doctype = getDocumentType();
		if (doctype != null) {
			settings.put(SETTINGS_PUBLIC_ID, doctype.getPublicId());
			final String key = SETTINGS_ROOT_ELEMENT_PREFIX + doctype.getPublicId();
			settings.put(key, getRootElementName());
		}
	}

	// ============================================================== PRIVATE

	private IDialogSettings settings;
	private final DocumentType[] doctypes;
	private Combo typeCombo;
	private Combo elementCombo;

	/**
	 * Update the elementCombo to reflect elements in the currently selected type.
	 */
	private void updateRootElementCombo() {
		final DocumentType documentType = getDocumentType();
		final String[] rootElements = getRootElements(documentType);
		Arrays.sort(rootElements);

		elementCombo.removeAll();
		elementCombo.setItems(rootElements);

		// Restore the last used root element
		final String key = SETTINGS_ROOT_ELEMENT_PREFIX + documentType.getPublicId();
		final String selectedRoot = settings.get(key);

		setPageComplete(false);
		if (selectedRoot != null) {
			for (int i = 0; i < rootElements.length; i++) {
				if (rootElements[i].equals(selectedRoot)) {
					elementCombo.select(i);
					setPageComplete(true);
					break;
				}
			}
		}
	}

	private static String[] getRootElements(final DocumentType documentType) {
		final String[] selectedRootElements = documentType.getRootElements();
		if (selectedRootElements != null) {
			return selectedRootElements;
		}
		return getPossibleRootElements(documentType);
	}

	private static String[] getPossibleRootElements(final DocumentType documentType) {
		final IValidator validator = documentType.getValidator();
		if (validator == null) {
			return new String[0];
		}

		final Set<QualifiedName> validRootElements = validator.getValidRootElements();
		final String[] result = new String[validRootElements.size()];
		int i = 0;
		for (final QualifiedName validRootElementName : validRootElements) {
			result[i++] = validRootElementName.getLocalName();
		}
		return result;
	}

	/**
	 * Sets the root element combo box when the document type combo box is selected.
	 */
	private final SelectionListener typeComboSelectionListener = new SelectionListener() {
		public void widgetSelected(final SelectionEvent e) {
			updateRootElementCombo();
		}

		public void widgetDefaultSelected(final SelectionEvent e) {
		}
	};

	/**
	 * When a root element is selected, mark the page as complete.
	 */
	private final SelectionListener elementComboSelectionListener = new SelectionListener() {
		public void widgetSelected(final SelectionEvent e) {
			setPageComplete(true);
		}

		public void widgetDefaultSelected(final SelectionEvent e) {
		}
	};

}
