/*******************************************************************************
 * Copyright (c) 2011, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - tests for attribute namespaces
 *******************************************************************************/
package org.eclipse.vex.core.internal.validator;

import static org.eclipse.vex.core.provisional.dom.IValidator.PCDATA;
import static org.eclipse.vex.core.tests.TestResources.CONTENT_NS;
import static org.eclipse.vex.core.tests.TestResources.STRUCTURE_NS;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.eclipse.vex.core.tests.TestResources.getAsStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.provisional.dom.AttributeDefinition;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * @author Florian Thienel
 */
public class SchemaValidatorTest {

	private static final QualifiedName CHAPTER = new QualifiedName(STRUCTURE_NS, "chapter");
	private static final QualifiedName TITLE = new QualifiedName(STRUCTURE_NS, "title");

	private static final QualifiedName P = new QualifiedName(CONTENT_NS, "p");
	private static final QualifiedName B = new QualifiedName(CONTENT_NS, "b");
	private static final QualifiedName I = new QualifiedName(CONTENT_NS, "i");
	private static final QualifiedName XI = new QualifiedName(Namespace.XINCLUDE_NAMESPACE_URI, "include");

	@Test
	public void readDocumentWithTwoSchemas() throws Exception {
		final InputStream documentStream = getAsStream("document.xml");
		final InputSource documentInputSource = new InputSource(documentStream);

		final DocumentReader reader = new DocumentReader();
		reader.setDebugging(true);
		final IDocument document = reader.read(documentInputSource);
		assertNotNull(document);

		final IElement rootElement = document.getRootElement();
		assertNotNull(rootElement);
		assertEquals("chapter", rootElement.getLocalName());
		assertEquals("chapter", rootElement.getPrefixedName());
		assertEquals(CHAPTER, rootElement.getQualifiedName());
		assertEquals(STRUCTURE_NS, rootElement.getDefaultNamespaceURI());
		assertEquals(CONTENT_NS, rootElement.getNamespaceURI("c"));

		final IElement subChapterElement = rootElement.childElements().get(1);
		assertEquals("chapter", subChapterElement.getPrefixedName());
		assertEquals(CHAPTER, subChapterElement.getQualifiedName());

		final IElement paragraphElement = subChapterElement.childElements().get(1);
		assertEquals("p", paragraphElement.getLocalName());
		assertEquals("c:p", paragraphElement.getPrefixedName());
		assertEquals(P, paragraphElement.getQualifiedName());
	}

	@Test
	public void getCMDocumentsByLogicalName() throws Exception {
		final URIResolver uriResolver = URIResolverPlugin.createResolver();
		final ContentModelManager modelManager = ContentModelManager.getInstance();

		final String schemaLocation = uriResolver.resolve(null, STRUCTURE_NS, null);
		assertNotNull(schemaLocation);
		final CMDocument schema = modelManager.createCMDocument(schemaLocation, null);
		assertNotNull(schema);

		final String dtdLocation = uriResolver.resolve(null, TEST_DTD, null);
		assertNotNull(dtdLocation);
		final CMDocument dtd = modelManager.createCMDocument(dtdLocation, null);
		assertNotNull(dtd);
	}

	@Test
	public void useCMDocument() throws Exception {
		final URIResolver uriResolver = URIResolverPlugin.createResolver();
		final ContentModelManager modelManager = ContentModelManager.getInstance();

		final String structureSchemaLocation = uriResolver.resolve(null, STRUCTURE_NS, null);
		final CMDocument structureSchema = modelManager.createCMDocument(structureSchemaLocation, null);

		assertEquals(1, structureSchema.getElements().getLength());

		final CMElementDeclaration chapterElement = (CMElementDeclaration) structureSchema.getElements().item(0);
		assertEquals("chapter", chapterElement.getNodeName());

		assertEquals(2, chapterElement.getLocalElements().getLength());
	}

	@Test
	public void createValidatorWithNamespaceUri() throws Exception {
		final IValidator validator = new WTPVEXValidator(CONTENT_NS);
		assertEquals(1, validator.getValidRootElements().size());
		assertTrue(validator.getValidRootElements().contains(P));
	}

	@Test
	public void createValidatorWithDTDPublicId() throws Exception {
		final IValidator validator = new WTPVEXValidator(TEST_DTD);
		assertEquals(11, validator.getValidRootElements().size());
	}

	@Test
	public void validateSimpleSchema() throws Exception {
		final IValidator validator = new WTPVEXValidator(CONTENT_NS);
		assertIsValidSequence(validator, P, PCDATA);
		assertIsValidSequence(validator, P, B, I);
		assertIsValidSequence(validator, B, B, I);
		assertIsValidSequence(validator, B, I, B);
		assertIsValidSequence(validator, B, PCDATA, I, B);
		assertIsValidSequence(validator, I, B, I);
		assertIsValidSequence(validator, I, I, B);
		assertIsValidSequence(validator, I, PCDATA, I, B);
	}

	@Test
	public void validItemsFromSimpleSchema() throws Exception {
		final IValidator validator = new WTPVEXValidator();
		final IDocument doc = new Document(P);
		doc.setValidator(validator);
		doc.insertElement(2, B);
		doc.insertElement(3, I);

		assertValidItems(validator, doc.getRootElement(), B, I); // p
		assertValidItems(validator, doc.getElementForInsertionAt(2), B, I); // b
		assertValidItems(validator, doc.getElementForInsertionAt(3), B, I); // i
	}

