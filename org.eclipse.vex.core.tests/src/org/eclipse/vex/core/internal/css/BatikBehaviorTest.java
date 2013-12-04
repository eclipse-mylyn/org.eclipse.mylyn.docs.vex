/*******************************************************************************
 * Copyright (c) 2011, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - additional tests
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class BatikBehaviorTest {

	@Test
	public void pseudoElements() throws Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read("plan:before { display: block; font-size: 123px; }");
		final List<Rule> rules = styleSheet.getRules();
		assertEquals(1, rules.size());
		final Rule rule = rules.get(0);
		final Element element = new Element("plan");
		// The rule should match the parent of the pseudo element. See StyleSheet#getApplicableDeclarations
		assertTrue(rule.matches(element));
		final IElement before = styleSheet.getPseudoElement(element, "before", false);
		final Styles beforeStyles = styleSheet.getStyles(before);
		assertEquals("block", beforeStyles.get("display"));
		assertEquals(123.0f, beforeStyles.getFontSize(), 0.0f);
	}

	@Test
	public void pseudoElements_shouldInheritFromParent() throws Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read("plan {font-size: 123px;} plan:before { content: 'test' }");
		final Element element = new Element("plan");
		final IElement before = styleSheet.getPseudoElement(element, "before", false);
		final Styles beforeStyles = styleSheet.getStyles(before);
		assertEquals("test", beforeStyles.getContent(element).get(0));
		assertEquals(123.0f, beforeStyles.getFontSize(), 0.0f);
	}

	@Test
	public void testNamespace() throws Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read("vex|plan {font-size: 123px;} vex|plan:before { content: 'test' }");
		final Element element = new Element(new QualifiedName(Namespace.VEX_NAMESPACE_URI, "plan"));
		final Styles styles = styleSheet.getStyles(element);
		assertEquals(123.0f, styles.getFontSize(), 0.0f);
		final IElement before = styleSheet.getPseudoElement(element, "before", false);
		final Styles beforeStyles = styleSheet.getStyles(before);
		assertEquals("test", beforeStyles.getContent(element).get(0));
		assertEquals(123.0f, beforeStyles.getFontSize(), 0.0f);
	}

	@Test
	public void testNamespaceWithChildSelector() throws Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read("vex|parent {font-size: 123px;} vex|parent > child { content: 'child' } child {content: 'nochild'}");
		final Element element = new Element(new QualifiedName(Namespace.VEX_NAMESPACE_URI, "parent"));
		final Element child = new Element("child");
		final Element nochild = new Element("child");
		child.setParent(element);
		final Styles styles = styleSheet.getStyles(child);
		assertEquals(1, styles.getContent(element).size());
		assertEquals("child", styles.getContent(element).get(0));
		final Styles nochildStyles = styleSheet.getStyles(nochild);
		assertEquals("nochild", nochildStyles.getContent(element).get(0));
	}
}