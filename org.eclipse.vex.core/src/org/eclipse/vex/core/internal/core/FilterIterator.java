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
package org.eclipse.vex.core.internal.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Florian Thienel
 */
public class FilterIterator<T> implements Iterator<T> {

	private final Iterator<T> source;
	private final IFilter<T> filter;
	private T current;

	public FilterIterator(final Iterator<T> source, final IFilter<T> filter) {
		this.source = source;
		this.filter = filter;
		current = null;
		nextStep();
	}

	private void nextStep() {
		while (source.hasNext()) {
			final T next = source.next();
			if (filter.matches(next)) {
				current = next;
				return;
			}
		}
		current = null;
	}

	public boolean hasNext() {
		return current != null;
	}

	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		final T result = current;
		nextStep();
		return result;
	}

	public void remove() {
		source.remove();
	}
}
