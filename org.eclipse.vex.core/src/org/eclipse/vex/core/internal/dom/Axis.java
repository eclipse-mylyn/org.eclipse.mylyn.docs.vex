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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.core.AfterNIterator;
import org.eclipse.vex.core.internal.core.FilterIterator;
import org.eclipse.vex.core.internal.core.FirstNIterator;
import org.eclipse.vex.core.internal.core.IFilter;

/**
 * An Axis represents an iterable sequence of nodes. It is the main concept to navigate in the DOM, inspired by the
 * XPath axes. Besides implementing the Iterable interface, Axis provides a fluent interface to limit the sequence by
 * several criteria:
 * <ul>
 * <li>only nodes within a range in the content</li>
 * <li>omit text nodes</li>
 * <li>only nodes within a range of indices</li>
 * <li>only nodes which match a given chain of filters</li>
 * </ul>
 * 
 * Example:
 * 
 * <pre>
 * Iterator&lt;Node&gt; elementsInRange = parent.children().in(new ContentRange(1, 24)).matching(new IFilter&lt;Node&gt;() {
 * 	public boolean matches(Node node) {
 * 		return node instanceof Element;
 * 	}
 * }).iterator();
 * </pre>
 * 
 * The Iterable interface makes it very convenient to use the axis in a foreach loop.
 * 
 * @author Florian Thienel
 * @see <a href="http://www.w3.org/TR/xpath/#axes">http://www.w3.org/TR/xpath/#axes</a>
 */
public abstract class Axis implements Iterable<Node> {

	private static final int UNDEFINED = -1;

	private final Node sourceNode;

	private ContentRange contentRange = ContentRange.ALL;
	private boolean includeText = true;
	private int startIndex = UNDEFINED;
	private int endIndex = UNDEFINED;
	private final List<IteratorFactory> chain = new ArrayList<IteratorFactory>();

	/**
	 * @param sourceNode
	 *            the source node of this axis
	 */
	public Axis(final Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	/**
	 * @return the source node of this axis
	 */
	public Node getSourceNode() {
		return sourceNode;
	}

	/**
	 * Creates the root iterator which provides the original sequence of nodes of this axis. The root iterator must take
	 * care of the content range and the inclusion of Text nodes. The other criteria (index range, filters) are applied
	 * to the sequence by this axis.
	 * 
	 * @param contentRange
	 *            the defined content range, by default the range of the source node
	 * @param includeText
	 *            true if Text nodes should be included in the original sequence
	 */
	protected abstract Iterator<Node> createRootIterator(final ContentRange contentRange, final boolean includeText);

	public Iterator<Node> iterator() {
		Iterator<Node> result = rootIterator();

		if (startIndex != UNDEFINED && endIndex != UNDEFINED) {
			result = new AfterNIterator<Node>(result, startIndex);
			result = new FirstNIterator<Node>(result, endIndex - startIndex + 1);
		} else if (startIndex != UNDEFINED) {
			result = new AfterNIterator<Node>(result, startIndex);
		} else if (endIndex != UNDEFINED) {
			result = new FirstNIterator<Node>(result, endIndex + 1);
		}

		for (final IteratorFactory iteratorFactory : chain) {
			result = iteratorFactory.iterator(result);
		}

		return result;
	}

	private Iterator<Node> rootIterator() {
		return createRootIterator(contentRange, includeText);
	}

	/**
	 * Limit the nodes of this axis to the given content range. Cannot be combined with 'before' or 'after'. Can be
	 * applied only once.
	 * 
	 * @param range
	 *            the content range
	 * @return a reference to this axis
	 * @see Axis#before
	 * @see Axis#after
	 */
	public Axis in(final ContentRange range) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = range;
		return this;
	}

