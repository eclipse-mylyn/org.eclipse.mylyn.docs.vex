/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.vex.core.dom.ContentRange;
import org.junit.Test;

public class ContentRangeTest {

	@Test
	public void hasStartAndEndOffset() throws Exception {
		final ContentRange range = new ContentRange(1, 5);
		assertEquals(1, range.getStartOffset());
		assertEquals(5, range.getEndOffset());
	}

	@Test(expected = AssertionFailedException.class)
	public void shouldNotAcceptEndSmallerThanStart() throws Exception {
		new ContentRange(5, 1);
	}

	@Test
	public void shouldHaveLengthIncludingStartAndEnd() throws Exception {
		assertEquals(5, new ContentRange(1, 5).length());
	}

	@Test
	public void shouldContainSmallerRange() throws Exception {
		assertTrue(new ContentRange(1, 5).contains(new ContentRange(2, 4)));
		assertFalse(new ContentRange(2, 4).contains(new ContentRange(1, 5)));
	}

	@Test
	public void shouldContainItself() throws Exception {
		final ContentRange range = new ContentRange(1, 5);
		assertTrue(range.contains(range));
	}

	@Test
	public void canBeTrimmed() throws Exception {
		assertEquals(new ContentRange(2, 5), new ContentRange(1, 5).intersection(new ContentRange(2, 6)));
		assertEquals(new ContentRange(1, 4), new ContentRange(1, 5).intersection(new ContentRange(0, 4)));
	}

	@Test
	public void canMoveBounds() throws Exception {
		assertEquals(new ContentRange(1, 8), new ContentRange(3, 5).resizeBy(-2, 3));
	}

	@Test
	public void containsSingleOffset() throws Exception {
		final ContentRange range = new ContentRange(1, 5);
		assertFalse(range.contains(0));
		assertTrue(range.contains(1));
		assertTrue(range.contains(2));
		assertTrue(range.contains(4));
		assertTrue(range.contains(5));
		assertFalse(range.contains(6));
	}

	@Test
	public void isValueObject() throws Exception {
		final ContentRange range1a = new ContentRange(1, 5);
		final ContentRange range1b = new ContentRange(1, 5);
		final ContentRange range2 = new ContentRange(2, 4);

		assertTrue("equals if values equals", range1a.equals(range1b));
		assertTrue("equals is symmetric", range1b.equals(range1a));
		assertTrue("hashCode equals if equal", range1a.hashCode() == range1b.hashCode());
		assertFalse("not equal if no equal values", range1a.equals(range2));
	}
}
