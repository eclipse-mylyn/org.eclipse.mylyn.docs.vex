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
package org.eclipse.vex.ui.internal.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.editor.NoStyleForDoctypeException;
import org.eclipse.vex.ui.internal.editor.VexDocumentContentModel;
import org.junit.Before;
import org.junit.Test;

public class VexDocumentContentModelTest {

	private VexDocumentContentModel model;

	@Before
	public void setUp() throws Exception {
		model = new VexDocumentContentModel();
	}

	@Test
	public void resolveDoctypeByPublicId() throws Exception {
		try {
			model.initialize(null, "-//Vex//DTD Test//EN", null, new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		} catch (final NoStyleForDoctypeException e) {
			// We are not interested in the StyleSheet here
		}
		assertTrue(model.isDtdAssigned());
		assertNotNull(model.getDocumentType());
		assertEquals("test doctype", model.getDocumentType().getName());
	}

	@Test
	public void resolveDoctypeBySystemId() throws Exception {
		try {
			model.initialize(null, "UnknownPublicId", "test.dtd", new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		} catch (final NoStyleForDoctypeException e) {
			// We are not interested in the StyleSheet here
		}
		assertTrue(model.isDtdAssigned());
		assertNotNull(model.getDocumentType());
		assertEquals("test doctype", model.getDocumentType().getName());
	}

	@Test
	public void resolveDoctypeByNamespace() throws Exception {
		try {
			model.initialize(null, null, null, new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		} catch (final NoStyleForDoctypeException e) {
			// We are not interested in the StyleSheet here
		}
		assertFalse(model.isDtdAssigned());
		assertNotNull(model.getDocumentType());
		assertEquals("test schema doctype", model.getDocumentType().getName());
	}

	@Test
	public void resolveCSSByPublicId() throws Exception {
		model.initialize(null, "-//Vex//DTD Test//EN", null, new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		assertTrue(model.isDtdAssigned());
		final Style style = model.getStyle();
		assertNotNull(style);
		assertTrue(style.getResourceUri().toString().endsWith("test.css"));
	}

	@Test
	public void resolveCSSBySystemId() throws Exception {
		model.initialize(null, null, "test.dtd", new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		assertTrue(model.isDtdAssigned());
		final Style style = model.getStyle();
		assertNotNull(style);
		assertTrue(style.getResourceUri().toString().endsWith("test.css"));
	}

	@Test
	public void resolveCSSByPluginId() throws Exception {
		model.initialize(null, null, null, new Element(new QualifiedName("http://org.eclipse.vex/namespace2", "rootElement")));
		final Style style = model.getStyle();
		assertNotNull(style);
		assertTrue(style.getResourceUri().toString().endsWith("test-doctype-id.css"));
	}

	@Test
	public void resolveCSSByNamespace() throws Exception {
		model.initialize(null, null, null, new Element(new QualifiedName("http://org.eclipse.vex/namespace", "rootElement")));
		assertFalse(model.isDtdAssigned());
		final Style style = model.getStyle();
		assertNotNull(style);
		assertTrue(style.getResourceUri().toString().endsWith("test-schema.css"));
	}
}
