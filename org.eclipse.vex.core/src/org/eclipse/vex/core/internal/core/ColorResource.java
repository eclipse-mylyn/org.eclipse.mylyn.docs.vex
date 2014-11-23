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
package org.eclipse.vex.core.internal.core;

/**
 * Wrapper for a toolkit-defined color. Color objects are system resources. They should be retrieved with the
 * Graphics.createColor method and should be disposed when no longer needed.
 */
public interface ColorResource {

	ColorResource NULL = new ColorResource() {
		@Override
		public void dispose() {
		}
	};

	int SELECTION_BACKGROUND = 0;
	int SELECTION_FOREGROUND = 1;

	void dispose();

}
