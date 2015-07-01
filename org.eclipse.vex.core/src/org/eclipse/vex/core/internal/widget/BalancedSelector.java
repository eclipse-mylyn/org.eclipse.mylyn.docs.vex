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

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;

/**
 * @author Florian Thienel
 */
public class BalancedSelector extends BaseSelector {

	private IDocument document;

	public void setDocument(final IDocument document) {
		this.document = document;
		setMark(0);
	}

	@Override
	public void moveTo(final int offset) {
		if (document == null) {
			return;
		}
		if (offset == getMark()) {
			setMark(offset);
		}

		final Movement movement = new Movement(offset);
		final BalancableRange rawRange = new BalancableRange(document, getMark(), offset);

		if (movement.movingForward && movement.movingTowardMark) {
			setRange(rawRange.reduceForward());
			setCaretOffset(getStartOffset());
		} else if (movement.movingBackward && movement.movingTowardMark) {
			setRange(rawRange.reduceBackward());
			setCaretOffset(getEndOffset());
		} else if (movement.movingForward && movement.movingAwayFromMark) {
			setRange(rawRange.expand());
			setCaretOffset(getEndOffset());
		} else if (movement.movingBackward && movement.movingAwayFromMark) {
			setRange(rawRange.expand());
			setCaretOffset(getStartOffset());
		}
	}

	@Override
	public void endAt(final int offset) {
		if (document == null) {
			return;
		}
		if (offset == getMark()) {
			setMark(offset);
		}

		final Movement movement = new Movement(offset);
		final BalancableRange rawRange = new BalancableRange(document, getMark(), offset);

		if (movement.beforeMark) {
			setRange(rawRange.expand());
			setCaretOffset(getStartOffset());
		} else if (movement.afterMark) {
			setRange(rawRange.expand());
			setCaretOffset(getEndOffset());
		}
	}

	private void setRange(final ContentRange range) {
		setStartOffset(range.getStartOffset());
		setEndOffset(range.getEndOffset());
	}

	private class Movement {

		public final boolean movingForward;
		public final boolean movingBackward;
		public final boolean beforeMark;
		public final boolean afterMark;
		public final boolean movingTowardMark;
		public final boolean movingAwayFromMark;

		public Movement(final int offset) {
			movingForward = offset > getCaretOffset();
			movingBackward = offset < getCaretOffset();
			beforeMark = offset < getMark();
			afterMark = offset > getMark();
			movingTowardMark = movingForward && beforeMark || movingBackward && afterMark;
			movingAwayFromMark = movingForward && afterMark || movingBackward && beforeMark;
		}
	}

}
