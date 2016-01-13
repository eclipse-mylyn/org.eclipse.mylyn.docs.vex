/*******************************************************************************
 * Copyright (c) 2012, 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *      Carsten Hiesserich - additional tests
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SelectionTest {

	private IDocumentEditor editor;

	@Before
	public void setUp() throws Exception {
		final IDocument document = createDocumentWithDTD(TEST_DTD, "section");
		editor = new DocumentEditor(new FakeCursor(document));
		editor.setDocument(document);
	}

	@Test
	public void givenNodeInDocument_whenNodeHasContent_shouldSelectContentOfNode() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("Hello World");

		editor.selectContentOf(title);

		assertEquals(title.getRange().resizeBy(1, 0), editor.getSelectedRange());
		assertTrue(editor.hasSelection());
	}

	@Test
	public void givenNodeInDocument_whenNodeIsEmpty_shouldMoveCaretIntoNode() throws Exception {
		final IElement title = editor.insertElement(TITLE);

		editor.selectContentOf(title);

		assertEquals(title.getEndPosition(), editor.getCaretPosition());
		assertFalse(editor.hasSelection());
	}

	@Test
	public void givenNodeInDocument_shouldSelectCompleteNodeWithStartAndEndTags() throws Exception {
		final IElement title = editor.insertElement(TITLE);
		editor.insertText("Hello World");

		editor.select(title);

		assertEquals(title.getRange().resizeBy(0, 1), editor.getSelectedRange());
		assertTrue(editor.hasSelection());
	}
}
