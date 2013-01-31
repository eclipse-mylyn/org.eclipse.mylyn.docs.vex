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
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2CommentEditingTest {

	private VexWidgetImpl widget;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void givenAnElement_whenInsertingAComment_elementShouldContainComment() throws Exception {
		final Comment comment = widget.insertComment();
		assertTrue(rootElement.getRange().contains(comment.getRange()));
		assertSame(rootElement, comment.getParent());
		assertEquals(comment.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithComment_whenInsertingTextWithinComment_shouldAddTextToComment() throws Exception {
		final Comment comment = widget.insertComment();
		widget.insertText("Hello World");
		assertEquals("Hello World", comment.getText());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingBackspace_shouldDeleteComment() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		final Comment comment = widget.insertComment();
		widget.deletePreviousChar();
		assertFalse(titleElement.hasChildren());
		assertFalse(comment.isAssociated());
		assertNull(comment.getParent());
	}

	@Test
	public void givenAnEmptyComment_whenCaretInCommentAndHittingDelete_shouldDeleteComment() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		final Comment comment = widget.insertComment();
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
}
