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

import org.eclipse.vex.core.internal.undo.CannotApplyException;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.junit.Before;
import org.junit.Test;

public class L2ProcessingInstructionEditingTest {

	private IDocumentEditor editor;

	@Before
	public void setUp() throws Exception {
		editor = new BaseVexWidget(new MockHostComponent());
		editor.setDocument(createDocumentWithDTD(TEST_DTD, "section"));
	}

	@Test
	public void insertProcessingInstruction() throws Exception {
		editor.insertElement(TITLE);
		editor.insertProcessingInstruction("target");
		editor.insertText("data");

		editor.moveBy(-1);
		final INode node = editor.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("target", pi.getTarget());
		assertEquals("data", pi.getText());
	}

	@Test(expected = CannotApplyException.class)
	public void shouldNotInsertInvalidProcessingInstruction() throws Exception {
		editor.insertElement(TITLE);
		editor.insertProcessingInstruction(" tar get");
	}

	@Test
	public void undoInsertProcessingInstructionWithSubsequentDelete() throws Exception {
		editor.insertElement(PARA);
		final String expectedXml = getCurrentXML(editor);

		final IProcessingInstruction pi = editor.insertProcessingInstruction("target");

		editor.moveTo(pi.getStartPosition());
		editor.moveTo(pi.getEndPosition(), true);
		editor.deleteSelection();

		editor.undo(); // delete
		editor.undo(); // insert comment

		assertEquals(expectedXml, getCurrentXML(editor));
	}

	@Test
	public void undoRemoveProcessingInstruction() throws Exception {
		editor.insertElement(TITLE);
		editor.insertText("1text before pi");
		final INode pi = editor.insertProcessingInstruction("target");
		editor.insertText("2pi text2");
		editor.moveBy(1);
		editor.insertText("3text after pi");

		final String expectedContentStructure = getContentStructure(editor.getDocument().getRootElement());

		editor.doWork(new Runnable() {
			@Override
			public void run() {
				editor.moveTo(pi.getStartPosition().moveBy(1), false);
				editor.moveTo(pi.getEndPosition().moveBy(-1), true);
				final IDocumentFragment fragment = editor.getSelectedFragment();
				editor.deleteSelection();

				editor.moveBy(-1, false);
				editor.moveBy(1, true);
				editor.deleteSelection();

				editor.insertFragment(fragment);
			}
		});

		editor.undo();

		assertEquals(expectedContentStructure, getContentStructure(editor.getDocument().getRootElement()));
	}

	@Test
	public void editProcessingInstruction() throws Exception {
		editor.insertElement(TITLE);
		editor.insertProcessingInstruction("target");
		editor.insertText("oldData");

		editor.moveBy(-1);
		editor.editProcessingInstruction("new", "newData");

		final INode node = editor.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("new", pi.getTarget());
		assertEquals("newData", pi.getText());
	}

	@Test
	public void undoEditProcessingInstruction() throws Exception {
		editor.insertElement(TITLE);
		editor.insertText("1text before pi");
		final IProcessingInstruction pi = editor.insertProcessingInstruction("target");
		editor.insertText("2data");
		editor.moveBy(1);
		editor.insertText("3text after pi");

		final String expectedContentStructure = getContentStructure(editor.getDocument().getRootElement());

		editor.moveTo(pi.getStartPosition().moveBy(1));
		editor.editProcessingInstruction("new", "data");
		editor.undo();

		assertEquals(expectedContentStructure, getContentStructure(editor.getDocument().getRootElement()));
	}

	@Test
	public void editProcessingInstruction_newTargetOnly() throws Exception {
		editor.insertElement(TITLE);
		editor.insertProcessingInstruction("target");
		editor.insertText("oldData");

		editor.moveBy(-1);
		editor.editProcessingInstruction("new", null);

		final INode node = editor.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("new", pi.getTarget());
		assertEquals("oldData", pi.getText());
	}

	@Test
	public void editProcessingInstruction_newDataOnly() throws Exception {
		editor.insertElement(TITLE);
		editor.insertProcessingInstruction("target");
		editor.insertText("oldData");

		editor.moveBy(-1);
		editor.editProcessingInstruction(null, "newData");

		final INode node = editor.getCurrentNode();
		assertTrue(node instanceof IProcessingInstruction);
		final IProcessingInstruction pi = (IProcessingInstruction) node;
		assertEquals("target", pi.getTarget());
		assertEquals("newData", pi.getText());
	}
}
