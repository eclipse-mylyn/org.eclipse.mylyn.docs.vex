package org.eclipse.vex.ui.internal.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.VexPreferences;

public class RootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public RootPreferencePage() {
		super(GRID);
		setPreferenceStore(VexPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(VexPreferences.LINE_WIDTH, "Line width:", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(VexPreferences.INDENTATION_CHAR_CHOICE, "Indentation", 1, new String[][] { { "Indent using tabs", VexPreferences.INDENTATION_CHAR_TAB },
				{ "Indent using spaces", VexPreferences.INDENTATION_CHAR_SPACE } }, getFieldEditorParent()));
		addField(new IntegerFieldEditor(VexPreferences.INDENTATION_SIZE, "Indentation size:", getFieldEditorParent()));
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

}