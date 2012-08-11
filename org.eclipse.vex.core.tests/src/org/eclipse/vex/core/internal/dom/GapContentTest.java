/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test the GapContent class
 */
public class GapContentTest {

	@Test
	public void insertSingleElementMarker() throws Exception {
		final GapContent content = new GapContent(3);
		content.insertElementMarker(0);
		assertEquals(1, content.getLength());
		assertTrue(content.isElementMarker(content.getString(0, content.getLength()).charAt(0)));
	}

	@Test
	public void insertMultipleElementMarkers() throws Exception {
		final GapContent elementMarkerContent = new GapContent(4);
		elementMarkerContent.insertElementMarker(0);
		elementMarkerContent.insertElementMarker(0);

		final GapContent stringContent = new GapContent(4);
		stringContent.insertString(0, "\0\0");

		assertEquals(stringContent.getLength(), elementMarkerContent.getLength());
		assertEquals(stringContent.getString(0, stringContent.getLength()), elementMarkerContent.getString(0, elementMarkerContent.getLength()));
	}

	@Test
	public void testGapContent() throws Exception {
		//
		// a b (gap) c d
		// | | | | |
		// 0 1 2 3 4
		//

		final GapContent content = new GapContent(2);
		assertEquals(0, content.getLength());
		content.insertString(0, "a");
		assertEquals(1, content.getLength());
		content.insertString(1, "d");
		assertEquals(2, content.getLength());
		content.insertString(1, "c");
		assertEquals(3, content.getLength());
		content.insertString(1, "b");
		assertEquals(4, content.getLength());

		final Position pa = content.createPosition(0);
		final Position pb = content.createPosition(1);
		final Position pc = content.createPosition(2);
		final Position pd = content.createPosition(3);
		final Position pe = content.createPosition(4);

		try {
			content.getString(-1, 1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getString(4, 1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getString(0, -1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getString(0, 5);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.createPosition(-1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.createPosition(5);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		assertEquals("a", content.getString(0, 1));
		assertEquals("b", content.getString(1, 1));
		assertEquals("c", content.getString(2, 1));
		assertEquals("d", content.getString(3, 1));

		assertEquals("ab", content.getString(0, 2));
		assertEquals("bc", content.getString(1, 2));
		assertEquals("cd", content.getString(2, 2));

		assertEquals("abc", content.getString(0, 3));
		assertEquals("bcd", content.getString(1, 3));

		assertEquals("abcd", content.getString(0, 4));

		//
		// a b x (gap) y c d
		// | | | | | | |
		// 0 1 2 3 4 5 6
		// 
		content.insertString(2, "y");
		assertEquals(5, content.getLength());
		content.insertString(2, "x");
		assertEquals(6, content.getLength());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());
		assertEquals(4, pc.getOffset());
		assertEquals(5, pd.getOffset());
		assertEquals(6, pe.getOffset());

		final Position px = content.createPosition(2);
		final Position py = content.createPosition(3);

		content.remove(2, 2);

		assertEquals(4, content.getLength());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());
		assertEquals(2, px.getOffset());
		assertEquals(2, py.getOffset());
		assertEquals(2, pc.getOffset());
		assertEquals(3, pd.getOffset());
		assertEquals(4, pe.getOffset());

		assertEquals("a", content.getString(0, 1));
		assertEquals("b", content.getString(1, 1));
		assertEquals("c", content.getString(2, 1));
		assertEquals("d", content.getString(3, 1));

		assertEquals("ab", content.getString(0, 2));
		assertEquals("bc", content.getString(1, 2));
		assertEquals("cd", content.getString(2, 2));

		assertEquals("abc", content.getString(0, 3));
		assertEquals("bcd", content.getString(1, 3));

		assertEquals("abcd", content.getString(0, 4));

	}
}
