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
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.internal.core.DisplayDevice;

public class MockDisplayDevice extends DisplayDevice {

	private final int horizontalPPI;
	private final int verticalPPI;

	public MockDisplayDevice(final int horizontalPPI, final int verticalPPI) {
		this.horizontalPPI = horizontalPPI;
		this.verticalPPI = verticalPPI;
	}

	@Override
	public int getHorizontalPPI() {
		return horizontalPPI;
	}

	/**
	 *
	 */

	@Override
	public int getVerticalPPI() {
		return verticalPPI;
	}

}
