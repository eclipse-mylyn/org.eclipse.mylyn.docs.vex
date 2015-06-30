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

import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;

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

		final boolean movingForward = offset > getCaretOffset();
		final boolean movingBackward = offset < getCaretOffset();
		final boolean beforeMark = offset < getMark();
		final boolean afterMark = offset > getMark();
		final boolean movingTowardMark = movingForward && beforeMark || movingBackward && afterMark;
		final boolean movingAwayFromMark = movingForward && afterMark || movingBackward && beforeMark;

		// expand or shrink the selection to make sure the selection is balanced
		final int balancedStart = Math.min(getMark(), offset);
		final int balancedEnd = Math.max(getMark(), offset);
		final INode balancedNode = document.findCommonNode(balancedStart, balancedEnd);

		if (movingForward && movingTowardMark) {
			setStartOffset(balanceForward(balancedStart, balancedNode));
			setEndOffset(balanceForward(balancedEnd, balancedNode));
			setCaretOffset(getStartOffset());
		} else if (movingBackward && movingTowardMark) {
			setStartOffset(balanceBackward(balancedStart, balancedNode));
			setEndOffset(balanceBackward(balancedEnd, balancedNode));
			setCaretOffset(getEndOffset());
		} else if (movingForward && movingAwayFromMark) {
			setStartOffset(balanceBackward(balancedStart, balancedNode));
			setEndOffset(balanceForward(balancedEnd, balancedNode));
			setCaretOffset(getEndOffset());
		} else if (movingBackward && movingAwayFromMark) {
			setStartOffset(balanceBackward(balancedStart, balancedNode));
			setEndOffset(balanceForward(balancedEnd, balancedNode));
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

		final boolean beforeMark = offset < getMark();
		final boolean afterMark = offset > getMark();

		// expand or shrink the selection to make sure the selection is balanced
		final int balancedStart = Math.min(getMark(), offset);
		final int balancedEnd = Math.max(getMark(), offset);
		final INode balancedNode = document.findCommonNode(balancedStart, balancedEnd);

		if (beforeMark) {
			setStartOffset(balanceBackward(balancedStart, balancedNode));
			setEndOffset(balanceForward(balancedEnd, balancedNode));
			setCaretOffset(getStartOffset());
		} else if (afterMark) {
			setStartOffset(balanceBackward(balancedStart, balancedNode));
			setEndOffset(balanceForward(balancedEnd, balancedNode));
			setCaretOffset(getEndOffset());
		}
	}

	private int balanceForward(final int offset, final INode node) {
		if (getParentForInsertionAt(offset) == node) {
			return offset;
		}

		// Move the position to the start of the next node, this will insert in the parent
		int balancedOffset = moveToNextNode(offset);
		while (getParentForInsertionAt(balancedOffset) != node) {
			balancedOffset = document.getChildAt(balancedOffset).getParent().getEndOffset() + 1;
		}
		return balancedOffset;
	}

	private int moveToNextNode(final int offset) {
		final INode nodeAtOffset = getParentForInsertionAt(offset);
		final IParent parent = nodeAtOffset.getParent();
		if (parent == null) {
			// No parent, so return the end of the current node
			return nodeAtOffset.getEndOffset();
		}
		final IAxis<? extends INode> siblings = parent.children().after(offset);
		if (!siblings.isEmpty()) {
			return siblings.first().getStartOffset();
		} else {
			return parent.getEndOffset();
		}
	}

	private INode getParentForInsertionAt(final int offset) {
		final INode node = document.getChildAt(offset);
		if (offset == node.getStartOffset()) {
			return node.getParent();
		} else if (node instanceof IText) {
			return node.getParent();
		} else {
			return node;
		}
	}

	private int balanceBackward(final int offset, final INode node) {
		if (getParentForInsertionAt(offset) == node) {
			return offset;
		}

		// Insertion at the start position of a node inserts into the parent
		int balancedOffset = document.getChildAt(offset).getStartOffset();
		while (document.getChildAt(balancedOffset).getParent() != node) {
			balancedOffset = document.getChildAt(balancedOffset).getParent().getStartOffset();
		}
		return balancedOffset;
	}
}
