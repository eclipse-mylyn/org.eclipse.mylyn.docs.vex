/*******************************************************************************
 * Copyright (c) 2015 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;

import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

public class L2DocumentPositionTest {

	private IDocumentEditor editor;

	@Before
	public void setUp() throws Exception {
		editor = new BaseVexWidget(new MockHostComponent());
		editor.setDocument(createDocumentWithDTD(TEST_DTD, "section"));
	}

	@Test
	public void moveToNextWord() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Test Word Another Word");
		editor.moveTo(paraElement.getStartPosition().moveBy(1));
		editor.moveToNextWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(5), editor.getCaretPosition());
	}

	@Test
	public void moveToPreviousWord() throws Exception {
		editor.insertElement(TITLE);
		editor.moveBy(1);
		final IElement paraElement = editor.insertElement(PARA);
		editor.insertText("Test Word Another Word");
		editor.moveTo(paraElement.getEndPosition().moveBy(1));
		editor.moveToPreviousWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(19), editor.getCaretPosition());
		editor.moveToPreviousWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(11), editor.getCaretPosition());
	}
}
