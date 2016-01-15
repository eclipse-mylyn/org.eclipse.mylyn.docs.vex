/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class BaseClipboardTest {

	private UniversalTestDocument document;
	private DocumentEditor editor;
	private IClipboard clipboard;

	protected abstract IClipboard createClipboard();

	@Before
	public void setUp() throws Exception {
		document = new UniversalTestDocument(1);
		editor = new DocumentEditor(new FakeCursor(document.getDocument()));
		editor.setDocument(document.getDocument());
		clipboard = createClipboard();
	}

	@After
	public void disposeClipboard() {
		clipboard.dispose();
	}

	@Test
	public void givenFirstParagraphSelected_cutSelection_shouldRemoveFirstParagraphFromDocument() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();

		editor.select(firstParagraph);
		clipboard.cutSelection(editor);

		assertNotSame(firstParagraph, section.children().first());
		assertNull(firstParagraph.getDocument());
	}

	@Test
	public void givenFirstParagraphSelected_cutAndPasteAfterSecondParagraph_shouldInsertFirstParagraphAfterSecondParagraph() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();
		final INode secondParagraph = section.children().last();
		final String firstParagraphContent = firstParagraph.getText();

		editor.select(firstParagraph);
		clipboard.cutSelection(editor);
		editor.moveTo(secondParagraph.getEndPosition().moveBy(1));
		clipboard.paste(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(2, section.children().count());
	}

	@Test
	public void givenFirstParagraphSelected_copyAndPasteAfterSecondParagraph_shouldInsertFirstParagraphAgainAfterSecondParagraph() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();
		final INode secondParagraph = section.children().last();
		final String firstParagraphContent = firstParagraph.getText();

		editor.select(firstParagraph);
		clipboard.copySelection(editor);
		editor.moveTo(secondParagraph.getEndPosition().moveBy(1));
		clipboard.paste(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(3, section.children().count());
	}

	@Test
	public void givenNothingSelected_cutSelection_shouldNotChangeClipboardContent() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();
		final INode secondParagraph = section.children().last();
		final String firstParagraphContent = firstParagraph.getText();

		editor.select(firstParagraph);
		clipboard.copySelection(editor);
		editor.moveTo(secondParagraph.getEndPosition().moveBy(1));
		clipboard.cutSelection(editor);
		clipboard.paste(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(3, section.children().count());
	}

	@Test
	public void givenNothingSelected_copySelection_shouldNotChangeClipboardContent() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();
		final INode secondParagraph = section.children().last();
		final String firstParagraphContent = firstParagraph.getText();

		editor.select(firstParagraph);
		clipboard.copySelection(editor);
		editor.moveTo(secondParagraph.getEndPosition().moveBy(1));
		clipboard.copySelection(editor);
		clipboard.paste(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(3, section.children().count());
	}

	@Test
	public void givenFirstParagraphSelected_copyAndPasteTextIntoSecondParagraph_shouldInsertTextOfFirstParagraphIntoSecondParagraph() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();
		final INode secondParagraph = section.children().last();
		final String firstParagraphContent = firstParagraph.getText();

		editor.select(firstParagraph);
		clipboard.copySelection(editor);
		editor.moveTo(secondParagraph.getEndPosition());
		clipboard.pasteText(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(2, section.children().count());
	}

	@Test
	public void givenFirstParagraphSelected_copySelection_shouldIndicateContentAndTextContentAvailable() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();

		editor.select(firstParagraph);
		clipboard.copySelection(editor);

		assertTrue("hasContent", clipboard.hasContent());
		assertTrue("hasTextContent", clipboard.hasTextContent());
	}

	@Test
	public void givenContentOfFirstParagraphSelected_copySelection_shouldIndicateContentAndTextContentAvailable() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();

		editor.selectContentOf(firstParagraph);
		clipboard.copySelection(editor);

		assertTrue("hasContent", clipboard.hasContent());
		assertTrue("hasTextContent", clipboard.hasTextContent());
	}

	@Test
	public void givenSecondParagraphSelected_copySelection_shouldIndicateContentButNotTextContentAvailable() throws Exception {
		final IElement section = document.getSection(0);
		final INode secondParagraph = section.children().last();

		editor.select(secondParagraph);
		clipboard.copySelection(editor);

		assertTrue("hasContent", clipboard.hasContent());
		assertFalse("hasTextContent", clipboard.hasTextContent());
	}
}
