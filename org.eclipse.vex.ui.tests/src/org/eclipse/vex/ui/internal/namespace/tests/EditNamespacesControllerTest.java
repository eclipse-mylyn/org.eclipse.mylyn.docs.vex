/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.namespace.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.widget.BaseVexWidget;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.core.internal.widget.MockHostComponent;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.namespace.EditNamespacesController;
import org.eclipse.vex.ui.internal.namespace.EditableNamespaceDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class EditNamespacesControllerTest {

	private IVexWidget widget;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());
		widget.setDocument(new Document(new QualifiedName(null, "root")), StyleSheet.NULL);
	}

	@Test
	public void populateFromElement() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName("http://namespace/uri/default", "element"));
		element.declareDefaultNamespace("http://namespace/uri/default");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		element.declareNamespace("ns2", "http://namespace/uri/2");
		final EditNamespacesController controller = new EditNamespacesController(widget);

		assertEquals("http://namespace/uri/default", controller.getDefaultNamespaceURI());

		final List<EditableNamespaceDefinition> namespaces = controller.getNamespaceDefinitions();
		assertEquals(2, namespaces.size());
		assertContainsNamespaceDefinition(new EditableNamespaceDefinition("ns1", "http://namespace/uri/1"), namespaces);
		assertContainsNamespaceDefinition(new EditableNamespaceDefinition("ns2", "http://namespace/uri/2"), namespaces);
	}

	@Test
	public void defaultNamespaceNotNull() throws Exception {
		final EditNamespacesController controller = new EditNamespacesController(widget);
		assertNotNull(controller.getDefaultNamespaceURI());
	}

	@Test
	public void editDefaultNamespace() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName(null, "element"));
		final EditNamespacesController controller = new EditNamespacesController(widget);
		controller.setDefaultNamespaceURI("http://namespace/uri/default");
		assertEquals("http://namespace/uri/default", controller.getDefaultNamespaceURI());
		assertNull(element.getDeclaredDefaultNamespaceURI());

		controller.applyToElement();
		assertEquals("http://namespace/uri/default", element.getDefaultNamespaceURI());
		assertNull(element.getQualifiedName().getQualifier());
	}

	@Test
	public void removeDefaultNamespace() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName("http://namespace/uri/default", "element"));
		element.declareDefaultNamespace("http://namespace/uri/default");
		final EditNamespacesController controller = new EditNamespacesController(widget);

		controller.setDefaultNamespaceURI("");
		controller.applyToElement();
		assertNull(element.getDeclaredDefaultNamespaceURI());
		assertEquals("http://namespace/uri/default", element.getQualifiedName().getQualifier());
	}

	@Test
	public void addNamespaceDefinition() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName(null, "element"));
		final EditNamespacesController controller = new EditNamespacesController(widget);

		assertTrue(controller.getNamespaceDefinitions().isEmpty());
		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());

		final EditableNamespaceDefinition newDefinition = controller.addNamespaceDefinition();
		assertNotNull(newDefinition);

		assertEquals(1, controller.getNamespaceDefinitions().size());
		assertSame(newDefinition, controller.getNamespaceDefinitions().get(0));
		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());

		newDefinition.setPrefix("ns1");
		newDefinition.setUri("http://namespace/uri/1");

		controller.applyToElement();
		assertEquals(1, element.getDeclaredNamespacePrefixes().size());
	}

	@Test
	public void removeNamespaceDefinition() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName(null, "element"));
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = new EditNamespacesController(widget);

		controller.removeNamespaceDefinition(controller.getNamespaceDefinitions().get(0));

		assertTrue(controller.getNamespaceDefinitions().isEmpty());
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns1"));

		controller.applyToElement();
		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());
	}

	@Test
	public void editNamespacePrefix() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName(null, "element"));
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = new EditNamespacesController(widget);
		final EditableNamespaceDefinition definition = controller.getNamespaceDefinitions().get(0);

		definition.setPrefix("ns2");
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns1"));
		assertEquals(1, element.getDeclaredNamespacePrefixes().size());

		controller.applyToElement();
		assertFalse(element.getDeclaredNamespacePrefixes().contains("ns1"));
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns2"));
		assertEquals(1, element.getDeclaredNamespacePrefixes().size());
	}

	@Test
	public void editNamespaceUri() throws Exception {
		final IElement element = widget.insertElement(new QualifiedName(null, "element"));
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = new EditNamespacesController(widget);
		final EditableNamespaceDefinition definition = controller.getNamespaceDefinitions().get(0);

		definition.setUri("http://namespace/uri/2");
		assertEquals("http://namespace/uri/1", element.getNamespaceURI("ns1"));

		controller.applyToElement();
		assertEquals("http://namespace/uri/2", element.getNamespaceURI("ns1"));
		assertEquals(1, element.getDeclaredNamespacePrefixes().size());
	}

	private static void assertContainsNamespaceDefinition(final EditableNamespaceDefinition expected, final List<EditableNamespaceDefinition> actualList) {
		for (final EditableNamespaceDefinition definition : actualList) {
			if (expected.getPrefix().equals(definition.getPrefix()) && expected.getUri().equals(definition.getUri())) {
				return;
			}
		}
		fail("namespace definition not found: " + expected.getPrefix() + "=" + expected.getUri());
	}

}