	@Test
	public void validateComplexSchema() throws Exception {
		final IValidator validator = new WTPVEXValidator(STRUCTURE_NS);
		assertIsValidSequence(validator, CHAPTER, TITLE, P);
		assertIsValidSequence(validator, CHAPTER, P);
		assertIsValidSequence(validator, P, PCDATA, B, I);
	}

	@Test
	public void validItemsFromComplexSchema() throws Exception {
		/*
		 * We have to check this using a document, because B and I are not defined as standalone elements. The Validator
		 * needs their parent to find the definition of their content model.
		 */
		final IValidator validator = new WTPVEXValidator();
		final IDocument doc = new Document(CHAPTER);
		doc.setValidator(validator);
		doc.insertElement(2, TITLE);
		doc.insertElement(4, P);
		doc.insertElement(5, B);
		doc.insertElement(6, I);

		assertValidItems(validator, doc.getRootElement(), CHAPTER, TITLE, P); // chapter
		assertValidItems(validator, doc.getElementForInsertionAt(3)); // title
		assertValidItems(validator, doc.getElementForInsertionAt(5), B, I); // p
		assertValidItems(validator, doc.getElementForInsertionAt(6), B, I); // b
		assertValidItems(validator, doc.getElementForInsertionAt(7), B, I); // i
	}

	@Test
	public void getAllRequiredNamespacesForSimpleSchema() throws Exception {
		final IValidator validator = new WTPVEXValidator(new DocumentContentModel(null, null, null, new Element(P)));
		final Set<String> requiredNamespaces = validator.getRequiredNamespaces();
		assertEquals(1, requiredNamespaces.size());
	}

	@Test
	public void getAllRequiredNamespacesForComplexSchema() throws Exception {
		final IValidator validator = new WTPVEXValidator(new DocumentContentModel(null, null, null, new Element(CHAPTER)));
		final Set<String> requiredNamespaces = validator.getRequiredNamespaces();
		assertEquals(2, requiredNamespaces.size());
		assertTrue(requiredNamespaces.contains(CONTENT_NS));
		assertTrue(requiredNamespaces.contains(STRUCTURE_NS));
	}

	private void assertIsValidSequence(final IValidator validator, final QualifiedName parentElement, final QualifiedName... sequence) {
		for (int i = 0; i < sequence.length; i++) {
			final List<QualifiedName> prefix = createPrefix(i, sequence);
			final List<QualifiedName> toInsert = Collections.singletonList(sequence[i]);
			final List<QualifiedName> suffix = createSuffix(i, sequence);

			assertTrue(validator.isValidSequence(parentElement, prefix, toInsert, suffix, false));
		}
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

	private static void assertValidItems(final IValidator validator, final IElement element, final QualifiedName... expectedItems) {
		final Set<QualifiedName> expected = new HashSet<QualifiedName>(expectedItems.length);
		for (final QualifiedName expectedItem : expectedItems) {
			expected.add(expectedItem);
		}

		final Set<QualifiedName> candidateItems = validator.getValidItems(element);
		assertEquals(expected, candidateItems);
	}

	@Test
	public void testGetAttributes_shouldReturnAllAttributes() throws Exception {
		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap();
		assertEquals("Expect all defined attributes", 4, defs.size());
	}

	@Test
	public void testAttribueWithNamespace() throws Exception {
		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap();

		final AttributeDefinition ad = defs.get(new QualifiedName("http://www.eclipse.org/vex/test/test_ns1", "att1"));
		assertNotNull("AttributeDefinition 'ns1:att1' not found", ad);
		assertEquals("Namespace of ns1:att1", "http://www.eclipse.org/vex/test/test_ns1", ad.getQualifiedName().getQualifier());
	}

	@Test
	public void testEnumAttribute() throws Exception {
		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap();

		final AttributeDefinition ad = defs.get(new QualifiedName(null, "enatt"));
		assertEquals("enatt", ad.getName());

		final String[] enumValues = ad.getValues();
		assertEquals(3, enumValues.length);
	}

	@Test
	public void testDefaultValue() throws Exception {
		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap();

		final AttributeDefinition ad = defs.get(new QualifiedName(null, "enatt"));
		assertNotNull("AttributeDefinition 'enatt' not found", ad);
		assertEquals("Default value of 'enatt'", "value1", ad.getDefaultValue());
	}

	@Test
	public void testRequiredAttribute() throws Exception {
		final Map<QualifiedName, AttributeDefinition> defs = getAttributeMap();

		final AttributeDefinition ad = defs.get(new QualifiedName(null, "reqatt"));
		assertNotNull("AttributeDefinition 'reqatt' not found", ad);
		assertTrue("isRequired should be true", ad.isRequired());
	}

	@Test
	public void testValidationShouldIgnoreXInclude() throws Exception {
		final IValidator validator = new WTPVEXValidator(CONTENT_NS);
		assertIsValidSequence(validator, P, XI);
		assertIsValidSequence(validator, P, XI, B, I);
	}

	private Map<QualifiedName, AttributeDefinition> getAttributeMap() {
		final IDocument doc = new Document(new QualifiedName(null, "chapter"));
		final IValidator validator = new WTPVEXValidator(STRUCTURE_NS);
		doc.setValidator(validator);
		final IElement chapterElement = doc.getRootElement();

		final List<AttributeDefinition> atts = validator.getAttributeDefinitions(chapterElement);
		final Map<QualifiedName, AttributeDefinition> adMap = new HashMap<QualifiedName, AttributeDefinition>();
		for (final AttributeDefinition ad : atts) {
			adMap.put(ad.getQualifiedName(), ad);
		}
		return adMap;
	}
}
