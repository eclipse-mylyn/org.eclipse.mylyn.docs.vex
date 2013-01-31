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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L1CommentHandlingTest {

	private Document document;
	private Element rootElement;
	private Element titleElement;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		rootElement = document.getRootElement();
		titleElement = document.insertElement(2, new QualifiedName(null, "title"));
	}

	@Test
	public void shouldIndicateValidCommentInsertionPoints() throws Exception {
		assertFalse(document.canInsertComment(document.getStartOffset()));
		assertTrue(document.canInsertComment(rootElement.getStartOffset()));
		assertTrue(document.canInsertComment(titleElement.getStartOffset()));
		assertTrue(document.canInsertComment(titleElement.getEndOffset()));
		assertTrue(document.canInsertComment(rootElement.getEndOffset()));
		assertTrue(document.canInsertComment(document.getEndOffset()));
		assertFalse(document.canInsertComment(document.getEndOffset() + 1));
	}

	@Test
	public void shouldInsertCommentAtValidInsertionPoint() throws Exception {
		final Comment comment = document.insertComment(titleElement.getStartOffset());

		assertSame(rootElement, comment.getParent());
		assertTrue(comment.isAssociated());
		final Iterator<Node> actualChildren = rootElement.children().iterator();
		assertSame(comment, actualChildren.next());
		assertSame(titleElement, actualChildren.next());
	}

	@Test
	public void shouldInsertTextIntoComment() throws Exception {
		// text may only be inserted in the comment and in the title element
		document.setValidator(new DummyValidator() {
			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
				return "title".equals(element.getLocalName());
			}

			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3,
					final boolean partial) {
				return "title".equals(element.getLocalName());
			}
		});
		final Comment comment = document.insertComment(titleElement.getStartOffset());
		document.insertText(comment.getEndOffset(), "Hello World");

		assertEquals("Hello World", comment.getText());
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotInsertCommentAtInvalidInsertionPoint() throws Exception {
		document.insertComment(document.getStartOffset());
	}

	@Test
	public void shouldInsertCommentBeforeRootElement() throws Exception {
		final Comment comment = document.insertComment(rootElement.getStartOffset());
		final Iterator<Node> actualChildren = document.children().iterator();
		assertSame(comment, actualChildren.next());
		assertSame(rootElement, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}
}
