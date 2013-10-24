/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Test;

public class AttributeTest {

	@Test
	public void testWithoutNamespace() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "a"));
		final IElement a = doc.getRootElement();
		final IElement ns = doc.insertElement(2, new QualifiedName("http://namespace/uri", "b"));

		a.setAttribute("attr1", "attr1Val");
		assertEquals("Expected attribute count", 1, a.getAttributes().size());
		assertEquals("attr1Val", a.getAttribute("attr1").getValue());
		assertEquals("attr1Val", a.getAttributeValue("attr1"));
		a.removeAttribute("attr1");
		assertEquals("Expected attribute count", 0, a.getAttributes().size());

		ns.setAttribute("attr2", "attr2Val");
		assertEquals("Expected attribute count", 1, ns.getAttributes().size());
		assertEquals("attr2Val", ns.getAttribute("attr2").getValue());
		assertEquals("attr2Val", ns.getAttributeValue("attr2"));
		ns.removeAttribute("attr2");
		assertEquals("Expected attribute count", 0, ns.getAttributes().size());
	}

	@Test
	public void testWithNamespace() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "a"));
		final IElement a = doc.getRootElement();
		final IElement ns = doc.insertElement(2, new QualifiedName("http://namespace/uri", "b"));

		a.setAttribute(new QualifiedName("http://namespace/attr", "attr1"), "attr1Val");
		assertEquals("Expected attribute count", 1, a.getAttributes().size());
		assertEquals("attr1Val", a.getAttribute(new QualifiedName("http://namespace/attr", "attr1")).getValue());
		assertEquals("attr1Val", a.getAttributeValue(new QualifiedName("http://namespace/attr", "attr1")));
		a.removeAttribute(new QualifiedName("http://namespace/attr", "attr1"));
		assertEquals("Expected attribute count", 0, a.getAttributes().size());

		ns.setAttribute(new QualifiedName("http://namespace/attr", "attr2"), "attr2Val");
		assertEquals("Expected attribute count", 1, ns.getAttributes().size());
		assertEquals("attr2Val", ns.getAttribute(new QualifiedName("http://namespace/attr", "attr2")).getValue());
		assertEquals("attr2Val", ns.getAttributeValue(new QualifiedName("http://namespace/attr", "attr2")));
		ns.removeAttribute(new QualifiedName("http://namespace/attr", "attr2"));
		assertEquals("Expected attribute count", 0, ns.getAttributes().size());
	}
}
