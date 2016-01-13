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
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getContentStructure;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getCurrentXML;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2CommentEditingTest {

	private IDocumentEditor editor;
	private IElement rootElement;

	@Before
	public void setUp() throws Exception {
		editor = new BaseVexWidget(new MockHostComponent());
		editor.setDocument(createDocumentWithDTD(TEST_DTD, "section"));
		rootElement = editor.getDocument().getRootElement();
	}

	@Test
	public void givenAnElement_whenInsertingAComment_elementShouldContainComment() throws Exception {
		final IComment comment = editor.insertComment();
		assertTrue(rootElement.getRange().contains(comment.getRange()));
		assertSame(rootElement, comment.getParent());
		assertEquals(comment.getEndPosition(), editor.getCaretPosition());
	}

	@Test
	public void givenAnElementWithComment_whenInsertingTextWithinComment_shouldAddTextToComment() throws Exception {
		final IComment comment = editor.insertComment();
		editor.insertText("Hello World");
		assertEquals("Hello World", comment.getText());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingBackspace_shouldDeleteComment() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		final IComment comment = editor.insertComment();
		editor.deleteBackward();
		assertFalse(titleElement.hasChildren());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingDelete_shouldDeleteComment() throws Exception {
		final IElement titleElement = editor.insertElement(TITLE);
		final IComment comment = editor.insertComment();
		editor.deleteForward();
		assertFalse(titleElement.hasChildren());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAComment_whenCaretInComment_shouldNotAllowToInsertAComment() throws Exception {
		editor.insertComment();
		assertFalse("can insert comment within comment", editor.canInsertComment());
	}

	@Test
	public void undoRemoveCommentTag() throws Exception {
		editor.insertElement(TITLE);
		editor.insertText("1text before comment1");
		editor.insertComment();
		final INode comment = editor.getDocument().getChildAt(editor.getCaretPosition());
		editor.insertText("2comment text2");
		editor.moveBy(1);
		editor.insertText("3text after comment3");

		final String expectedContentStructure = getContentStructure(editor.getDocument().getRootElement());

		editor.doWork(new Runnable() {
			@Override
			public void run() {
				editor.moveTo(comment.getStartPosition().moveBy(1), false);
				editor.moveTo(comment.getEndPosition().moveBy(-1), true);
				final IDocumentFragment fragment = editor.getSelectedFragment();
				editor.deleteSelection();

				editor.moveBy(-1, false);
				editor.moveBy(1, true);
				editor.deleteSelection();

				editor.insertFragment(fragment);
			}
		});

		editor.undo();

		assertEquals(expectedContentStructure, getContentStructure(editor.getDocument().getRootElement()));
	}

	@Test
	public void undoRedoInsertCommentWithSubsequentDelete() throws Exception {
		editor.insertElement(PARA);
		final String expectedXml = getCurrentXML(editor);

		final IComment comment = editor.insertComment();
		editor.moveTo(comment.getStartPosition());
		editor.moveTo(comment.getEndPosition(), true);
		editor.deleteSelection();

		editor.undo(); // delete
		editor.undo(); // insert comment

		assertEquals(expectedXml, getCurrentXML(editor));
	}

}
