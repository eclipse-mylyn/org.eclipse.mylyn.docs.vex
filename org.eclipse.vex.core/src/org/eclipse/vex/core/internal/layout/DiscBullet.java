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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class DiscBullet extends Bullet {

	public DiscBullet(final float fontSize) {
		super(fontSize);
	}

	@Override
	public void draw(final Graphics g, final int x, final int y) {
		g.fillOval(x, y - size - lift, size, size);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(0, -size - lift, size, size);
	}

}
