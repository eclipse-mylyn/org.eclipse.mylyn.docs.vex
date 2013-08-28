/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - added processing instructions
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.List;

import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;

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
	private final IContent content;
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
	public DeepCopyVisitor(final List<Node> rootNodes, final IContent content, final int delta) {
		this.rootNodes = rootNodes;
		this.content = content;
		this.delta = delta;
	}

	public void visit(final IDocument document) {
		throw new UnsupportedOperationException("Document cannot be deep copied");
	}

	public void visit(final IDocumentFragment fragment) {
		copyChildren(fragment, null);
	}

	public void visit(final IElement element) {
		final Element copy = (Element) copy(element);
		addToParent(copy);
		associate(element, copy);

		copyChildren(element, copy);
	}

	public void visit(final IText text) {
		// ignore Text nodes because they are created dynamically in Element.getChildNodes()
	}

	public void visit(final IComment comment) {
		final Comment copy = (Comment) copy(comment);
		addToParent(copy);
		associate(comment, copy);
	}

	public void visit(final IProcessingInstruction pi) {
		final ProcessingInstruction copy = (ProcessingInstruction) copy(pi);
		addToParent(copy);
		associate(pi, copy);
	}

	@SuppressWarnings("unchecked")
	private <T extends INode> T copy(final T node) {
		return (T) node.accept(copyVisitor);
	}

	private void addToParent(final Node node) {
		if (currentParent == null) {
			rootNodes.add(node);
		} else {
			currentParent.addChild(node);
		}
	}

	private void associate(final INode source, final Node copy) {
		if (source.isAssociated()) {
			final ContentRange range = source.getRange();
			copy.associate(content, range.moveBy(delta));
		}
	}

	private void copyChildren(final IParent source, final Parent copy) {
		final Parent lastParent = currentParent;
		for (final INode child : source.children()) {
			currentParent = copy;
			child.accept(this);
		}
		currentParent = lastParent;
	}

}
