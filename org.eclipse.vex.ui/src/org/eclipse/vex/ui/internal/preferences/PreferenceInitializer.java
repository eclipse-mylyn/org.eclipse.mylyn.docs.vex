/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.VexPreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = VexPlugin.getDefault().getPreferenceStore();
		store.setDefault(VexPreferences.INDENTATION_CHAR_CHOICE, "\t");
		store.setDefault(VexPreferences.INDENTATION_SIZE, 1);
		store.setDefault(VexPreferences.LINE_WIDTH, 72);
		store.setDefault(VexPreferences.EXPERIMENTAL_USE_NEW_BOX_MODEL, false);
	}

}
