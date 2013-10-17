/*******************************************************************************
 * Copyright (c) 2009 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Stack;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.MockDisplayDevice;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TableLayoutTest {

	private static interface StackVisitor {
		void visit(StackElement element);
	}

	private static class StackElement {
		public final int indent;
		public final Box box;

		public StackElement(final int indent, final Box box) {
			this.indent = indent;
			this.box = box;
		}
	}

	private LayoutContext context;
	private Document document;
	private RootBox rootBox;
	private int caretPosition;

	@Before
	public void setUp() throws Exception {

		// display dummy
		DisplayDevice.setCurrent(new MockDisplayDevice(90, 90));

		// context dummy
		context = new LayoutContext();
		context.setBoxFactory(new MockBoxFactory());
		context.setGraphics(new FakeGraphics());

		// set CSS
		final String css = "root   {display:block}" + "inline {display:inline}" + "table  {display:table}" + "tcap   {display:table-caption}" + "td     {display:table-cell}"
				+ "tc     {display:table-column}" + "tcg    {display:table-column-group}" + "tfg    {display:table-footer-group}" + "thg    {display:table-header-group}"
				+ "tr     {display:table-row}" + "trg    {display:table-row-group}";
		final StyleSheet styleSheet = new StyleSheetReader().read(css);
		context.setStyleSheet(styleSheet);
		context.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));

		resetDocument();
	}

	@After
	public void tearDown() throws Exception {
		rootBox = null;
		document = null;
		context = null;
	}

	private void resetDocument() {
		document = new Document(new QualifiedName(null, "root"));
		context.setDocument(document);
		caretPosition = 2;
		rootBox = new RootBox(context, document, 500);
	}

	private void insertElement(final String elementName) {
		document.insertElement(caretPosition, new QualifiedName(null, elementName));
		caretPosition++;
	}

	private void insertText(final String text) {
		document.insertText(caretPosition, text);
		;
		caretPosition += text.length();
	}

	@Test
	public void testValidTable() throws Exception {

		// single cell Table
		insertElement("table");
		insertElement("tr");
		insertElement("td");
		insertText("a");
		assertCount(1, TableBox.class);
		assertCount(1, TableRowBox.class);
		assertCount(1, TableCellBox.class);
		assertCount(1, DocumentTextBox.class);
		assertEquals("a", contentAsText());

		// 2x2 table plus text
		resetDocument();
		insertText("_");
		insertElement("table");
		insertElement("tr");
		insertElement("td");
		insertText("a");
		caretPosition++;
		insertElement("td");
		insertText("b");
		caretPosition += 2;
		insertElement("tr");
		insertElement("td");
		insertText("c");
		caretPosition++;
		insertElement("td");
		insertText("d");
		assertCount(1, TableBox.class);
		assertCount(2, TableRowBox.class);
		assertCount(4, TableCellBox.class);
		assertCount(5, DocumentTextBox.class);
		assertEquals("_abcd", contentAsText());
	}

	// table elements outside table (separately tested to improve tracing if
	// StackOverflowError will be thrown)
	@Test
	public void testCaptionOutsideTable() {
		test("tcap");
	}

	@Test
	public void testCellOutsideTable() {
		test("td");
	}

	@Test
	public void testColumnOutsideTable() {
		test("tc");
	}

	@Test
	public void testColumnGroupOutsideTable() {
		test("tcg");
	}

	@Test
	public void testFooterGroupOutsideTable() {
		test("tfg");
	}

	@Test
	public void testHeaderGroupOutsideTable() {
		test("thg");
	}

	@Test
	public void testRowOutsideTable() {
		test("tr");
	}

	@Test
	public void testRowGroupOutsideTable() {
		test("trg");
	}

	// invalid nested table elements (separately tested to improve tracing if
	// StackOverflowError will be thrown)
	@Test
	public void testInvalidNesting1() {
		test("inline", "tcap");
	}

	@Test
	public void testInvalidNesting2() {
		test("table", "td");
	}

	@Test
	public void testInvalidNesting3() {
		test("td", "tr");
	}

	@Test
	public void testInvalidNesting5() {
		test("tr", "tfg");
	}

	@Test
	public void testInvalidNesting6() {
		test("td", "thg");
	}

	@Test
	public void testInvalidNesting7() {
		test("table", "tc");
	}

	@Test
	public void testInvalidNesting8() {
		test("thg", "tcg");
	}

	private void test(final String... elements) {
		resetDocument();
		insertElement("inline");
		for (final String element : elements) {
			insertElement(element);
		}
		insertText("x");
		assertCount(1, DocumentTextBox.class);
		assertEquals("x", contentAsText());
	}

	private String contentAsText() {
		return document.getText();
	}

	private void assertCount(final int expected, final Class<? extends Box> blockClass) {
		final int count = count(blockClass);
		final String message = "expected count of <" + blockClass.getSimpleName() + ">: <" + expected + "> but was: <" + count + ">\n" + "Actual layout stack trace:\n" + layoutStackToString();
		assertEquals(message, expected, count);
	}

	private int count(final Class<? extends Box> blockClass) {
		final int[] mutableInteger = new int[1];
		mutableInteger[0] = 0;
		travelLayoutStack(new StackVisitor() {

			public void visit(final StackElement element) {
				if (element.box.getClass().equals(blockClass)) {
					mutableInteger[0]++;
				}
			}

		});
		return mutableInteger[0];
	}

	private String layoutStackToString() {
		final StringBuilder result = new StringBuilder();
		travelLayoutStack(new StackVisitor() {

			public void visit(final StackElement element) {
				if (element.indent > 0) {
					final char[] indentChars = new char[element.indent * 2];
					Arrays.fill(indentChars, ' ');
					result.append(indentChars);
				}
				result.append(element.box.getClass().getSimpleName());
				result.append('\n');

			}

		});
		return result.toString();
	}

	private void travelLayoutStack(final StackVisitor visitor) {

		// already layouted?
		final Box[] rootElementChildren = rootBox.getChildren()[0].getChildren();
		if (rootElementChildren == null || rootElementChildren.length == 0) {
			rootBox.layout(context, 0, Integer.MAX_VALUE);
		}

		final Stack<StackElement> stack = new Stack<StackElement>();
		stack.push(new StackElement(0, rootBox));
		while (!stack.isEmpty()) {
			final StackElement current = stack.pop();
			visitor.visit(current);

			// iterate deep-first
			for (final Box child : current.box.getChildren()) {
				stack.push(new StackElement(current.indent + 1, child));
			}
		}
	}

}