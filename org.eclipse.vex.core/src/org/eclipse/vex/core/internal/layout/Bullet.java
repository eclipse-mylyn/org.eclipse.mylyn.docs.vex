/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.internal.core.Drawable;

/**
 * @author Florian Thienel
 */
public abstract class Bullet implements Drawable {

	// size of the bullet as fraction of font-size
	private static final float BULLET_SIZE = 0.5f;
	// vspace between list-item bullet and baseine, as fraction of font-size
	private static final float BULLET_LIFT = 0.1f;

	final int size;
	final int lift;

	public Bullet(final float fontSize) {
		size = Math.round(BULLET_SIZE * fontSize);
		lift = Math.round(BULLET_LIFT * fontSize);
	}

	public int getSize() {
		return size;
	}

	public int getLift() {
		return lift;
	}

}
