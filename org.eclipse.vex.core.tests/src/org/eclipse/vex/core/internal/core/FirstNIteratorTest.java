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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class FirstNIteratorTest {

	@Test
	public void givenCollection_whenEnoughElements_shouldProvideFirstNElements() throws Exception {
		final FirstNIterator<String> iterator = new FirstNIterator<String>(Arrays.asList("one", "two", "three", "four").iterator(), 2);

		assertEquals("one", iterator.next());
		assertEquals("two", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenCollection_whenLessElements_shouldProvideLessElements() throws Exception {
		final FirstNIterator<String> iterator = new FirstNIterator<String>(Arrays.asList("one").iterator(), 2);

		assertEquals("one", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenCollection_whenEmpty_shouldProvideNoElements() throws Exception {
		final FirstNIterator<String> iterator = new FirstNIterator<String>(Collections.<String> emptyList().iterator(), 2);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenCollection_shouldRemoveElements() throws Exception {
		final List<String> elements = new ArrayList<String>(Arrays.asList("one", "two", "three", "four"));
		final FirstNIterator<String> iterator = new FirstNIterator<String>(elements.iterator(), 2);
		iterator.next();
		iterator.remove();

		assertEquals(3, elements.size());
		assertEquals("two", elements.get(0));
	}
}
