/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class DocumentPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}

	public void defineActions(final IPageLayout layout) {
		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipse.vex.ui.NewDocumentWizard");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$

		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
	}

	public void defineLayout(final IPageLayout layout) {

		final String editorArea = layout.getEditorArea();

		final IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float) 0.2, editorArea);//$NON-NLS-1$
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		final IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, (float) 0.75, editorArea);//$NON-NLS-1$
		topRight.addView(IPageLayout.ID_OUTLINE);

		final IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, (float) 0.5, "topRight");//$NON-NLS-1$ //$NON-NLS-2$
		bottomRight.addView(IPageLayout.ID_PROP_SHEET);

	}

}
