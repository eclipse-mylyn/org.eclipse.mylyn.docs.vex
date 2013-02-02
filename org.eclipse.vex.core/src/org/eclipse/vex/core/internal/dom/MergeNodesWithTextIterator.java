package org.eclipse.vex.core.internal.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;

public class MergeNodesWithTextIterator implements Iterator<Node> {

	private final Parent parent;
	private final Iterator<Node> nodes;
	private final Content content;
	private final ContentRange contentRange;

	private int textCursor;
	private Node currentChild;
	private ContentRange nextTextGap;

	public MergeNodesWithTextIterator(final Parent parent, final Iterable<Node> nodes, final Content content, final ContentRange contentRange) {
		this.parent = parent;
		this.nodes = nodes.iterator();
		this.content = content;
		this.contentRange = contentRange.intersection(parent.getRange());
		initialize();
	}

	private void initialize() {
		currentChild = null;
		nextTextGap = contentRange;
		textCursor = contentRange.getStartOffset();
		nextStep();
	}

	private void nextStep() {
		while (nodes.hasNext()) {
			currentChild = nodes.next();
			if (!currentChild.isAssociated()) {
				nextTextGap = contentRange;
				return;
			} else if (currentChild.isInRange(contentRange)) {
				nextTextGap = currentChild.getRange();
				textCursor = findNextTextStart(textCursor, nextTextGap.getStartOffset());
				return;
			} else if (contentRange.contains(currentChild.getStartOffset())) {
				nextTextGap = contentRange.intersection(currentChild.getRange());
				textCursor = findNextTextStart(textCursor, nextTextGap.getStartOffset());
				currentChild = null; // we can bail out here because we are behind the trimmed range now
				return;
			} else if (contentRange.contains(currentChild.getEndOffset())) {
				textCursor = currentChild.getEndOffset() + 1;
			}
		}

		currentChild = null;
		nextTextGap = new ContentRange(contentRange.getEndOffset(), contentRange.getEndOffset());
		textCursor = findNextTextStart(textCursor, contentRange.getEndOffset());
	}

	private int findNextTextStart(int currentOffset, final int maximumOffset) {
		while (currentOffset < maximumOffset && content.isTagMarker(currentOffset)) {
			currentOffset++;
		}
		return currentOffset;
	}

	private int findNextTextEnd(int currentOffset, final int minimumOffset) {
		while (currentOffset > minimumOffset && content.isTagMarker(currentOffset)) {
			currentOffset--;
		}
		return currentOffset;
	}

	public boolean hasNext() {
		return hasMoreChildrenInRange() || hasMoreText();
	}

	private boolean hasMoreChildrenInRange() {
		return currentChild != null;
	}

	private boolean hasMoreText() {
		return textCursor < nextTextGap.getStartOffset();
	}

	public Node next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		if (currentChild != null && !currentChild.isAssociated()) {
			return nextChild();
		}

		final int textStart = findNextTextStart(textCursor, nextTextGap.getStartOffset());
		final int textEnd = findNextTextEnd(nextTextGap.getStartOffset(), textStart);
		textCursor = nextTextGap.getEndOffset() + 1;

		if (textStart < textEnd) {
			return nextText(textStart, textEnd);
		}
		if (textStart == textEnd && !content.isTagMarker(textStart)) {
			return nextText(textStart, textEnd);
		}

		Assert.isNotNull(currentChild, "No text and no node make Vex go crazy!");

		return nextChild();
	}

	private Node nextChild() {
		final Node child = currentChild;
		nextStep();
		return child;
	}

	private Node nextText(final int textStart, final int textEnd) {
		return new Text(parent, content, new ContentRange(textStart, textEnd));
	}

	public void remove() {
		throw new UnsupportedOperationException("Cannot remove node.");
	}
}