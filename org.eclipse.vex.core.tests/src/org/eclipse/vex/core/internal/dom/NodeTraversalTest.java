/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class NodeTraversalTest {

	@Test
	public void givenOneNode_shouldVisitNode() throws Exception {
		final Text node = new Text(null, new GapContent(1), new Range(0, 0));
		final boolean[] nodeWasVisited = new boolean[1];
		final INodeVisitor nodeVisitor = new BaseNodeVisitor() {
			@Override
			public void visit(final Text text) {
				nodeWasVisited[0] = node == text;
			}
		};
		new NodeTraversal(nodeVisitor).traverse(node);
		assertTrue(nodeWasVisited[0]);
	}

	@Test
	public void givenParentWithChildren_shouldVisitParentAndAllChildren() throws Exception {
		final Content content = new GapContent(10);
		final Element parent = createElement(content, 0, "parent");
		final Element child1 = createElement(content, parent.getEndOffset(), "child");
		parent.addChild(child1);
		final Element child2 = createElement(content, parent.getEndOffset(), "child");
		parent.addChild(child2);
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getEndOffset(), "World");

		final Set<Node> visitedNodes = new HashSet<Node>();
		final INodeVisitor nodeVisitor = new BaseNodeVisitor() {
			@Override
			public void visit(final Element element) {
				visitedNodes.add(element);
			}

			@Override
			public void visit(final Text text) {
				visitedNodes.add(text);
			}
		};
		new NodeTraversal(nodeVisitor).traverse(parent);

		assertTrue(visitedNodes.contains(parent));
		assertTrue(visitedNodes.contains(child1));
		assertTrue(visitedNodes.contains(child2));
		assertEquals(5, visitedNodes.size());
	}

	private static Element createElement(final Content content, final int insertionOffset, final String localName) {
		final Element element = new Element(new QualifiedName(null, localName));
		content.insertElementMarker(insertionOffset);
		content.insertElementMarker(insertionOffset);
		element.associate(content, new Range(insertionOffset, insertionOffset + 1));
		return element;
	}
}
