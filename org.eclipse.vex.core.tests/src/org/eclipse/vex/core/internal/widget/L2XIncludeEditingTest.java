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

import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.junit.Before;
import org.junit.Test;

public class L2XIncludeEditingTest {

	private final String includeXml = "<para>12<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"test\" />34</para>";
	private final String includeInlineXml = "<para>12<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"test\" parse=\"text\" />34</para>";

	private IDocumentEditor editor;
	private IElement rootElement;
	private IElement para;
	private IIncludeNode includeNode;

	@Before
	public void setUp() throws Exception {
		final IDocument document = createDocumentWithDTD(TEST_DTD, "section");
		editor = new DocumentEditor(new FakeCursor(document));
		editor.setDocument(document);
	}

	@Test
	public void whenCaretAfterEmptyInclude_BackspaceShouldDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		editor.moveTo(includeNode.getEndPosition().moveBy(1));
		editor.deleteBackward();

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void whenIncludeSelected_ShouldDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		editor.moveTo(includeNode.getStartPosition());
		editor.moveTo(includeNode.getEndPosition(), true);
		editor.deleteSelection();

		final String currentXml = getCurrentXML(editor);
		assertEquals("<section><para>1234</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void deleteInlineIncludeWithSurroundingContent() throws Exception {
		createIncludeElement(includeInlineXml);

		editor.moveTo(includeNode.getStartPosition());
		editor.moveTo(para.getEndPosition().moveBy(-1), true);
		editor.deleteSelection();

		final String currentXml = getCurrentXML(editor);
		assertEquals("<section><para>124</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void deleteInlineIncludeWithSurroundingContent_selectingBackwards() throws Exception {
		createIncludeElement(includeInlineXml);

		// Yes, the direction of the selection makes a difference
		editor.moveTo(para.getEndPosition().moveBy(-1));
		editor.moveTo(includeNode.getStartPosition(), true);

		editor.deleteSelection();

		final String currentXml = getCurrentXML(editor);
		assertEquals("<section><para>124</para></section>", currentXml);

		assertTrue(para.children().withoutText().isEmpty());
		assertFalse(includeNode.isAssociated());
		assertNull(includeNode.getReference().getParent());
	}

	@Test
	public void undoDeleteInclude() throws Exception {
		createIncludeElement(includeXml);
		editor.moveTo(includeNode.getStartPosition());
		editor.moveTo(includeNode.getEndPosition(), true);
		final String exepctedXml = getCurrentXML(editor);
		editor.deleteSelection();
		editor.undo();
		assertEquals(exepctedXml, getCurrentXML(editor));

		para = (IElement) rootElement.children().get(0);
		includeNode = (IIncludeNode) para.children().withoutText().get(0);
		assertTrue(includeNode.isAssociated());
		assertEquals(para, includeNode.getReference().getParent());
	}

	private void createIncludeElement(final String xml) {
		editor.insertFragment(new XMLFragment(xml).getDocumentFragment());
		rootElement = editor.getDocument().getRootElement();
		para = (IElement) rootElement.children().get(0);
		includeNode = (IIncludeNode) para.children().withoutText().get(0);
	}

}
