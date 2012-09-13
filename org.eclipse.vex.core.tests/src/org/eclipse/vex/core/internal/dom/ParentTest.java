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

	private GapContent content;

	@Before
	public void setUp() throws Exception {
		parent = new TestParent();

		content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		parent.associate(content, 0, 1);
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
		addTestChild();
		addTestChild();
		addTestChild();
		final List<Node> childNodes = parent.getChildNodes();
		childNodes.add(new TestChild());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldReturnUnmodifiableChildNodesIterator() throws Exception {
		addTestChild();
		addTestChild();
		addTestChild();
		final Iterator<Node> iterator = parent.getChildIterator();
		iterator.next();
		iterator.remove();
	}

	@Test
	public void shouldProvideTextNodesWithChildNodes() throws Exception {
		// <p>Hello <c></c>World</p>
		final TestChild child = addTestChild();
		content.insertText(child.getStartOffset(), "Hello ");
		content.insertText(parent.getEndOffset(), "World");

		assertEquals(3, parent.getChildNodes().size());
		assertTrue(parent.getChildNodes().get(0) instanceof Text);
		assertSame(child, parent.getChildNodes().get(1));
		assertTrue(parent.getChildNodes().get(2) instanceof Text);
	}

	@Test
	public void shouldNotProvideEmptyTextNodesWithChildNodes() throws Exception {
		final TestChild child = addTestChild();

		assertEquals(0, parent.getStartOffset());
		assertEquals(3, parent.getEndOffset());
		assertEquals(1, child.getStartOffset());
		assertEquals(2, child.getEndOffset());

		assertEquals(1, parent.getChildNodes().size());
		assertSame(child, parent.getChildNodes().get(0));
	}

	@Test
	public void shouldProvideChildNodesInAGivenRange() throws Exception {
		addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();
		addTestChild();

		final List<Node> childNodes = parent.getChildNodes(child2.getStartOffset(), child3.getEndOffset());
		assertEquals(2, childNodes.size());
		assertSame(child2, childNodes.get(0));
		assertSame(child3, childNodes.get(1));
	}

	@Test
	public void shouldCutTextOnEdges() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();

		content.insertText(child1.getStartOffset(), "Hello");
		content.insertText(child2.getStartOffset(), "World!");

		final List<Node> childNodes = parent.getChildNodes(child1.getStartOffset() - 2, child1.getEndOffset() + 2);
		assertEquals(3, childNodes.size());
		assertTrue(childNodes.get(0) instanceof Text);
		assertSame(child1, childNodes.get(1));
		assertTrue(childNodes.get(2) instanceof Text);
		assertEquals("lo", childNodes.get(0).getText());
		assertEquals("", childNodes.get(1).getText());
		assertEquals("Wo", childNodes.get(2).getText());
	}

	private TestChild addTestChild() {
		final int offset = parent.getEndOffset();
		content.insertElementMarker(offset);
		content.insertElementMarker(offset);
		final TestChild result = new TestChild();
		parent.addChild(result);
		result.associate(content, offset, offset + 1);
		return result;
	}

	private static class TestParent extends Parent {
		@Override
		public String getBaseURI() {
			return null;
		}

		@Override
		public void accept(final INodeVisitor visitor) {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestChild extends Node {
		@Override
		public String getBaseURI() {
			return null;
		}

		@Override
		public void accept(final INodeVisitor visitor) {
			throw new UnsupportedOperationException();
		}
	}
}
