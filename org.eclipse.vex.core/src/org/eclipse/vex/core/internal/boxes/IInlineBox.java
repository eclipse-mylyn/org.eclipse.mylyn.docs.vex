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
public interface IInlineBox extends IChildBox {

	void setPosition(int top, int left);

	/**
	 * The baseline is relative to the top of this box.
	 */
	int getBaseline();

	boolean canJoin(IInlineBox other);

	boolean join(IInlineBox other);

	boolean canSplit();

	IInlineBox splitTail(Graphics graphics, int headWidth, boolean force);

}
