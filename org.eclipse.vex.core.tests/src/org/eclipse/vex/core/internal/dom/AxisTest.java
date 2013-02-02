/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.vex.core.internal.core.IFilter;
import org.junit.Before;
import org.junit.Test;

public class AxisTest {

	private TestNode source;

	@Before
	public void setUp() throws Exception {
		source = new TestNode("source");
	}

	@Test
	public void givenAxis_whenEmpty_shouldIndicateThatAxisIsEmpty() throws Exception {
		assertTrue(axis(source, nodes()).isEmpty());
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldIndicateThatAxisIsNotEmpty() throws Exception {
		assertFalse(axis(source, nodes("node")).isEmpty());
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldProvideFirstNode() throws Exception {
		assertEquals("first", axis(source, nodes("first", "second", "third", "last")).first().toString());
	}

	@Test(expected = NoSuchElementException.class)
	public void givenAxis_whenEmpty_shouldNotProvideFirstNode() throws Exception {
		axis(source, nodes()).first();
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldProvideNodeByIndex() throws Exception {
		assertEquals("second", axis(source, nodes("first", "second", "third", "last")).get(1).toString());
	}

	@Test(expected = NoSuchElementException.class)
	public void givenAxis_whenEmpty_shouldNotProvideNodeByIndex() throws Exception {
		axis(source, nodes()).get(1);
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldProvideLastNode() throws Exception {
		assertEquals("last", axis(source, nodes("first", "second", "third", "last")).last().toString());
	}

	@Test(expected = NoSuchElementException.class)
	public void givenAxis_whenEmpty_shouldNotProvideLastNode() throws Exception {
		axis(source, nodes()).last();
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldProvideNodesAsList() throws Exception {
		final List<Node> expectedNodes = Arrays.<Node> asList(new TestNode("one"), new TestNode("two"), new TestNode("three"));
		final List<Node> actualNodes = axis(source, expectedNodes).asList();
		assertTrue(Arrays.equals(expectedNodes.toArray(), actualNodes.toArray()));
	}

	@Test
	public void givenAxis_whenEmpty_shouldProvideEmptyNodesList() throws Exception {
		assertTrue(axis(source, nodes()).asList().isEmpty());
	}

	@Test
	public void givenAxis_whenNotEmpty_shouldProvideNodeCount() throws Exception {
		assertEquals(3, axis(source, nodes("one", "two", "three")).count());
	}

	@Test
	public void givenAxis_whenEmpty_shouldProvideZeroNodeCount() throws Exception {
		assertEquals(0, axis(source, nodes()).count());
	}

	@Test
	public void givenAxisAndVisitor_shouldVisitAllNodes() throws Exception {
		final int[] visits = new int[1];
		axis(source, nodes(visits, "one", "two", "three")).accept(new BaseNodeVisitor());
		assertEquals(3, visits[0]);
	}

	@Test
	public void givenAxisWithElements_whenFilterIsGiven_shouldProvideOnlyMatchingNodes() throws Exception {
		int actualNodeCount = 0;
		for (final Node actualNode : axis(source, nodes("matching", "matching", "not", "not", "matching")).matching(new IFilter<Node>() {
			public boolean matches(final Node node) {
				return "matching".equals(((TestNode) node).getName());
			}
		})) {
			assertEquals("matching", ((TestNode) actualNode).getName());
			actualNodeCount++;
		}
		assertEquals(3, actualNodeCount);
	}

	@Test
	public void givenAxisWithElements_whenEndIndexIsGiven_shouldProvideOnlyNodesUpToEndIndex() throws Exception {
		final Iterator<Node> actualNodes = axis(source, nodes("one", "two", "three")).to(1).iterator();
		assertEquals("one", actualNodes.next().toString());
		assertEquals("two", actualNodes.next().toString());
		assertFalse(actualNodes.hasNext());
	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotAcceptMoreThanOneEndIndex() throws Exception {
		axis(source, nodes()).to(0).to(0);
	}

	@Test
	public void givenAxisWithElements_whenStartIndexIsGiven_shouldProvideOnlyNodesFromStartIndex() throws Exception {
		final Iterator<Node> actualNodes = axis(source, nodes("one", "two", "three")).from(1).iterator();
		assertEquals("two", actualNodes.next().toString());
		assertEquals("three", actualNodes.next().toString());
		assertFalse(actualNodes.hasNext());
	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotAcceptMoreThanOneStartIndex() throws Exception {
		axis(source, nodes()).from(0).from(0);
	}

	@Test
	public void givenAxisWithElements_whenStartAndEndIndexAreGiven_shouldProvideOnlyNodesFromStartToEndIndex() throws Exception {
		final Iterator<Node> actualNodes = axis(source, nodes("one", "two", "three", "four", "five")).from(1).to(2).iterator();
		assertEquals("two", actualNodes.next().toString());
		assertEquals("three", actualNodes.next().toString());
		assertFalse(actualNodes.hasNext());
	}

	private static Axis axis(final Node sourceNode, final Iterable<Node> source) {
		return new Axis(sourceNode) {
			@Override
			protected Iterator<Node> iterator(final Node sourceNode, final Axis axis) {
				return source.iterator();
			}

		};
	}

	private static Iterable<Node> nodes(final String... nodeNames) {
		return nodes(null, nodeNames);
	}

	private static Iterable<Node> nodes(final int[] visits, final String... nodeNames) {
		final ArrayList<Node> result = new ArrayList<Node>();
		for (final String nodeName : nodeNames) {
			result.add(new TestNode(nodeName, visits));
		}
		return result;
	}

	private static class TestNode extends Node {

		private final String name;
		private int[] visits;

		public TestNode(final String name) {
			this.name = name;
		}

		public TestNode(final String name, final int[] visits) {
			this.name = name;
			this.visits = visits;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean isKindOf(final Node node) {
			return false;
		}

		@Override
		public void accept(final INodeVisitor visitor) {
			if (visits == null) {
				throw new IllegalStateException();
			}
			visits[0]++;
		}

		@Override
		public <T> T accept(final INodeVisitorWithResult<T> visitor) {
			throw new UnsupportedOperationException();
		}
	}
}
