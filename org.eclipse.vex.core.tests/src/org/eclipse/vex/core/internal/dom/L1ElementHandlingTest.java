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

import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L1ElementHandlingTest {

	private static final QualifiedName VALID_CHILD = new QualifiedName(null, "validChild");
	private static final QualifiedName INVALID_CHILD = new QualifiedName(null, "invalidChild");

	private Document document;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "root"));
		rootElement = document.getRootElement();
		document.setValidator(new DummyValidator() {
			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
				return "root".equals(element.getLocalName());
			}

			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3,
					final boolean partial) {
				return "root".equals(element.getLocalName()) && VALID_CHILD.equals(sequence2.get(0));
			}
		});
	}

	@Test
	public void shouldIndicateValidInsertionPoint() throws Exception {
		assertFalse(document.canInsertElement(rootElement.getStartOffset(), VALID_CHILD));
		assertTrue(document.canInsertElement(rootElement.getEndOffset(), VALID_CHILD));
	}

	@Test
	public void insertElementAtValidInsertionPoint() throws Exception {
		final IContent content = document.getContent();
		final int contentLengthBefore = content.length();
		final Element newElement = document.insertElement(rootElement.getEndOffset(), VALID_CHILD);
		assertEquals("validChild", newElement.getLocalName());
		assertSame(rootElement, newElement.getParent());
		assertEquals(contentLengthBefore + 2, content.length());
		assertSame(content, newElement.getContent());
		assertEquals(new ContentRange(2, 3), newElement.getRange());
	}

	@Test(expected = DocumentValidationException.class)
	public void cannotInsertInvalidElement() throws Exception {
		document.insertElement(rootElement.getEndOffset(), INVALID_CHILD);
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotInsertElementBeforeRootElement() throws Exception {
		document.insertElement(rootElement.getStartOffset(), VALID_CHILD);
	}

	@Test(expected = AssertionFailedException.class)
	public void cannotInsertElementAfterRootElement() throws Exception {
		document.insertElement(rootElement.getEndOffset() + 1, VALID_CHILD);
	}

	@Test(expected = DocumentValidationException.class)
	public void shouldNotDeleteRootElement() throws Exception {
		document.delete(rootElement.getRange());
	}
}
