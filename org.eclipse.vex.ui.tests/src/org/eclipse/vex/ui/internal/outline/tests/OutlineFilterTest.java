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
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
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
		filter.addFilter(OutlineFilter.FILTER_ID_INLINE_ELEMENTS);
		final IElement inline = new Element("title");
		assertFalse("Should filter inline element", filter.select(null, null, inline));

		final IElement block = new Element("child");
		assertTrue("Should not filter block element", filter.select(null, null, block));
	}
}
