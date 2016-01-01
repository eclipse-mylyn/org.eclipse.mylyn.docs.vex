package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;

public class BalancableRange {
	private final int startOffset;
	private final int endOffset;
	private final INode node;
	private final IDocument document;

	public BalancableRange(final IDocument document, final int mark, final int offset) {
		this.document = document;
		startOffset = Math.min(mark, offset);
		endOffset = Math.max(mark, offset);
		node = document.findCommonNode(startOffset, endOffset);
	}

	public ContentRange expand() {
		final int balancedStart = balanceBackward(startOffset, node);
		final int balancedEnd = balanceForward(endOffset, node);
		return new ContentRange(balancedStart, balancedEnd);
	}

	public ContentRange reduceForward() {
		final int balancedStart = balanceForward(startOffset, node);
		final int balancedEnd = balanceForward(endOffset, node);
		return new ContentRange(balancedStart, balancedEnd);
	}

	public ContentRange reduceBackward() {
		final int balancedStart = balanceBackward(startOffset, node);
		final int balancedEnd = balanceBackward(endOffset, node);
		return new ContentRange(balancedStart, balancedEnd);
	}

	private int balanceForward(final int offset, final INode node) {
		if (getParentForInsertionAt(offset) == node) {
			return offset;
		}

		int balancedOffset = moveToNextNode(offset);
		while (getParentForInsertionAt(balancedOffset) != node) {
			balancedOffset = document.getChildAt(balancedOffset).getEndOffset() + 1;
		}
		return balancedOffset;
	}

	private int moveToNextNode(final int offset) {
		final INode nodeAtOffset = getParentForInsertionAt(offset);
		final IParent parent = nodeAtOffset.getParent();
		if (parent == null) {
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

		int balancedOffset = document.getChildAt(offset).getStartOffset();
		IParent parent = document.getChildAt(balancedOffset).getParent();
		while (parent != null && parent != node) {
			balancedOffset = parent.getStartOffset();
			parent = document.getChildAt(balancedOffset).getParent();
		}

		return balancedOffset;
	}

}
