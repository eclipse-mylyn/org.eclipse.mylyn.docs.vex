/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class CssWhitespacePolicyTest {

	private IDocument document;

	private CssWhitespacePolicy policy;

	@Before
	public void setUp() throws Exception {
		document = new Document(new QualifiedName(null, "parent"));
	}

	@Test
	public void givenBlockElement_shouldIndicateBlock() throws Exception {
		givenCss("element { display: block; }");
		assertIsBlock(element());
	}

	@Test
	public void givenListItem_shouldIndicateBlock() throws Exception {
		givenCss("element { display: list-item; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableCaption_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-caption; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableCaption_whenParentIsNoTable_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-caption; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableCell_whenParentIsTableRow_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-cell; } parent { display: table-row; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableCell_whenParentIsNoTableRow_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-cell; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableColumn_whenParentIsTableColumnGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-column; } parent { display: table-column-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableColumn_whenParentIsNoTableColumnGroup_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-column; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableColumnGroup_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-column-group; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableColumnGroup_whenParentIsNoTable_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-column-group; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableFooterGroup_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-footer-group; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableFooterGroup_whenParentIsTableRowGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-footer-group; } parent { display: table-row-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableFooterGroup_whenParentIsOther_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-footer-group; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableHeaderGroup_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-header-group; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableHeaderGroup_whenParentIsTableRowGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-header-group; } parent { display: table-row-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableHeaderGroup_whenParentIsOther_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-header-group; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableRowGroup_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row-group; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRowGroup_whenParentIsTableRowGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row-group; } parent { display: table-row-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRowGroup_whenParentIsOther_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-row-group; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenTableRow_whenParentIsTable_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row; } parent { display: table; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRow_whenParentIsTableRowGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row; } parent { display: table-row-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRow_whenParentIsTableHeaderGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row; } parent { display: table-header-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRow_whenParentIsTableFooterGroup_shouldIndicateBlock() throws Exception {
		givenCss("element { display: table-row; } parent { display: table-footer-group; }");
		assertIsBlock(element());
	}

	@Test
	public void givenTableRow_whenParentIsOther_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: table-row; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenInlineElement_shouldIndicateNoBlock() throws Exception {
		givenCss("element { display: inline; }");
		assertIsNoBlock(element());
	}

	@Test
	public void givenDocument_shouldIndicateBlock() throws Exception {
		givenCss("");

		assertIsBlock(document);
	}

	@Test
	public void givenInlineComment_whenParentIsInline_shouldIndicateNoBlock() throws Exception {
		givenCss("vex|comment { display: inline; } parent { display: inline; }");

		assertIsNoBlock(comment());
	}

	@Test
	public void givenInlineComment_whenParentIsBlock_shouldIndicateBlock() throws Exception {
		givenCss("vex|comment { display: inline; } parent { display: block; }");

		assertIsBlock(comment());
	}

	@Test
	public void givenBlockComment_whenParentIsInline_shouldIndicateBlock() throws Exception {
		givenCss("vex|comment { display: block; } parent { display: inline; }");

		assertIsBlock(comment());
	}

	@Test
	public void givenBlockComment_whenParentIsBlock_shouldIndicateBlock() throws Exception {
		givenCss("vex|comment { display: block; } parent { display: block; }");

		assertIsBlock(comment());
	}

	@Test
	public void givenInlineProcessingInstruction_whenParentIsInline_shouldIndicateNoBlock() throws Exception {
		givenCss("vex|processing-instruction { display: inline; } parent { display: inline; }");

		assertIsNoBlock(processingInstruction());
	}

	@Test
	public void givenInlineProcessingInstruction_whenParentIsBlock_shouldIndicateBlock() throws Exception {
		givenCss("vex|processing-instruction { display: inline; } parent { display: block; }");

		assertIsBlock(processingInstruction());
	}

	@Test
	public void givenBlockProcessingInstruction_whenParentIsInline_shouldIndicateBlock() throws Exception {
		givenCss("vex|processing-instruction { display: block; } parent { display: inline; }");

		assertIsBlock(processingInstruction());
	}

	@Test
	public void givenBlockProcessingInstruction_whenParentIsBlock_shouldIndicateBlock() throws Exception {
		givenCss("vex|processing-instruction { display: block; } parent { display: block; }");

		assertIsBlock(processingInstruction());
	}

	@Test
	public void givenText_shouldIndicateNoBlock() throws Exception {
		givenCss("");

		assertIsNoBlock(text());
	}

	private void givenCss(final String css) throws Exception {
		policy = policyForCss(css);
	}

	private static CssWhitespacePolicy policyForCss(final String css) throws IOException {
		final StyleSheet styleSheet = new StyleSheetReader().read(css);
		return new CssWhitespacePolicy(styleSheet);
	}

	private void assertIsBlock(final INode nodeUnderTest) {
		assertTrue(policy.isBlock(nodeUnderTest));
	}

	private void assertIsNoBlock(final INode nodeUnderTest) {
		assertFalse(policy.isBlock(nodeUnderTest));
	}

	private IElement element() {
		return document.insertElement(document.getRootElement().getEndOffset(), new QualifiedName(null, "element"));
	}

	private IComment comment() {
		return document.insertComment(document.getRootElement().getEndOffset());
	}

	private IProcessingInstruction processingInstruction() {
		return document.insertProcessingInstruction(document.getRootElement().getEndOffset(), "testPi");
	}

	private IText text() {
		document.insertText(document.getRootElement().getEndOffset(), "Hello World");
		return (IText) document.getRootElement().children().first();
	}
}
