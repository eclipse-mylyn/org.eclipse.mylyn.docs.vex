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

import org.eclipse.core.runtime.QualifiedName;
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
		final Document doc = new Document(new QualifiedName(null, "root"));
		final Element root = doc.getRootElement();
		doc.insertElement(2, new QualifiedName(null, "beforeBlock"));
		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 500);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		Box[] children;
		BlockElementBox beb;

		children = rootBox.getChildren();
		assertEquals(1, children.length);
		assertEquals(BlockElementBox.class, children[0].getClass());
		beb = (BlockElementBox) children[0];
		assertEquals(doc, beb.getNode());

		children = beb.getChildren();
		assertEquals(1, children.length);
		assertEquals(BlockElementBox.class, children[0].getClass());
		beb = (BlockElementBox) children[0];
		assertEquals(root, beb.getNode());

		children = beb.getChildren();
		assertEquals(1, children.length);
		assertEquals(BlockElementBox.class, children[0].getClass());
		beb = (BlockElementBox) children[0];
		assertEquals("beforeBlock", ((Element) beb.getNode()).getPrefixedName());
	}

}
