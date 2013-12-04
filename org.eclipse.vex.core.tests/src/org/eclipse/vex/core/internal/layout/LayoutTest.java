/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - Extended TestSuite to perform simple actions (bug 408482)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.IStyleSheetProvider;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Runs several suites of layout tests. Each suite is defined in an XML file. The XML files to run are registered in the
 * suite() method.<br />
 * When the attribute <code>performActions</code> in the test's root element is set, some simple actions defined as
 * element's attributes my be performed before re-running the layout test:
 * <ul>
 * <li>insertTextAction="someText" - Insert the given text at the end of the element.</li>
 * <li>removeTextAction="5" - Remove given length text from the start of the element.</li>
 * <li>removeElementAction="1" - Remove the element that defined this box.</li>
 * <li>shouldBeRemoved="1" - This box is expected to be removed;
 * <li>InvalidateAction="1" - Invalidates the element (only valid in BlockBox instances)</li>
 * </ul>
 * The expected layout state of an box may be checked with the attribute <code>layoutState="LAYOUT_XXX"</code> (
 * {@link AbstractBlockBox#LAYOUT_OK}, {@link AbstractBlockBox#LAYOUT_REDO}, {@link AbstractBlockBox#LAYOUT_PROPAGATE}).<br />
 * The expected text after actions are performed is defined with the attribute <code>textAfter</code>.
 * 
 */
@RunWith(AllTests.class)
public class LayoutTest extends TestCase {

	public String id;
	public String documentContent;
	public int layoutWidth = 100;
	public boolean performActions = false;
	public boolean invalidateParentBlock;
	public BoxSpec result;
	public String css;

	public static Test suite() throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
		final TestSuite suite = new TestSuite(LayoutTest.class.getName());
		suite.addTest(loadSuite("block-inline.xml"));
		suite.addTest(loadSuite("before-after.xml"));
		suite.addTest(loadSuite("linebreaks.xml"));
		suite.addTest(loadSuite("tables.xml"));
		suite.addTest(loadSuite("simple-edit.xml"));
		suite.addTest(loadSuite("comment-processing-instr.xml"));
		return suite;
	}

	public static Test loadSuite(final String filename) throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
		final XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		final TestCaseBuilder builder = new TestCaseBuilder();
		xmlReader.setContentHandler(builder);
		// xmlReader.setEntityResolver(builder);
		final URL url = LayoutTest.class.getResource(filename);
		xmlReader.parse(new InputSource(url.toString()));

		final TestSuite suite = new TestSuite(filename);
		for (final LayoutTest test : builder.testCases) {
			suite.addTest(test);
		}
		return suite;
	}

	public LayoutTest() {
		super("testLayout");
	}

	@Override
	public String getName() {
		return id;
	}

	public void testLayout() throws Exception {
		final URL url = LayoutTest.class.getResource(css);
		final StyleSheet styleSheet = new StyleSheetReader().read(url);

		final FakeGraphics g = new FakeGraphics();

		final LayoutContext context = new LayoutContext();
		context.setBoxFactory(new MockBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(styleSheet);
		context.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));

		final DocumentReader reader = new DocumentReader();
		reader.setValidator(new LayoutTestValidator());
		reader.setStyleSheetProvider(new IStyleSheetProvider() {
			public StyleSheet getStyleSheet(final DocumentContentModel documentContentModel) {
				return styleSheet;
			}
		});
		reader.setWhitespacePolicyFactory(CssWhitespacePolicy.FACTORY);
		final IDocument document = reader.read(documentContent);
		document.setValidator(reader.getValidator());
		context.setDocument(document);

		final RootBox rootBox = new RootBox(context, document, layoutWidth);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		System.out.println("Test: " + id);
		assertBox(result, rootBox, "");

		if (performActions) {
			performActions(result, rootBox, document);
			assertLayoutStates(result, rootBox);
			rootBox.layout(context, 0, Integer.MAX_VALUE);
			assertBox(result, rootBox, "", true);
		}
	}

	private void performActions(final BoxSpec boxSpec, final Box box, final IDocument doc) {

		if (boxSpec.insertTextAction != null) {
			doc.insertText(box.getNode().getEndOffset(), boxSpec.insertTextAction);
		}

		if (boxSpec.removeTextAction > 0) {
			System.out.println("Removing text ");
			if (box.getNode() == null || box.getNode().getText().length() < boxSpec.removeTextAction) {
				fail(String.format("Error in test configuration. Can not remove %s chars from element with text:'%s'", boxSpec.removeTextAction, box.getNode().getText() != null ? box.getNode()
						.getText() : "null"));
			}
			final int startOffset = box.getNode().getStartOffset();
			doc.getContent().remove(new ContentRange(startOffset + 1, startOffset + boxSpec.removeTextAction));
		}

		if (boxSpec.removeElementAction) {
			if (box.getNode() == null) {
				fail(String.format("Error in test configuration. Can not remove element for box'%s'", boxSpec.toString()));
			}
			System.out.println("Removing element " + box.getNode());
			final int startOffset = box.getNode().getStartOffset();
			final int endOffset = box.getNode().getEndOffset();
			doc.delete(new ContentRange(startOffset, endOffset));
			invalidateParentBlock = true;
			return;
		}

		if (boxSpec.invalidateAction && box instanceof BlockBox) {
			((BlockBox) box).invalidate(true);
		}

		final List<BoxSpec> toRemove = new ArrayList<BoxSpec>();
		for (int i = 0; i < boxSpec.children.size(); i++) {
			final BoxSpec childSpec = boxSpec.children.get(i);
			performActions(childSpec, box.getChildren()[i], doc);
			if (childSpec.removeElementAction || childSpec.shouldBeRemoved) {
				toRemove.add(childSpec);
			}
		}

		boxSpec.children.removeAll(toRemove);

		if (invalidateParentBlock && box instanceof BlockBox && box.getNode() != null) {
			((BlockBox) box).invalidate(true);
			invalidateParentBlock = false;
		}
	}

	private static void assertBox(final BoxSpec boxSpec, final Box box, final String indent) {
		assertBox(boxSpec, box, indent, false);
	}

	private static void assertBox(final BoxSpec boxSpec, final Box box, final String indent, final boolean afterAction) {

		System.out.println(indent + boxSpec.className);

		if (boxSpec.className != null) {
			String actualClassName = box.getClass().getName();
			if (boxSpec.className.lastIndexOf('.') == -1) {
				// no dot in box spec classname, so strip the prefix from the
				// actual classname
				final int lastDot = actualClassName.lastIndexOf('.');
				actualClassName = actualClassName.substring(lastDot + 1);
			}
			assertEquals(boxSpec.className, actualClassName);
		}

		if (boxSpec.element != null) {
			assertNotNull(box.getNode());
			assertEquals(boxSpec.element, getPrefixedNameOfElement(box.getNode()));
		}

		if (boxSpec.text != null && box instanceof TextBox) {
			final String expected = !afterAction || boxSpec.textAfter == null ? boxSpec.text : boxSpec.textAfter;
			System.out.println(indent + "  Expexted: " + expected + " Actual:" + ((TextBox) box).getText());
			assertEquals("Content of " + boxSpec.className + " does not match.", expected, ((TextBox) box).getText());
		}

		if (!boxSpec.children.isEmpty() && box.getChildren() == null) {
			fail("Expected " + boxSpec.children.size() + " children, but " + boxSpec.className + "'s children is null");
		}

		if (boxSpec.children.size() != box.getChildren().length) {
			System.out.println("Wrong number of child boxes");
			System.out.println("  Expected:");
			for (final BoxSpec childSpec : boxSpec.children) {
				System.out.print("    " + childSpec.className);
				final String expected = !afterAction || boxSpec.textAfter == null ? boxSpec.text : boxSpec.textAfter;
				if (expected != null) {
					System.out.print(" '" + childSpec.text + "'");
				}
				System.out.println();
			}
			System.out.println("  Actual:");
			for (final Box childBox : box.getChildren()) {
				System.out.println("    " + childBox.getClass() + ": " + childBox);
			}
			fail("Wrong number of child boxes.");
		}

		for (int i = 0; i < boxSpec.children.size(); i++) {
			assertBox(boxSpec.children.get(i), box.getChildren()[i], indent + "  ", afterAction);
		}

	}

	private static void assertLayoutStates(final BoxSpec boxSpec, final Box box) {

		if (boxSpec.layoutState >= 0 && box instanceof AbstractBlockBox) {
			assertEquals("Unexpected LayoutState for " + boxSpec.className, boxSpec.layoutState, ((AbstractBlockBox) box).getLayoutState());
		}

		for (int i = 0; i < boxSpec.children.size(); i++) {
			assertLayoutStates(boxSpec.children.get(i), box.getChildren()[i]);
		}

	}

	private static String getPrefixedNameOfElement(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<String>("") {
			@Override
			public String visit(final IElement element) {
				return element.getPrefixedName();
			}
		});
	}

	private static class TestCaseBuilder extends DefaultHandler {

		private List<LayoutTest> testCases;
		private String css;
		private LayoutTest testCase;
		private BoxSpec boxSpec;
		private Stack<BoxSpec> boxSpecs;
		private boolean inDoc;

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {

			final String s = new String(ch, start, length).trim();
			if (s.length() > 0) {
				if (inDoc) {
					testCase.documentContent = testCase.documentContent + new String(ch, start, length);
				} else {
					throw new IllegalStateException();
				}
			}
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			if (qName.equals("box")) {
				if (boxSpecs.isEmpty()) {
					boxSpec = null;
				} else {
					boxSpec = boxSpecs.pop();
				}
			} else if (qName.equals("doc")) {
				inDoc = false;
			}
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

			if (qName.equals("testcases")) {
				testCases = new ArrayList<LayoutTest>();
				css = attributes.getValue("css");
				if (css == null) {
					css = "test.css";
				}
				testCase = null;
				boxSpecs = new Stack<BoxSpec>();
			} else if (qName.equals("test")) {
				testCase = new LayoutTest();
				testCase.id = attributes.getValue("id");
				testCase.css = css;
				final String layoutWidth = attributes.getValue("layoutWidth");
				if (layoutWidth != null) {
					testCase.layoutWidth = Integer.parseInt(layoutWidth);
				}
				testCase.performActions = attributes.getValue("performActions") != null;
				testCases.add(testCase);
			} else if (qName.equals("doc")) {
				inDoc = true;
				testCase.documentContent = "";
			} else if (qName.equals("result")) {
			} else if (qName.equals("box")) {
				final BoxSpec parent = boxSpec;
				boxSpec = new BoxSpec();
				boxSpec.className = attributes.getValue("class");
				boxSpec.element = attributes.getValue("element");
				boxSpec.text = attributes.getValue("text");
				boxSpec.textAfter = attributes.getValue("textAfter");
				boxSpec.insertTextAction = attributes.getValue("insertTextAction");
				try {
					boxSpec.removeTextAction = Integer.parseInt(attributes.getValue("removeTextAction"));
				} catch (final NumberFormatException e) {
					boxSpec.removeTextAction = 0;
				}
				boxSpec.removeElementAction = attributes.getValue("removeElementAction") != null;
				boxSpec.invalidateAction = attributes.getValue("invalidateAction") != null;
				boxSpec.shouldBeRemoved = attributes.getValue("shouldBeRemoved") != null;
				String layoutStateAttr = attributes.getValue("layoutState");
				if (layoutStateAttr != null) {
					layoutStateAttr = layoutStateAttr.trim().toLowerCase();
					if (layoutStateAttr.equals("LAYOUT_OK".toLowerCase())) {
						boxSpec.layoutState = AbstractBlockBox.LAYOUT_OK;
					} else if (layoutStateAttr.equals("LAYOUT_PROPAGATE".toLowerCase())) {
						boxSpec.layoutState = AbstractBlockBox.LAYOUT_PROPAGATE;
					} else if (layoutStateAttr.equals("LAYOUT_REDO".toLowerCase())) {
						boxSpec.layoutState = AbstractBlockBox.LAYOUT_REDO;
					}
				}

				if (parent == null) {
					testCase.result = boxSpec;
				} else {
					boxSpecs.push(parent);
					parent.children.add(boxSpec);
				}
			} else {
				throw new SAXException("Unrecognized element: " + qName);
			}
		}
	}

	private static class BoxSpec {
		public String className;
		public String element;
		public List<BoxSpec> children = new ArrayList<BoxSpec>();
		public String text;
		public String textAfter;
		public byte layoutState = -1;
		public String insertTextAction;
		public int removeTextAction;
		public boolean removeElementAction;
		public boolean shouldBeRemoved;
		public boolean invalidateAction = false;
	}

}
