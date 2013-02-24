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

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getContentStructure;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
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

	private IVexWidget widget;
	private IElement rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void givenAnElement_whenInsertingAComment_elementShouldContainComment() throws Exception {
		final IComment comment = widget.insertComment();
		assertTrue(rootElement.getRange().contains(comment.getRange()));
		assertSame(rootElement, comment.getParent());
		assertEquals(comment.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithComment_whenInsertingTextWithinComment_shouldAddTextToComment() throws Exception {
		final IComment comment = widget.insertComment();
		widget.insertText("Hello World");
		assertEquals("Hello World", comment.getText());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingBackspace_shouldDeleteComment() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		final IComment comment = widget.insertComment();
		widget.deletePreviousChar();
		assertFalse(titleElement.hasChildren());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingDelete_shouldDeleteComment() throws Exception {
		final IElement titleElement = widget.insertElement(TITLE);
		final IComment comment = widget.insertComment();
		widget.deleteNextChar();
		assertFalse(titleElement.hasChildren());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAComment_whenCaretInComment_shouldNotAllowToInsertAComment() throws Exception {
		widget.insertComment();
		assertFalse("can insert comment within comment", widget.canInsertComment());
	}

	@Test
	public void undoRemoveCommentTag() throws Exception {
		widget.insertElement(TITLE);
		widget.insertText("1text before comment1");
		widget.insertComment();
		final INode comment = widget.getDocument().getChildAt(widget.getCaretOffset());
		widget.insertText("2comment text2");
		widget.moveBy(1);
		widget.insertText("3text after comment3");

		final String expectedContentStructure = getContentStructure(widget.getDocument().getRootElement());

		widget.doWork(new Runnable() {
			public void run() {
				widget.moveTo(comment.getStartOffset() + 1, false);
				widget.moveTo(comment.getEndOffset() - 1, true);
				final IDocumentFragment fragment = widget.getSelectedFragment();
				widget.deleteSelection();

				widget.moveBy(-1, false);
				widget.moveBy(1, true);
				widget.deleteSelection();

				widget.insertFragment(fragment);
			}
		});

		widget.undo();

		assertEquals(expectedContentStructure, getContentStructure(widget.getDocument().getRootElement()));
	}

}
