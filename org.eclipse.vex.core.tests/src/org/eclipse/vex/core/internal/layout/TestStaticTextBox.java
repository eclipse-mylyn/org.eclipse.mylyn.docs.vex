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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.MockDisplayDevice;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.provisional.dom.INode;
import org.junit.Before;
import org.junit.Test;

public class TestStaticTextBox {

	FakeGraphics g;
	LayoutContext context;
	Element root = new Element("root");
	Styles styles;

	@Before
	public void setUp() throws Exception {
		DisplayDevice.setCurrent(new MockDisplayDevice(90, 90));
		final URL url = this.getClass().getResource("test.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);

		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new CssBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(ss);
		context.setWhitespacePolicy(new CssWhitespacePolicy(ss));

		styles = context.getStyleSheet().getStyles(root);

	}

	@Test
	public void testSplit() throws Exception {

		final StaticTextBox box = new StaticTextBox(context, root, "test");
		assertEquals(styles.getLineHeight(), box.getHeight());

		assertSplit(root, 22, false, "baggy orange ", "trousers");
		assertSplit(root, 21, false, "baggy orange ", "trousers");
		assertSplit(root, 20, false, "baggy orange ", "trousers");
		assertSplit(root, 13, false, "baggy orange ", "trousers");
		assertSplit(root, 12, false, "baggy ", "orange trousers");
		assertSplit(root, 6, false, "baggy ", "orange trousers");
		assertSplit(root, 5, false, null, "baggy orange trousers");
		assertSplit(root, 1, false, null, "baggy orange trousers");
		assertSplit(root, 0, false, null, "baggy orange trousers");
		assertSplit(root, -1, false, null, "baggy orange trousers");

		assertSplit(root, 22, true, "baggy orange ", "trousers");
		assertSplit(root, 21, true, "baggy orange ", "trousers");
		assertSplit(root, 20, true, "baggy orange ", "trousers");
		assertSplit(root, 13, true, "baggy orange ", "trousers");
		assertSplit(root, 12, true, "baggy ", "orange trousers");
		assertSplit(root, 6, true, "baggy ", "orange trousers");
		assertSplit(root, 5, true, "baggy ", "orange trousers");
		assertSplit(root, 4, true, "bagg", "y orange trousers");
		assertSplit(root, 3, true, "bag", "gy orange trousers");
		assertSplit(root, 2, true, "ba", "ggy orange trousers");
		assertSplit(root, 1, true, "b", "aggy orange trousers");
		assertSplit(root, 0, true, "b", "aggy orange trousers");
		assertSplit(root, -1, true, "b", "aggy orange trousers");
	}

	@Test
	public void testSpaceSplit() throws Exception {
		assertSplit(root, 11, false, "red  ", "green");
		assertSplit(root, 10, false, "red  ", "green");
		assertSplit(root, 9, false, "red  ", "green");
		assertSplit(root, 5, false, "red  ", "green");
	}

	@Test
	public void testMultipleSplit() throws Exception {
		final StaticTextBox box = new StaticTextBox(context, root, "12345 67890 ");
		final InlineBox.Pair pair = box.split(context, 150, false);
		assertEquals("12345 ", ((StaticTextBox) pair.getLeft()).getText());
		final InlineBox.Pair pair2 = pair.getRight().split(context, 100, false);
		assertNull(pair2.getLeft());
		assertEquals("67890 ", ((StaticTextBox) pair2.getRight()).getText());
		assertEquals("Last right box should have a width", 36, pair2.getRight().getWidth());
	}

	private void assertSplit(final INode node, final int splitPos, final boolean force, final String left, final String right) {

		final StaticTextBox box = new StaticTextBox(context, node, left != null ? left + right : right);

		final Styles styles = context.getStyleSheet().getStyles(box.getNode());
		final int width = g.getCharWidth();

		final InlineBox.Pair pair = box.split(context, splitPos * width, force);

		final StaticTextBox leftBox = (StaticTextBox) pair.getLeft();
		final StaticTextBox rightBox = (StaticTextBox) pair.getRight();

		if (left == null) {
			assertNull(leftBox);
		} else {
			assertNotNull(leftBox);
			assertEquals(left, leftBox.getText());
			assertEquals(left.length() * width, leftBox.getWidth());
			assertEquals(styles.getLineHeight(), leftBox.getHeight());
		}

		if (right == null) {
			assertNull(rightBox);
		} else {
			assertNotNull(rightBox);
			assertEquals(right, rightBox.getText());
			assertEquals(styles.getLineHeight(), rightBox.getHeight());
		}

	}
}
