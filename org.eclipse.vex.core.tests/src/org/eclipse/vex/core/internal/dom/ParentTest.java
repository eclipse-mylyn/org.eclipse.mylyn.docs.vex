package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
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
		parent.associate(content, new Range(0, 1));
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

		final List<Node> childNodes = parent.getChildNodes(new Range(child2.getStartOffset(), child3.getEndOffset()));
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

		final List<Node> childNodes = parent.getChildNodes(child1.getRange().moveBounds(-2, 2));
		assertEquals(3, childNodes.size());
		assertTrue(childNodes.get(0) instanceof Text);
		assertSame(child1, childNodes.get(1));
		assertTrue(childNodes.get(2) instanceof Text);
		assertEquals("lo", childNodes.get(0).getText());
		assertEquals("", childNodes.get(1).getText());
		assertEquals("Wo", childNodes.get(2).getText());
	}

	@Test
	public void shouldSetParentOnTextNodes() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		assertSame(parent, parent.getChildNodes().get(0).getParent());
	}

	@Test
	public void shouldProvideNoChildNodesIfEmpty() throws Exception {
		assertTrue(parent.getChildNodes().isEmpty());
	}

	@Test
	public void shouldProvideAddedChildren() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			children.add(addTestChild());
		}

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(4, childNodes.size());
		for (int i = 0; i < 4; i++) {
			assertSame(children.get(i), childNodes.get(i));
		}
	}

	@Test
	public void shouldProvideAddedChildrenInRange1To3() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			children.add(addTestChild());
		}

		final List<Node> childNodes = parent.getChildNodes(new Range(children.get(1).getStartOffset(), children.get(3).getEndOffset()));
		assertEquals(3, childNodes.size());
		assertSame(children.get(1), childNodes.get(0));
		assertSame(children.get(2), childNodes.get(1));
		assertSame(children.get(3), childNodes.get(2));
	}

	@Test
	public void shouldProvideAddedChildrenInRange1To2() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			children.add(addTestChild());
		}

		final List<Node> childNodes = parent.getChildNodes(new Range(children.get(1).getStartOffset(), children.get(3).getStartOffset()));
		assertEquals(2, childNodes.size());
		assertSame(children.get(1), childNodes.get(0));
		assertSame(children.get(2), childNodes.get(1));
	}

	@Test
	public void shouldProvideAddedChildrenInRange2() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			children.add(addTestChild());
		}

		final List<Node> childNodes = parent.getChildNodes(new Range(children.get(1).getEndOffset(), children.get(3).getStartOffset()));
		assertEquals(1, childNodes.size());
		assertSame(children.get(2), childNodes.get(0));
	}

	@Test
	public void shouldProvideAddedChildrenInRange2To3() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			children.add(addTestChild());
		}

		final List<Node> childNodes = parent.getChildNodes(new Range(children.get(1).getEndOffset(), children.get(3).getEndOffset()));
		assertEquals(2, childNodes.size());
		assertSame(children.get(2), childNodes.get(0));
		assertSame(children.get(3), childNodes.get(1));
	}

	@Test
	public void shouldProvideAllDissociatedChildren() throws Exception {
		final ArrayList<TestChild> children = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			final TestChild child = new TestChild();
			children.add(child);
			parent.addChild(child);
		}

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(4, childNodes.size());
		for (int i = 0; i < 4; i++) {
			assertSame(children.get(i), childNodes.get(i));
		}
	}

	@Test
	public void shouldProvideSingleText() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(1, childNodes.size());
		assertTextNodeEquals("Hello World", 1, 11, childNodes.get(0));
	}

	@Test
	public void shouldProvideSingleCharacterText() throws Exception {
		content.insertText(parent.getEndOffset(), "x");

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(1, childNodes.size());
		assertTextNodeEquals("x", 1, 1, childNodes.get(0));
	}

	@Test
	public void shouldProvideTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		addTestChild();

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(2, childNodes.size());
		assertTextNodeEquals("Hello World", 1, 11, childNodes.get(0));
		assertChildNodeEquals("", 12, 13, childNodes.get(1));
	}

	@Test
	public void shouldProvideSingleCharacterTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "x");
		addTestChild();

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(2, childNodes.size());
		assertTextNodeEquals("x", 1, 1, childNodes.get(0));
		assertChildNodeEquals("", 2, 3, childNodes.get(1));
	}

	@Test
	public void shouldProvideTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "Hello World");

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(2, childNodes.size());
		assertChildNodeEquals("", 1, 2, childNodes.get(0));
		assertTextNodeEquals("Hello World", 3, 13, childNodes.get(1));
	}

	@Test
	public void shouldProvideSingleCharacterTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "x");

		final List<Node> childNodes = parent.getChildNodes();

		assertEquals(2, childNodes.size());
		assertChildNodeEquals("", 1, 2, childNodes.get(0));
		assertTextNodeEquals("x", 3, 3, childNodes.get(1));
	}

	@Test
	public void shouldProvideAllChildNodesIncludingText() throws Exception {
		setUpChildNodes();
		final List<Node> childNodes = parent.getChildNodes();
		assertTextNodeEquals("Hello ", 1, 6, childNodes.get(0));
		assertChildNodeEquals("Child1", 7, 14, childNodes.get(1));
		assertChildNodeEquals("Child2", 15, 22, childNodes.get(2));
		assertTextNodeEquals(" World", 23, 28, childNodes.get(3));
	}

	@Test
	public void shouldHandleSmallerStartOffset() throws Exception {
		setUpChildNodes();
		content.insertText(parent.getStartOffset(), "prefix");
		final List<Node> childNodes = parent.getChildNodes(parent.getRange().moveBounds(-2, 0));
		assertTextNodeEquals("Hello ", 7, 12, childNodes.get(0));
		assertChildNodeEquals("Child1", 13, 20, childNodes.get(1));
		assertChildNodeEquals("Child2", 21, 28, childNodes.get(2));
		assertTextNodeEquals(" World", 29, 34, childNodes.get(3));
	}

	@Test
	public void shouldHandleBiggerEndOffset() throws Exception {
		setUpChildNodes();
		content.insertText(parent.getEndOffset() + 1, "suffix");
		final List<Node> childNodes = parent.getChildNodes();
		assertTextNodeEquals("Hello ", 1, 6, childNodes.get(0));
		assertChildNodeEquals("Child1", 7, 14, childNodes.get(1));
		assertChildNodeEquals("Child2", 15, 22, childNodes.get(2));
		assertTextNodeEquals(" World", 23, 28, childNodes.get(3));
	}

	@Test
	public void shouldProvideSelfOnOwnBoundaries() throws Exception {
		assertSame(parent, parent.getChildNodeAt(parent.getStartOffset()));
		assertSame(parent, parent.getChildNodeAt(parent.getEndOffset()));
	}

	@Test
	public void shouldReturnTextWithinBoundaries() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		final Node text = parent.getChildNodes().get(0);
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset()));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset() + 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset() - 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset()));
	}

	@Test
	public void shouldReturnTextWithinChildBoundaries() throws Exception {
		final int offset = parent.getEndOffset();
		content.insertElementMarker(offset);
		content.insertElementMarker(offset);
		final Element child = new Element("child");
		parent.addChild(child);
		child.associate(content, new Range(offset, offset + 1));
		content.insertText(child.getEndOffset(), "Hello World");
		final Node text = child.getChildNodes().get(0);
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset()));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset() + 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset() - 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset()));
	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotProvideChildNodeBeforeStartOffset() throws Exception {
		content.insertText(parent.getStartOffset(), "prefix");
		parent.getChildNodeAt(parent.getStartOffset() - 1);

	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotProvideChildNodeAfterEndOffset() throws Exception {
		content.insertText(parent.getEndOffset() + 1, "suffix");
		parent.getChildNodeAt(parent.getEndOffset() + 1);
	}

	@Test
	public void shouldProvideChildNodesBeforeOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final List<Node> childNodes12 = parent.getChildNodesBefore(child3.getStartOffset());
		assertEquals(2, childNodes12.size());
		assertSame(child1, childNodes12.get(0));
		assertSame(child2, childNodes12.get(1));

		final List<Node> childNodes123 = parent.getChildNodesBefore(parent.getEndOffset());
		assertEquals(3, childNodes123.size());
		assertSame(child1, childNodes123.get(0));
		assertSame(child2, childNodes123.get(1));
		assertSame(child3, childNodes123.get(2));

		assertTrue(parent.getChildNodesBefore(parent.getStartOffset()).isEmpty());
	}

	@Test
	public void shouldProvideChildNodesAfterOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final List<Node> childNodes23 = parent.getChildNodesAfter(child1.getEndOffset());
		assertEquals(2, childNodes23.size());
		assertSame(child2, childNodes23.get(0));
		assertSame(child3, childNodes23.get(1));

		final List<Node> childNodes123 = parent.getChildNodesAfter(parent.getStartOffset());
		assertEquals(3, childNodes123.size());
		assertSame(child1, childNodes123.get(0));
		assertSame(child2, childNodes123.get(1));
		assertSame(child3, childNodes123.get(2));

		assertTrue(parent.getChildNodesAfter(parent.getEndOffset()).isEmpty());
	}

	@Test
	public void shouldProvideInsertionIndexForOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		assertEquals(0, parent.getInsertionIndex(child1.getStartOffset()));
		assertEquals(1, parent.getInsertionIndex(child2.getStartOffset()));
		assertEquals(2, parent.getInsertionIndex(child3.getStartOffset()));
		assertEquals(3, parent.getInsertionIndex(parent.getEndOffset()));
	}

	private static void assertTextNodeEquals(final String text, final int startOffset, final int endOffset, final Node actualNode) {
		assertTrue(actualNode instanceof Text);
		assertEquals(text, actualNode.getText());
		assertEquals(startOffset, actualNode.getStartOffset());
		assertEquals(endOffset, actualNode.getEndOffset());
	}

	private static void assertChildNodeEquals(final String text, final int startOffset, final int endOffset, final Node actualNode) {
		assertTrue(actualNode instanceof TestChild);
		assertEquals(text, actualNode.getText());
		assertEquals(startOffset, actualNode.getStartOffset());
		assertEquals(endOffset, actualNode.getEndOffset());
	}

	private void setUpChildNodes() {
		content.insertText(parent.getEndOffset(), "Hello ");
		final TestChild child1 = addTestChild();
		content.insertText(child1.getEndOffset(), "Child1");
		final TestChild child2 = addTestChild();
		content.insertText(child2.getEndOffset(), "Child2");
		content.insertText(parent.getEndOffset(), " World");
	}

	private TestChild addTestChild() {
		final int offset = parent.getEndOffset();
		content.insertElementMarker(offset);
		content.insertElementMarker(offset);
		final TestChild result = new TestChild();
		parent.addChild(result);
		result.associate(content, new Range(offset, offset + 1));
		return result;
	}

	private static class TestParent extends Parent {
		@Override
		public void accept(final INodeVisitor visitor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T accept(final INodeVisitorWithResult<T> visitor) {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestChild extends Node {
		@Override
		public void accept(final INodeVisitor visitor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T accept(final INodeVisitorWithResult<T> visitor) {
			throw new UnsupportedOperationException();
		}
	}
}
