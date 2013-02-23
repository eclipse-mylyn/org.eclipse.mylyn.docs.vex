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

import org.eclipse.vex.core.internal.core.FontMetrics;

/**
 * Wrapper for the SWT FontMetrics class.
 */
public class SwtFontMetrics implements FontMetrics {

	private final org.eclipse.swt.graphics.FontMetrics swtFontMetrics;

	public SwtFontMetrics(final org.eclipse.swt.graphics.FontMetrics swtFontMetrics) {
		this.swtFontMetrics = swtFontMetrics;
	}

	/**
	 * @see org.eclipse.vex.core.internal.core.FontMetrics#getAscent()
	 */
	public int getAscent() {
		return swtFontMetrics.getAscent();
	}

	/**
	 * @see org.eclipse.vex.core.internal.core.FontMetrics#getDescent()
	 */
	public int getDescent() {
		return swtFontMetrics.getDescent();
	}

	/**
	 * @see org.eclipse.vex.core.internal.core.FontMetrics#getHeight()
	 */
	public int getHeight() {
		return swtFontMetrics.getHeight();
	}

	/**
	 * @see org.eclipse.vex.core.internal.core.FontMetrics#getLeading()
	 */
	public int getLeading() {
		return swtFontMetrics.getLeading();
	}

}
