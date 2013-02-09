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

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.dom.ContentRange;
import org.eclipse.vex.core.dom.IDocument;
import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Document;
import org.junit.Test;

/**
 * Tests the DocumentTestBox class. We focus here on proper offsets, since text splitting is tested thoroughly in
 * TestStaticTextBox.
 */
public class TestDocumentTextBox {

	FakeGraphics g;
	LayoutContext context;

	public TestDocumentTextBox() throws Exception {
		final URL url = this.getClass().getResource("test.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);

		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new CssBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(ss);
	}

	@Test
	public void testSplit() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final Styles styles = context.getStyleSheet().getStyles(root);

		final int width = g.getCharWidth();

		// 0 6 13 21
		// / / / /
		// baggy orange trousers

		doc.insertText(2, "baggy orange trousers");
		final DocumentTextBox box = new DocumentTextBox(context, root, 2, 23);
		assertEquals(box.getText().length() * width, box.getWidth());
		assertEquals(styles.getLineHeight(), box.getHeight());
		assertSplit(box, 22, false, "baggy orange ", "trousers");
		assertSplit(box, 21, false, "baggy orange ", "trousers");
		assertSplit(box, 20, false, "baggy orange ", "trousers");
		assertSplit(box, 13, false, "baggy orange ", "trousers");
		assertSplit(box, 12, false, "baggy ", "orange trousers");
		assertSplit(box, 6, false, "baggy ", "orange trousers");
		assertSplit(box, 5, false, null, "baggy orange trousers");
		assertSplit(box, 1, false, null, "baggy orange trousers");
		assertSplit(box, 0, false, null, "baggy orange trousers");
		assertSplit(box, -1, false, null, "baggy orange trousers");

		assertSplit(box, 22, true, "baggy orange ", "trousers");
		assertSplit(box, 21, true, "baggy orange ", "trousers");
		assertSplit(box, 20, true, "baggy orange ", "trousers");
		assertSplit(box, 13, true, "baggy orange ", "trousers");
		assertSplit(box, 12, true, "baggy ", "orange trousers");
		assertSplit(box, 6, true, "baggy ", "orange trousers");
		assertSplit(box, 5, true, "baggy ", "orange trousers");
		assertSplit(box, 4, true, "bagg", "y orange trousers");
		assertSplit(box, 3, true, "bag", "gy orange trousers");
		assertSplit(box, 2, true, "ba", "ggy orange trousers");
		assertSplit(box, 1, true, "b", "aggy orange trousers");
		assertSplit(box, 0, true, "b", "aggy orange trousers");
		assertSplit(box, -1, true, "b", "aggy orange trousers");

		doc.delete(new ContentRange(3, 22));
	}

	private void assertSplit(final DocumentTextBox box, final int splitPos, final boolean force, final String left, final String right) {

		final Styles styles = context.getStyleSheet().getStyles(box.getNode());

		final int width = g.getCharWidth();

		final InlineBox.Pair pair = box.split(context, splitPos * width, force);

		final DocumentTextBox leftBox = (DocumentTextBox) pair.getLeft();
		final DocumentTextBox rightBox = (DocumentTextBox) pair.getRight();

		final int leftOffset = 2;
		final int midOffset = leftOffset + (left == null ? 0 : left.length());
		final int rightOffset = leftOffset + box.getText().length();

		if (left == null) {
			assertNull(leftBox);
		} else {
			assertNotNull(leftBox);
			assertEquals(left, leftBox.getText());
			assertEquals(left.length() * width, leftBox.getWidth());
			assertEquals(styles.getLineHeight(), leftBox.getHeight());
			assertEquals(leftOffset, leftBox.getStartOffset());
			assertEquals(midOffset - 1, leftBox.getEndOffset());
		}

		if (right == null) {
			assertNull(rightBox);
		} else {
			assertNotNull(rightBox);
			assertEquals(right, rightBox.getText());
			assertEquals(right.length() * width, rightBox.getWidth());
			assertEquals(styles.getLineHeight(), rightBox.getHeight());
			assertEquals(midOffset, rightBox.getStartOffset());
			assertEquals(rightOffset, rightBox.getEndOffset());
		}

	}
}
