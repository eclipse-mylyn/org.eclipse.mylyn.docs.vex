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
package org.eclipse.vex.ui.internal.swt;

import org.eclipse.vex.core.internal.core.FontResource;

/**
 * Wrapper for the SWT Font class.
 */
public class SwtFont implements FontResource {

	private final org.eclipse.swt.graphics.Font swtFont;

	public SwtFont(final org.eclipse.swt.graphics.Font swtFont) {
		this.swtFont = swtFont;
	}

	org.eclipse.swt.graphics.Font getSwtFont() {
		return swtFont;
	}

	public void dispose() {
		swtFont.dispose();
	}
}
