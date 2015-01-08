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
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class FakeContentBox extends BaseBox implements IContentBox {

	private final int startOffset;
	private final int endOffset;
	private final Rectangle area;

	public FakeContentBox(final int startOffset, final int endOffset, final Rectangle area) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.area = area;
	}

	@Override
	public int getAbsoluteTop() {
		return area.getY();
	}

	@Override
	public int getAbsoluteLeft() {
		return area.getX();
	}

	@Override
	public int getTop() {
		return area.getY();
	}

	@Override
	public int getLeft() {
		return area.getX();
	}

	@Override
	public int getWidth() {
		return area.getWidth();
	}

	@Override
	public int getHeight() {
		return area.getHeight();
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		// ignore
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return null;
	}

	@Override
	public void layout(final Graphics graphics) {
		// ignore
	}

	@Override
	public void paint(final Graphics graphics) {
		// ignore
	}

	@Override
	public int getStartOffset() {
		return startOffset;
	}

	@Override
	public int getEndOffset() {
		return endOffset;
	}

	@Override
	public Rectangle getPositionArea(final Graphics graphics, final int offset) {
		return area;
	}

	@Override
	public int getOffsetForCoordinates(final Graphics graphics, final int x, final int y) {
		return startOffset;
	}

}
