/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
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

import java.net.URL;

import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.VexPreferences;
import org.eclipse.vex.ui.internal.config.ConfigSource;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.junit.Test;

public class PreferencesTest {

	@Test
	public void testStylesheetPreference() throws Exception {
		final VexPreferences preferences = VexPlugin.getDefault().getPreferences();
		final DocumentType doctype = new DocumentType(new MockConfigSource());
		doctype.setSimpleId("vex_test_doctype");
		final Style style = new Style(new MockConfigSource());
		style.setSimpleId("vex-test-style");
		preferences.setPreferredStyleId(doctype, style.getUniqueId());

		final String preferredStyleId = preferences.getPreferredStyleId(doctype);
		assertEquals(style.getUniqueId(), preferredStyleId);
	}

	private class MockConfigSource extends ConfigSource {

		public MockConfigSource() {
			super("test_config");
		}

		@Override
		public URL getBaseUrl() {
			return null;
		}

	}
}
