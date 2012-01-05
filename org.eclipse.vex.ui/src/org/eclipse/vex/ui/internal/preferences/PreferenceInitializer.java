package org.eclipse.vex.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.VexPreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		final IPreferenceStore store = VexPlugin.getDefault().getPreferenceStore();
		store.setDefault(VexPreferences.INDENTATION_CHAR_CHOICE, "\t");
		store.setDefault(VexPreferences.INDENTATION_SIZE, 1);
		store.setDefault(VexPreferences.LINE_WIDTH, 72);
	}

}
