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
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.widget.MockHostComponent;
import org.eclipse.vex.core.internal.widget.VexWidgetImpl;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.namespace.EditNamespacesController;
import org.eclipse.vex.ui.internal.namespace.EditableNamespaceDefinition;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class EditNamespacesControllerTest {

	@Test
	public void populateFromElement() throws Exception {
		final Element element = new Element(new QualifiedName("http://namespace/uri/default", "element"));
		element.declareDefaultNamespace("http://namespace/uri/default");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		element.declareNamespace("ns2", "http://namespace/uri/2");
		final EditNamespacesController controller = createController(element);

		assertEquals("http://namespace/uri/default", controller.getDefaultNamespaceURI());

		final List<EditableNamespaceDefinition> namespaces = controller.getNamespaceDefinitions();
		assertEquals(2, namespaces.size());
		assertContainsNamespaceDefinition(new EditableNamespaceDefinition("ns1", "http://namespace/uri/1"), namespaces);
		assertContainsNamespaceDefinition(new EditableNamespaceDefinition("ns2", "http://namespace/uri/2"), namespaces);
	}

	private EditNamespacesController createController(final IElement element) {
		final VexWidgetImpl widget = new VexWidgetImpl(new MockHostComponent()) {
			@Override
			public IElement getCurrentElement() {
				return element;
			}
		};
		return new EditNamespacesController(widget);
	}

	@Test
	public void defaultNamespaceNotNull() throws Exception {
		final EditNamespacesController controller = createController(new Element("element"));
		assertNotNull(controller.getDefaultNamespaceURI());
	}

	@Test
	public void editDefaultNamespace() throws Exception {
		final Element element = new Element("element");
		final EditNamespacesController controller = createController(element);
		controller.setDefaultNamespaceURI("http://namespace/uri/default");
		assertEquals("http://namespace/uri/default", controller.getDefaultNamespaceURI());
		assertNull(element.getDeclaredDefaultNamespaceURI());

		controller.applyToElement();
		assertEquals("http://namespace/uri/default", element.getDefaultNamespaceURI());
		assertNull(element.getQualifiedName().getQualifier());
	}

	@Test
	public void removeDefaultNamespace() throws Exception {
		final Element element = new Element(new QualifiedName("http://namespace/uri/default", "element"));
		element.declareDefaultNamespace("http://namespace/uri/default");
		final EditNamespacesController controller = createController(element);

		controller.setDefaultNamespaceURI("");
		controller.applyToElement();
		assertNull(element.getDeclaredDefaultNamespaceURI());
		assertEquals("http://namespace/uri/default", element.getQualifiedName().getQualifier());
	}

	@Test
	public void addNamespaceDefinition() throws Exception {
		final Element element = new Element("element");
		final EditNamespacesController controller = createController(element);

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
		final Element element = new Element("element");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = createController(element);

		controller.removeNamespaceDefinition(controller.getNamespaceDefinitions().get(0));

		assertTrue(controller.getNamespaceDefinitions().isEmpty());
		assertTrue(element.getDeclaredNamespacePrefixes().contains("ns1"));

		controller.applyToElement();
		assertTrue(element.getDeclaredNamespacePrefixes().isEmpty());
	}

	@Test
	public void editNamespacePrefix() throws Exception {
		final Element element = new Element("element");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = createController(element);
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
		final Element element = new Element("element");
		element.declareNamespace("ns1", "http://namespace/uri/1");
		final EditNamespacesController controller = createController(element);
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
