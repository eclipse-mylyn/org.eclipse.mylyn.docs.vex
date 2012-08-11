/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * Handler for language-specific strings in Vex.
 */
public class Messages {

	private static ResourceBundle resources;

	private Messages() {
	}

	/**
	 * Returns the language-specific string for the given key, or the key itself if not found.
	 */
	public static String getString(final String key) {
		if (resources == null) {
			resources = ResourceBundle.getBundle("org.eclipse.vex.ui.internal.config.messages"); //$NON-NLS-1$
		}

		try {
			return resources.getString(key);
		} catch (final MissingResourceException ex) {
			final String message = Messages.getString("Messages.cantFindResource"); //$NON-NLS-1$
			VexPlugin.getDefault().log(IStatus.WARNING, MessageFormat.format(message, new Object[] { key }));
			return key;
		}
	}

}
