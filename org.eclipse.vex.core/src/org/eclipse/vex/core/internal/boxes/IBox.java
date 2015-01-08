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
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public interface IBox {

	int getAbsoluteTop();

	int getAbsoluteLeft();

	int getTop();

	int getLeft();

	int getWidth();

	int getHeight();

	void accept(IBoxVisitor visitor);

	<T> T accept(IBoxVisitorWithResult<T> visitor);

	void layout(Graphics graphics);

	void paint(Graphics graphics);

	public abstract boolean isLeftFrom(final int x);

	public abstract boolean isRightFrom(final int x);

	public abstract boolean containsX(final int x);

	public abstract boolean isBelow(final int y);

	public abstract boolean isAbove(final int y);

	public abstract boolean containsY(final int y);

	public abstract boolean containsCoordinates(final int x, final int y);
}
