/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.visualization.DocumentRootVisualization;
import org.eclipse.vex.core.internal.visualization.ParagraphVisualization;
import org.eclipse.vex.core.internal.visualization.StructureElementVisualization;
import org.eclipse.vex.core.internal.visualization.TextVisualization;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestCursorPosition {

	private RootBox rootBox;
	private ContentMap contentMap;
	private CursorPosition cursorPosition;

	@Before
	public void setUp() throws Exception {
		rootBox = createTestModel();
		contentMap = new ContentMap();
		contentMap.setRootBox(rootBox);
		cursorPosition = new CursorPosition(contentMap);
	}

	@Test
	public void canMoveCursorOneCharacterLeft() throws Exception {
		cursorPosition.setOffset(5);
		cursorPosition.left();
		assertEquals(4, cursorPosition.getOffset());
	}

	@Test
	public void whenAtFirstPosition_cannotMoveCursorOneCharacterLeft() throws Exception {
		cursorPosition.setOffset(0);
		cursorPosition.left();
		assertEquals(0, cursorPosition.getOffset());
	}

	@Test
	public void canMoveCursorOneCharacterRight() throws Exception {
		cursorPosition.setOffset(5);
		cursorPosition.right();
		assertEquals(6, cursorPosition.getOffset());
	}

	@Test
	public void whenAtLastPosition_cannotMoveCursorOneCharacterRight() throws Exception {
		cursorPosition.setOffset(37);
		cursorPosition.right();
		assertEquals(37, cursorPosition.getOffset());
	}

	private static RootBox createTestModel() {
		final IDocument document = createTestDocument();
		final VisualizationChain visualizationChain = buildVisualizationChain();
		return visualizationChain.visualizeRoot(document);
	}

	private static VisualizationChain buildVisualizationChain() {
		final VisualizationChain visualizationChain = new VisualizationChain();
		visualizationChain.addForRoot(new DocumentRootVisualization());
		visualizationChain.addForStructure(new ParagraphVisualization());
		visualizationChain.addForStructure(new StructureElementVisualization());
		visualizationChain.addForInline(new TextVisualization());
		return visualizationChain;
	}

	private static IDocument createTestDocument() {
		final Document document = new Document(new QualifiedName(null, "doc"));
		insertSection(document.getRootElement());
		insertSection(document.getRootElement());
		return document;
	}

	private static void insertSection(final IParent parent) {
		final IElement section = insertElement(parent, "section");
		insertText(insertElement(section, "para"), "LOREM IPSUM");
		insertElement(section, "para");
	}

	private static IElement insertElement(final IParent parent, final String localName) {
		final IDocument document = parent.getDocument();
		return document.insertElement(parent.getEndOffset(), new QualifiedName(null, localName));
	}

	private static void insertText(final IParent parent, final String text) {
		final IDocument document = parent.getDocument();
		document.insertText(parent.getEndOffset(), text);
	}
}
