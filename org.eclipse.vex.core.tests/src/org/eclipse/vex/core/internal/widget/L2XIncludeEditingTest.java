/*******************************************************************************
 * Copyright (c) 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getCurrentXML;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class L2XIncludeEditingTest {

	private final String includeXml = "<para>12<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"test\" />34</para>";
	private final String includeInlineXml = "<para>12<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"test\" parse=\"text\" />34</para>";

	private IVexWidget widget;
	private IElement rootElement;
	private IElement para;
	private IIncludeNode includeNode;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
	}

	@Test
	public void whenCaretAfterEmptyInclude_BackspaceShouldDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		widget.moveTo(includeNode.getEndPosition().moveBy(1));
		widget.deletePreviousChar();

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void whenIncludeSelected_ShouldDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		widget.moveTo(includeNode.getStartPosition());
		widget.moveTo(includeNode.getEndPosition(), true);
		widget.deleteSelection();

		final String currentXml = getCurrentXML(widget);
		assertEquals("<section><para>1234</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void deleteInlineIncludeWithSurroundingContent() throws Exception {
		createIncludeElement(includeInlineXml);

		widget.moveTo(includeNode.getStartPosition());
		widget.moveTo(para.getEndPosition().moveBy(-1), true);
		widget.deleteSelection();

		final String currentXml = getCurrentXML(widget);
		assertEquals("<section><para>124</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void deleteInlineIncludeWithSurroundingContent_selectingBackwards() throws Exception {
		createIncludeElement(includeInlineXml);

		// Yes, the direction of the selection makes a difference
		widget.moveTo(para.getEndPosition().moveBy(-1));
		widget.moveTo(includeNode.getStartPosition(), true);

		widget.deleteSelection();

		final String currentXml = getCurrentXML(widget);
		assertEquals("<section><para>124</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void undoDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		widget.moveTo(includeNode.getStartPosition());
		widget.moveTo(includeNode.getEndPosition(), true);
		final String exepctedXml = getCurrentXML(widget);
		widget.deleteSelection();
		widget.undo();
		assertEquals(exepctedXml, getCurrentXML(widget));

		para = (IElement) rootElement.children().get(0);
		includeNode = (IIncludeNode) para.children().withoutText().get(0);
		assertTrue(includeNode.isAssociated());
		assertEquals(para, includeNode.getReference().getParent());
	}

	private void createIncludeElement(final String xml) {
		widget.insertFragment(new XMLFragment(xml).getDocumentFragment());
		rootElement = widget.getDocument().getRootElement();
		para = (IElement) rootElement.children().get(0);
		includeNode = (IIncludeNode) para.children().withoutText().get(0);
	}

}
