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

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

public class L2DocumentPositionTest {

	private IVexWidget widget;
	private MockHostComponent hostComponent;

	@Before
	public void setUp() throws Exception {
		hostComponent = new MockHostComponent();
		widget = new BaseVexWidget(hostComponent);
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
	}

	@Test
	public void moveToNextWord() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Test Word Another Word");
		widget.moveTo(paraElement.getStartPosition().moveBy(1));
		widget.moveToNextWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(5), widget.getCaretPosition());
	}

	@Test
	public void moveToPreviousWord() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement paraElement = widget.insertElement(PARA);
		widget.insertText("Test Word Another Word");
		widget.moveTo(paraElement.getEndPosition().moveBy(1));
		widget.moveToPreviousWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(19), widget.getCaretPosition());
		widget.moveToPreviousWord(false);
		assertEquals(paraElement.getStartPosition().moveBy(11), widget.getCaretPosition());
	}
}
