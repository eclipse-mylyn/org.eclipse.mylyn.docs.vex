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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.AssertionFailedException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public abstract class NodeTest {

	private Node node;

	protected abstract Node createNode();

	@Before
	public void setup() throws Exception {
		node = createNode();
	}

	@Test
	public void shouldProvideNodeThroughSetup() throws Exception {
		// just to be shure
		assertNotNull("A subclass of NodeTest must provide a Node instance through createNode().", node);
	}

	@Test
	public void canBeAssociatedToContentRegion() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);

		node.associate(content, 0, 1);
		assertEquals(0, node.getStartOffset());
		assertEquals(1, node.getEndOffset());

		content.insertText(1, "Hello");
		assertEquals(0, node.getStartOffset());
		assertEquals(6, node.getEndOffset());
		assertSame(content, node.getContent());
	}

	@Test
	public void canBeDissociatedFromContent() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);

		node.associate(content, 0, 1);
		node.dissociate();

		content.insertText(1, "Hello");
		assertNull(node.getContent());
	}

	@Test
	public void hasTextualContent() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);

		node.associate(content, 0, 1);
		assertEquals("", node.getText());

		content.insertText(1, "Hello");
		assertEquals("Hello", node.getText());
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotHaveTextualContentIfNotAssociatedToContent() throws Exception {
		node.getText();
	}

	@Test
	public void shouldContainStartOffset() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertText(1, "Hello World");
		node.associate(content, 0, content.length() - 1);
		content.insertText(0, "prefix");

		assertFalse(node.containsOffset(node.getStartOffset() - 1));
		assertTrue(node.containsOffset(node.getStartOffset()));
		assertTrue(node.containsOffset(node.getStartOffset() + 1));
	}

	@Test
	public void shouldContainEndOffset() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertText(1, "Hello World");
		node.associate(content, 0, content.length() - 1);
		content.insertText(content.length(), "suffix");

		assertTrue(node.containsOffset(node.getEndOffset() - 1));
		assertTrue(node.containsOffset(node.getEndOffset()));
		assertFalse(node.containsOffset(node.getEndOffset() + 1));
	}

	@Test
	public void shouldContainNoOffsetIfNotAssociated() throws Exception {
		assertFalse(node.containsOffset(0));
	}

	@Test
	public void shouldIndicateIfWithinRange() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertText(1, "Hello World");
		node.associate(content, 0, content.length() - 1);
		content.insertText(0, "prefix");
		content.insertText(content.length(), "suffix");

		assertTrue(node.isInRange(node.getStartOffset() - 1, node.getEndOffset()));
		assertTrue(node.isInRange(node.getStartOffset(), node.getEndOffset()));
		assertFalse(node.isInRange(node.getStartOffset() + 1, node.getEndOffset()));
		assertTrue(node.isInRange(node.getStartOffset(), node.getEndOffset() + 1));
		assertFalse(node.isInRange(node.getStartOffset(), node.getEndOffset() - 1));
		assertTrue(node.isInRange(node.getStartOffset() - 1, node.getEndOffset() + 1));
		assertFalse(node.isInRange(node.getStartOffset() + 1, node.getEndOffset() - 1));
	}
}
