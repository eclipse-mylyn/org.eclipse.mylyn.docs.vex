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
package org.eclipse.vex.ui.internal.outline.tests;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.outline.DefaultOutlineProvider;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class OutlineProviderTest {

	private DefaultOutlineProvider outlineProvider;

	@Before
	public void setUp() throws Exception {
		final URL url = this.getClass().getResource("/tests/resources/outlineTest.css");
		final StyleSheet styleSheet = new StyleSheetReader().read(url);
		outlineProvider = new DefaultOutlineProvider();
		outlineProvider.init(styleSheet);
	}

	@Test
	public void testContentProvider() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		final IElement parent = doc.insertElement(2, new QualifiedName(null, "parent"));
		doc.insertElement(parent.getEndOffset(), new QualifiedName(null, "child"));
		doc.insertElement(parent.getEndOffset(), new QualifiedName(null, "child"));

		final Object[] outlineElements = outlineProvider.getContentProvider().getElements(doc);
		assertEquals("Count of root elements", 1, outlineElements.length);
		assertEquals(parent, outlineElements[0]);
		final Object[] childElements = outlineProvider.getContentProvider().getChildren(parent);
		assertEquals("Count of child elements", 2, childElements.length);
	}

	@Test
	public void testLabelProviderWithoutContent() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		final IDocumentFragment fragment = new XMLFragment("<parent><title>titleText</title></parent>").getDocumentFragment();
		doc.insertFragment(2, fragment);
		final Object[] outlineElements = outlineProvider.getContentProvider().getElements(doc);
		assertEquals("Count of root elements", 1, outlineElements.length);

		outlineProvider.setState(DefaultOutlineProvider.SHOW_ELEMENT_CONTENT, false);

		final IElement parent = (IElement) outlineElements[0];
		assertEquals("Should return local name", "parent", outlineProvider.getOutlineLabel(parent).getString());

		final IElement title = (IElement) outlineProvider.getContentProvider().getChildren(outlineElements[0])[0];
		assertEquals("Should return local name", "title", outlineProvider.getOutlineLabel(title).getString());
	}

	@Test
	public void testLabelProviderWithContent() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		final IDocumentFragment fragment = new XMLFragment("<parent><title>titleText</title></parent>").getDocumentFragment();
		doc.insertFragment(2, fragment);
		final Object[] outlineElements = outlineProvider.getContentProvider().getElements(doc);
		assertEquals("Count of root elements", 1, outlineElements.length);

		outlineProvider.setState(DefaultOutlineProvider.SHOW_ELEMENT_CONTENT, true);

		final IElement parent = (IElement) outlineElements[0];
		assertEquals("Should return local name:title content", "parent : titleText", outlineProvider.getOutlineLabel(parent).getString());

		final IElement title = (IElement) outlineProvider.getContentProvider().getChildren(outlineElements[0])[0];
		assertEquals("Should return local name:content", "title : titleText", outlineProvider.getOutlineLabel(title).getString());
	}
}
