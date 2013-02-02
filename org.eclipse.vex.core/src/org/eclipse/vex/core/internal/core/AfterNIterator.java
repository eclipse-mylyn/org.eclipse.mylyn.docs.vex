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

/**
 * @author Florian Thienel
 */
public class AfterNIterator<T> implements Iterator<T> {

	private final Iterator<T> source;

	public AfterNIterator(final Iterator<T> source, final int n) {
		this.source = source;
		forward(source, n);
	}

	private static void forward(final Iterator<?> iterator, final int steps) {
		int i = 0;
		while (i++ < steps && iterator.hasNext()) {
			iterator.next();
		}
	}

	public boolean hasNext() {
		return source.hasNext();
	}

	public T next() {
		return source.next();
	}

	public void remove() {
		source.remove();
	}

}
