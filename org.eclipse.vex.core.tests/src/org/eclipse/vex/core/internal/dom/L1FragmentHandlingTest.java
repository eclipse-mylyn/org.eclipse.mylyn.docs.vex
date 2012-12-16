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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L1FragmentHandlingTest {

	private static final QualifiedName VALID_CHILD = new QualifiedName(null, "validChild");
	private static final QualifiedName INVALID_CHILD = new QualifiedName(null, "invalidChild");

	private Document document;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		document = new Document(new Element("root"));
		rootElement = document.getRootElement();
		document.setValidator(new DummyValidator() {
			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
				return "root".equals(element.getLocalName());
			}

			@Override
			public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3,
					final boolean partial) {
				return "root".equals(element.getLocalName()) && containsOnlyValidChildren(sequence2);
			}

			private boolean containsOnlyValidChildren(final List<QualifiedName> sequence) {
				for (final QualifiedName name : sequence) {
					if (!VALID_CHILD.equals(name)) {
						return false;
					}
				}
				return true;
			}
		});
	}

	@Test
	public void shouldIndicateValidInsertionPoint() throws Exception {
		assertTrue(document.canInsertFragment(rootElement.getEndOffset(), createFragment(VALID_CHILD, VALID_CHILD)));
		assertFalse(document.canInsertFragment(rootElement.getEndOffset(), createFragment(INVALID_CHILD, VALID_CHILD)));
		assertFalse(document.canInsertFragment(rootElement.getEndOffset(), createFragment(VALID_CHILD, INVALID_CHILD)));
		assertFalse(document.canInsertFragment(rootElement.getEndOffset(), createFragment(INVALID_CHILD, INVALID_CHILD)));
	}

	@Test
	public void insertFragmentAtValidInsertionPoint() throws Exception {
		document.insertFragment(rootElement.getEndOffset(), createFragment(VALID_CHILD, VALID_CHILD));
		assertEquals(2, rootElement.getChildCount());
		assertEquals(VALID_CHILD, ((Element) rootElement.getChildNode(0)).getQualifiedName());
		assertEquals(VALID_CHILD, ((Element) rootElement.getChildNode(1)).getQualifiedName());
	}

	@Test(expected = DocumentValidationException.class)
	public void cannotInsertFragmentAtInvalidInsertionPoint() throws Exception {
		document.insertFragment(rootElement.getEndOffset(), createFragment(VALID_CHILD, INVALID_CHILD, VALID_CHILD));
	}

	private static DocumentFragment createFragment(final QualifiedName... elementNames) {
		final Content content = new GapContent(10);
		final List<Node> nodes = new ArrayList<Node>();
		for (final QualifiedName elementName : elementNames) {
			final int insertOffset = content.length();
			content.insertElementMarker(insertOffset);
			content.insertElementMarker(insertOffset);
			final Element element = new Element(elementName);
			element.associate(content, new ContentRange(insertOffset, insertOffset + 1));
			nodes.add(element);
		}
		return new DocumentFragment(content, nodes);
	}
}
