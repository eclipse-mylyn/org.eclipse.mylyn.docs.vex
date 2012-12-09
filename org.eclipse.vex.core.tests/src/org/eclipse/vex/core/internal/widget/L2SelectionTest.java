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

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2SelectionTest {

	private VexWidgetImpl widget;
	private Element rootElement;

	@Before
	public void setUp() throws Exception {
		widget = new VexWidgetImpl(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		rootElement = widget.getDocument().getRootElement();
	}

	@Test
	public void givenCaretInElement_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.moveBy(-1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getStartOffset(), widget.getSelectionStart());
		assertEquals(titleElement.getEndOffset(), widget.getSelectionEnd());
	}

	@Test
	public void givenCaretInElementWith_whenSelectionIncludesStartOffset_shouldExpandSelectionToEndOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(-5, false);
		widget.moveTo(titleElement.getStartOffset(), true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getStartOffset(), widget.getSelectionStart());
		assertEquals(titleElement.getEndOffset(), widget.getSelectionEnd());
	}

	@Test
	public void givenCaretInElementAtEndOffset_whenMovedByOneBehindEndOffset_shouldExpandSelectionToStartOffset() throws Exception {
		final Element titleElement = widget.insertElement(TITLE);
		widget.insertText("Hello World");
		widget.moveBy(1, true);
		assertTrue(widget.hasSelection());
		assertEquals(titleElement.getStartOffset(), widget.getSelectionStart());
		assertEquals(titleElement.getEndOffset() + 1, widget.getSelectionEnd());
	}
}
