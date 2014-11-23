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
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.GapContent;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.undo.CannotRedoException;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Before;
import org.junit.Test;

public class L2XmlInsertionTest {

	private static final QualifiedName PRE = new QualifiedName(null, "pre");
	private static final QualifiedName EMPHASIS = new QualifiedName(null, "emphasis");

	private BaseVexWidget widget;
	private IElement para1;
	private IElement pre;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		final StyleSheet styleSheet = new StyleSheetReader().read(TestResources.get("test.css"));
		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), styleSheet);
		widget.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));
		para1 = widget.insertElement(PARA);
		widget.moveBy(1);
		widget.insertElement(PARA);
		widget.moveTo(para1.getEndPosition());
		pre = widget.insertElement(PRE);
	}

	@Test
	public void givenNonPreElement_whenInsertingNotAllowedXML_shouldInsertTextOnly() throws Exception {
		widget.moveTo(para1.getStartPosition().moveBy(1));
		widget.insertXML(createParaXML());

		assertEquals("beforeinnerafter", para1.getText());
	}

	@Test(expected = DocumentValidationException.class)
	public void givenNonPreElement_whenInsertingInvalidXML_shouldThrowDocumentValidationExeption() throws Exception {
		widget.moveTo(para1.getStartPosition().moveBy(1));

		widget.insertXML("<emphasis>someText</para>");
	}

	@Test
	public void givenNonPreElement_whenInsertingValidXML_shouldInsertXml() throws Exception {
		widget.moveTo(para1.getEndPosition());
		widget.insertXML(createInlineXML("before", "inner", "after"));

		final List<? extends INode> children = para1.children().asList();
		assertTrue("Expecting IParent", children.get(0) instanceof IParent); // the pre element
		assertTrue("Expecting IText", children.get(1) instanceof IText);
		assertEquals("before", children.get(1).getText());
		assertTrue("Expecting IParent", children.get(2) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(2).getText());
		assertTrue("Expecting IText", children.get(3) instanceof IText);
		assertEquals("after", children.get(3).getText());
	}

	@Test
	public void givenPreElement_whenInsertingInvalidXML_shouldInsertTextWithWhitespace() throws Exception {
		widget.moveTo(pre.getStartPosition().moveBy(1));
		widget.insertXML(createParaXML());

		assertEquals("beforeinnerafter", pre.getText());
	}

	@Test
	public void givenPreElement_whenInsertingValidXML_shouldInsertXML() throws Exception {
		widget.moveTo(pre.getEndPosition());
		widget.insertXML(createInlineXML("before", "inner", "after"));

		final List<? extends INode> children = pre.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("before", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after", children.get(2).getText());
	}

	@Test
	public void givenPreElement_whenInsertingValidXMLWithWhitespace_shouldKeepWhitespace() throws Exception {
		widget.moveTo(pre.getEndPosition());
		widget.insertXML(createInlineXML("line1\nline2   end", "inner", "after"));

		final List<? extends INode> children = pre.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("line1\nline2   end", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after", children.get(2).getText());
	}

	@Test
	public void whenInsertingMixedXMLWithWhitespace_shouldKeepWhitecpaceInPre() throws Exception {
		widget.moveTo(para1.getEndPosition());
		widget.insertXML("before<pre>pre1\npre2  end</pre>after   \nend");

		final List<? extends INode> children = para1.children().after(pre.getEndPosition().getOffset()).asList();
		assertEquals("New children count", 3, children.size());
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("before", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted pre element
		assertEquals("pre1\npre2  end", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after end", children.get(2).getText());
	}

	@Test(expected = CannotRedoException.class)
	public void givenNonPreElement_whenInsertingNotAllowedFragment_shouldThrowCannotRedoException() throws Exception {
		widget.moveTo(para1.getStartPosition().moveBy(1));
		widget.insertFragment(createParaFragment());
	}

	@Test
	public void givenNonPreElement_whenInsertingValidFragment_shouldInsertXml() throws Exception {
		widget.moveTo(para1.getEndPosition());
		widget.insertFragment(createInlineFragment("before", "inner", "after"));

		final List<? extends INode> children = para1.children().asList();
		assertTrue("Expecting IParent", children.get(0) instanceof IParent); // the pre element
		assertTrue("Expecting IText", children.get(1) instanceof IText);
		assertEquals("before", children.get(1).getText());
		assertTrue("Expecting IParent", children.get(2) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(2).getText());
		assertTrue("Expecting IText", children.get(3) instanceof IText);
		assertEquals("after", children.get(3).getText());
	}

	@Test(expected = CannotRedoException.class)
	public void givenPreElement_whenInsertingInvalidFragment_shouldThrowCannotRedoException() throws Exception {
		widget.moveTo(pre.getStartPosition().moveBy(1));
		widget.insertFragment(createParaFragment());
	}

	@Test
	public void givenPreElement_whenInsertingValidFragment_shouldInsertXML() throws Exception {
		widget.moveTo(pre.getEndPosition());
		widget.insertFragment(createInlineFragment("before", "inner", "after"));

		final List<? extends INode> children = pre.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("before", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after", children.get(2).getText());
	}

	@Test
	public void givenPreElement_whenInsertingValidFragmentWithWhitespace_shouldKeepWhitespace() throws Exception {
		widget.moveTo(pre.getEndPosition());
		widget.insertFragment(createInlineFragment("line1\nline2   end", "inner", "after"));

		final List<? extends INode> children = pre.children().asList();
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("line1\nline2   end", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted emphasis
		assertEquals("inner", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after", children.get(2).getText());
	}

	@Test
	public void whenInsertingMixedFragmentWithWhitespace_shouldKeepWhitecpaceInPre() throws Exception {
		widget.moveTo(para1.getEndPosition());
		widget.insertFragment(new XMLFragment("before<pre>pre1\npre2  end</pre>after   \nend").getDocumentFragment());

		final List<? extends INode> children = para1.children().after(pre.getEndPosition().getOffset()).asList();
		assertEquals("New children count", 3, children.size());
		assertTrue("Expecting IText", children.get(0) instanceof IText);
		assertEquals("before", children.get(0).getText());
		assertTrue("Expecting IParent", children.get(1) instanceof IParent); // the inserted pre element
		assertEquals("pre1\npre2  end", children.get(1).getText());
		assertTrue("Expecting IText", children.get(2) instanceof IText);
		assertEquals("after end", children.get(2).getText());
	}

	private IDocumentFragment createInlineFragment(final String before, final String inner, final String after) {
		return new XMLFragment(createInlineXML(before, inner, after)).getDocumentFragment();
	}

	private String createInlineXML(final String before, final String inner, final String after) {
		final IContent content = new GapContent(10);
		final List<Node> nodes = new ArrayList<Node>();

		content.insertText(0, before);
		final int offset = content.length();
		content.insertTagMarker(offset);
		content.insertTagMarker(offset + 1);
		final Element emp = new Element(EMPHASIS);
		emp.associate(content, new ContentRange(offset, offset + 1));
		nodes.add(emp);

		content.insertText(emp.getEndPosition().getOffset(), inner);

		content.insertText(content.length(), after);

		return new XMLFragment(new DocumentFragment(content, nodes)).getXML();
	}

	private IDocumentFragment createParaFragment() {
		return new XMLFragment(createParaXML()).getDocumentFragment();
	}

	private String createParaXML() {
		final IContent content = new GapContent(10);
		final List<Node> nodes = new ArrayList<Node>();

		content.insertText(0, "before");
		final int offset = content.length();
		content.insertTagMarker(offset);
		content.insertTagMarker(offset + 1);
		final Element para = new Element(PARA);
		para.associate(content, new ContentRange(offset, offset + 1));
		nodes.add(para);

		content.insertText(para.getEndPosition().getOffset(), "inner");

		content.insertText(content.length(), "after");

		return new XMLFragment(new DocumentFragment(content, nodes)).getXML();
	}

}
