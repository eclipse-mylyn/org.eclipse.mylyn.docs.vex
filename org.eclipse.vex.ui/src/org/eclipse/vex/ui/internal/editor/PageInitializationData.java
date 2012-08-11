package org.eclipse.vex.ui.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.wst.xml.ui.internal.Logger;

public class PageInitializationData {
	IConfigurationElement fElement;
	String fPropertyName;
	Object fData;

	PageInitializationData(final IConfigurationElement cfig, final String propertyName, final Object data) {
		super();
		fElement = cfig;
		fPropertyName = propertyName;
		fData = data;
	}

	void sendInitializationData(final IExecutableExtension executableExtension) {
		try {
			executableExtension.setInitializationData(fElement, fPropertyName, fData);
		} catch (final CoreException e) {
			Logger.logException(e);
		}
	}
}