/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.AttributeDefinition;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * @author Florian Thienel
 */
public class UniversalTestDocument {

	private static final QualifiedName DOC = new QualifiedName(null, "doc");
	private static final QualifiedName SECTION = new QualifiedName(null, "section");
	private static final QualifiedName PARA = new QualifiedName(null, "para");
	private static final QualifiedName B = new QualifiedName(null, "b");
	private static final QualifiedName I = new QualifiedName(null, "i");

	private static final String LOREM_IPSUM_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.";

	private final IDocument document;

	public UniversalTestDocument(final int sampleCount) {
		document = createTestDocument(sampleCount);
	}

	public IDocument getDocument() {
		return document;
	}

	public IElement getSection(final int index) {
		return (IElement) document.getRootElement().children().get(index);
	}

	public IElement getParagraphWithText(final int index) {
		return (IElement) getSection(index).children().first();
	}

	public IElement getEmptyParagraph(final int index) {
		return (IElement) getSection(index).children().last();
	}

	public int getOffsetWithinText(final int index) {
		final IElement paragraph = getParagraphWithText(index);
		return paragraph.getStartOffset() + 5;
	}

	public static IDocument createTestDocument(final int sampleCount) {
		final Document document = new Document(DOC);
		document.setValidator(new UniversalTestDocumentValidator());

		for (int i = 0; i < sampleCount; i += 1) {
			insertSection(document.getRootElement(), i, false);
		}
		return document;
	}

	private static void insertSection(final IParent parent, final int index, final boolean withInlineElements) {
		final IElement section = insertElement(parent, SECTION);
		insertParagraph(section, index, withInlineElements);
		insertEmptyParagraph(section);
	}

	private static void insertParagraph(final IParent parent, final int index, final boolean withInlineElements) {
		if (withInlineElements) {
			insertTextWithInlineElements(insertEmptyParagraph(parent), index + " " + LOREM_IPSUM_LONG);
		} else {
			insertText(insertEmptyParagraph(parent), index + " " + LOREM_IPSUM_LONG);
		}
	}

	private static IElement insertEmptyParagraph(final IParent parent) {
		return insertElement(parent, PARA);
	}

	private static IElement insertElement(final IParent parent, final QualifiedName elementName) {
		final IDocument document = parent.getDocument();
		return document.insertElement(parent.getEndOffset(), elementName);
	}

	private static void insertText(final IParent parent, final String text) {
		final IDocument document = parent.getDocument();
		document.insertText(parent.getEndOffset(), text);
	}

	public static IDocument createTestDocumentWithInlineElements(final int sampleCount) {
		final Document document = new Document(DOC);
		document.setValidator(new UniversalTestDocumentValidator());

		for (int i = 0; i < sampleCount; i += 1) {
			insertSection(document.getRootElement(), i, true);
		}
		return document;
	}

	private static void insertTextWithInlineElements(final IParent parent, final String text) {
		final IDocument document = parent.getDocument();

		int startOffset = 0;
		while (startOffset < text.length()) {
			final int wordStart = text.indexOf(" ", startOffset);
			final int wordEnd = text.indexOf(" ", wordStart + 1);

			if (wordStart < 0 || wordEnd < 0) {
				document.insertText(parent.getEndOffset(), text.substring(startOffset));
				startOffset = text.length();
			} else {
				document.insertText(parent.getEndOffset(), text.substring(startOffset, wordStart + 1));
				final IElement inlineElement = document.insertElement(parent.getEndOffset(), B);
				document.insertText(inlineElement.getEndOffset(), text.substring(wordStart + 1, wordEnd));
				document.insertText(parent.getEndOffset(), text.substring(wordEnd, wordEnd + 1));
				startOffset = wordEnd + 1;
			}
		}
	}

	private static class UniversalTestDocumentValidator implements IValidator {

		private static final Map<QualifiedName, Set<QualifiedName>> VALID_ITEMS = new HashMap<QualifiedName, Set<QualifiedName>>() {
			private static final long serialVersionUID = 1L;

			{
				put(DOC, set(SECTION));
				put(SECTION, set(PARA));
				put(PARA, set(IValidator.PCDATA, B, I));
				put(B, set(IValidator.PCDATA, B, I));
				put(I, set(IValidator.PCDATA, B, I));
			}
		};

		private final DocumentContentModel documentContentModel = new DocumentContentModel();

		@Override
		public DocumentContentModel getDocumentContentModel() {
			return documentContentModel;
		}

		@Override
		public AttributeDefinition getAttributeDefinition(final IAttribute attribute) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<AttributeDefinition> getAttributeDefinitions(final IElement element) {
			return Collections.emptyList();
		}

		@Override
		public Set<QualifiedName> getValidItems(final IElement element) {
			return VALID_ITEMS.get(element.getQualifiedName());
		}

		private static Set<QualifiedName> set(final QualifiedName... names) {
			return new HashSet<QualifiedName>(Arrays.asList(names));
		}

		@Override
		public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
			final Set<QualifiedName> validItems = VALID_ITEMS.get(element);
			for (final QualifiedName node : nodes) {
				if (!validItems.contains(node)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> seq1, final List<QualifiedName> seq2, final List<QualifiedName> seq3, final boolean partial) {
			final List<QualifiedName> joinedSequence = new ArrayList<QualifiedName>();
			if (seq1 != null) {
				joinedSequence.addAll(seq1);
			}
			if (seq2 != null) {
				joinedSequence.addAll(seq2);
			}
			if (seq3 != null) {
				joinedSequence.addAll(seq3);
			}
			return isValidSequence(element, joinedSequence, partial);
		}

		@Override
		public boolean isValidSequenceXInclude(final List<QualifiedName> nodes, final boolean partial) {
			return true;
		}

		@Override
		public Set<QualifiedName> getValidRootElements() {
			return set(DOC);
		}

		@Override
		public Set<String> getRequiredNamespaces() {
			return Collections.emptySet();
		}

	}

}
