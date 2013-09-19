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
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getContentStructure;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.getCurrentXML;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.undo.CannotRedoException;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.junit.Before;
import org.junit.Test;

public class L2ProcessingInstructionEditingTest {

	private IVexWidget widget;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), StyleSheet.NULL);
	}

	@Test
	public void insertProcessingInstruction() throws Exception {
		widget.insertElement(TITLE);
		widget.insertProcessingInstruction("target");
		widget.insertText("data");

		widget.moveBy(-1);
		final INode node = widget.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("target", pi.getTarget());
		assertEquals("data", pi.getText());
	}

	@Test(expected = CannotRedoException.class)
	public void shouldNotInsertInvalidProcessingInstruction() throws Exception {
		widget.insertElement(TITLE);
		widget.insertProcessingInstruction(" tar get");
	}

	@Test
	public void undoInsertProcessingInstructionWithSubsequentDelete() throws Exception {
		widget.insertElement(PARA);
		final String expectedXml = getCurrentXML(widget);

		final IProcessingInstruction pi = widget.insertProcessingInstruction("target");

		widget.moveTo(pi.getStartOffset());
		widget.moveTo(pi.getEndOffset(), true);
		widget.deleteSelection();

		widget.undo(); // delete
		widget.undo(); // insert comment

		assertEquals(expectedXml, getCurrentXML(widget));
	}

	@Test
	public void undoRemoveProcessingInstruction() throws Exception {
		widget.insertElement(TITLE);
		widget.insertText("1text before pi");
		final INode pi = widget.insertProcessingInstruction("target");
		widget.insertText("2pi text2");
		widget.moveBy(1);
		widget.insertText("3text after pi");

		final String expectedContentStructure = getContentStructure(widget.getDocument().getRootElement());

		widget.doWork(new Runnable() {
			@Override
			public void run() {
				widget.moveTo(pi.getStartOffset() + 1, false);
				widget.moveTo(pi.getEndOffset() - 1, true);
				final IDocumentFragment fragment = widget.getSelectedFragment();
				widget.deleteSelection();

				widget.moveBy(-1, false);
				widget.moveBy(1, true);
				widget.deleteSelection();

				widget.insertFragment(fragment);
			}
		});

		widget.undo();

		assertEquals(expectedContentStructure, getContentStructure(widget.getDocument().getRootElement()));
	}

	@Test
	public void editProcessingInstruction() throws Exception {
		widget.insertElement(TITLE);
		widget.insertProcessingInstruction("target");
		widget.insertText("oldData");

		widget.moveBy(-1);
		widget.editProcessingInstruction("new", "newData");

		final INode node = widget.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("new", pi.getTarget());
		assertEquals("newData", pi.getText());
	}

	@Test
	public void undoEditProcessingInstruction() throws Exception {
		widget.insertElement(TITLE);
		widget.insertText("1text before pi");
		final IProcessingInstruction pi = widget.insertProcessingInstruction("target");
		widget.insertText("2data");
		widget.moveBy(1);
		widget.insertText("3text after pi");

		final String expectedContentStructure = getContentStructure(widget.getDocument().getRootElement());

		widget.moveTo(pi.getStartOffset() + 1);
		widget.editProcessingInstruction("new", "data");
		widget.undo();

		assertEquals(expectedContentStructure, getContentStructure(widget.getDocument().getRootElement()));
	}

	@Test
	public void editProcessingInstruction_newTargetOnly() throws Exception {
		widget.insertElement(TITLE);
		widget.insertProcessingInstruction("target");
		widget.insertText("oldData");

		widget.moveBy(-1);
		widget.editProcessingInstruction("new", null);

		final INode node = widget.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("new", pi.getTarget());
		assertEquals("oldData", pi.getText());
	}

	@Test
	public void editProcessingInstruction_newDataOnly() throws Exception {
		widget.insertElement(TITLE);
		widget.insertProcessingInstruction("target");
		widget.insertText("oldData");

		widget.moveBy(-1);
		widget.editProcessingInstruction(null, "newData");

		final INode node = widget.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("target", pi.getTarget());
		assertEquals("newData", pi.getText());
	}
}
