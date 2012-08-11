/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * Wizard page for selecting the file name for a new Vex document.
 */
public class DocumentFileCreationPage extends WizardNewFileCreationPage {

	/**
	 * Class constructor. Supplies a title and description to the superclass.
	 * 
	 * @param pageName
	 *            name of the page
	 * @param selection
	 *            selection active when the wizard was started
	 */
	public DocumentFileCreationPage(final String pageName, final IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(Messages.getString("DocumentFileCreationPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("DocumentFileCreationPage.desc")); //$NON-NLS-1$
	}

	/**
	 * Returns the initial contents of the file. The initial contents are set by the wizard via the
	 * {@link setInitialContents} method.
	 */
	@Override
	protected InputStream getInitialContents() {
		return initialContents;
	}

	/**
	 * Sets the initial contents to be used when the document is created.
	 * 
	 * @param initialContents
	 *            initial contents for the new document.
	 */
	public void setInitialContents(final InputStream initialContents) {
		this.initialContents = initialContents;
	}

	private InputStream initialContents;
}
