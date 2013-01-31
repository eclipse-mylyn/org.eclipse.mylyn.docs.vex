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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

public class FilterIteratorTest {

	@Test
	public void givenAnEmptySource_shouldBeEmpty() throws Exception {
		final Iterator<Object> source = Collections.emptyList().iterator();
		final FilterIterator<Object> iterator = new FilterIterator<Object>(source, all());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenASourceWithObjects_shouldNotBeEmpty() throws Exception {
		final Iterator<String> source = Arrays.asList("Hello", "", "World").iterator();
		final FilterIterator<String> iterator = new FilterIterator<String>(source, FilterIteratorTest.<String> all());
		assertTrue(iterator.hasNext());
	}

	@Test
	public void givenASourceAndASelectiveFilter_shouldProvideOnlyMatchingObjects() throws Exception {
		final Iterator<String> source = Arrays.asList("Hello", "", "World").iterator();
		final FilterIterator<String> iterator = new FilterIterator<String>(source, noEmptyStrings());
		assertEquals("Hello", iterator.next());
		assertEquals("World", iterator.next());
		assertFalse(iterator.hasNext());
	}

	private static <T> IFilter<T> all() {
		return new IFilter<T>() {
			public boolean matches(final T t) {
				return true;
			}
		};
	}

	private static IFilter<String> noEmptyStrings() {
		return new IFilter<String>() {
			public boolean matches(final String string) {
				return string.length() > 0;
			}
		};
	}
}
