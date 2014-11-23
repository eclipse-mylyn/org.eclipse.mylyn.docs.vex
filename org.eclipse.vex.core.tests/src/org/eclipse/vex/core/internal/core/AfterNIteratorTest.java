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
import java.util.List;

import org.junit.Test;

public class AfterNIteratorTest {

	@Test
	public void givenCollection_whenEnoughElements_shouldStartAfterNthElement() throws Exception {
		final AfterNIterator<String> iterator = new AfterNIterator<String>(Arrays.asList("one", "two", "three", "four").iterator(), 2);

		assertEquals("three", iterator.next());
		assertEquals("four", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenCollection_whenLessElements_shouldProvideNoElements() throws Exception {
		final AfterNIterator<String> iterator = new AfterNIterator<String>(Arrays.asList("one").iterator(), 2);

		assertFalse(iterator.hasNext());
	}

	@Test
	public void givenCollection_shouldRemoveElements() throws Exception {
		final List<String> elements = new ArrayList<String>(Arrays.asList("one", "two", "three", "four"));
		final AfterNIterator<String> iterator = new AfterNIterator<String>(elements.iterator(), 3);
		iterator.next();
		iterator.remove();

		assertEquals(3, elements.size());
		assertEquals("three", elements.get(2));
	}

}
