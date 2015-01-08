/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

/**
 * @author Florian Thienel
 */
public abstract class BaseBox implements IBox {

	@Override
	public final boolean containsCoordinates(final int x, final int y) {
		return containsX(x) && containsY(y);
	}

	@Override
	public final boolean containsY(final int y) {
		return !(isAbove(y) || isBelow(y));
	}

	@Override
	public final boolean isAbove(final int y) {
		return y > getAbsoluteTop() + getHeight();
	}

	@Override
	public final boolean isBelow(final int y) {
		return y < getAbsoluteTop();
	}

	@Override
	public final boolean containsX(final int x) {
		return x >= getAbsoluteLeft() && x <= getAbsoluteLeft() + getWidth();
	}

	@Override
	public final boolean isRightFrom(final int x) {
		return x < getAbsoluteLeft();
	}

	@Override
	public final boolean isLeftFrom(final int x) {
		return x > getAbsoluteLeft() + getWidth();
	}

}
