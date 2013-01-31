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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.AssertionFailedException;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DocumentFragmentTest {

	@Test(expected = AssertionFailedException.class)
	public void contentMustNotBeEmpty() throws Exception {
		final GapContent emptyContent = new GapContent(0);
		new DocumentFragment(emptyContent, Collections.<Node> emptyList());
	}

	@Test
	public void shouldAssociateOnCreation() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertText(0, "abc");
		final DocumentFragment fragment = new DocumentFragment(content, Collections.<Node> emptyList());
		assertTrue(fragment.isAssociated());
		assertSame(content, fragment.getContent());
	}

	@Test
	public void shoudlContainGivenText() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertText(0, "abc");
		final DocumentFragment fragment = new DocumentFragment(content, Collections.<Node> emptyList());
		final Iterator<Node> actualChildren = fragment.children().iterator();
		assertTrue(actualChildren.next() instanceof Text);
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldContainGivenChildren() throws Exception {
		final GapContent content = new GapContent(4);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element child1 = new Element("child");
		child1.associate(content, new ContentRange(0, 1));
		final Element child2 = new Element("child");
		child2.associate(content, new ContentRange(2, 3));

		final DocumentFragment fragment = new DocumentFragment(content, Arrays.<Node> asList(child1, child2));
		final Iterator<Node> actualChildren = fragment.children().iterator();
		assertSame(child1, actualChildren.next());
		assertSame(child2, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void hasNoOwnElementMarkers() throws Exception {
		final GapContent content = new GapContent(4);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Element child1 = new Element("child");
		child1.associate(content, new ContentRange(0, 1));
		final Element child2 = new Element("child");
		child2.associate(content, new ContentRange(2, 3));

		final DocumentFragment fragment = new DocumentFragment(content, Arrays.<Node> asList(child1, child2));
		assertEquals(fragment.getStartOffset(), child1.getStartOffset());
		assertEquals(fragment.getEndOffset(), child2.getEndOffset());
	}

	@Test
	public void shouldNotHaveBaseUri() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertText(0, "abc");
		final DocumentFragment fragment = new DocumentFragment(content, Collections.<Node> emptyList());
		assertNull(fragment.getBaseURI());
	}
}
