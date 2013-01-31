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
import org.eclipse.vex.core.internal.core.FilterIterator;
import org.eclipse.vex.core.internal.core.IFilter;

/**
 * An Axis represents an iterable set of nodes. It is the main concept to navigate in the DOM, inspired by the XPath
 * axes.
 * 
 * @author Florian Thienel
 * @see <a href="http://www.w3.org/TR/xpath/#axes">http://www.w3.org/TR/xpath/#axes</a>
 */
public abstract class Axis implements Iterable<Node> {

	private final List<IteratorFactory> chain = new ArrayList<IteratorFactory>();

	private final Node sourceNode;
	private boolean willBeEmpty;
	private ContentRange contentRange;
	private boolean includeText;

	public Axis(final Node sourceNode) {
		this.sourceNode = sourceNode;
		setDefaultValues();
	}

	private void setDefaultValues() {
		willBeEmpty = false;
		contentRange = sourceNode.getRange();
		includeText = true;
	}

	protected abstract Iterator<Node> iterator(final ContentRange range, final boolean includeText);

	public Iterator<Node> iterator() {
		Iterator<Node> result = rootIterator();

		for (final IteratorFactory iteratorFactory : chain) {
			result = iteratorFactory.iterator(result);
		}

		return result;
	}

	private Iterator<Node> rootIterator() {
		if (willBeEmpty) {
			return Collections.<Node> emptyList().iterator();
		}
		return iterator(contentRange, includeText);
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

	public Node at(final int index) {
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

	private static interface IteratorFactory {
		Iterator<Node> iterator(Iterator<Node> source);
	}
}
