/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.core.Rectangle;

public class FakeViewPort implements IViewPort {

	@Override
	public void reconcile(final int maximumHeight) {
		// ignore
	}

	@Override
	public Rectangle getVisibleArea() {
		return new Rectangle(0, 0, 100, 100);
	}

	@Override
	public void moveRelative(final int delta) {
		// ignore
	}
}