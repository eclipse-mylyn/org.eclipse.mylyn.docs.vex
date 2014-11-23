/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Florian Thienel - initial API and implementation
 *		Carsten Hiesserich - extended insert / remove tests for nodes with text (bug 408731)
 *******************************************************************************/
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
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IText;
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
	public void isInitiallyEmpty() throws Exception {
		assertFalse(parent.hasChildren());
	}

	@Test
	public void addChild() throws Exception {
		final TestChild child = new TestChild();
		parent.addChild(child);
		assertTrue(parent.hasChildren());
		assertSame(child, parent.children().get(0));
		assertSame(child, parent.children().first());
	}

	@Test
	public void insertChild() throws Exception {
		addTestChild();
		addTestChild();

		final TestChild child = new TestChild();
		parent.insertChildAt(parent.children().get(1).getStartOffset(), child);
		assertSame(child, parent.children().get(1));
	}

	@Test
	public void insertChildBefore() throws Exception {
		addTestChild();
		addTestChild();

		final TestChild child = new TestChild();
		parent.insertChildBefore(parent.children().get(1), child);
		assertSame(child, parent.children().get(1));
	}

	@Test
	public void removeChild() throws Exception {
		final TestChild secondChild = new TestChild();
		parent.addChild(new TestChild());
		parent.addChild(secondChild);
		parent.addChild(new TestChild());
		assertTrue(parent.hasChildren());

		parent.removeChild(secondChild);
		assertTrue(parent.hasChildren());
		for (final INode child : parent.children()) {
			assertTrue(child != secondChild);
		}
	}

	@Test
	public void insertChildBeforeFirstInline() throws Exception {
		content.insertText(parent.getEndOffset(), "1234");
		final TestChild firstInlineElement = addTestChild();
		content.insertText(parent.getEndOffset(), "5678");
		addTestChild();

		// insert element between 2 and 3
		final TestChild insertedElement = insertTestChildAt(firstInlineElement.getStartOffset() - 2);

		assertEquals(6, parent.children().count());
		assertTrue(parent.children().get(0) instanceof IText);
		assertEquals("12", parent.children().get(0).getText());
		assertSame(insertedElement, parent.children().get(1));
		assertTrue(parent.children().get(2) instanceof IText);
		assertEquals("34", parent.children().get(2).getText());
	}

	@Test
	public void insertChildAfterLastInline() throws Exception {
		content.insertText(parent.getEndOffset(), "1234");
		addTestChild();
		content.insertText(parent.getEndOffset(), "5678");
		final TestChild lastInlineElement = addTestChild();
		content.insertText(parent.getEndOffset(), "90");

		// insert element between 9 and 0
		final TestChild insertedElement = insertTestChildAt(lastInlineElement.getEndOffset() + 2);

		for (final INode child : parent.children()) {
			System.out.println(child);
		}
		assertEquals(7, parent.children().count());
		assertTrue(parent.children().get(4) instanceof IText);
		assertEquals("9", parent.children().get(4).getText());
		assertSame(insertedElement, parent.children().get(5));
		assertTrue(parent.children().get(6) instanceof IText);
		assertEquals("0", parent.children().get(6).getText());
	}

	@Test
	public void removeChildExtended() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "12");
		final TestChild secondChild = addTestChild();
		content.insertText(parent.getEndOffset(), "34");
		addTestChild();

		parent.removeChild(secondChild);

		assertTrue(parent.children().get(1) instanceof IText);
		assertEquals("1234", parent.children().get(1).getText());
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
		addTestChild();
		addTestChild();

		final TestChild child = new TestChild();
		assertNull(child.getParent());

		parent.insertChildAt(parent.children().get(1).getStartOffset(), child);
		assertSame(parent, child.getParent());
	}

	@Test
	public void shouldSetParentOnChildInsertedBeforeNode() throws Exception {
		addTestChild();
		addTestChild();

		final TestChild child = new TestChild();
		assertNull(child.getParent());

		parent.insertChildBefore(parent.children().get(1), child);
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
		final Iterator<INode> iterator = parent.children().iterator();
		iterator.next();
		iterator.remove();
	}

	@Test
	public void shouldProvideTextNodesWithChildNodes() throws Exception {
		// <p>Hello <c></c>World</p>
		final TestChild child = addTestChild();
		content.insertText(child.getStartOffset(), "Hello ");
		content.insertText(parent.getEndOffset(), "World");

		final Iterator<INode> children = parent.children().iterator();
		assertTrue(children.next() instanceof IText);
		assertSame(child, children.next());
		assertTrue(children.next() instanceof IText);
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldNotProvideEmptyTextNodesWithChildNodes() throws Exception {
		final TestChild child = addTestChild();

		assertEquals(0, parent.getStartOffset());
		assertEquals(3, parent.getEndOffset());
		assertEquals(1, child.getStartOffset());
		assertEquals(2, child.getEndOffset());

		final Iterator<INode> children = parent.children().iterator();
		assertSame(child, children.next());
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldProvideChildNodesInAGivenRange() throws Exception {
		addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();
		addTestChild();

		final Iterator<? extends INode> children = parent.children().in(new ContentRange(child2.getStartOffset(), child3.getEndOffset())).iterator();
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

		final Iterator<? extends INode> children = parent.children().in(child1.getRange().resizeBy(-2, 2)).iterator();
		assertTextNodeEquals("lo", 4, 5, children.next());
		assertChildNodeEquals("", 6, 7, children.next());
		assertTextNodeEquals("Wo", 8, 9, children.next());
		assertFalse(children.hasNext());
	}

	@Test
	public void shouldSetParentOnTextNodes() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		assertSame(parent, parent.children().first().getParent());
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
		for (final INode actualChild : parent.children()) {
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

		final Iterator<? extends INode> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getStartOffset(), expectedChildren.get(3).getEndOffset())).iterator();
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

		final Iterator<? extends INode> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getStartOffset(), expectedChildren.get(3).getStartOffset())).iterator();
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

		final Iterator<? extends INode> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getEndOffset(), expectedChildren.get(3).getStartOffset())).iterator();
		assertSame(expectedChildren.get(2), actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAddedChildrenInRange2To3() throws Exception {
		final ArrayList<TestChild> expectedChildren = new ArrayList<TestChild>();
		for (int i = 0; i < 4; i++) {
			expectedChildren.add(addTestChild());
		}

		final Iterator<? extends INode> actualChildren = parent.children().in(new ContentRange(expectedChildren.get(1).getEndOffset(), expectedChildren.get(3).getEndOffset())).iterator();
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
		for (final INode actualChild : parent.children()) {
			assertSame(expectedChildren.get(i++), actualChild);
		}
	}

	@Test
	public void shouldProvideSingleText() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterText() throws Exception {
		content.insertText(parent.getEndOffset(), "x");

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("x", 1, 1, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		addTestChild();

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
		assertChildNodeEquals("", 12, 13, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterTextBeforeChild() throws Exception {
		content.insertText(parent.getEndOffset(), "x");
		addTestChild();

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("x", 1, 1, actualChildren.next());
		assertChildNodeEquals("", 2, 3, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertChildNodeEquals("", 1, 2, actualChildren.next());
		assertTextNodeEquals("Hello World", 3, 13, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideSingleCharacterTextAfterChild() throws Exception {
		addTestChild();
		content.insertText(parent.getEndOffset(), "x");

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertChildNodeEquals("", 1, 2, actualChildren.next());
		assertTextNodeEquals("x", 3, 3, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideAllChildNodesIncludingText() throws Exception {
		setUpChildNodes();
		final Iterator<INode> actualChildren = parent.children().iterator();
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
		final Iterator<? extends INode> actualChildren = parent.children().in(parent.getRange().resizeBy(-2, 0)).iterator();
		assertTextNodeEquals("Hello ", 7, 12, actualChildren.next());
		assertChildNodeEquals("Child1", 13, 20, actualChildren.next());
		assertChildNodeEquals("Child2", 21, 28, actualChildren.next());
		assertTextNodeEquals(" World", 29, 34, actualChildren.next());
	}

	@Test
	public void shouldHandleBiggerEndOffset() throws Exception {
		setUpChildNodes();
		content.insertText(parent.getEndOffset() + 1, "suffix");
		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello ", 1, 6, actualChildren.next());
		assertChildNodeEquals("Child1", 7, 14, actualChildren.next());
		assertChildNodeEquals("Child2", 15, 22, actualChildren.next());
		assertTextNodeEquals(" World", 23, 28, actualChildren.next());
	}

	@Test
	public void shouldProvideSelfOnOwnBoundaries() throws Exception {
		assertSame(parent, parent.getChildAt(parent.getStartOffset()));
		assertSame(parent, parent.getChildAt(parent.getEndOffset()));
	}

	@Test
	public void shouldReturnChildAtOffset() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello ");
		final TestChild child1 = addTestChild();
		content.insertText(child1.getEndOffset(), "Child1");

		assertSame(child1, parent.getChildAt(9));
	}

	@Test
	public void shouldReturnTextAtOffsetBetweenChildren() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello ");
		final TestChild child1 = addTestChild();
		content.insertText(child1.getEndOffset(), "Child1");
		content.insertText(parent.getEndOffset(), " Gap ");
		final TestChild child2 = addTestChild();
		content.insertText(child2.getEndOffset(), "Child2");
		content.insertText(parent.getEndOffset(), " World");

		final INode text = parent.children().get(2);

		assertTextNodeEquals(" Gap ", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset() + 1));
	}

	@Test
	public void shouldReturnTextAtOffsetBeforeFirstChild() throws Exception {
		setUpChildNodes();

		final INode text = parent.children().get(0);

		assertTextNodeEquals("Hello ", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset() + 1));
	}

	@Test
	public void shouldReturnTextAtOffsetAfterLastChild() throws Exception {
		setUpChildNodes();

		final INode text = parent.children().get(3);

		assertTextNodeEquals(" World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset() + 1));
	}

	@Test
	public void shouldReturnTextWithinBoundaries() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");
		final INode text = parent.children().first();
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset()));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset() + 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getEndOffset() - 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getEndOffset()));
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
		final INode text = child.children().first();
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset()));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getStartOffset() + 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getEndOffset() - 1));
		assertTextNodeEquals("Hello World", text.getStartOffset(), text.getEndOffset(), parent.getChildAt(text.getEndOffset()));
	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotProvideChildNodeBeforeStartOffset() throws Exception {
		content.insertText(parent.getStartOffset(), "prefix");
		parent.getChildAt(parent.getStartOffset() - 1);

	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotProvideChildNodeAfterEndOffset() throws Exception {
		content.insertText(parent.getEndOffset() + 1, "suffix");
		parent.getChildAt(parent.getEndOffset() + 1);
	}

	@Test
	public void shouldProvideChildNodesBeforeOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final Iterator<? extends INode> childNodes12 = parent.children().before(child3.getStartOffset()).iterator();
		assertSame(child1, childNodes12.next());
		assertSame(child2, childNodes12.next());
		assertFalse(childNodes12.hasNext());

		final Iterator<? extends INode> childNodes123 = parent.children().before(parent.getEndOffset()).iterator();
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

		final Iterator<? extends INode> childNodes12 = parent.children().before(child3.getStartOffset() + 5).iterator();
		assertSame(child1, childNodes12.next());
		assertSame(child2, childNodes12.next());
		assertFalse(childNodes12.hasNext());
	}

	@Test
	public void shouldProvideChildNodesAfterOffset() throws Exception {
		final TestChild child1 = addTestChild();
		final TestChild child2 = addTestChild();
		final TestChild child3 = addTestChild();

		final Iterator<? extends INode> childNodes23 = parent.children().after(child1.getEndOffset()).iterator();
		assertSame(child2, childNodes23.next());
		assertSame(child3, childNodes23.next());
		assertFalse(childNodes23.hasNext());

		final Iterator<? extends INode> childNodes123 = parent.children().after(parent.getStartOffset()).iterator();
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

		final Iterator<? extends INode> childNodes23 = parent.children().after(child1.getEndOffset() - 5).iterator();
		assertSame(child2, childNodes23.next());
		assertSame(child3, childNodes23.next());
		assertFalse(childNodes23.hasNext());
	}

	@Test
	public void givenChildNodesAndText_shouldProvideChildrenAxisIncludingText() throws Exception {
		setUpChildNodes();

		final Iterator<INode> actualChildren = parent.children().iterator();
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

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertSame(child1, actualChildren.next());
		assertSame(child2, actualChildren.next());
		assertSame(child3, actualChildren.next());
	}

	@Test
	public void givenOnlyText_shouldProvideTextOnChildrenAxis() throws Exception {
		content.insertText(parent.getEndOffset(), "Hello World");

		final Iterator<INode> actualChildren = parent.children().iterator();
		assertTextNodeEquals("Hello World", 1, 11, actualChildren.next());
	}

	@Test
	public void givenEmptyParent_shouldIndicateEmptyChildrenAxis() throws Exception {
		assertTrue(parent.children().isEmpty());
	}

	@Test
	public void whenTextExcludedOnAxis_shouldNotProvideTextOnChildrenAxis() throws Exception {
		setUpChildNodes();
		final Iterator<? extends INode> actualChildren = parent.children().withoutText().iterator();
		assertEquals("Child1", actualChildren.next().getText());
		assertEquals("Child2", actualChildren.next().getText());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void shouldProvideChildrenAxisAsList() throws Exception {
		setUpChildNodes();
		final List<? extends INode> actualList = parent.children().asList();
		assertEquals(4, actualList.size());
		assertTextNodeEquals("Hello ", 1, 6, actualList.get(0));
		assertChildNodeEquals("Child1", 7, 14, actualList.get(1));
		assertChildNodeEquals("Child2", 15, 22, actualList.get(2));
		assertTextNodeEquals(" World", 23, 28, actualList.get(3));
	}

	@Test
	public void shouldProvideNodeOnChildrenAxisByIndex() throws Exception {
		setUpChildNodes();
		assertEquals("Child1", parent.children().get(1).getText());
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

	@Test
	public void shouldProvideNodeCountOnChildrenAxis() throws Exception {
		setUpChildNodes();
		assertEquals(4, parent.children().count());
	}

	private static void assertTextNodeEquals(final String text, final int startOffset, final int endOffset, final INode actualNode) {
		assertTrue(actualNode instanceof IText);
		assertEquals(text, actualNode.getText());
		assertEquals(startOffset, actualNode.getStartOffset());
		assertEquals(endOffset, actualNode.getEndOffset());
	}

	private static void assertChildNodeEquals(final String text, final int startOffset, final int endOffset, final INode actualNode) {
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

	private TestChild insertTestChildAt(final int offset) {
		content.insertTagMarker(offset);
		content.insertTagMarker(offset);
		final TestChild result = new TestChild();
		parent.insertChildAt(offset, result);
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
		public boolean isKindOf(final INode node) {
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
		public boolean isKindOf(final INode node) {
			return false;
		}
	}
}
