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
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		parent.associate(content, new ContentRange(0, 1));
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
		assertSame(child, parent.children().iterator().next());
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
		final Iterator<Node> actualChildren = parent.children().iterator();
		actualChildren.next(); // TODO implement Axis.get(int);
		assertSame(child, actualChildren.next());
	}

	@Test
	public void removeChild() throws Exception {
		final TestChild secondChild = new TestChild();
		parent.addChild(new TestChild());
		parent.addChild(secondChild);
		parent.addChild(new TestChild());
		assertTrue(parent.hasChildren());
		assertEquals(3, parent.getChildCount());

		parent.removeChild(secondChild);
		assertTrue(parent.hasChildren());
		assertEquals(2, parent.getChildCount());
		for (final Node child : parent.children()) {
			assertTrue(child != secondChild);
		}
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
	public void shouldReturnUnmodifiableChildNodesIterator() throws Exception {
		addTestChild();
		addTestChild();
		addTestChild();
		final Iterator<Node> iterator = parent.children().iterator();
		iterator.next();
		iterator.remove();
	}

	@Test
	public void shouldProvideTextNodesWithChildNodes() throws Exception {
		// <p>Hello <c></c>World</p>
		final TestChild child = addTestChild();
		content.insertText(child.getStartOffset(), "Hello ");
		content.insertText(parent.getEndOffset(), "World");

		final Iterator<Node> children = parent.children().iterator();
		assertTrue(children.next() instanceof Text);
		assertSame(child, children.next());
		assertTrue(children.next() instanceof Text);
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldNotProvideEmptyTextNodesWithChildNodes() throws Exception {
		final TestChild child = addTestChild();

		assertEquals(0, parent.getStartOffset());
		assertEquals(3, parent.getEndOffset());
		assertEquals(1, child.getStartOffset());
		assertEquals(2, child.getEndOffset());

		final Iterator<Node> children = parent.children().iterator();
		assertSame(child, children.next());
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldProvideChildNodesInAGivenRange() throws Exception {
		addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();
		addTestChild();

		final Iterator<Node> children = parent.children().in(new ContentRange(child2.getStartOffset(), child3.getEndOffset())).iterator();
		assertSame(child2, children.next());
		assertSame(child3, children.next());
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldCutTextOnEdges() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();

		content.insertText(child1.getStartOffset(), "Hello");
		content.insertText(child2.getStartOffset(), "World!");

		final Iterator<Node> children = parent.children().in(child1.getRange().resizeBy(-2, 2)).iterator();
		assertTextNodeEquals("lo", 4, 5, children.next());
		assertChildNodeEquals("", 6, 7, children.next());
		assertTextNodeEquals("Wo", 8, 9, children.next());
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldSetParentOnTextNodes() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		assertSame(parent, parent.children().iterator().next().getParent());
	}

	@Test
	public void shouldProvideNoChildNodesIfEmpty() throws Exception {
		assertFalse(parent.children().iterator().hasNext());
	}

	@Test
	public void shouldProvideAddedChildren() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		int i = 0;
		for (final Node actualChild : parent.children()) {
			assertTrue(i < expectedChildren.size());
			assertSame(expectedChildren.get(i++), actualChild);
		}
	}

	@Test
	public void shouldProvideAddedChildrenInRange1To3() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		final Iterator<Node> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getStartOffset(), expectedChildren.get(3).getEndOffset())).iterator();
		assertSame(expectedChildren.get(1), actualChildren.next());
		assertSame(expectedChildren.get(2), actualChildren.next());
		assertSame(expectedChildren.get(3), actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAddedChildrenInRange1To2() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		final Iterator<Node> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getStartOffset(), expectedChildren.get(3).getStartOffset())).iterator();
		assertSame(expectedChildren.get(1), actualChildren.next());
		assertSame(expectedChildren.get(2), actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAddedChildrenInRange2() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		final Iterator<Node> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getEndOffset(), expectedChildren.get(3).getStartOffset())).iterator();
		assertSame(expectedChildren.get(2), actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAddedChildrenInRange2To3() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		final Iterator<Node> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getEndOffset(), expectedChildren.get(3).getEndOffset())).iterator();
		assertSame(expectedChildren.get(2), actualChildren.next());
		assertSame(expectedChildren.get(3), actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAllDissociatedChildren() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			final TestChild child = new TestChild();
			expectedChildren.add(child);
			parent.addChild(child);
		}

		int i = 0;
		for (final Node actualChild : parent.children()) {
			assertSame(expectedChildren.get(i++), actualChild);
		}
	}

	@Test
	public void shouldProvideSingleText() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterText() throws Exception {
		content.insertText(parent.getEndOffset(), "x");

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("x", 1, 1, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		addTestChild();

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
		assertChildNodeEquals("", 12, 13, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "x");
		addTestChild();

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("x", 1, 1, actualChildren.next());
		assertChildNodeEquals("", 2, 3, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertChildNodeEquals("", 1, 2, actualChildren.next());
		assertTextNodeEquals("Hello World", 3, 13, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "x");

		final Iterator<Node> actualChildren = parent.children().iterator();
		assertChildNodeEquals("", 1, 2, actualChildren.next());
		assertTextNodeEquals("x", 3, 3, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAllChildNodesIncludingText() throws Exception {
		setUpChildNodes();
		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello ", 1, 6, actualChildren.next());
		assertChildNodeEquals("Child1", 7, 14, actualChildren.next());
		assertChildNodeEquals("Child2", 15, 22, actualChildren.next());
		assertTextNodeEquals(" World", 23, 28, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldHandleSmallerStartOffset() throws Exception {
		setUpChildNodes();
		content.insertText(parent.getStartOffset(), "prefix");
		final Iterator<Node> actualChildren = parent.children().in(parent.getRange().resizeBy(-2, 0)).iterator();
		assertTextNodeEquals("Hello ", 7, 12, actualChildren.next());
		assertChildNodeEquals("Child1", 13, 20, actualChildren.next());
		assertChildNodeEquals("Child2", 21, 28, actualChildren.next());
		assertTextNodeEquals(" World", 29, 34, actualChildren.next());
	}

	@Test
	public void shouldHandleBiggerEndOffset() throws Exception {
		setUpChildNodes();
		content.insertText(parent.getEndOffset() + 1, "suffix");
		final Iterator<Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello ", 1, 6, actualChildren.next());
		assertChildNodeEquals("Child1", 7, 14, actualChildren.next());
		assertChildNodeEquals("Child2", 15, 22, actualChildren.next());
		assertTextNodeEquals(" World", 23, 28, actualChildren.next());
	}

	@Test
	public void shouldProvideSelfOnOwnBoundaries() throws Exception {
		assertSame(parent, parent.getChildNodeAt(parent.getStartOffset()));
		assertSame(parent, parent.getChildNodeAt(parent.getEndOffset()));
	}

	@Test
	public void shouldReturnTextWithinBoundaries() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		final Node text = parent.children().iterator().next();
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset()));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getStartOffset() + 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset() - 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildNodeAt(text.getEndOffset()));
	}

	@Test
	public void shouldReturnTextWithinChildBoundaries() throws Exception {
		final int offset = parent.getEndOffset();
		content.insertTagMarker(offset);
		content.insertTagMarker(offset);
		final Element child = new Element("child");
		parent.addChild(child);
		child.associate(content, new ContentRange(offset, offset + 1));
		content.insertText(child.getEndOffset(), "Hello World");
		final Node text = child.children().iterator().next();
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

		final Iterator<Node> childNodes12 = parent.children().before(child3.getStartOffset()).iterator();
		assertSame(child1, childNodes12.next());
		assertSame(child2, childNodes12.next());
		assertFalse(childNodes12.hasNext());

		final Iterator<Node> childNodes123 = parent.children().before(parent.getEndOffset()).iterator();
		assertSame(child1, childNodes123.next());
		assertSame(child2, childNodes123.next());
		assertSame(child3, childNodes123.next());
		assertFalse(childNodes123.hasNext());

		assertTrue(parent.children().before(parent.getStartOffset()).isEmpty());
	}

	@Test
	public void shouldProvideChildNodesBeforeOffsetWithoutTextfragmentOfIntersectingChild() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();
		content.insertText(child3.getEndOffset(), "Hello World");

		final Iterator<Node> childNodes12 = parent.children().before(child3.getStartOffset() + 5).iterator();
		assertSame(child1, childNodes12.next());
		assertSame(child2, childNodes12.next());
		assertFalse(childNodes12.hasNext());
	}

	@Test
	public void shouldProvideChildNodesAfterOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final Iterator<Node> childNodes23 = parent.children().after(child1.getEndOffset()).iterator();
		assertSame(child2, childNodes23.next());
		assertSame(child3, childNodes23.next());
		assertFalse(childNodes23.hasNext());

		final Iterator<Node> childNodes123 = parent.children().after(parent.getStartOffset()).iterator();
		assertSame(child1, childNodes123.next());
		assertSame(child2, childNodes123.next());
		assertSame(child3, childNodes123.next());
		assertFalse(childNodes123.hasNext());

		assertTrue(parent.children().after(parent.getEndOffset()).isEmpty());
	}

	@Test
	public void shouldProvideChildNodesAfterOffsetWithoutTextfragmentOfIntersectingChild() throws Exception {
		final TestChild child1 = addTestChild();
		content.insertText(child1.getEndOffset(), "Hello World");
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final Iterator<Node> childNodes23 = parent.children().after(child1.getEndOffset() - 5).iterator();
		assertSame(child2, childNodes23.next());
		assertSame(child3, childNodes23.next());
		assertFalse(childNodes23.hasNext());
	}

	@Test
	public void shouldProvideInsertionIndexForOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		assertEquals(0, parent.getIndexOfChildNextTo(child1.getStartOffset()));
		assertEquals(1, parent.getIndexOfChildNextTo(child2.getStartOffset()));
		assertEquals(2, parent.getIndexOfChildNextTo(child3.getStartOffset()));
		assertEquals(3, parent.getIndexOfChildNextTo(parent.getEndOffset()));
	}

	@Test
	public void givenChildNodesAndText_shouldProvideChildrenAxisIncludingText() throws Exception {
		setUpChildNodes();

		final Iterator<? extends Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello ", 1, 6, actualChildren.next());
		assertChildNodeEquals("Child1", 7, 14, actualChildren.next());
		assertChildNodeEquals("Child2", 15, 22, actualChildren.next());
		assertTextNodeEquals(" World", 23, 28, actualChildren.next());
	}

	@Test
	public void givenOnlyChildNodesAndNoText_shouldProvideChildrenAxisWithOnlyChildNodes() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final Iterator<? extends Node> actualChildren = parent.children().iterator();
		assertSame(child1, actualChildren.next());
		assertSame(child2, actualChildren.next());
		assertSame(child3, actualChildren.next());
	}

	@Test
	public void givenOnlyText_shouldProvideTextOnChildrenAxis() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<? extends Node> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
	}

	@Test
	public void givenEmptyParent_shouldIndicateEmptyChildrenAxis() throws Exception {
		assertFalse(parent.children().iterator().hasNext());
	}

	@Test
	public void whenTextExcludedOnAxis_shouldNotProvideTextOnChildrenAxis() throws Exception {
		setUpChildNodes();
		final Iterator<Node> actualChildren = parent.children().withoutText().iterator();
		assertEquals("Child1", actualChildren.next().getText());
		assertEquals("Child2", actualChildren.next().getText());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideChildrenAxisAsList() throws Exception {
		setUpChildNodes();
		final List<Node> actualList = parent.children().asList();
		assertEquals(4, actualList.size());
		assertTextNodeEquals("Hello ", 1, 6, actualList.get(0));
		assertChildNodeEquals("Child1", 7, 14, actualList.get(1));
		assertChildNodeEquals("Child2", 15, 22, actualList.get(2));
		assertTextNodeEquals(" World", 23, 28, actualList.get(3));
	}

	@Test
	public void shouldProvideNodeOnChildrenAxisByIndex() throws Exception {
		setUpChildNodes();
		assertEquals("Child1", parent.children().at(1).getText());
	}

	@Test
	public void shouldProvideFirstNodeOnChildrenAxis() throws Exception {
		setUpChildNodes();
		assertEquals("Hello ", parent.children().first().getText());
	}

	@Test
	public void shouldProvideLastNodeOnChildrenAxis() throws Exception {
		setUpChildNodes();
		assertEquals(" World", parent.children().last().getText());
	}

	@Test
	public void shouldAcceptVisitorOnAllNodesOnChildrenAxis() throws Exception {
		final boolean[] childVisited = new boolean[1];
		parent.addChild(new TestChild() {
			@Override
			public void accept(final INodeVisitor visitor) {
				childVisited[0] = true;
			}
		});
		parent.children().accept(new BaseNodeVisitor());
		assertTrue(childVisited[0]);
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
		content.insertTagMarker(offset);
		content.insertTagMarker(offset);
		final TestChild result = new TestChild();
		parent.addChild(result);
		result.associate(content, new ContentRange(offset, offset + 1));
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

		@Override
		public boolean isKindOf(final Node node) {
			return false;
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

		@Override
		public boolean isKindOf(final Node node) {
			return false;
		}
	}
}
