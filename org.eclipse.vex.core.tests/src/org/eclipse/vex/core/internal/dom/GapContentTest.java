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
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test the GapContent class
 */
public class GapContentTest extends ContentTest {

	@Override
	protected Content createContent() {
		return new GapContent(100);
	}

	@Test
	public void useChar0AsElementMarker() throws Exception {
		final GapContent elementMarkerContent = new GapContent(4);
		elementMarkerContent.insertElementMarker(0);
		elementMarkerContent.insertElementMarker(0);

		final GapContent stringContent = new GapContent(4);
		stringContent.insertText(0, "\0\0");

		assertEquals(stringContent.length(), elementMarkerContent.length());
		assertEquals(stringContent.getText(0, stringContent.length() - 1), elementMarkerContent.getText(0, elementMarkerContent.length() - 1));
	}

	@Test
	public void testGapContent() throws Exception {
		//
		// a b (gap) c d
		// | | | | |
		// 0 1 2 3 4
		//

		final GapContent content = new GapContent(2);
		assertEquals(0, content.length());
		content.insertText(0, "a");
		assertEquals(1, content.length());
		content.insertText(1, "d");
		assertEquals(2, content.length());
		content.insertText(1, "c");
		assertEquals(3, content.length());
		content.insertText(1, "b");
		assertEquals(4, content.length());

		final Position pa = content.createPosition(0);
		final Position pb = content.createPosition(1);
		final Position pc = content.createPosition(2);
		final Position pd = content.createPosition(3);
		final Position pe = content.createPosition(4);

		try {
			content.getText(-1, 0);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getText(4, 4);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getText(0, -1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.getText(0, 4);
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

		assertEquals("a", content.getText(0, 0));
		assertEquals("b", content.getText(1, 1));
		assertEquals("c", content.getText(2, 2));
		assertEquals("d", content.getText(3, 3));

		assertEquals("ab", content.getText(0, 1));
		assertEquals("bc", content.getText(1, 2));
		assertEquals("cd", content.getText(2, 3));

		assertEquals("abc", content.getText(0, 2));
		assertEquals("bcd", content.getText(1, 3));

		assertEquals("abcd", content.getText(0, 3));

		//
		// a b x (gap) y c d
		// | | | | | | |
		// 0 1 2 3 4 5 6
		// 
		content.insertText(2, "y");
		assertEquals(5, content.length());
		content.insertText(2, "x");
		assertEquals(6, content.length());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());
		assertEquals(4, pc.getOffset());
		assertEquals(5, pd.getOffset());
		assertEquals(6, pe.getOffset());

		final Position px = content.createPosition(2);
		final Position py = content.createPosition(3);

		content.remove(2, 2);

		assertEquals(4, content.length());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());
		assertEquals(2, px.getOffset());
		assertEquals(2, py.getOffset());
		assertEquals(2, pc.getOffset());
		assertEquals(3, pd.getOffset());
		assertEquals(4, pe.getOffset());

		assertEquals("a", content.getText(0, 0));
		assertEquals("b", content.getText(1, 1));
		assertEquals("c", content.getText(2, 2));
		assertEquals("d", content.getText(3, 3));

		assertEquals("ab", content.getText(0, 1));
		assertEquals("bc", content.getText(1, 2));
		assertEquals("cd", content.getText(2, 3));

		assertEquals("abc", content.getText(0, 2));
		assertEquals("bcd", content.getText(1, 3));

		assertEquals("abcd", content.getText(0, 3));

	}

}
