/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L1ProcessingInstructionHandlingTest {

	private static final QualifiedName VALID_CHILD = new QualifiedName(null, "validChild");

	private IDocument document;
	private IElement rootElement;
	private IElement titleElement;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		rootElement = document.getRootElement();
		titleElement = document.insertElement(2, new QualifiedName(null, "title"));
	}

	@Test
	public void shouldIndicateValidInsertionPoints() throws Exception {
		assertFalse(document.canInsertProcessingInstruction(document.getStartOffset(), null));
		assertTrue(document.canInsertProcessingInstruction(rootElement.getStartOffset(), null));
		assertTrue(document.canInsertProcessingInstruction(titleElement.getStartOffset(), null));
		assertTrue(document.canInsertProcessingInstruction(titleElement.getEndOffset(), null));
		assertTrue(document.canInsertProcessingInstruction(rootElement.getEndOffset(), null));
		assertTrue(document.canInsertProcessingInstruction(document.getEndOffset(), null));
		assertFalse(document.canInsertProcessingInstruction(document.getEndOffset() + 1, null));
	}

	@Test
	public void shouldInsertProcessingInstructionAtValidInsertionPoint() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(titleElement.getStartOffset(), "pi");

		assertSame(rootElement, pi.getParent());
		assertTrue(pi.isAssociated());
		final Iterator<INode> actualChildren = rootElement.children().iterator();
		assertSame(pi, actualChildren.next());
		assertSame(titleElement, actualChildren.next());
	}

	@Test
	public void shouldInsertTextIntoProcessingInstruction() throws Exception {
		// text may only be inserted in the processing instruction and in the title element
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
		final IProcessingInstruction pi = document.insertProcessingInstruction(titleElement.getStartOffset(), "pi");
		document.insertText(pi.getEndOffset(), "data=test");

		assertEquals("data=test", pi.getText());
		assertEquals("pi", pi.getTarget());
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotAcceptTargetStartingWithXML() throws Exception {
		document.insertProcessingInstruction(rootElement.getStartOffset(), "XmL");
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotAcceptTargetWithLeadingWhitespace() throws Exception {
		document.insertProcessingInstruction(rootElement.getStartOffset(), " target");
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotAcceptTargetWithInnerWhitespace() throws Exception {
		document.insertProcessingInstruction(rootElement.getStartOffset(), "tar get");
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotAcceptEndTagInTarget() throws Exception {
		document.insertProcessingInstruction(rootElement.getStartOffset(), "tar?>get");
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotInsertEndTagInProcessingInstruction() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(rootElement.getStartOffset(), "");
		document.insertText(pi.getEndOffset(), "Test?>After");
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotInsertProcessingInstructionAtInvalidInsertionPoint() throws Exception {
		document.insertProcessingInstruction(document.getStartOffset(), "target");
	}

	@Test
	public void shouldInsertProcesingInstructionBeforeRootElement() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(rootElement.getStartOffset(), "pi");
		final Iterator<INode> actualChildren = document.children().iterator();
		assertSame(pi, actualChildren.next());
		assertSame(rootElement, actualChildren.next());
		assertFalse(actualChildren.hasNext());
	}

	@Test
	public void givenInvalidTarget_shouldIndicateInvalidInsertion() throws Exception {
		assertFalse(document.canInsertProcessingInstruction(titleElement.getStartOffset(), "invalid target"));
	}

	@Test
	public void shouldIndicateInvalidInsertionInProcessingInstruction() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(titleElement.getStartOffset(), "pi");
		assertFalse(document.canInsertProcessingInstruction(pi.getEndOffset(), null));
	}

	@Test
	public void shouldIndicateInvalidInsertionInComments() throws Exception {
		final IComment comment = document.insertComment(titleElement.getStartOffset());
		assertFalse(document.canInsertProcessingInstruction(comment.getEndOffset(), null));
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotInsertElementInProcessingInstruction() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(titleElement.getStartOffset(), "pi");
		document.insertElement(pi.getEndOffset(), VALID_CHILD);
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotInsertProcessingInstructionInProcessingInstruction() throws Exception {
		final IProcessingInstruction pi = document.insertProcessingInstruction(titleElement.getStartOffset(), "pi");
		document.insertProcessingInstruction(pi.getEndOffset(), "target");
	}
}
