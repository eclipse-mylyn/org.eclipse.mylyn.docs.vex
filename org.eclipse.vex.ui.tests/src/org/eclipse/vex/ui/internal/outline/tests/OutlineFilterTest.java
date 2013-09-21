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
package org.eclipse.vex.ui.internal.outline.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.ProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.ui.internal.outline.OutlineFilter;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class OutlineFilterTest {

	private OutlineFilter filter;

	@Before
	public void setUp() throws Exception {
		final URL url = this.getClass().getResource("/tests/resources/outlineTest.css");
		final StyleSheet styleSheet = new StyleSheetReader().read(url);
		filter = new OutlineFilter(styleSheet);
	}

	@Test
	public void testFilterShouldAcceptAllNodes() throws Exception {
		final IComment comment = new Comment();
		assertFalse("Should filter unknown node types", filter.select(null, null, comment));
	}

	@Test
	public void testInlineElementFilter() throws Exception {
		final IElement inline = new Element("title");
		assertFalse("Should filter inline element", filter.select(null, null, inline));

		filter.addFilter(OutlineFilter.FILTER_ID_INCLUDE_INLINE_ELEMENTS);
		final IElement block = new Element("child");
		assertTrue("Should not filter block element", filter.select(null, null, block));
	}

	@Test
	public void testCommentFilter() throws Exception {
		final IComment comment = new Comment();
		assertFalse("Should filter comment", filter.select(null, null, comment));

		filter.addFilter(OutlineFilter.FILTER_ID_INCLUDE_COMMENTS);
		assertTrue("Should not filter comment when enabled", filter.select(null, null, comment));
	}

	@Test
	public void testProcessingInstrFilter() throws Exception {
		final IProcessingInstruction pi = new ProcessingInstruction("target");
		assertFalse("Should filter processing instr.", filter.select(null, null, pi));

		filter.addFilter(OutlineFilter.FILTER_ID_INCLUDE_PROC_INSTR);
		assertTrue("Should not filter processing instr. when enabled", filter.select(null, null, pi));
	}
}
