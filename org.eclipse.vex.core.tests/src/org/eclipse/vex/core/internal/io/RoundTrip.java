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
package org.eclipse.vex.core.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.BaseNodeVisitor;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.dom.Parent;
import org.eclipse.vex.core.internal.dom.Text;

/**
 * This class provides some special assertions for round trip tests.
 * 
 * @author Florian Thienel
 */
public class RoundTrip {

	public static void assertDocumentsEqual(final Document expected, final Document actual) {
		assertEquals(expected.getPublicID(), actual.getPublicID());
		assertEquals(expected.getSystemID(), actual.getSystemID());
		assertContentEqual(expected, actual);
	}

	public static void assertContentEqual(final Parent expected, final Parent actual) {
		assertContentRangeEqual(expected, actual);
		final List<Node> expectedContent = expected.getChildNodes();
		final List<Node> actualContent = actual.getChildNodes();
		assertEquals("children of " + expected, expectedContent.size(), actualContent.size());
		for (int i = 0; i < expectedContent.size(); i++) {
			final Node expectedNode = expectedContent.get(i);
			final Node actualNode = actualContent.get(i);
			assertContentRangeEqual(expectedNode, actualNode);
			assertEquals(expectedNode.getClass(), actualNode.getClass());
			expectedNode.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final Element element) {
					assertElementsEqual((Element) expectedNode, (Element) actualNode);
				}

				@Override
				public void visit(final Comment comment) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}

				@Override
				public void visit(final Text text) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}
			});
		}
	}

	public static void assertContentRangeEqual(final Node expected, final Node actual) {
		assertEquals("content range of " + expected, expected.getRange(), actual.getRange());
	}

	public static void assertElementsEqual(final Element expected, final Element actual) {
		assertEquals("qualified name of " + expected, expected.getQualifiedName(), actual.getQualifiedName());
		assertAttributesEqual(expected, actual);
		assertNamespacesEqual(expected, actual);
		assertContentEqual(expected, actual);
	}

	public static void assertAttributesEqual(final Element expected, final Element actual) {
		final List<QualifiedName> expectedAttrs = expected.getAttributeNames();
		final List<QualifiedName> actualAttrs = actual.getAttributeNames();

		assertEquals("attributes of " + expected, expectedAttrs.size(), actualAttrs.size());
		for (int i = 0; i < expectedAttrs.size(); i++) {
			assertEquals(expectedAttrs.get(i), actualAttrs.get(i));
		}
	}

	public static void assertNamespacesEqual(final Element expected, final Element actual) {
		assertEquals("declared default namespace of " + expected, expected.getDeclaredDefaultNamespaceURI(), actual.getDeclaredDefaultNamespaceURI());

		final Collection<String> expectedNamespacePrefixes = expected.getDeclaredNamespacePrefixes();
		final Collection<String> actualNamespacePrefixes = actual.getDeclaredNamespacePrefixes();
		assertEquals("declared namespaces of " + expected, expectedNamespacePrefixes.size(), actualNamespacePrefixes.size());
		for (final String prefix : expectedNamespacePrefixes) {
			assertTrue("namespace not declared: " + prefix, actualNamespacePrefixes.contains(prefix));
			assertEquals("namespace URI of prefix " + prefix, expected.getNamespaceURI(prefix), actual.getNamespaceURI(prefix));
		}
	}

}
