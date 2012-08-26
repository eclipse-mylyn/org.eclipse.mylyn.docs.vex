/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;

public class TestBlockElementBox extends TestCase {

	FakeGraphics g;
	LayoutContext context;

	public TestBlockElementBox() throws Exception {
		final URL url = this.getClass().getResource("test.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);

		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new MockBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(ss);
	}

	public void testBeforeAfter() throws Exception {
		final Element root = new Element("root");
		final Document doc = new Document(root);
		doc.insertElement(1, new Element("beforeBlock"));
		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, root, 500);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		Box[] children;
		BlockElementBox beb;

		children = rootBox.getChildren();
		assertEquals(1, children.length);
		assertEquals(BlockElementBox.class, children[0].getClass());
		beb = (BlockElementBox) children[0];
		assertEquals(root, beb.getElement());

		children = beb.getChildren();
		assertEquals(1, children.length);
		assertEquals(BlockElementBox.class, children[0].getClass());
		beb = (BlockElementBox) children[0];
		assertEquals("beforeBlock", beb.getElement().getPrefixedName());

	}

}