	/**
	 * Limit the nodes of this axis to nodes before the given offset. Cannot be combined with 'in' or 'after'. Can be
	 * applied only once.
	 * 
	 * @param beforeOffset
	 *            the offset
	 * @return a reference to this axis
	 * @see Axis#in
	 * @see Axis#after
	 */
	public Axis before(final int beforeOffset) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = new ContentRange(contentRange.getStartOffset(), beforeOffset);
		return this;
	}

	/**
	 * Limit the nodes of this axis to nodes after the given offset. Cannot be combined with 'in' or 'before'. Can be
	 * applied only once.
	 * 
	 * @param afterOffset
	 *            the offset
	 * @return a reference to this axis
	 * @see Axis#in
	 * @see Axis#before
	 */
	public Axis after(final int afterOffset) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = new ContentRange(afterOffset, contentRange.getEndOffset());
		return this;
	}

	/**
	 * Do not include Text nodes in this axis.
	 * 
	 * @return a reference to this axis
	 */
	public Axis withoutText() {
		includeText = false;
		return this;
	}

	/**
	 * Limit the nodes of this axis to nodes matching the given filter. Can be applied multiple times to chain multiple
	 * filters.
	 * 
	 * @param filter
	 *            the filter
	 * @return a reference to this axis
	 */
	public Axis matching(final IFilter<Node> filter) {
		chain.add(new IteratorFactory() {
			public Iterator<Node> iterator(final Iterator<Node> source) {
				return new FilterIterator<Node>(source, filter);
			}
		});
		return this;
	}

	/**
	 * Start the sequence of this axis at the given index. Can be applied only once.
	 * 
	 * @param startIndex
	 *            the start index
	 * @return a reference to this axis
	 * @see Axis#to
	 */
	public Axis from(final int startIndex) {
		Assert.isTrue(this.startIndex == UNDEFINED, "Can set start index only once.");
		this.startIndex = startIndex;
		return this;
	}

	/**
	 * End the sequence of this axis at the given index. Can be applied only once.
	 * 
	 * @param endIndex
	 *            the end index
	 * @return a reference to this axis
	 * @see Axis#from
	 */
	public Axis to(final int endIndex) {
		Assert.isTrue(this.endIndex == UNDEFINED, "Can set end index only once.");
		this.endIndex = endIndex;
		return this;
	}

	/**
	 * @return true if this sequence of this axis with all given criteria applied is empty
	 */
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	/**
	 * Create a list with all nodes of this axis that satisfy all given criteria. Be aware that this method goes through
	 * the whole sequence to collect all matching nodes.
	 * 
	 * @return all nodes to which the given criteria apply as list
	 */
	public List<Node> asList() {
		final ArrayList<Node> result = new ArrayList<Node>();
		for (final Node node : this) {
			result.add(node);
		}
		return result;
	}

	/**
	 * @return the first node of this axis that satisfies all given critera
	 */
	public Node first() {
		return iterator().next();
	}

	/**
	 * Find the last node of this axis which satisfies all given criteria. Be aware that this method goes through the
	 * whole sequence to find the last matching node.
	 * 
	 * @return the last node of this axis that satisfies all given criteria
	 */
	public Node last() {
		Node result = null;
		final Iterator<Node> iterator = iterator();
		while (iterator.hasNext()) {
			result = iterator.next();
		}
		if (result == null) {
			throw new NoSuchElementException();
		}
		return result;
	}

	/**
	 * @param index
	 *            the index
	 * @return the node with the given index in the resulting sequence of nodes
	 */
	public Node get(final int index) {
		final Iterator<Node> iterator = iterator();
		int i = 0;
		while (i++ < index) {
			iterator.next();
		}
		return iterator.next();
	}

	/**
	 * Visit all nodes of this axis that satisfy all given criteria.
	 * 
	 * @param visitor
	 *            the visitor
	 */
	public void accept(final INodeVisitor visitor) {
		for (final Node node : this) {
			node.accept(visitor);
		}
	}

	/**
	 * Count all nodes of this axis that satisfy all given criteria. Be aware that this method goes through the whole
	 * sequence to count all matching nodes.
	 * 
	 * @return the number of nodes of this axis that satisfy all given criteria
	 */
	public int count() {
		int result = 0;
		final Iterator<Node> iterator = iterator();
		while (iterator.hasNext()) {
			result++;
			iterator.next();
		}
		return result;
	}

	private static interface IteratorFactory {
		Iterator<Node> iterator(Iterator<Node> source);
	}
}
