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
package org.eclipse.vex.core.internal.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Before;
import org.junit.Test;

public class CssTest {

	@Before
	public void setUp() throws Exception {
		DisplayDevice.setCurrent(new MockDisplayDevice(90, 90));
	}

	@Test
	public void testBorderColor() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		Styles styles;
		final Color red = new Color(255, 0, 0);
		final Color green = new Color(0, 128, 0);
		final Color blue = new Color(0, 0, 255);
		final Color white = new Color(255, 255, 255);

		styles = ss.getStyles(new Element("borderColor1"));
		assertEquals(red, styles.getBorderTopColor());
		assertEquals(red, styles.getBorderLeftColor());
		assertEquals(red, styles.getBorderRightColor());
		assertEquals(red, styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("borderColor2"));
		assertEquals(red, styles.getBorderTopColor());
		assertEquals(green, styles.getBorderLeftColor());
		assertEquals(green, styles.getBorderRightColor());
		assertEquals(red, styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("borderColor3"));
		assertEquals(red, styles.getBorderTopColor());
		assertEquals(green, styles.getBorderLeftColor());
		assertEquals(green, styles.getBorderRightColor());
		assertEquals(blue, styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("borderColor4"));
		assertEquals(red, styles.getBorderTopColor());
		assertEquals(green, styles.getBorderRightColor());
		assertEquals(blue, styles.getBorderBottomColor());
		assertEquals(white, styles.getBorderLeftColor());

	}

	@Test
	public void testBorderStyle() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		Styles styles;

		styles = ss.getStyles(new Element("borderStyle1"));
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(CSS.SOLID, styles.getBorderLeftStyle());
		assertEquals(CSS.SOLID, styles.getBorderRightStyle());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());

		styles = ss.getStyles(new Element("borderStyle2"));
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(CSS.DOTTED, styles.getBorderLeftStyle());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());

		styles = ss.getStyles(new Element("borderStyle3"));
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(CSS.DOTTED, styles.getBorderLeftStyle());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());
		assertEquals(CSS.DASHED, styles.getBorderBottomStyle());

		styles = ss.getStyles(new Element("borderStyle4"));
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());
		assertEquals(CSS.DASHED, styles.getBorderBottomStyle());
		assertEquals(CSS.OUTSET, styles.getBorderLeftStyle());

	}

	@Test
	public void testBorderWidth() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		Styles styles;

		styles = ss.getStyles(new Element("borderWidth1"));
		assertEquals(1, styles.getBorderTopWidth());
		assertEquals(1, styles.getBorderLeftWidth());
		assertEquals(1, styles.getBorderRightWidth());
		assertEquals(1, styles.getBorderBottomWidth());

		styles = ss.getStyles(new Element("borderWidth2"));
		assertEquals(1, styles.getBorderTopWidth());
		assertEquals(2, styles.getBorderLeftWidth());
		assertEquals(2, styles.getBorderRightWidth());
		assertEquals(1, styles.getBorderBottomWidth());

		styles = ss.getStyles(new Element("borderWidth3"));
		assertEquals(1, styles.getBorderTopWidth());
		assertEquals(2, styles.getBorderLeftWidth());
		assertEquals(2, styles.getBorderRightWidth());
		assertEquals(3, styles.getBorderBottomWidth());

		styles = ss.getStyles(new Element("borderWidth4"));
		assertEquals(1, styles.getBorderTopWidth());
		assertEquals(2, styles.getBorderRightWidth());
		assertEquals(3, styles.getBorderBottomWidth());
		assertEquals(4, styles.getBorderLeftWidth());

	}

	@Test
	public void testDefaults() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(new Element("defaults"));

		assertEquals(15.0f, styles.getFontSize(), 0.1);

		assertNull(styles.getBackgroundColor());

		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(0, styles.getBorderBottomWidth());

		assertEquals(new Color(0, 0, 0), styles.getBorderLeftColor());
		assertEquals(CSS.NONE, styles.getBorderLeftStyle());
		assertEquals(0, styles.getBorderLeftWidth());

		assertEquals(new Color(0, 0, 0), styles.getBorderRightColor());
		assertEquals(CSS.NONE, styles.getBorderRightStyle());
		assertEquals(0, styles.getBorderRightWidth());

		assertEquals(new Color(0, 0, 0), styles.getBorderTopColor());
		assertEquals(CSS.NONE, styles.getBorderTopStyle());
		assertEquals(0, styles.getBorderTopWidth());

		assertEquals(new Color(0, 0, 0), styles.getColor());
		assertEquals(CSS.INLINE, styles.getDisplay());

		assertEquals(0, styles.getMarginBottom().get(10));
		assertEquals(0, styles.getMarginLeft().get(10));
		assertEquals(0, styles.getMarginRight().get(10));
		assertEquals(0, styles.getMarginTop().get(10));

		assertEquals(0, styles.getPaddingBottom().get(10));
		assertEquals(0, styles.getPaddingLeft().get(10));
		assertEquals(0, styles.getPaddingRight().get(10));
		assertEquals(0, styles.getPaddingTop().get(10));
	}

	@Test
	public void testDefaultInheritance() throws Exception {
		final Document doc = new Document(new QualifiedName(null, "simple"));
		final Element defaults = doc.insertElement(2, new QualifiedName(null, "defaults"));

		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(defaults);

		assertEquals(12.5f, styles.getFontSize(), 0.1);

		assertNull(styles.getBackgroundColor());

		assertEquals(new Color(0, 128, 0), styles.getBorderBottomColor());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(0, styles.getBorderBottomWidth());

		assertEquals(new Color(0, 128, 0), styles.getBorderLeftColor());
		assertEquals(CSS.NONE, styles.getBorderLeftStyle());
		assertEquals(0, styles.getBorderLeftWidth());

		assertEquals(new Color(0, 128, 0), styles.getBorderRightColor());
		assertEquals(CSS.NONE, styles.getBorderRightStyle());
		assertEquals(0, styles.getBorderRightWidth());

		assertEquals(new Color(0, 128, 0), styles.getBorderTopColor());
		assertEquals(CSS.NONE, styles.getBorderTopStyle());
		assertEquals(0, styles.getBorderTopWidth());

		assertEquals(new Color(0, 128, 0), styles.getColor());
		assertEquals(CSS.INLINE, styles.getDisplay());

		assertEquals(0, styles.getMarginBottom().get(10));
		assertEquals(0, styles.getMarginLeft().get(10));
		assertEquals(0, styles.getMarginRight().get(10));
		assertEquals(0, styles.getMarginTop().get(10));

		assertEquals(0, styles.getPaddingBottom().get(10));
		assertEquals(0, styles.getPaddingLeft().get(10));
		assertEquals(0, styles.getPaddingRight().get(10));
		assertEquals(0, styles.getPaddingTop().get(10));
	}

	@Test
	public void testExpandBorder() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		Styles styles;

		styles = ss.getStyles(new Element("expandBorder"));
		assertEquals(2, styles.getBorderBottomWidth());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderBottomColor());
		assertEquals(2, styles.getBorderLeftWidth());
		assertEquals(CSS.SOLID, styles.getBorderLeftStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderLeftColor());
		assertEquals(2, styles.getBorderRightWidth());
		assertEquals(CSS.SOLID, styles.getBorderRightStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderRightColor());
		assertEquals(2, styles.getBorderTopWidth());
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderTopColor());

		styles = ss.getStyles(new Element("expandBorderBottom"));
		assertEquals(2, styles.getBorderBottomWidth());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderBottomColor());
		assertEquals(0, styles.getBorderLeftWidth());
		assertEquals(CSS.NONE, styles.getBorderLeftStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderLeftColor());
		assertEquals(0, styles.getBorderRightWidth());
		assertEquals(CSS.NONE, styles.getBorderRightStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderRightColor());
		assertEquals(0, styles.getBorderTopWidth());
		assertEquals(CSS.NONE, styles.getBorderTopStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderTopColor());

		styles = ss.getStyles(new Element("expandBorderLeft"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(2, styles.getBorderLeftWidth());
		assertEquals(CSS.SOLID, styles.getBorderLeftStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderLeftColor());
		assertEquals(0, styles.getBorderRightWidth());
		assertEquals(CSS.NONE, styles.getBorderRightStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderRightColor());
		assertEquals(0, styles.getBorderTopWidth());
		assertEquals(CSS.NONE, styles.getBorderTopStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderTopColor());

		styles = ss.getStyles(new Element("expandBorderRight"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(0, styles.getBorderLeftWidth());
		assertEquals(CSS.NONE, styles.getBorderLeftStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderLeftColor());
		assertEquals(2, styles.getBorderRightWidth());
		assertEquals(CSS.SOLID, styles.getBorderRightStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderRightColor());
		assertEquals(0, styles.getBorderTopWidth());
		assertEquals(CSS.NONE, styles.getBorderTopStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderTopColor());

		styles = ss.getStyles(new Element("expandBorderTop"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(0, styles.getBorderLeftWidth());
		assertEquals(CSS.NONE, styles.getBorderLeftStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderLeftColor());
		assertEquals(0, styles.getBorderRightWidth());
		assertEquals(CSS.NONE, styles.getBorderRightStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderRightColor());
		assertEquals(2, styles.getBorderTopWidth());
		assertEquals(CSS.SOLID, styles.getBorderTopStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderTopColor());

		styles = ss.getStyles(new Element("expandBorder1"));
		assertEquals(2, styles.getBorderBottomWidth());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("expandBorder2"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("expandBorder3"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("expandBorder4"));
		assertEquals(3, styles.getBorderBottomWidth());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("expandBorder5"));
		assertEquals(3, styles.getBorderBottomWidth());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());

		styles = ss.getStyles(new Element("expandBorder6"));
		assertEquals(0, styles.getBorderBottomWidth());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(new Color(255, 0, 0), styles.getBorderBottomColor());

	}

	@Test
	public void testExpandMargins() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");

		Styles styles = ss.getStyles(new Element("margin1"));
		assertEquals(10, styles.getMarginTop().get(67));
		assertEquals(10, styles.getMarginLeft().get(67));
		assertEquals(10, styles.getMarginRight().get(67));
		assertEquals(10, styles.getMarginBottom().get(67));

		styles = ss.getStyles(new Element("margin2"));
		assertEquals(10, styles.getMarginTop().get(67));
		assertEquals(20, styles.getMarginLeft().get(67));
		assertEquals(20, styles.getMarginRight().get(67));
		assertEquals(10, styles.getMarginBottom().get(67));

		styles = ss.getStyles(new Element("margin3"));
		assertEquals(10, styles.getMarginTop().get(67));
		assertEquals(20, styles.getMarginLeft().get(67));
		assertEquals(20, styles.getMarginRight().get(67));
		assertEquals(30, styles.getMarginBottom().get(67));

		styles = ss.getStyles(new Element("margin4"));
		assertEquals(10, styles.getMarginTop().get(67));
		assertEquals(20, styles.getMarginRight().get(67));
		assertEquals(30, styles.getMarginBottom().get(67));
		assertEquals(40, styles.getMarginLeft().get(67));
	}

	@Test
	public void testExtras() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(new Element("extras"));

		assertEquals(new Color(0, 255, 0), styles.getBackgroundColor());

		assertEquals(new Color(128, 0, 0), styles.getBorderBottomColor());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());

		assertEquals(new Color(0, 0, 128), styles.getBorderLeftColor());
		assertEquals(CSS.DASHED, styles.getBorderLeftStyle());

		assertEquals(new Color(128, 128, 0), styles.getBorderRightColor());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());

		assertEquals(new Color(128, 0, 128), styles.getBorderTopColor());
		assertEquals(CSS.DOUBLE, styles.getBorderTopStyle());

		assertEquals(new Color(255, 0, 0), styles.getColor());
		assertEquals(CSS.INLINE, styles.getDisplay());
	}

	@Test
	public void testExtras2() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(new Element("extras2"));

		assertEquals(new Color(192, 192, 192), styles.getBackgroundColor());

		assertEquals(new Color(0, 128, 128), styles.getBorderBottomColor());
		assertEquals(CSS.NONE, styles.getBorderBottomStyle());
		assertEquals(0, styles.getBorderBottomWidth());

		assertEquals(new Color(255, 255, 255), styles.getBorderLeftColor());
		assertEquals(CSS.GROOVE, styles.getBorderLeftStyle());

		assertEquals(new Color(255, 255, 0), styles.getBorderRightColor());
		assertEquals(CSS.RIDGE, styles.getBorderRightStyle());

		assertEquals(CSS.INSET, styles.getBorderTopStyle());
	}

	@Test
	public void testFontSize() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		Styles styles;

		styles = ss.getStyles(new Element("medium"));
		assertEquals(15.0f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("small"));
		assertEquals(12.5f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("xsmall"));
		assertEquals(10.4f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("xxsmall"));
		assertEquals(8.7f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("large"));
		assertEquals(18.0f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("xlarge"));
		assertEquals(21.6f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("xxlarge"));
		assertEquals(25.9, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("smaller"));
		assertEquals(12.5f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("font100pct"));
		assertEquals(15.0f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("font80pct"));
		assertEquals(12.0f, styles.getFontSize(), 0.1);

		styles = ss.getStyles(new Element("font120pct"));
		assertEquals(18.0f, styles.getFontSize(), 0.1);

	}

	@Test
	public void testForcedInheritance() throws Exception {
		final Document doc = new Document(new QualifiedName(null, "simple"));
		final Element inherit = doc.insertElement(2, new QualifiedName(null, "inherit"));

		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(inherit);

		assertEquals(12.5f, styles.getFontSize(), 0.1);

		assertEquals(new Color(0, 255, 255), styles.getBackgroundColor());

		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(1, styles.getBorderBottomWidth());

		assertEquals(new Color(0, 0, 255), styles.getBorderLeftColor());
		assertEquals(CSS.DASHED, styles.getBorderLeftStyle());
		assertEquals(3, styles.getBorderLeftWidth());

		assertEquals(new Color(255, 0, 255), styles.getBorderRightColor());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());
		assertEquals(5, styles.getBorderRightWidth());

		assertEquals(new Color(128, 128, 128), styles.getBorderTopColor());
		assertEquals(CSS.DOUBLE, styles.getBorderTopStyle());
		assertEquals(1, styles.getBorderTopWidth());

		assertEquals(new Color(0, 128, 0), styles.getColor());
		assertEquals(CSS.BLOCK, styles.getDisplay());

		assertEquals(3543, styles.getMarginBottom().get(10));
		assertEquals(0, styles.getMarginLeft().get(10));
		assertEquals(125, styles.getMarginRight().get(10));
		assertEquals(75, styles.getMarginTop().get(10));

		assertEquals(450, styles.getPaddingBottom().get(10));
		assertEquals(4252, styles.getPaddingLeft().get(10));
		assertEquals(120, styles.getPaddingRight().get(10));
		assertEquals(19, styles.getPaddingTop().get(10));
	}

	@Test
	public void testImportant() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("testImportant.css");
		final Element a = new Element("a");
		final Styles styles = ss.getStyles(a);

		final Color black = new Color(0, 0, 0);
		// Color white = new Color(255, 255, 255);
		// Color red = new Color(255, 0, 0);
		final Color blue = new Color(0, 0, 255);

		assertEquals(black, styles.getBackgroundColor());
		assertEquals(black, styles.getColor());
		assertEquals(blue, styles.getBorderTopColor());

	}

	@Test
	public void testMarginInheritance() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Element root = new Element("margin1");
		final Element child = new Element("defaults");
		child.setParent(root);
		final Styles styles = ss.getStyles(child);

		assertEquals(0, styles.getMarginTop().get(67));
		assertEquals(0, styles.getMarginLeft().get(67));
		assertEquals(0, styles.getMarginRight().get(67));
		assertEquals(0, styles.getMarginBottom().get(67));
	}

	@Test
	public void testSimple() throws Exception {
		final StyleSheet ss = parseStyleSheetResource("test2.css");
		final Styles styles = ss.getStyles(new Element("simple"));

		assertEquals(12.5f, styles.getFontSize(), 0.1);

		assertEquals(new Color(0, 255, 255), styles.getBackgroundColor());

		assertEquals(new Color(0, 0, 0), styles.getBorderBottomColor());
		assertEquals(CSS.SOLID, styles.getBorderBottomStyle());
		assertEquals(1, styles.getBorderBottomWidth());

		assertEquals(new Color(0, 0, 255), styles.getBorderLeftColor());
		assertEquals(CSS.DASHED, styles.getBorderLeftStyle());
		assertEquals(3, styles.getBorderLeftWidth());

		assertEquals(new Color(255, 0, 255), styles.getBorderRightColor());
		assertEquals(CSS.DOTTED, styles.getBorderRightStyle());
		assertEquals(5, styles.getBorderRightWidth());

		assertEquals(new Color(128, 128, 128), styles.getBorderTopColor());
		assertEquals(CSS.DOUBLE, styles.getBorderTopStyle());
		assertEquals(1, styles.getBorderTopWidth());

		assertEquals(new Color(0, 128, 0), styles.getColor());
		assertEquals(CSS.BLOCK, styles.getDisplay());

		assertEquals(3543, styles.getMarginBottom().get(10));
		assertEquals(0, styles.getMarginLeft().get(10));
		assertEquals(125, styles.getMarginRight().get(10));
		assertEquals(75, styles.getMarginTop().get(10));

		assertEquals(450, styles.getPaddingBottom().get(10));
		assertEquals(4252, styles.getPaddingLeft().get(10));
		assertEquals(120, styles.getPaddingRight().get(10));
		assertEquals(19, styles.getPaddingTop().get(10));

	}

	private StyleSheet parseStyleSheetResource(final String resource) throws java.io.IOException {

		final URL url = this.getClass().getResource(resource);
		final StyleSheetReader reader = new StyleSheetReader();
		return reader.read(url);

	}
}
