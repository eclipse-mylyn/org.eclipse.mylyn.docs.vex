/*******************************************************************************
 * Copyright (c) 2012, 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

public class L1DeletionTests {

	private IDocument document;
	private IElement child;
	private IElement grandchild;
	private IElement sibling;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		child = document.insertElement(2, new QualifiedName(null, "child"));
		document.insertText(child.getEndOffset(), "12345");
		grandchild = document.insertElement(child.getEndOffset(), new QualifiedName(null, "grandchild"));
		document.insertText(grandchild.getEndOffset(), "ABCDEF");
		document.insertText(child.getEndOffset(), "67890");
		sibling = document.insertElement(document.getRootElement().getEndOffset(), new QualifiedName(null, "sibling"));
	}

	@Test
	public void shouldDeleteRangeWithTextOnly() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset() + 2, grandchild.getEndOffset() - 2));
		assertEquals("Text content after deletion", "AF", grandchild.getText());
	}

	@Test
	public void shouldDeleteChildWhenRangeEqualsChildRange() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset(), grandchild.getEndOffset()));
		assertEquals("Text content after deletion", "1234567890", child.getText());
		assertTrue("Child should be deleted", child.children().withoutText().count() == 0);
	}

	@Test
	public void shouldDeleteChildWhenRangeStartsAtChildStartPos() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset(), child.getEndOffset() - 1));
		assertEquals("Text content after deletion", "12345", child.getText());
		assertTrue("Child should be deleted", child.children().withoutText().count() == 0);
	}

	@Test
	public void shouldDeleteChildWhenRangeEndsAtChildEndPos() throws Exception {
		document.delete(new ContentRange(child.getStartOffset() + 1, grandchild.getEndOffset()));
		assertEquals("Text content after deletion", "67890", child.getText());
		assertTrue("Child should be deleted", child.children().withoutText().count() == 0);
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotDeleteUnbalancedRange() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset() + 1, child.getEndOffset() - 1));
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotDeleteUnbalancedRangeWhenRangeEndsAtEndOffset() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset() + 1, grandchild.getEndOffset()));
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotDeleteUnbalancedRangeWhenRangeStartsAtStartOffset() throws Exception {
		document.delete(new ContentRange(grandchild.getStartOffset(), grandchild.getEndOffset() - 1));
	}

	@Test
	public void shouldDeleteMultipleElementsAtOnce() throws Exception {
		document.delete(child.getRange().union(sibling.getRange()));
		assertTrue("no more children", document.getRootElement().children().withoutText().isEmpty());
	}
}
