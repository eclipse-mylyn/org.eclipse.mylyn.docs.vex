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
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.vex.core.internal.core.ColorResource;

/**
 * Wrapper for the SWT Color class.
 */
public class SwtColor implements ColorResource {

	private final org.eclipse.swt.graphics.Color swtColor;

	public SwtColor(final org.eclipse.swt.graphics.Color swtColor) {
		this.swtColor = swtColor;
	}

	org.eclipse.swt.graphics.Color getSwtColor() {
		return swtColor;
	}

	public void dispose() {
		swtColor.dispose();
	}
}
