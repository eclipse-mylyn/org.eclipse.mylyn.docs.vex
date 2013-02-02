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
import java.util.Collections;
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

	private final List<IteratorFactory> chain = new ArrayList<IteratorFactory>();

	private final Node sourceNode;
	private boolean willBeEmpty;
	private ContentRange contentRange;
	private boolean includeText;

	private int startIndex;
	private int endIndex;

	public Axis(final Node sourceNode) {
		this.sourceNode = sourceNode;
		setDefaultValues();
	}

	private void setDefaultValues() {
		willBeEmpty = false;
		contentRange = sourceNode.getRange();
		includeText = true;
		startIndex = UNDEFINED;
		endIndex = UNDEFINED;
	}

	protected ContentRange getContentRange() {
		return contentRange;
	}

	protected boolean shouldIncludeText() {
		return includeText;
	}

	protected abstract Iterator<Node> iterator(final Node sourceNode, final Axis axis);

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
		if (willBeEmpty) {
			return Collections.<Node> emptyList().iterator();
		}
		return iterator(sourceNode, this);
	}

	public Axis in(final ContentRange range) {
		Assert.isTrue(sourceNode.getRange().equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		if (!sourceNode.getRange().intersects(range)) {
			willBeEmpty = true;
		}
		contentRange = range;
		return this;
	}

	public Axis before(final int beforeOffset) {
		Assert.isTrue(sourceNode.getRange().equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		if (beforeOffset <= sourceNode.getStartOffset()) {
			contentRange = ContentRange.NULL;
			willBeEmpty = true;
		} else {
			contentRange = new ContentRange(sourceNode.getStartOffset() + 1, beforeOffset);
		}
		return this;
	}

	public Axis after(final int afterOffset) {
		Assert.isTrue(sourceNode.getRange().equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		if (afterOffset >= sourceNode.getEndOffset()) {
			contentRange = ContentRange.NULL;
			willBeEmpty = true;
		} else {
			contentRange = new ContentRange(afterOffset, sourceNode.getEndOffset() - 1);
		}
		return this;
	}

	public Axis withoutText() {
		includeText = false;
		return this;
	}

	public Axis matching(final IFilter<Node> filter) {
		chain.add(new IteratorFactory() {
			public Iterator<Node> iterator(final Iterator<Node> source) {
				return new FilterIterator<Node>(source, filter);
			}
		});
		return this;
	}

	public Axis from(final int startIndex) {
		Assert.isTrue(this.startIndex == UNDEFINED, "Can set start index only once.");
		this.startIndex = startIndex;
		return this;
	}

	public Axis to(final int endIndex) {
		Assert.isTrue(this.endIndex == UNDEFINED, "Can set end index only once.");
		this.endIndex = endIndex;
		return this;
	}

	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	public List<Node> asList() {
		final ArrayList<Node> result = new ArrayList<Node>();
		for (final Node node : this) {
			result.add(node);
		}
		return result;
	}

	public Node first() {
		return iterator().next();
	}

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

	public Node get(final int index) {
		final Iterator<Node> iterator = iterator();
		int i = 0;
		while (i++ < index) {
			iterator.next();
		}
		return iterator.next();
	}

	public void accept(final INodeVisitor visitor) {
		for (final Node node : this) {
			node.accept(visitor);
		}
	}

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
