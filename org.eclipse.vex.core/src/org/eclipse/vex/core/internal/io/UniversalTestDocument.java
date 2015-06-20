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

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * @author Florian Thienel
 */
public class UniversalTestDocument {

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
		final Document document = new Document(new QualifiedName(null, "doc"));
		for (int i = 0; i < sampleCount; i += 1) {
			insertSection(document.getRootElement(), i);
		}
		return document;
	}

	private static void insertSection(final IParent parent, final int index) {
		final IElement section = insertElement(parent, "section");
		insertParagraph(section, index);
		insertEmptyParagraph(section);
	}

	private static void insertParagraph(final IParent parent, final int index) {
		insertText(insertEmptyParagraph(parent), index + " " + LOREM_IPSUM_LONG);
	}

	private static IElement insertEmptyParagraph(final IParent parent) {
		return insertElement(parent, "para");
	}

	private static IElement insertElement(final IParent parent, final String localName) {
		final IDocument document = parent.getDocument();
		return document.insertElement(parent.getEndOffset(), new QualifiedName(null, localName));
	}

	private static void insertText(final IParent parent, final String text) {
		final IDocument document = parent.getDocument();
		document.insertText(parent.getEndOffset(), text);
	}

}
