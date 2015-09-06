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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.vex.ui.internal.config.ConfigLoaderJob;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistry;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistryImpl;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.config.StylePropertyPage;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the CSS styles definition in a Vex Plugin Project
 *
 * @author chi
 *
 */
public class PluginStyleConfigTest {

	private IProject pluginProject;

	@After
	public void dispose() {
		if (pluginProject != null) {
			try {
				pluginProject.delete(true, null);
			} catch (final CoreException e) {
			}
		}
		pluginProject = null;
	}

	/**
	 * Make sure the property page is created without errors.
	 */
	@Test
	public void testCssPropertyPage() throws CoreException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final IProject pluginProject = PluginProjectTest.createVexPluginProject("PropertyPageTest");

		final Display display = Display.getCurrent();
		final Shell shell = new Shell(display);
		final StylePropertyPage page = new StylePropertyPage();

		page.setElement(pluginProject.getFile(PluginProjectTest.CSS_FILE_NAME));
		page.createControl(shell);

		// Make sure the table contains the style declared in the plugin project
		final Field f = page.getClass().getDeclaredField("doctypesTable");
		f.setAccessible(true);
		final Table doctypesTable = (Table) f.get(page);
		final ArrayList<String> doctypes = new ArrayList<String>();
		for (final TableItem item : doctypesTable.getItems()) {
			if (item.getChecked()) {
				// The item should be selected
				doctypes.add(item.getText());
			}
		}
		assertTrue("Doctype defined by plugin project should be selected", doctypes.contains(PluginProjectTest.DTD_FILE_NAME));

		page.dispose();
	}

	@Test
	public void testDoctypeStyle() throws Exception {
		final IProject pluginProject = PluginProjectTest.createVexPluginProject("PropertyPageTest");

		final ConfigurationRegistry configurationRegistry = new ConfigurationRegistryImpl(new ConfigLoaderJob());
		configurationRegistry.loadConfigurations();
		assertNotNull(configurationRegistry.getPluginProject(pluginProject));

		final DocumentType doctype = configurationRegistry.getDocumentType(PluginProjectTest.DTD_DOCTYPE_ID, null);
		assertNotNull(doctype);
		final Style[] styles = configurationRegistry.getStyles(doctype);

		assertEquals(styles[0], configurationRegistry.getStyle("PropertyPageTest." + PluginProjectTest.CSS_ID));

		configurationRegistry.dispose();
	}
}
