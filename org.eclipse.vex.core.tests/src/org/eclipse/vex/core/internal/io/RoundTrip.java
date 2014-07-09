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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * This class provides some special assertions for round trip tests.
 *
 * @author Florian Thienel
 */
public class RoundTrip {

	public static void assertDocumentsEqual(final IDocument expected, final IDocument actual) {
		assertEquals(expected.getPublicID(), actual.getPublicID());
		assertEquals(expected.getSystemID(), actual.getSystemID());
		assertContentEqual(expected, actual);
	}

	public static void assertContentEqual(final IParent expected, final IParent actual) {
		assertContentRangeEqual(expected, actual);
		final Iterator<INode> expectedChildren = expected.children().iterator();
		final Iterator<INode> actualChildren = actual.children().iterator();
		while (expectedChildren.hasNext() && actualChildren.hasNext()) {
			final INode expectedNode = expectedChildren.next();
			final INode actualNode = actualChildren.next();
			assertContentRangeEqual(expectedNode, actualNode);
			assertEquals(expectedNode.getClass(), actualNode.getClass());
			expectedNode.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final IElement element) {
					assertElementsEqual((IElement) expectedNode, (IElement) actualNode);
				}

				@Override
				public void visit(final IComment comment) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}

				@Override
				public void visit(final IText text) {
					assertEquals(expectedNode.getText(), actualNode.getText());
				}
			});
		}
		assertFalse("more children expected", expectedChildren.hasNext());
		assertFalse("less children expected", actualChildren.hasNext());
	}

	public static void assertContentRangeEqual(final INode expected, final INode actual) {
		assertEquals("content range of " + expected, expected.getRange(), actual.getRange());
	}

	public static void assertElementsEqual(final IElement expected, final IElement actual) {
		assertEquals("qualified name of " + expected, expected.getQualifiedName(), actual.getQualifiedName());
		assertAttributesEqual(expected, actual);
		assertNamespacesEqual(expected, actual);
		assertContentEqual(expected, actual);
	}

	public static void assertAttributesEqual(final IElement expected, final IElement actual) {
		final Iterator<QualifiedName> expectedAttrs = expected.getAttributeNames().iterator();
		final Iterator<QualifiedName> actualAttrs = actual.getAttributeNames().iterator();

		while (expectedAttrs.hasNext() && actualAttrs.hasNext()) {
			assertEquals("attributes of " + expected, expectedAttrs.next(), actualAttrs.next());
		}
		assertFalse(expected + ": expected more attributes", expectedAttrs.hasNext());
		assertFalse(expected + ": expected less attributes", actualAttrs.hasNext());
	}

	public static void assertNamespacesEqual(final IElement expected, final IElement actual) {
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
