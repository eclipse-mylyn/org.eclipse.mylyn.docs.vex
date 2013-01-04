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

import org.junit.Before;
import org.junit.Test;

public abstract class ContentTest {

	private Content content;

	@Before
	public void setUp() throws Exception {
		content = createContent();
	}

	protected abstract Content createContent();

	@Test
	public void insertText() throws Exception {
		final String text = "Hello World";

		assertEquals(0, content.length());
		content.insertText(0, text);
		assertEquals(text.length(), content.length());
	}

	@Test
	public void insertTagMarker() throws Exception {
		content.insertTagMarker(0);
		assertEquals(1, content.length());
		assertTrue(content.isTagMarker(0));
	}

	@Test
	public void mixTextWithTagMarkers() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		content.insertTagMarker(5);
		assertEquals("Each tag marker should count 1 in the length calculation.", text.length() + 1, content.length());
	}

	@Test
	public void shouldReturnPlainTextWithoutTagMarkers() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		content.insertTagMarker(5);
		assertEquals(text, content.getText());
	}

	@Test
	public void shouldReturnAPartialCopy() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		final Content partialCopy = content.getContent(new ContentRange(3, 7));
		assertEquals("lo Wo", partialCopy.getText());

		// make shure, it is a copy
		content.insertText(6, "New ");
		assertEquals("Hello New World", content.getText());
		assertEquals("lo Wo", partialCopy.getText());
	}

	@Test
	public void shouldReturnAFullCopy() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		final Content fullCopy = content.getContent();
		assertEquals(text, fullCopy.getText());

		// make shure, it is a copy
		content.insertText(6, "New ");
		assertEquals("Hello New World", content.getText());
		assertEquals(text, fullCopy.getText());
	}

	@Test
	public void insertContent() throws Exception {
		content.insertText(0, "Hello World");
		final Content other = createContent();
		other.insertTagMarker(0);
		other.insertText(1, "New");
		other.insertTagMarker(4);

		content.insertContent(6, other);
		assertEquals(16, content.length());
		assertEquals("Hello NewWorld", content.getText());
		assertTrue(content.isTagMarker(6));
		assertTrue(content.isTagMarker(10));
	}

	@Test
	public void removeAndInsertContent() throws Exception {
		content.insertText(0, "Hello Cut Out World");
		content.insertTagMarker(6);
		content.insertTagMarker(14);

		content.remove(new ContentRange(7, 13));
		assertTrue(content.isTagMarker(6));
		assertTrue(content.isTagMarker(7));

		content.remove(new ContentRange(6, 7));
		assertEquals("Hello  World", content.getText());

		content.insertText(6, "Cut Out");
		assertEquals("Hello Cut Out World", content.getText());
	}

	@Test
	public void shouldIncreasePositionsOnInsertText() throws Exception {
		content.insertText(0, "Hello World");
		final Position helloPosition = content.createPosition(0);
		final Position worldPosition = content.createPosition(6);
		assertEquals("Hello", content.getText(new ContentRange(helloPosition.getOffset(), helloPosition.getOffset() + 4)));
		assertEquals("World", content.getText(new ContentRange(worldPosition.getOffset(), worldPosition.getOffset() + 4)));

		content.insertText(6, "New ");
		assertEquals(0, helloPosition.getOffset());
		assertEquals(10, worldPosition.getOffset());
		assertEquals("Hello", content.getText(new ContentRange(helloPosition.getOffset(), helloPosition.getOffset() + 4)));
		assertEquals("World", content.getText(new ContentRange(worldPosition.getOffset(), worldPosition.getOffset() + 4)));
	}

	@Test
	public void shouldIncreasePositionsOnInsertTagMarker() throws Exception {
		content.insertText(0, "Hello World");
		final Position worldStartPosition = content.createPosition(6);
		final Position worldEndPosition = content.createPosition(10);
		assertEquals("d", content.getText(new ContentRange(worldEndPosition.getOffset(), worldEndPosition.getOffset())));
		assertEquals("World", content.getText(new ContentRange(worldStartPosition.getOffset(), worldEndPosition.getOffset())));

		content.insertTagMarker(11);
		content.insertTagMarker(6);
		assertEquals(7, worldStartPosition.getOffset());
		assertEquals(11, worldEndPosition.getOffset());
		assertEquals("d", content.getText(new ContentRange(worldEndPosition.getOffset(), worldEndPosition.getOffset())));
		assertEquals("World", content.getText(new ContentRange(worldStartPosition.getOffset(), worldEndPosition.getOffset())));
		assertTrue(content.isTagMarker(worldStartPosition.getOffset() - 1));
		assertTrue(content.isTagMarker(worldEndPosition.getOffset() + 1));
	}

	@Test
	public void shouldDecreasePositionOnRemove() throws Exception {
		content.insertText(0, "Hello New World");
		content.insertTagMarker(8);
		content.insertTagMarker(6);
		final Position worldStartPosition = content.createPosition(12);
		final Position worldEndPosition = content.createPosition(16);
		assertEquals("d", content.getText(new ContentRange(worldEndPosition.getOffset(), worldEndPosition.getOffset())));
		assertEquals("World", content.getText(new ContentRange(worldStartPosition.getOffset(), worldEndPosition.getOffset())));

		content.remove(new ContentRange(6, 11));
		assertEquals("d", content.getText(new ContentRange(worldEndPosition.getOffset(), worldEndPosition.getOffset())));
		assertEquals("World", content.getText(new ContentRange(worldStartPosition.getOffset(), worldEndPosition.getOffset())));
		assertEquals(6, worldStartPosition.getOffset());
		assertEquals(10, worldEndPosition.getOffset());
	}

	@Test
	public void shouldMovePositionsWithinRemovedRangeToRangeStart() throws Exception {
		content.insertText(0, "Hello New World");
		final Position nPosition = content.createPosition(6);
		final Position ePosition = content.createPosition(7);
		final Position wPosition = content.createPosition(8);

		content.remove(new ContentRange(6, 8));

		assertEquals(6, nPosition.getOffset());
		assertEquals(6, ePosition.getOffset());
		assertEquals(6, wPosition.getOffset());
	}

	@Test
	public void canRemovePosition() throws Exception {
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Position position = content.createPosition(1);
		assertEquals(1, position.getOffset());

		content.removePosition(position);
		content.insertText(1, "Hello");
		assertEquals(1, position.getOffset());
	}

	@Test
	public void invalidatesPositionsOnRemoval() throws Exception {
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		final Position position = content.createPosition(1);
		assertTrue(position.isValid());
		content.removePosition(position);
		assertFalse(position.isValid());
	}

	@Test
	public void rawTextContainsTagMarkers() throws Exception {
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertText(1, "Hello World");
		content.insertTagMarker(6);

		assertFalse(content.getText().equals(content.getRawText()));
		assertEquals(content.getText().length() + 3, content.getRawText().length());
		assertFalse(content.getText().charAt(0) == content.getRawText().charAt(0));
		assertEquals(content.getText(new ContentRange(1, 5)), content.getRawText(new ContentRange(1, 5)));
		assertEquals(content.getText().substring(0, 5), content.getRawText(new ContentRange(1, 5)));
	}

	@Test
	public void shouldReturnCharacterAtOffset() throws Exception {
		content.insertTagMarker(0);
		content.insertTagMarker(0);
		content.insertText(1, "Hello World");

		for (int i = 0; i < content.length(); i++) {
			assertEquals(content.getRawText().charAt(i), content.charAt(i));
		}
	}
}
