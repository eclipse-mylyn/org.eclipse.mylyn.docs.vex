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
package org.eclipse.vex.core.internal.widget;

/**
 * @author Florian Thienel
 */
public class SimpleSelector extends BaseSelector {

	@Override
	public void moveTo(final int offset) {
		final boolean movingForward = offset > getCaretOffset();
		final boolean movingBackward = offset < getCaretOffset();
		final boolean movingTowardMark = movingForward && getMark() >= offset || movingBackward && getMark() <= offset;
		final boolean movingAwayFromMark = !movingTowardMark;

		if (movingForward && movingTowardMark) {
			setStartOffset(offset);
			setEndOffset(getMark());
			setCaretOffset(getStartOffset());
		} else if (movingBackward && movingTowardMark) {
			setStartOffset(getMark());
			setEndOffset(offset);
			setCaretOffset(getEndOffset());
		} else if (movingForward && movingAwayFromMark) {
			setStartOffset(getMark());
			setEndOffset(offset);
			setCaretOffset(getEndOffset());
		} else if (movingBackward && movingAwayFromMark) {
			setStartOffset(offset);
			setEndOffset(getMark());
			setCaretOffset(getStartOffset());
		}
	}

}
