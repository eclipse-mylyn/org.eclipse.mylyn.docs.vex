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

import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public interface IStructuralBox extends IChildBox {

	void setPosition(int top, int left);

	void setWidth(int width);

	/**
	 * The bounds are always relative to the parent box.
	 */
	Rectangle getBounds();

}
