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

import org.eclipse.swt.widgets.Display;
import org.eclipse.vex.core.internal.core.DisplayDevice;

/**
 * Adapts the DisplayDevice display to the current SWT display.
 */
public class SwtDisplayDevice extends DisplayDevice {

	/**
	 * Class constructor.
	 */
	public SwtDisplayDevice() {
		// We used to do it like this, but it turns out sometimes we did it
		// too early and getCurrent() returned null, so now the convoluted stuff
		// below.
		// Display display = Display.getCurrent();
		// this.horizontalPPI = display.getDPI().x;
		// this.verticalPPI = display.getDPI().y;
	}

	@Override
	public int getHorizontalPPI() {
		if (!loaded) {
			load();
		}
		return horizontalPPI;
	}

	@Override
	public int getVerticalPPI() {
		if (!loaded) {
			load();
		}
		return verticalPPI;
	}

	private boolean loaded = false;
	private int horizontalPPI = 72;
	private int verticalPPI = 72;

	private void load() {
		final Display display = Display.getCurrent();
		if (display != null) {
			horizontalPPI = display.getDPI().x;
			verticalPPI = display.getDPI().y;
			loaded = true;
		}
	}

}
