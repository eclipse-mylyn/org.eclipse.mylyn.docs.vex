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
public class CursorPosition {

	private final ContentMap contentMap;
	private int offset;

	public CursorPosition(final ContentMap contentMap) {
		this.contentMap = contentMap;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void left() {
		offset = Math.max(0, offset - 1);
	}

	public void right() {
		offset = Math.min(offset + 1, contentMap.getLastPosition());
	}
}
