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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.junit.Before;
import org.junit.Test;

public class InMemoryClipboardTest {

	private UniversalTestDocument document;
	private DocumentEditor editor;
	private InMemoryClipboard clipboard;

	@Before
	public void setUp() throws Exception {
		document = new UniversalTestDocument(1);
		editor = new DocumentEditor(new FakeCursor(document.getDocument()));
		editor.setDocument(document.getDocument());
		clipboard = new InMemoryClipboard();
	}

	@Test
	public void givenFirstParagraphSelected_cutSelection_shouldRemoveFirstParagraphFromDocument() throws Exception {
		final IElement section = document.getSection(0);
		final INode firstParagraph = section.children().first();

		select(firstParagraph);
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

		select(firstParagraph);
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

		select(firstParagraph);
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

		select(firstParagraph);
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

		select(firstParagraph);
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

		select(firstParagraph);
		clipboard.copySelection(editor);
		editor.moveTo(secondParagraph.getEndPosition());
		clipboard.pasteText(editor);

		assertEquals(firstParagraphContent, section.children().last().getText());
		assertEquals(2, section.children().count());
	}

	private void select(final INode node) {
		editor.moveTo(node.getStartPosition());
		editor.moveBy(1, true);
	}
}
