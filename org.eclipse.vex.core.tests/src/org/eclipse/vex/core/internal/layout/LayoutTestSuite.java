/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
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

import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentContentModel;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.widget.CssWhitespacePolicy;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Runs several suites of layout tests. Each suite is defined in an XML file. The XML files to run are registered in the
 * suite() method.
 */
public class LayoutTestSuite extends TestCase {

	public String id;
	public String doc;
	public int layoutWidth = 100;
	public BoxSpec result;
	public String css;

	public static Test suite() throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
		final TestSuite suite = new TestSuite(LayoutTestSuite.class.getName());
		suite.addTest(loadSuite("block-inline.xml"));
		suite.addTest(loadSuite("before-after.xml"));
		suite.addTest(loadSuite("linebreaks.xml"));
		suite.addTest(loadSuite("tables.xml"));
		return suite;
	}

	public static Test loadSuite(final String filename) throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
		final XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		final TestCaseBuilder builder = new TestCaseBuilder();
		xmlReader.setContentHandler(builder);
		// xmlReader.setEntityResolver(builder);
		final URL url = LayoutTestSuite.class.getResource(filename);
		xmlReader.parse(new InputSource(url.toString()));

		final TestSuite suite = new TestSuite(filename);
		for (final TestCase test : builder.testCases) {
			suite.addTest(test);
		}
		return suite;
	}

	public LayoutTestSuite() {
		super("testLayout");
	}

	@Override
	public String getName() {
		return id;
	}

	public void testLayout() throws Exception {

		final URL url = LayoutTestSuite.class.getResource(css);
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);

		final FakeGraphics g = new FakeGraphics();

		final LayoutContext context = new LayoutContext();
		context.setBoxFactory(new MockBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(ss);
		final CssWhitespacePolicy policy = new CssWhitespacePolicy(ss);

		final DocumentReader docReader = new DocumentReader();
		docReader.setDocumentContentModel(new DocumentContentModel() {
			@Override
			public IWhitespacePolicy getWhitespacePolicy() {
				return policy;
			}
		});
		final Document doc = docReader.read(this.doc);
		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, layoutWidth);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		assertBox(result, rootBox, "");
	}

	private static void assertBox(final BoxSpec boxSpec, final Box box, final String indent) {

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
			assertEquals(boxSpec.text, ((TextBox) box).getText());
		}

		if (!boxSpec.children.isEmpty() && box.getChildren() == null) {
			fail("Expected " + boxSpec.children.size() + " children, but " + boxSpec.className + "'s children is null");
		}

		if (boxSpec.children.size() != box.getChildren().length) {
			System.out.println("Wrong number of child boxes");
			System.out.println("  Expected:");
			for (final BoxSpec childSpec : boxSpec.children) {
				System.out.print("    " + childSpec.className);
				if (childSpec.text != null) {
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
			assertBox(boxSpec.children.get(i), box.getChildren()[i], indent + "  ");
		}

	}

	private static String getPrefixedNameOfElement(final Node node) {
		return node.accept(new BaseNodeVisitorWithResult<String>("") {
			@Override
			public String visit(final Element element) {
				return element.getPrefixedName();
			}
		});
	}

	private static class TestCaseBuilder extends DefaultHandler {

		private List<TestCase> testCases;
		private String css;
		private LayoutTestSuite testCase;
		private BoxSpec boxSpec;
		private Stack<BoxSpec> boxSpecs;
		private boolean inDoc;

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {

			final String s = new String(ch, start, length).trim();
			if (s.length() > 0) {
				if (inDoc) {
					testCase.doc = new String(ch, start, length);
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
				testCases = new ArrayList<TestCase>();
				css = attributes.getValue("css");
				if (css == null) {
					css = "test.css";
				}
				testCase = null;
				boxSpecs = new Stack<BoxSpec>();
			} else if (qName.equals("test")) {
				testCase = new LayoutTestSuite();
				testCase.id = attributes.getValue("id");
				testCase.css = css;
				final String layoutWidth = attributes.getValue("layoutWidth");
				if (layoutWidth != null) {
					testCase.layoutWidth = Integer.parseInt(layoutWidth);
				}
				testCases.add(testCase);
			} else if (qName.equals("doc")) {
				inDoc = true;
			} else if (qName.equals("result")) {
			} else if (qName.equals("box")) {
				final BoxSpec parent = boxSpec;
				boxSpec = new BoxSpec();
				boxSpec.className = attributes.getValue("class");
				boxSpec.element = attributes.getValue("element");
				boxSpec.text = attributes.getValue("text");
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
	}

}
