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
package org.eclipse.vex.core.internal.dom;

import static org.eclipse.vex.core.internal.dom.Validator.PCDATA;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
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

	@Test
	public void readDocumentWithTwoSchemas() throws Exception {
		final InputStream documentStream = getAsStream("document.xml");
		final InputSource documentInputSource = new InputSource(documentStream);

		final DocumentReader reader = new DocumentReader();
		reader.setDebugging(true);
		final Document document = reader.read(documentInputSource);
		assertNotNull(document);

		final Element rootElement = document.getRootElement();
		assertNotNull(rootElement);
		assertEquals("chapter", rootElement.getLocalName());
		assertEquals("chapter", rootElement.getPrefixedName());
		assertEquals(CHAPTER, rootElement.getQualifiedName());
		assertEquals(STRUCTURE_NS, rootElement.getDefaultNamespaceURI());
		assertEquals(CONTENT_NS, rootElement.getNamespaceURI("c"));

		final Element subChapterElement = rootElement.getChildElements().get(1);
		assertEquals("chapter", subChapterElement.getPrefixedName());
		assertEquals(CHAPTER, subChapterElement.getQualifiedName());

		final Element paragraphElement = subChapterElement.getChildElements().get(1);
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
		final Validator validator = new WTPVEXValidator(CONTENT_NS);
		assertEquals(1, validator.getValidRootElements().size());
		assertTrue(validator.getValidRootElements().contains(P));
	}

	@Test
	public void createValidatorWithDTDPublicId() throws Exception {
		final Validator validator = new WTPVEXValidator(TEST_DTD);
		assertEquals(10, validator.getValidRootElements().size());
	}

	@Test
	public void validateSimpleSchema() throws Exception {
		final Validator validator = new WTPVEXValidator(CONTENT_NS);
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
	public void proposeElementsFromSimpleSchema() throws Exception {
		final Validator validator = new WTPVEXValidator(CONTENT_NS);
		final Document doc = new Document(new RootElement(P));
		doc.setValidator(validator);
		doc.insertElement(1, new Element(B));
		doc.insertText(2, "ab");
		doc.insertElement(5, new Element(I));
		assertEquals(8, doc.getLength());

		assertValidItemsAt(doc, 1, B, I);
		assertValidItemsAt(doc, 2, B, I);
		assertValidItemsAt(doc, 3, B, I);
		assertValidItemsAt(doc, 4, B, I);
		assertValidItemsAt(doc, 5, B, I);
		assertValidItemsAt(doc, 6, B, I);
		assertValidItemsAt(doc, 7, B, I);
	}

	@Test
	public void validateComplexSchema() throws Exception {
		final Validator validator = new WTPVEXValidator(STRUCTURE_NS);
		assertIsValidSequence(validator, CHAPTER, TITLE, P);
		assertIsValidSequence(validator, CHAPTER, P);
		assertIsValidSequence(validator, P, PCDATA, B, I);
	}

	@Test
	public void proposeElementsFromComplexSchema() throws Exception {
		final Validator validator = new WTPVEXValidator();
		final Document doc = new Document(new RootElement(CHAPTER));
		doc.setValidator(validator);
		doc.insertElement(1, new Element(TITLE));
		doc.insertText(2, "ab");
		doc.insertElement(5, new Element(CHAPTER));
		doc.insertElement(6, new Element(TITLE));
		doc.insertText(7, "cd");
		doc.insertElement(10, new Element(P));
		doc.insertElement(11, new Element(B));

		assertValidItemsAt(doc, 1, TITLE, CHAPTER, P);
		assertValidItemsAt(doc, 2);
		assertValidItemsAt(doc, 3);
		assertValidItemsAt(doc, 4);
		assertValidItemsAt(doc, 5, TITLE, CHAPTER, P);
		assertValidItemsAt(doc, 6, TITLE, CHAPTER, P);
		assertValidItemsAt(doc, 7);
		assertValidItemsAt(doc, 8);
		assertValidItemsAt(doc, 9);
		assertValidItemsAt(doc, 10, TITLE, CHAPTER, P);
		assertValidItemsAt(doc, 11, B, I);
		assertValidItemsAt(doc, 12, B, I);
		assertValidItemsAt(doc, 13, B, I);
		assertValidItemsAt(doc, 14, TITLE, CHAPTER, P);
	}

	private void assertIsValidSequence(final Validator validator, final QualifiedName parentElement, final QualifiedName... sequence) {
		for (int i = 0; i < sequence.length; i++) {
			final List<QualifiedName> prefix = createPrefix(i, sequence);
			final List<QualifiedName> toInsert = Collections.singletonList(sequence[i]);
			final List<QualifiedName> suffix = createSuffix(i, sequence);

			assertTrue(validator.isValidSequence(parentElement, prefix, toInsert, suffix, false));
		}
	}

	private static List<QualifiedName> createPrefix(final int index, final QualifiedName... sequence) {
		final List<QualifiedName> prefix = new ArrayList<QualifiedName>();
		for (int i = 0; i < index; i++)
			prefix.add(sequence[i]);
		return prefix;
	}

	private static List<QualifiedName> createSuffix(final int index, final QualifiedName... sequence) {
		final List<QualifiedName> suffix = new ArrayList<QualifiedName>();
		for (int i = index + 1; i < sequence.length; i++)
			suffix.add(sequence[i]);
		return suffix;
	}

	private static void assertValidItemsAt(final Document doc, final int offset, final QualifiedName... expectedItems) {
		final Set<QualifiedName> expected = new HashSet<QualifiedName>(expectedItems.length);
		for (final QualifiedName expectedItem : expectedItems)
			expected.add(expectedItem);

		final Set<QualifiedName> validItems = doc.getValidator().getValidItems(doc.getElementAt(offset));
		assertEquals(expected, validItems);
	}

}
