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
public class FirstNIterator<T> implements Iterator<T> {

	private final Iterator<T> source;
	private final int n;
	private int cursor;

	public FirstNIterator(final Iterator<T> source, final int n) {
		this.source = source;
		this.n = n;
		cursor = 0;
	}

	public boolean hasNext() {
		return cursor < n && source.hasNext();
	}

	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		cursor++;
		return source.next();
	}

	public void remove() {
		source.remove();
	}

}
