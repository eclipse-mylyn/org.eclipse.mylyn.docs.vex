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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

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

}
