/*******************************************************************************
 * Copyright (c) 2004, 2015 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - support for doctypes with no public id
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * Property page for .css files.
 */
public class StylePropertyPage extends PropertyPage {

	private static final int NAME_WIDTH = 150;

	private PluginProject pluginProject;

	private Style style;

	private Composite pane;
	private Text nameText;
	private Table doctypesTable;

	private IConfigListener configListener;

	@Override
	protected Control createContents(final Composite parent) {
		pane = new Composite(parent, SWT.NONE);

		pluginProject = new PluginProject(((IFile) getElement()).getProject());
		try {
			pluginProject.load();
		} catch (final CoreException e) {
			VexPlugin.getDefault().getLog().log(e.getStatus());
		}

		createPropertySheet();

		configListener = new IConfigListener() {
			@Override
			public void configChanged(final ConfigEvent event) {
				try {
					pluginProject.load();
				} catch (final CoreException e) {
					VexPlugin.getDefault().getLog().log(e.getStatus());
				}
				style = (Style) pluginProject.getItemForResource((IFile) getElement());
				populateDoctypes();
			}

			@Override
			public void configLoaded(final ConfigEvent event) {
				setMessage(getTitle());
				populateStyle();
				setValid(true);

				try { // force an incremental build
					pluginProject.writeConfigXml();
				} catch (final Exception e) {
					final String message = MessageFormat.format(Messages.getString("StylePropertyPage.writeError"), //$NON-NLS-1$
							new Object[] { PluginProject.PLUGIN_XML });
					VexPlugin.getDefault().log(IStatus.ERROR, message, e);
				}
			}
		};
		VexPlugin.getDefault().getConfigurationRegistry().addConfigListener(configListener);

		if (VexPlugin.getDefault().getConfigurationRegistry().isLoaded()) {
			populateStyle();
			populateDoctypes();
		} else {
			setValid(false);
			setMessage(Messages.getString("StylePropertyPage.loading")); //$NON-NLS-1$
		}

		return pane;
	}

	private void createPropertySheet() {
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		pane.setLayout(layout);
		GridData gd;

		Label label;

		label = new Label(pane, SWT.NONE);
		label.setText(Messages.getString("StylePropertyPage.name")); //$NON-NLS-1$
		nameText = new Text(pane, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = NAME_WIDTH;
		nameText.setLayoutData(gd);

		final IFile file = (IFile) getElement();
		style = (Style) pluginProject.getItemForResource(file);
		if (style == null) {
			style = new Style(pluginProject);
			URI uri;
			try {
				uri = new URI(file.getProjectRelativePath().toString());
				style.setResourceUri(uri);
				pluginProject.addItem(style);
			} catch (final URISyntaxException e) {
				// This should never happen
			}
		}

		// Generate a simple ID for this one if necessary
		if (style.getSimpleId() == null || style.getSimpleId().length() == 0) {
			style.setSimpleId(style.generateSimpleId());
		}

		label = new Label(pane, SWT.NONE);
		label.setText(Messages.getString("StylePropertyPage.doctypes")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		final Composite tablePane = new Composite(pane, SWT.BORDER);

		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.horizontalSpan = 2;
		tablePane.setLayoutData(gd);

		final FillLayout fillLayout = new FillLayout();
		tablePane.setLayout(fillLayout);

		doctypesTable = new Table(tablePane, SWT.CHECK);

	}

	private void populateStyle() {
		setText(nameText, style.getName());
	}

	private static void setText(final Text textBox, final String text) {
		textBox.setText(text == null ? "" : text); //$NON-NLS-1$
	}

	private void populateDoctypes() {
		doctypesTable.removeAll();

		final DocumentType[] documentTypes = VexPlugin.getDefault().getConfigurationRegistry().getDocumentTypes();
		Arrays.sort(documentTypes);
		for (final DocumentType documentType : documentTypes) {
			final TableItem item = new TableItem(doctypesTable, SWT.NONE);
			item.setText(documentType.getName());
			if (style != null && style.appliesTo(documentType)) {
				item.setChecked(true);
			}
		}
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	@Override
	public void performApply() {
		style.setName(nameText.getText());

		final ArrayList<String> selectedDoctypes = new ArrayList<String>();
		for (final TableItem item : doctypesTable.getItems()) {
			if (item.getChecked()) {
				selectedDoctypes.add(item.getText());
			}
		}

		style.removeAllDocumentTypes();

		final DocumentType[] documentTypes = VexPlugin.getDefault().getConfigurationRegistry().getDocumentTypes();
		Arrays.sort(documentTypes);
		for (final DocumentType documentType : documentTypes) {
			if (selectedDoctypes.contains(documentType.getName())) {
				style.addDocumentType(documentType.getMainId());
			}
		}

		try {
			pluginProject.writeConfigXml();
		} catch (final Exception e) {
			final String message = MessageFormat.format(Messages.getString("StylePropertyPage.writeError"), //$NON-NLS-1$
					new Object[] { PluginProject.PLUGIN_XML });
			VexPlugin.getDefault().log(IStatus.ERROR, message, e);
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		populateStyle();
		populateDoctypes();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (configListener != null) {
			VexPlugin.getDefault().getConfigurationRegistry().removeConfigListener(configListener);
		}
	}
}
