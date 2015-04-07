/*******************************************************************************
 * Copyright (c) 2013, 2015 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich  - initial API and implementation
 *******************************************************************************/

package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;
import org.junit.Before;
import org.junit.Test;

public class DocumentEventTest {

	private IDocument document;
	private IElement childNode;
	private ContentChangeEvent contentChangeEvent = null;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		childNode = document.insertElement(2, new QualifiedName(null, "child"));
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void attributeChanged(final AttributeChangeEvent event) {

			}

			@Override
			public void namespaceChanged(final NamespaceDeclarationChangeEvent event) {

			}

			@Override
			public void beforeContentDeleted(final ContentChangeEvent event) {

			}

			@Override
			public void beforeContentInserted(final ContentChangeEvent event) {

			}

			@Override
			public void contentDeleted(final ContentChangeEvent event) {
				contentChangeEvent = event;
			}

			@Override
			public void contentInserted(final ContentChangeEvent event) {
				contentChangeEvent = event;
			}

		});
	}

	@Test
	public void givenEmptyElement_whenInsertingText_shouldIndicateStructuralChange() throws Exception {
		document.insertText(childNode.getEndOffset(), "Hello World");
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertTrue("Expecting structural change", contentChangeEvent.isStructuralChange());
	}

	@Test
	public void givenElementWithText_whenInsertingText_shouldNotIndicateStructuralChange() throws Exception {
		document.insertText(childNode.getEndOffset(), "Some Text");
		document.insertText(childNode.getEndOffset(), "Hello World");
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertFalse("Expecting no structural change", contentChangeEvent.isStructuralChange());
	}

	@Test
	public void testInsertElement() throws Exception {
		document.insertElement(childNode.getEndOffset(), new QualifiedName(null, "subchild"));
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertTrue("Expecting structural change", contentChangeEvent.isStructuralChange());
		assertEquals(childNode, contentChangeEvent.getParent());
	}

	@Test
	public void testDeleteText() throws Exception {
		document.insertText(childNode.getEndOffset(), "Hello World");
		contentChangeEvent = null;
		document.delete(new ContentRange(childNode.getRange().getStartOffset() + 1, childNode.getRange().getStartOffset() + 6));
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertFalse("Expecting no structural change", contentChangeEvent.isStructuralChange());
		assertEquals(childNode, contentChangeEvent.getParent());
	}

	@Test
	public void testDeleteNodeWithText() throws Exception {
		document.insertText(childNode.getEndOffset(), "Hello World");
		contentChangeEvent = null;
		document.delete(childNode.getRange());
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertTrue("Expecting structural change", contentChangeEvent.isStructuralChange());
		assertEquals(document.getRootElement(), contentChangeEvent.getParent());
	}

	@Test
	public void testInsertTextFragment() throws Exception {
		final IDocumentFragment fragment = new XMLFragment("Hello World").getDocumentFragment();
		document.insertFragment(childNode.getStartOffset() + 1, fragment);
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertFalse("Expecting no structural change", contentChangeEvent.isStructuralChange());
		assertEquals(childNode, contentChangeEvent.getParent());
	}

	@Test
	public void testInsertFragmentWithNode() throws Exception {
		final IDocumentFragment fragment = new XMLFragment("<child>Hello World</child>").getDocumentFragment();
		document.insertFragment(childNode.getStartOffset() + 1, fragment);
		assertNotNull("Expecting ContentChangeEvent", contentChangeEvent);
		assertTrue("Expecting structural change", contentChangeEvent.isStructuralChange());
		assertEquals(childNode, contentChangeEvent.getParent());
	}

}
