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
 *     Florian Thienel - bug 299999 - completed implementation of validation
 *     Carsten Hiesserich - tests for attribute namespaces
 *******************************************************************************/
package org.eclipse.vex.core.internal.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.eclipse.vex.core.provisional.dom.AttributeDefinition;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Before;
import org.junit.Test;

public class DTDValidatorTest {

	private IValidator validator = null;
	private static final QualifiedName XI = new QualifiedName(Namespace.XINCLUDE_NAMESPACE_URI, "include");

	@Before
	public void setUp() {
		try {
			validator = new WTPVEXValidator(TestResources.TEST_DTD);
		} catch (final Exception ex) {
			fail("Failed to load test1.dtd");
		}
	}

	@Test
	public void testEnumAttribute() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		final IElement sectionElement = doc.getRootElement();
		final AttributeDefinition ad = getAttributeMap(sectionElement).get(new QualifiedName(null, "enatt"));
		assertEquals("enatt", ad.getName());

		final String[] enumValues = ad.getValues();
		assertEquals(3, enumValues.length);
	}

	// public void testEmptyDTD() throws Exception {
	// VEXDocument doc;
	// Set expected;
	//
	// doc = new Document(new RootElement("empty"));
	// doc.setValidator(validator);
	// assertEquals(Collections.EMPTY_SET, getValidItemsAt(doc, 1));
	// }

	// public void testAnyDTD() throws Exception {
	// VEXDocument doc;
	// Set expected;
	//
	// doc = new Document(new RootElement("any"));
	// doc.setValidator(validator);
	// Set anySet = new HashSet();
	// anySet.add(Validator.PCDATA);
	// anySet.add("any");
	// anySet.add("empty");
	// anySet.add("section");
	// anySet.add("title");
	// anySet.add("para");
	// anySet.add("emphasis");
	// assertEquals(anySet, getValidItemsAt(doc, 1));
	//
	// }

	@Test
	public void testSectionElement() {
		// <section> <title> a b </title> <para> </para> </section>
		// 1 2 3 4 5 6 7
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		doc.insertElement(2, new QualifiedName(null, "title"));
		doc.insertText(3, "ab");
		doc.insertElement(6, new QualifiedName(null, "para"));

		assertValidItemsAt(doc, 0);
		assertValidItemsAt(doc, 1);
		assertValidItemsAt(doc, 2, "title", "para");
		assertValidItemsAt(doc, 3, "#PCDATA");
		assertValidItemsAt(doc, 4, "#PCDATA");
		assertValidItemsAt(doc, 5, "#PCDATA");
		assertValidItemsAt(doc, 6, "title", "para");
		assertValidItemsAt(doc, 7, "emphasis", "pre", "#PCDATA");
		assertValidItemsAt(doc, 8, "title", "para");
	}

	@Test
	public void testOneKindOfChild() {
		final IDocument doc = new Document(new QualifiedName(null, "one-kind-of-child"));
		doc.setValidator(validator);
		assertValidItemsAt(doc, 2, "section");
	}

	private static void assertValidItemsAt(final IDocument doc, final int offset, final String... expectedItems) {
		final Set<QualifiedName> expected = new HashSet<QualifiedName>(expectedItems.length);
		for (final String expectedItem : expectedItems) {
			expected.add(new QualifiedName(null, expectedItem));
		}

		IElement element = doc.getElementForInsertionAt(offset);
		if (element == null) {
			assertEquals(0, expectedItems.length);
			return;
		}

		if (offset == element.getStartOffset()) {
			element = element.getParentElement();
		}

		final Set<QualifiedName> validItems = doc.getValidator().getValidItems(element);
		assertEquals(expected, validItems);
	}

	@Test
	public void testSequences() {
		assertFullyValidSequence("title", "#PCDATA");
		assertFullyValidSequence("para", "#PCDATA");

		assertInvalidSequence("empty", "#PCDATA");

		assertFullyValidSequence("index", "para");

		assertPartiallyValidSequence("section", "title"); // partially valid, para is still missing
		assertFullyValidSequence("section", "title", "para");
		assertFullyValidSequence("section", "para");
		assertFullyValidSequence("section", "para", "para");
		assertInvalidSequence("section", "para", "title");
		assertInvalidSequence("section", "title", "title");
		assertInvalidSequence("section", "title", "title", "para");
		assertInvalidSequence("section", "title", "#PCDATA");

		assertFullyValidSequence("document", "preface", "section", "index");
		assertFullyValidSequence("document", "title", "preface", "section", "index");
		assertFullyValidSequence("document", "title", "preface", "section", "section", "section", "index");
		assertPartiallyValidSequence("document", "title", "preface");
		assertPartiallyValidSequence("document", "preface", "section");
		assertPartiallyValidSequence("document", "preface", "section", "section");

		assertInvalidSequence("document", "title", "index");
		assertInvalidSequence("document", "title", "preface", "index");
		assertInvalidSequence("document", "preface", "index");
	}

	private void assertFullyValidSequence(final String element, final String... sequence) {
		// fully includes partially
		assertValidSequence(true, element, true, true, sequence);
	}

	private void assertPartiallyValidSequence(final String element, final String... sequence) {
		// as partial sequence valid...
		assertValidSequence(true, element, false, true, sequence);

		// ... but as full sequence invalid
		assertValidSequence(false, element, true, false, sequence);
	}

	private void assertInvalidSequence(final String element, final String... sequence) {
		// partially _and_ fully
		assertValidSequence(false, element, true, true, sequence);
	}

	private void assertValidSequence(final boolean expected, final String element, final boolean validateFully, final boolean validatePartially, final String... sequence) {
		final QualifiedName elementName = new QualifiedName(null, element);
		for (int i = 0; i < sequence.length; i++) {
			final List<QualifiedName> prefix = createPrefix(i, sequence);
			final List<QualifiedName> toInsert = Collections.singletonList(new QualifiedName(null, sequence[i]));
			final List<QualifiedName> suffix = createSuffix(i, sequence);

			if (validateFully) {
				assertEquals(expected, validator.isValidSequence(elementName, prefix, toInsert, suffix, false));
			}
			if (validatePartially) {
				assertEquals(expected, validator.isValidSequence(elementName, prefix, toInsert, suffix, true));
			}
		}
	}

	private void assertIsValidSequence(final QualifiedName parentElement, final QualifiedName... sequence) {
		for (int i = 0; i < sequence.length; i++) {
			final List<QualifiedName> prefix = createPrefix(i, sequence);
			final List<QualifiedName> toInsert = Collections.singletonList(sequence[i]);
			final List<QualifiedName> suffix = createSuffix(i, sequence);

			assertTrue(validator.isValidSequence(parentElement, prefix, toInsert, suffix, false));
		}
	}

	private static List<QualifiedName> createPrefix(final int index, final String... sequence) {
		final List<QualifiedName> prefix = new ArrayList<QualifiedName>();
		for (int i = 0; i < index; i++) {
			prefix.add(new QualifiedName(null, sequence[i]));
		}
		return prefix;
	}

	private static List<QualifiedName> createSuffix(final int index, final String... sequence) {
		final List<QualifiedName> suffix = new ArrayList<QualifiedName>();
		for (int i = index + 1; i < sequence.length; i++) {
			suffix.add(new QualifiedName(null, sequence[i]));
		}
		return suffix;
	}

	private static List<QualifiedName> createPrefix(final int index, final QualifiedName... sequence) {
		final List<QualifiedName> prefix = new ArrayList<QualifiedName>();
		for (int i = 0; i < index; i++) {
			prefix.add(sequence[i]);
		}
		return prefix;
	}

	private static List<QualifiedName> createSuffix(final int index, final QualifiedName... sequence) {
		final List<QualifiedName> suffix = new ArrayList<QualifiedName>();
		for (int i = index + 1; i < sequence.length; i++) {
			suffix.add(sequence[i]);
		}
		return suffix;
	}

	@Test
	public void testGetAttributes_shouldReturnAllAttributesButNamespaceDefs() throws Exception {
		// xmlns:xxx attributes should not be returned
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		final IElement sectionElement = doc.getRootElement();

		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap(sectionElement);
		assertEquals("Expect all defined attributes", 4, defs.size());
	}

	@Test
	public void testAttribueWithNamespace() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		final IElement sectionElement = doc.getRootElement();

		final AttributeDefinition ad = getAttributeMap(sectionElement).get(new QualifiedName("http://www.eclipse.org/vex/test/test_ns1", "att1"));
		assertNotNull("AttributeDefinition 'ns1:att1' not found", ad);
		assertEquals("Namespace of ns1:att1", "http://www.eclipse.org/vex/test/test_ns1", ad.getQualifiedName().getQualifier());
	}

	@Test
	public void testDefaultValue() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		final IElement sectionElement = doc.getRootElement();

		final AttributeDefinition ad = getAttributeMap(sectionElement).get(new QualifiedName(null, "enatt"));
		assertNotNull("AttributeDefinition 'enatt' not found", ad);
		assertEquals("Default value of 'enatt'", "value1", ad.getDefaultValue());
	}

	@Test
	public void testRequiredAttribute() throws Exception {
		final IDocument doc = new Document(new QualifiedName(null, "section"));
		doc.setValidator(validator);
		final IElement sectionElement = doc.getRootElement();

		final AttributeDefinition ad = getAttributeMap(sectionElement).get(new QualifiedName(null, "reqatt"));
		assertNotNull("AttributeDefinition 'reqatt' not found", ad);
		assertTrue("isRequired should be true", ad.isRequired());
	}

	@Test
	public void givenEmptyElement_shouldBePartiallyValid() throws Exception {
		assertTrue(validator.isValidSequence(new QualifiedName(null, "section"), Collections.<QualifiedName> emptyList(), true));
	}

	private Map<QualifiedName, AttributeDefinition> getAttributeMap(final IElement element) {
		final List<AttributeDefinition> atts = validator.getAttributeDefinitions(element);
		final Map<QualifiedName, AttributeDefinition> adMap = new HashMap<QualifiedName, AttributeDefinition>();
		for (final AttributeDefinition ad : atts) {
			adMap.put(ad.getQualifiedName(), ad);
		}
		return adMap;
	}

	@Test
	public void testValidationShouldIgnoreXInclude() throws Exception {
		final QualifiedName section = new QualifiedName(null, "section");
		final QualifiedName title = new QualifiedName(null, "title");
		final QualifiedName para = new QualifiedName(null, "para");

		assertIsValidSequence(section, XI, title, para);
		assertIsValidSequence(section, para, XI, para);
	}
}
