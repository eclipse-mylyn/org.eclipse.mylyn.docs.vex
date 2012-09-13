package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class Parent extends Node {

	private final List<Node> children = new ArrayList<Node>();

	public void addChild(final Node child) {
		children.add(child);
		child.setParent(this);
	}

	public void insertChild(final int index, final Node child) {
		children.add(index, child);
		child.setParent(this);
	}

	public void removeChild(final Node child) {
		children.remove(child);
		child.setParent(null);
	}

	public List<Node> getChildNodes() {
		return getChildNodes(getStartOffset() + 1, getEndOffset() - 1);
	}

	/**
	 * Returns a list of all child nodes (including Text nodes) in the given range. The Text nodes are cut at the edges,
	 * all other nodes must be fully contained in the range (i.e. the start tag and the end tag).
	 * 
	 * @param startOffset
	 *            the start offset of the range
	 * @param endOffset
	 *            the end offset of the range
	 * @return all child nodes which are completely within the given range plus the textual content
	 */
	public List<Node> getChildNodes(final int startOffset, final int endOffset) {
		final List<Node> result = new ArrayList<Node>();
		int offset = Math.max(startOffset, getStartOffset() + 1);
		for (final Node child : children) {
			if (child.isAssociated()) {
				final int childStart = child.getStartOffset();
				final int childEnd = child.getEndOffset();
				if (offset < childStart) {
					final int textEnd = Math.min(childStart, endOffset);
					result.add(new Text(getContent(), offset, textEnd));
					offset = textEnd + 1;
				}
				if (childStart >= startOffset && childStart <= endOffset && childEnd <= endOffset) {
					result.add(child);
					offset = childEnd + 1;
				} else if (childEnd >= startOffset) {
					offset = childEnd + 1;
				}
			} else {
				result.add(child);
			}
		}

		final int tailTextEnd = Math.min(endOffset, getEndOffset());
		if (offset < tailTextEnd) {
			result.add(new Text(getContent(), offset, tailTextEnd));
		}

		return Collections.unmodifiableList(result);
	}

	public Iterator<Node> getChildIterator() {
		return getChildNodes().iterator();
	}

	public Node getChildNode(final int index) {
		return children.get(index);
	}

	public int getChildCount() {
		return children.size();
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
