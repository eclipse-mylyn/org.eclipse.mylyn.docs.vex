/*******************************************************************************
 * Copyright (c) 2010 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.vex.core.internal.core.Image;

/**
 * @author Florian Thienel
 */
public class SwtImage implements Image {

	public final ImageData imageData;

	public SwtImage(final ImageData imageData) {
		this.imageData = imageData;
	}

	@Override
	public int getHeight() {
		return imageData.height;
	}

	@Override
	public int getWidth() {
		return imageData.width;
	}
}
