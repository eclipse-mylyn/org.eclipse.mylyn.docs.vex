/*******************************************************************************
 * Copyright (c) 2015 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.vex.ui.internal.config.ConfigLoaderJob;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistry;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistryImpl;
import org.eclipse.vex.ui.internal.config.DoctypePropertyPage;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the doctype definition in a Vex Plugin Project
 *
 */
public class PluginDoctypeConfigTest {
	private IProject project;
	private ConfigurationRegistry configurationRegistry;

	private static final String UTF_8 = "UTF-8";
	private static final String SIMPLE_DTD = "<!ELEMENT el1 ANY><!ELEMENT el2 ANY>";

	@After
	public void dispose() {
		if (project != null) {
			try {
				project.delete(true, null);
			} catch (final CoreException e) {
			}
		}
		project = null;

		if (configurationRegistry != null) {
			configurationRegistry.dispose();
		}
		configurationRegistry = null;
	}

	/**
	 * Make sure the property page is created without errors.
	 */
	@Test
	public void testDtdPropertyPage() throws Exception {
		project = PluginProjectTest.createVexPluginProject("DtdPropertyPageTest");
		final InputStream is = new ByteArrayInputStream(SIMPLE_DTD.getBytes(UTF_8));
		project.getFile(PluginProjectTest.DTD_FILE_NAME).setContents(is, true, false, null);

		final Display display = Display.getCurrent();
		final Shell shell = new Shell(display);
		final DoctypePropertyPage page = new DoctypePropertyPage();

		final IFile dtdFile = project.getFile(PluginProjectTest.DTD_FILE_NAME);
		page.setElement(dtdFile);
		page.createControl(shell);

		// Make sure the table contains the style declared in the plugin project
		final Field f = page.getClass().getDeclaredField("rootElementsTable");
		f.setAccessible(true);
		final Table table = (Table) f.get(page);
		assertEquals("Displayed root elements", 2, table.getItems().length);

		page.dispose();
	}

	@Test
	public void testAddDoctype() throws Exception {
		project = PluginProjectTest.createVexPluginProject("DoctypePropertyPageTest");

		final Display display = Display.getCurrent();
		final Shell shell = new Shell(display);
		final DoctypePropertyPage page = new DoctypePropertyPage();

		// Create a new .dtd file and open the property page
		final IFile file = project.getFile("test_new.dtd");
		final InputStream is = new ByteArrayInputStream(SIMPLE_DTD.getBytes(UTF_8));
		file.create(is, true, null);
		page.setElement(file);
		page.createControl(shell);

		// Select a root element
		Field f = page.getClass().getDeclaredField("rootElementsTable");
		f.setAccessible(true);
		final Table table = (Table) f.get(page);
		for (final TableItem item : table.getItems()) {
			if (item.getText().equals("el2")) {
				item.setChecked(true);
			}
		}

		// Set the styles's name
		f = page.getClass().getDeclaredField("nameText");
		f.setAccessible(true);
		final Text nameText = (Text) f.get(page);
		nameText.setText("dtd test name");

		// Set a public id
		f = page.getClass().getDeclaredField("publicIdText");
		f.setAccessible(true);
		final Text publicIdText = (Text) f.get(page);
		publicIdText.setText("test public id");

		// Write the changed configuration
		page.performOk();
		page.dispose();

		// Reload the project
		project.close(null);
		project.open(null);

		//
		configurationRegistry = new ConfigurationRegistryImpl(new ConfigLoaderJob());
		configurationRegistry.loadConfigurations();

		final DocumentType doctype = configurationRegistry.getDocumentType("test public id", null);
		assertNotNull("New style should be present", doctype);
		assertEquals("test_new.dtd", doctype.getResourceUri().toString());
		assertEquals("test_new.dtd", doctype.getSystemId()); // The system id should match the filename by default
	}

}
