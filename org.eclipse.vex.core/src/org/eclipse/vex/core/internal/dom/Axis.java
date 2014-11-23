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
import org.eclipse.vex.core.IFilter;
import org.eclipse.vex.core.internal.core.AfterNIterator;
import org.eclipse.vex.core.internal.core.FilterIterator;
import org.eclipse.vex.core.internal.core.FirstNIterator;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;

/**
 * @author Florian Thienel
 */
public abstract class Axis<T extends INode> implements IAxis<T> {

	private static final int UNDEFINED = -1;

	private final INode sourceNode;

	private ContentRange contentRange = ContentRange.ALL;
	private boolean includeText = true;
	private int startIndex = UNDEFINED;
	private int endIndex = UNDEFINED;
	private final List<IteratorFactory> chain = new ArrayList<IteratorFactory>();

	public Axis(final INode sourceNode) {
		this.sourceNode = sourceNode;
	}

	@Override
	public INode getSourceNode() {
		return sourceNode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<T> iterator() {
		Iterator<? extends T> result = rootIterator();

		if (startIndex != UNDEFINED && endIndex != UNDEFINED) {
			result = new AfterNIterator<T>(result, startIndex);
			result = new FirstNIterator<T>(result, endIndex - startIndex + 1);
		} else if (startIndex != UNDEFINED) {
			result = new AfterNIterator<T>(result, startIndex);
		} else if (endIndex != UNDEFINED) {
			result = new FirstNIterator<T>(result, endIndex + 1);
		}

		for (final IteratorFactory iteratorFactory : chain) {
			result = (Iterator<? extends T>) iteratorFactory.iterator(result);
		}

		return (Iterator<T>) result;
	}

	private Iterator<? extends T> rootIterator() {
		return createRootIterator(contentRange, includeText);
	}

	protected abstract Iterator<? extends T> createRootIterator(final ContentRange contentRange, final boolean includeText);

	@Override
	public Axis<? extends T> in(final ContentRange range) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = range;
		return this;
	}

	@Override
	public Axis<? extends T> before(final int beforeOffset) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = new ContentRange(contentRange.getStartOffset(), beforeOffset);
		return this;
	}

	@Override
	public Axis<? extends T> after(final int afterOffset) {
		Assert.isTrue(ContentRange.ALL.equals(contentRange), "Can only use one of 'before', 'after' or 'in' in the same expression.");
		contentRange = new ContentRange(afterOffset, contentRange.getEndOffset());
		return this;
	}

	@Override
	public Axis<? extends T> withoutText() {
		includeText = false;
		return this;
	}

	@Override
	public Axis<? extends T> matching(final IFilter<INode> filter) {
		chain.add(new IteratorFactory() {
			@Override
			public Iterator<? extends INode> iterator(final Iterator<? extends INode> source) {
				return new FilterIterator<INode>(source, filter);
			}
		});
		return this;
	}

	@Override
	public Axis<? extends T> from(final int startIndex) {
		Assert.isTrue(this.startIndex == UNDEFINED, "Can set start index only once.");
		this.startIndex = startIndex;
		return this;
	}

	@Override
	public Axis<? extends T> to(final int endIndex) {
		Assert.isTrue(this.endIndex == UNDEFINED, "Can set end index only once.");
		this.endIndex = endIndex;
		return this;
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public List<T> asList() {
		final ArrayList<T> result = new ArrayList<T>();
		for (final T node : this) {
			result.add(node);
		}
		return result;
	}

	@Override
	public T first() {
		return iterator().next();
	}

	@Override
	public T last() {
		T result = null;
		final Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			result = iterator.next();
		}
		if (result == null) {
			throw new NoSuchElementException();
		}
		return result;
	}

	@Override
	public T get(final int index) {
		final Iterator<T> iterator = iterator();
		int i = 0;
		while (i++ < index) {
			iterator.next();
		}
		return iterator.next();
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		for (final INode node : this) {
			node.accept(visitor);
		}
	}

	@Override
	public int count() {
		int result = 0;
		final Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			result++;
			iterator.next();
		}
		return result;
	}

	private static interface IteratorFactory {
		Iterator<? extends INode> iterator(Iterator<? extends INode> source);
	}
}
