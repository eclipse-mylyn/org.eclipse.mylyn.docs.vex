/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - adapted tests to optimized split methods
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
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
		context.setWhitespacePolicy(new CssWhitespacePolicy(ss));
	}

	@Test
	public void testSplit() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final Styles styles = context.getStyleSheet().getStyles(root);

		// 0 6 13 21
		// / / / /
		// baggy orange trousers

		doc.insertText(2, "baggy orange trousers");
		final DocumentTextBox box = new DocumentTextBox(context, root, root.getStartOffset() + 1, root.getEndOffset() - 1);
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

		doc.delete(new ContentRange(3, 22));
	}

	@Test
	public void testMultipleSplit() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		doc.insertText(2, "12345 67890 ");
		final IElement root = doc.getRootElement();
		final DocumentTextBox box = new DocumentTextBox(context, root, root.getStartOffset() + 1, root.getEndOffset() - 1);
		final InlineBox.Pair pair = box.split(context, 150, false);
		assertEquals("12345 ", ((DocumentTextBox) pair.getLeft()).getText());
		final InlineBox.Pair pair2 = pair.getRight().split(context, 100, false);
		assertNull(pair2.getLeft());
		assertEquals("67890 ", ((DocumentTextBox) pair2.getRight()).getText());
		assertEquals("Last right box should have a width", 36, pair2.getRight().getWidth());
	}

	@Test
	public void testPositionUpdate() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "root"));
		doc.insertText(2, "before 12345 67890 ");
		final IElement root = doc.getRootElement();
		final DocumentTextBox box = new DocumentTextBox(context, root, root.getStartOffset() + 8, root.getEndOffset() - 1);
		final InlineBox.Pair pair = box.split(context, 150, false);
		doc.insertText(2, "before");
		assertEquals(root.getStartOffset() + 8 + 6, pair.getLeft().getStartOffset());
		assertEquals("12345 ", ((DocumentTextBox) pair.getLeft()).getText());
		assertEquals("67890 ", ((DocumentTextBox) pair.getRight()).getText());
	}

	private void assertSplit(final INode node, final int splitPos, final boolean force, final String left, final String right) {

		final DocumentTextBox box = new DocumentTextBox(context, node, node.getStartOffset() + 1, node.getEndOffset() - 1);

		final Styles styles = context.getStyleSheet().getStyles(box.getNode());
		final int textLength = box.getText().length();
		final int width = g.getCharWidth();

		final InlineBox.Pair pair = box.split(context, splitPos * width, force);

		final DocumentTextBox leftBox = (DocumentTextBox) pair.getLeft();
		final DocumentTextBox rightBox = (DocumentTextBox) pair.getRight();

		final int leftOffset = box.getNode().getStartOffset() + 1;
		final int midOffset = leftOffset + (left == null ? 0 : left.length());
		final int rightOffset = leftOffset + textLength;

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
			//assertEquals(right.length() * width, rightBox.getWidth());
			assertEquals(styles.getLineHeight(), rightBox.getHeight());
			assertEquals(midOffset, rightBox.getStartOffset());
			assertEquals(rightOffset - 1, rightBox.getEndOffset());
		}

	}
}
