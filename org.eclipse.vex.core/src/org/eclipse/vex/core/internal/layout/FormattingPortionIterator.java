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

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

/**
 * @author Florian Thienel
 */
public class FormattingPortionIterator {

	private final IWhitespacePolicy policy;
	private int startOffset;
	private final int endOffset;
	private final LinkedList<Iterator<? extends INode>> iteratorStack = new LinkedList<Iterator<? extends INode>>();
	private final LinkedList<Object> pushStack = new LinkedList<Object>();

	public FormattingPortionIterator(final IWhitespacePolicy policy, final IParent parent, final int startOffset, final int endOffset) {
		this.policy = policy;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		pushIteratorFor(parent);
	}

	/**
	 * Returns the next block element or inline range, or null if we're at the end.
	 */
	public Object next() {
		if (!pushStack.isEmpty()) {
			return pushStack.removeLast();
		} else if (startOffset >= endOffset) {
			return null;
		} else {
			final INode blockNode = findNextBlockNode(policy);
			if (blockNode == null) {
				if (startOffset < endOffset) {
					final ContentRange result = new ContentRange(startOffset, endOffset);
					startOffset = endOffset;
					return result;
				} else {
					return null;
				}
			} else if (blockNode.getStartOffset() > startOffset) {
				pushStack.addLast(blockNode);
				final ContentRange result = new ContentRange(startOffset, blockNode.getStartOffset());
				startOffset = blockNode.getEndOffset() + 1;
				return result;
			} else {
				startOffset = blockNode.getEndOffset() + 1;
				return blockNode;
			}
		}
	}

	public void push(final Object pushed) {
		pushStack.addLast(pushed);
	}

	private void pushIteratorFor(final IParent parent) {
		pushIterator(parent.children().in(new ContentRange(startOffset, endOffset)).withoutText().iterator());
	}

	private void pushIterator(final Iterator<? extends INode> iterator) {
		iteratorStack.addFirst(iterator);
	}

	private Iterator<? extends INode> peekIterator() {
		return iteratorStack.getFirst();
	}

	private void popIterator() {
		if (hasIterator()) {
			iteratorStack.removeFirst();
		}
	}

	private boolean hasIterator() {
		return !iteratorStack.isEmpty();
	}

	private INode findNextBlockNode(final IWhitespacePolicy policy) {
		for (final Iterator<? extends INode> iterator = peekIterator(); iterator.hasNext();) {
			final INode nextBlockNode = iterator.next().accept(new BaseNodeVisitorWithResult<INode>() {
				@Override
				public INode visit(final IElement element) {
					// found?
					if (policy.isBlock(element)) {
						return element;
					}

					// recursion
					pushIteratorFor(element);
					final INode fromChild = findNextBlockNode(policy);
					if (fromChild != null) {
						return fromChild;
					}

					return null;
				}

				@Override
				public INode visit(final IComment comment) {
					if (policy.isBlock(comment)) {
						return comment;
					}
					return null;
				}

				@Override
				public INode visit(final IProcessingInstruction pi) {
					if (policy.isBlock(pi)) {
						return pi;
					}
					return null;
				}

				@Override
				public INode visit(final IIncludeNode include) {
					// For exact WYSIWYG, we would have to check included content to determine how to display it.
					// Currently, the CSS for xi|include decides if we display inline or block.
					if (policy.isBlock(include)) {
						return include;
					}
					return null;
				}
			});
			if (nextBlockNode != null) {
				return nextBlockNode;
			}
		}

		popIterator();
		if (hasIterator()) {
			return findNextBlockNode(policy);
		}

		return null;
	}
}
