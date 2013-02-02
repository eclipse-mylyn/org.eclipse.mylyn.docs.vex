package org.eclipse.vex.core.internal.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodesInContentRangeIterator implements Iterator<Node> {

	private final Iterator<Node> nodes;
	private final ContentRange contentRange;

	private Node currentNode;

	public NodesInContentRangeIterator(final Iterable<Node> nodes, final ContentRange contentRange) {
		this.contentRange = contentRange;
		this.nodes = nodes.iterator();
		nextStep();
	}

	private void nextStep() {
		while (nodes.hasNext()) {
			currentNode = nodes.next();
			if (!currentNode.isAssociated()) {
				return;
			}
			if (contentRange.contains(currentNode.getRange())) {
				return;
			}
		}
		currentNode = null;
	}

	public boolean hasNext() {
		return currentNode != null;
	}

	public Node next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		final Node next = currentNode;
		nextStep();
		return next;
	}

	public void remove() {
		throw new UnsupportedOperationException("Cannot remove node.");
	}
}