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
package org.eclipse.vex.core.internal.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class FormattingPortionIteratorTest {

	private Document document;
	private IParent parent;
	private Element blockElement;

	@Test
	public void givenRangeWithBlockElement_whenBlockElementStartsAtRangeStart_shouldReturnBlockElement() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(blockElement.getStartOffset(), parent.getEndOffset());

		assertSame(blockElement, iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenBlockElementStartsAfterRangeStart_shouldReturnRangeBeforeBlockElement() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(blockElement.getStartOffset() - 2, parent.getEndOffset());

		assertEquals(new ContentRange(blockElement.getStartOffset() - 2, blockElement.getStartOffset()), iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenContentExistsAfterBlockElement_shouldReturnRangeAfterBlockElement() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(blockElement.getStartOffset(), parent.getEndOffset());

		iterator.next();
		assertEquals(new ContentRange(blockElement.getEndOffset() + 1, parent.getEndOffset()), iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenNoMoreContentExists_shouldReturnNull() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(blockElement.getEndOffset(), parent.getEndOffset());

		iterator.next();
		assertNull(iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenContentRangeIsPushed_shouldReturnContentRange() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(parent.getStartOffset(), parent.getEndOffset());

		final ContentRange contentRange = (ContentRange) iterator.next();
		iterator.push(contentRange);

		assertSame(contentRange, iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenBlockElementIsPushed_shouldReturnBlockElement() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(blockElement.getStartOffset(), parent.getEndOffset());

		final IElement element = (IElement) iterator.next();
		iterator.push(element);

		assertSame(element, iterator.next());
	}

	@Test
	public void givenRangeWithBlockElement_whenEndingWithinBlockElement_shouldNotReturnBlockElement() throws Exception {
		givenRangeWithBlockElement();

		final FormattingPortionIterator iterator = createIterator(parent.getStartOffset(), blockElement.getStartOffset() + 2);

		iterator.next();
		assertNull(iterator.next());
	}

	@Test
	public void givenRangeWithSeveralBlockElements_shouldReturnAllBlockElementsAndRanges() throws Exception {
		givenRangeWithBlockElements(10);

		final FormattingPortionIterator iterator = createIterator(parent.getStartOffset(), parent.getEndOffset());

		int count = 0;
		while (iterator.next() != null) {
			count++;
		}
		assertEquals(12, count);
	}

	private void givenRangeWithBlockElement() {
		givenRangeWithBlockElements(1);
	}

	private void givenRangeWithBlockElements(final int count) {
		document = new Document(new QualifiedName(null, "parent"));
		parent = document.getRootElement();
		document.insertText(parent.getEndOffset(), "Hello");
		for (int i = 0; i < count; i++) {
			blockElement = document.insertElement(parent.getEndOffset(), new QualifiedName(null, "element"));
			document.insertText(blockElement.getEndOffset(), "Block " + i);
		}
		document.insertText(parent.getEndOffset(), "World");
	}

	private FormattingPortionIterator createIterator(final int startOffset, final int endOffset) {
		return new FormattingPortionIterator(new IWhitespacePolicy() {
			@Override
			public boolean isBlock(final INode node) {
				return node instanceof IElement;
			}

			@Override
			public boolean isPre(final INode node) {
				return false;
			}
		}, parent, startOffset, endOffset);
	}
}
