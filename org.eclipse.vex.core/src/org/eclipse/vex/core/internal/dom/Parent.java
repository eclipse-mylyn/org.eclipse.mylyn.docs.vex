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
		//		return Document.createNodeList(getContent(), getStartOffset() + 1, getEndOffset(), childNodes);
		return Collections.unmodifiableList(children);
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
