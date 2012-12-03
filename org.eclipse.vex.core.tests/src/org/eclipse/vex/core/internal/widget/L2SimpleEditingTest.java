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
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SimpleEditingTest {

	private VexWidgetImpl widget;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void shouldStartInRootElement() throws Exception {
		assertSame(rootElement, widget.getCurrentElement());
		assertEquals(rootElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void shouldMoveCaretIntoInsertedElement() throws Exception {
		final Element titleElement = widget.insertElement(new QualifiedName(null, "title"));
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void shouldProvideInsertionElementAsCurrentElement() throws Exception {
		final Element titleElement = widget.insertElement(new QualifiedName(null, "title"));
		widget.moveBy(-1);
		assertEquals(titleElement.getStartOffset(), widget.getCaretOffset());
		assertSame(rootElement, widget.getCurrentElement());
	}

	@Test
	public void givenAnElementWithText_whenHittingBackspace_shouldDeleteLastCharacter() throws Exception {
		final Element titleElement = widget.insertElement(new QualifiedName(null, "title"));
		widget.insertText("Hello");
		widget.deletePreviousChar();
		assertEquals("Hell", titleElement.getText());
		assertEquals(titleElement.getEndOffset(), widget.getCaretOffset());
	}

	@Test
	public void givenAnElementWithText_whenHittingPos1AndDelete_shouldDeleteFirstCharacter() throws Exception {
		final Element titleElement = widget.insertElement(new QualifiedName(null, "title"));
		widget.insertText("Hello");
		widget.moveBy(-5);
		widget.deleteNextChar();
		assertEquals("ello", titleElement.getText());
		assertEquals(titleElement.getStartOffset() + 1, widget.getCaretOffset());
	}
}
