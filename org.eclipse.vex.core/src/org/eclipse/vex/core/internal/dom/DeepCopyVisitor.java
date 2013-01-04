package org.eclipse.vex.core.internal.dom;

import java.util.List;

/**
 * This visitor creates a deep copy of the visited nodes. Deep copy means a full copy of each visited node and its
 * children down to the leaf level. All copied nodes are associated with a given content. The copied nodes on the root
 * level are collected in a given List.
 * 
 * @author Florian Thienel
 */
public class DeepCopyVisitor implements INodeVisitor {

	private final CopyVisitor copyVisitor = new CopyVisitor();

	private final List<Node> rootNodes;
	private final Content content;
	private final int delta;

	private Parent currentParent = null;

	/**
	 * @param rootNodes
	 *            the List to collect the copied nodes on the root level
	 * @param content
	 *            the content to associate the copied nodes with
	 * @param delta
	 *            the shift for the content association, relative to the beginning of the first node in the source
	 *            content
	 */
	public DeepCopyVisitor(final List<Node> rootNodes, final Content content, final int delta) {
		this.rootNodes = rootNodes;
		this.content = content;
		this.delta = delta;
	}

	public void visit(final Document document) {
		throw new UnsupportedOperationException("Document cannot be deep copied");
	}

	public void visit(final DocumentFragment fragment) {
		copyChildren(fragment, null);
	}

	public void visit(final Element element) {
		final Element copy = copy(element);
		addToParent(copy);
		associate(element, copy);

		copyChildren(element, copy);
	}

	public void visit(final Text text) {
		// ignore Text nodes because they are created dynamically in Element.getChildNodes()
	}

	public void visit(final Comment comment) {
		final Comment copy = copy(comment);
		addToParent(copy);
		associate(comment, copy);
	}

	@SuppressWarnings("unchecked")
	private <T extends Node> T copy(final T node) {
		return (T) node.accept(copyVisitor);
	}

	private void addToParent(final Node node) {
		if (currentParent == null) {
			rootNodes.add(node);
		} else {
			currentParent.addChild(node);
		}
	}

	private void associate(final Node source, final Node copy) {
		if (source.isAssociated()) {
			final Range range = source.getRange();
			copy.associate(content, range.moveBounds(delta));
		}
	}

	private void copyChildren(final Parent source, final Parent copy) {
		final Parent lastParent = currentParent;
		for (final Node child : source.getChildNodes()) {
			currentParent = copy;
			child.accept(this);
		}
		currentParent = lastParent;
	}

}
