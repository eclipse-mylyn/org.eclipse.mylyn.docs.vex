package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ParentTest {

	private TestParent parent;

	@Before
	public void setUp() throws Exception {
		parent = new TestParent();

		final GapContent content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(1);
		parent.setContent(content, 0, 1);
	}

	@Test
	public void addChild() throws Exception {
		assertFalse(parent.hasChildren());
		assertEquals(0, parent.getChildCount());

		final TestChild child = new TestChild();
		parent.addChild(child);
		assertTrue(parent.hasChildren());
		assertEquals(1, parent.getChildCount());
		assertSame(child, parent.getChildNode(0));
		assertSame(child, parent.getChildNodes().get(0));
	}

	@Test
	public void insertChild() throws Exception {
		assertFalse(parent.hasChildren());
		assertEquals(0, parent.getChildCount());

		parent.addChild(new TestChild());
		parent.addChild(new TestChild());
		assertEquals(2, parent.getChildCount());

		final TestChild child = new TestChild();
		parent.insertChild(1, child);
		assertEquals(3, parent.getChildCount());
		assertSame(child, parent.getChildNode(1));
		assertSame(child, parent.getChildNodes().get(1));
	}

	@Test
	public void removeChild() throws Exception {
		final TestChild child = new TestChild();
		parent.addChild(new TestChild());
		parent.addChild(child);
		parent.addChild(new TestChild());
		assertTrue(parent.hasChildren());
		assertEquals(3, parent.getChildCount());

		parent.removeChild(child);
		assertTrue(parent.hasChildren());
		assertEquals(2, parent.getChildCount());
		assertFalse(parent.getChildNodes().contains(child));
	}

	@Test
	public void shouldSetParentOnAddedChild() throws Exception {
		final TestChild child = new TestChild();
		assertNull(child.getParent());

		parent.addChild(child);
		assertSame(parent, child.getParent());
	}

	@Test
	public void shouldSetParentOnInsertedChild() throws Exception {
		parent.addChild(new TestChild());
		parent.addChild(new TestChild());

		final TestChild child = new TestChild();
		assertNull(child.getParent());

		parent.insertChild(1, child);
		assertSame(parent, child.getParent());
	}

	@Test
	public void shouldResetParentOnRemovedChild() throws Exception {
		final TestChild child = new TestChild();
		parent.addChild(child);
		assertSame(parent, child.getParent());

		parent.removeChild(child);
		assertNull(child.getParent());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldReturnUnmodifiableChildNodesList() throws Exception {
		parent.addChild(new TestChild());
		parent.addChild(new TestChild());
		parent.addChild(new TestChild());
		final List<Node> childNodes = parent.getChildNodes();
		childNodes.add(new TestChild());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldReturnUnmodifiableChildNodesIterator() throws Exception {
		parent.addChild(new TestChild());
		parent.addChild(new TestChild());
		parent.addChild(new TestChild());
		final Iterator<Node> iterator = parent.getChildIterator();
		iterator.next();
		iterator.remove();
	}

	private static class TestParent extends Parent {
		@Override
		public String getBaseURI() {
			return null;
		}
	}

	private static class TestChild extends Node {
		@Override
		public void setParent(final Parent parent) {
			super.setParent(parent);
			if (parent != null) {
				setContent(parent.getContent(), 0, 0);
			}
		}

		@Override
		public String getBaseURI() {
			return null;
		}

	}
}
