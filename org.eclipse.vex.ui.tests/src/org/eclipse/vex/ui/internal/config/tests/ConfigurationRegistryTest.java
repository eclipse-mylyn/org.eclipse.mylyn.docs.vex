/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - additional tests
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.vex.ui.internal.config.ConfigEvent;
import org.eclipse.vex.ui.internal.config.ConfigLoaderJob;
import org.eclipse.vex.ui.internal.config.ConfigSource;
import org.eclipse.vex.ui.internal.config.ConfigurationLoader;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistry;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistryImpl;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.IConfigListener;
import org.eclipse.vex.ui.internal.config.PluginProject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author Florian Thienel
 */
public class ConfigurationRegistryTest {

	private ConfigurationRegistry registry;

	@Rule
	public TestName name = new TestName();

	@After
	public void disposeRegistry() {
		if (registry != null) {
			registry.dispose();
		}
		registry = null;
	}

	@Test
	public void notAutomaticallyLoaded() throws Exception {
		registry = new ConfigurationRegistryImpl(new MockConfigurationLoader());
		assertFalse(registry.isLoaded());
	}

	@Test
	public void fireLoadedEventOnLoad() throws Exception {
		registry = new ConfigurationRegistryImpl(new MockConfigurationLoader());
		final MockConfigListener configListener = new MockConfigListener();
		registry.addConfigListener(configListener);
		registry.loadConfigurations();
		assertTrue(registry.isLoaded());
		assertTrue(configListener.loaded);
		assertFalse(configListener.changed);
	}

	@Test
	public void loadNewPluginProjectAndFireChangedEvent() throws Exception {
		registry = new ConfigurationRegistryImpl(new MockConfigurationLoader());
		final MockConfigListener configListener = new MockConfigListener();
		registry.addConfigListener(configListener);
		registry.loadConfigurations();
		configListener.reset();
		final IProject project = PluginProjectTest.createVexPluginProject(name.getMethodName());
		assertFalse(configListener.loaded);
		assertTrue(configListener.changed);
		assertNotNull(registry.getPluginProject(project));
	}

	@Test
	public void reloadModifiedPluginProjectAndFireConfigChangedEvent() throws Exception {
		registry = new ConfigurationRegistryImpl(new MockConfigurationLoader());
		registry.loadConfigurations();
		final IProject project = PluginProjectTest.createVexPluginProject(name.getMethodName());
		final MockConfigListener configListener = new MockConfigListener();
		project.getFile("plugintest2.css").create(new ByteArrayInputStream(new byte[0]), true, null);
		final String fileContent = PluginProjectTest.createVexPluginFileContent(project, "plugintest.dtd", "plugintest.css", "plugintest2.css");
		registry.addConfigListener(configListener);
		project.getFile(PluginProject.PLUGIN_XML).setContents(new ByteArrayInputStream(fileContent.getBytes()), true, true, null);
		assertFalse(configListener.loaded);
		assertTrue(configListener.changed);
		assertNotNull(registry.getPluginProject(project).getItemForResource(project.getFile("plugintest2.css")));
	}

	@Ignore("I don't understand why it fails...WTF???")
	@Test
	public void removeDeletedPluginProjectAndFireConfigChangedEvent() throws Exception {
		registry = new ConfigurationRegistryImpl(new MockConfigurationLoader());
		registry.loadConfigurations();
		final IProject project = PluginProjectTest.createVexPluginProject(name.getMethodName());
		final MockConfigListener configListener = new MockConfigListener();
		registry.addConfigListener(configListener);
		project.getFile("plugintest.css").delete(true, null);
		assertTrue(configListener.changed);
		assertNotNull(registry.getPluginProject(project));
		configListener.reset();
		project.getFile("plugintest.dtd").delete(true, null);
		assertTrue(configListener.changed);
		assertNotNull(registry.getPluginProject(project));
		configListener.reset();
		project.getFile(PluginProject.PLUGIN_XML).delete(true, null);
		assertTrue(configListener.changed);
		assertNotNull(registry.getPluginProject(project));
	}

	@Test
	public void testPluginDoctypeDefinition() throws Exception {
		final ConfigurationRegistry configurationRegistry = new ConfigurationRegistryImpl(new ConfigLoaderJob());
		configurationRegistry.loadConfigurations();

		final DocumentType doctype = configurationRegistry.getDocumentType("-//Vex//DTD Test//EN", null);
		assertNotNull(doctype);
		assertEquals("test.dtd", doctype.getSystemId());
	}

	@Test
	public void testPluginNamespaceDefinition() throws Exception {
		final ConfigurationRegistry configurationRegistry = new ConfigurationRegistryImpl(new ConfigLoaderJob());
		configurationRegistry.loadConfigurations();

		final DocumentType doctype = configurationRegistry.getDocumentType("http://org.eclipse.vex/namespace", null);
		assertNotNull(doctype);
		assertEquals("test schema doctype", doctype.getName());
	}

	@Test
	public void testGetDocumentTypesWithStyles() throws Exception {
		final ConfigurationRegistry configurationRegistry = new ConfigurationRegistryImpl(new ConfigLoaderJob());
		configurationRegistry.loadConfigurations();

		final DocumentType[] doctypes = configurationRegistry.getDocumentTypesWithStyles();
		boolean dtdFound = false;
		boolean schemaFound = false;
		for (final DocumentType doctype : doctypes) {
			if ("test doctype".equals(doctype.getName())) {
				dtdFound = true;
			}
			if ("test schema doctype".equals(doctype.getName())) {
				schemaFound = true;
			}
		}
		assertTrue("DoctypeWithStyles should return Doctype with DTD ", dtdFound);
		assertTrue("DoctypeWithStyles should return Docype with namespace ", schemaFound);
	}

	private static class MockConfigurationLoader implements ConfigurationLoader {
		private final List<ConfigSource> loadedConfigSources;

		public MockConfigurationLoader() {
			this(Collections.<ConfigSource> emptyList());
		}

		public MockConfigurationLoader(final List<ConfigSource> loadedConfigSources) {
			this.loadedConfigSources = loadedConfigSources;
		}

		@Override
		public void load(final Runnable whenDone) {
			whenDone.run();
		}

		@Override
		public List<ConfigSource> getLoadedConfigSources() {
			return loadedConfigSources;
		}

		@Override
		public void join() throws InterruptedException {
			return;
		}
	}

	private static class MockConfigListener implements IConfigListener {
		public boolean changed = false;
		public boolean loaded = false;

		@Override
		public void configChanged(final ConfigEvent e) {
			changed = true;
		}

		@Override
		public void configLoaded(final ConfigEvent e) {
			loaded = true;
		}

		public void reset() {
			changed = false;
			loaded = false;
		}
	}
}
