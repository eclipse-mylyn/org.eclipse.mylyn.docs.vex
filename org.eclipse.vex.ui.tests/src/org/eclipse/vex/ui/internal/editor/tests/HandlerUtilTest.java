/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.widget.BaseVexWidget;
import org.eclipse.vex.core.internal.widget.CssTableModel;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.core.internal.widget.MockHostComponent;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.ui.internal.handlers.VexHandlerUtil;
import org.junit.Before;
import org.junit.Test;

public class HandlerUtilTest {

	private IVexWidget widget;
	private IElement table;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		final URL url = this.getClass().getResource("/tests/resources/tableTest.css");
		final StyleSheet styleSheet = new StyleSheetReader().read(url);
		widget.setTableModel(new CssTableModel(styleSheet));
		widget.setDocument(new Document(new QualifiedName(null, "root")), styleSheet);
		widget.insertElement(new QualifiedName(null, "root"));
		table = widget.insertElement(new QualifiedName(null, "table"));
	}

	@Test
	public void testGetCurrentTableRow() throws Exception {
		widget.moveTo(table.getStartPosition().moveBy(1));
		widget.insertFragment(new XMLFragment("<tr><td>content</td><td>td2</td></tr>").getDocumentFragment());

		final IElement currentRow = table.childElements().first();
		assertEquals("tr", currentRow.getLocalName());
		widget.moveTo(currentRow.children().first().getStartPosition().moveBy(1));
		assertEquals(currentRow, VexHandlerUtil.getCurrentTableRow(widget));
	}

	@Test
	public void testAddRowAbove() {
		widget.moveTo(table.getStartPosition().moveBy(1));
		widget.insertFragment(new XMLFragment("<tr><td>content</td><td>td2</td></tr>").getDocumentFragment());

		final IElement currentRow = table.childElements().first();
		widget.moveTo(currentRow.children().first().getStartPosition().moveBy(1));
		VexHandlerUtil.duplicateTableRow(widget, currentRow, true);
		final List<? extends IElement> rows = table.childElements().asList();
		assertEquals("Expecting two rows", 2, rows.size());
		assertEquals("Expecting old row on 2nd position", currentRow, rows.get(1));
		assertEquals("Expecting both cells to be created in new row", 2, rows.get(0).childElements().asList().size());
	}

	@Test
	public void testAddRowBelow() {
		widget.moveTo(table.getStartPosition().moveBy(1));
		widget.insertFragment(new XMLFragment("<tr><td>content</td><td>td2</td></tr>").getDocumentFragment());

		final IElement currentRow = table.childElements().first();
		widget.moveTo(currentRow.children().first().getStartPosition().moveBy(1));
		VexHandlerUtil.duplicateTableRow(widget, currentRow, false);
		final List<? extends IElement> rows = table.childElements().asList();
		assertEquals("Expecting two rows", 2, rows.size());
		assertEquals("Expecting old row on 1st position", currentRow, rows.get(0));
		assertEquals("Expecting both cells to be created in new row", 2, rows.get(1).childElements().asList().size());
	}

	@Test
	public void testDuplicateComments() {
		widget.moveTo(table.getStartPosition().moveBy(1));
		widget.insertFragment(new XMLFragment("<tr><td>content</td><td>td2</td><!--comment--></tr>").getDocumentFragment());

		final IElement currentRow = table.childElements().first();
		widget.moveTo(currentRow.children().first().getStartPosition().moveBy(1));
		VexHandlerUtil.duplicateTableRow(widget, currentRow, false);
		final List<? extends IElement> rows = table.childElements().asList();
		assertEquals("comment", rows.get(1).children().last().getText());
	}

	@Test
	public void testDuplicateProcessingInstructions() {
		widget.moveTo(table.getStartPosition().moveBy(1));
		widget.insertFragment(new XMLFragment("<tr><?target data?><td>content</td><td>td2</td></tr>").getDocumentFragment());

		final IElement currentRow = table.childElements().first();
		widget.moveTo(currentRow.children().first().getStartPosition().moveBy(1));
		VexHandlerUtil.duplicateTableRow(widget, currentRow, false);
		final List<? extends IElement> rows = table.childElements().asList();
		assertTrue("Expecting a processing instruction", rows.get(1).children().withoutText().first() instanceof IProcessingInstruction);
		assertEquals("Expecting all childs to be copied", 3, rows.get(1).children().withoutText().asList().size());
		final IProcessingInstruction pi = (IProcessingInstruction) rows.get(1).children().first();
		assertEquals("target", pi.getTarget());
		assertEquals("data", pi.getText());
	}
}
