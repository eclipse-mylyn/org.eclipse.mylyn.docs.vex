/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * A representation of one node in the XML structure. A node is associated to a range of the textual content.
 * <p>
 * This is the base class for all representatives of the XML structure in the document object model (DOM).
 */
public abstract class Node implements INode {

	private Parent parent;
	private IContent content;
	private IPosition startPosition = IPosition.NULL;
	private IPosition endPosition = IPosition.NULL;

	public Parent getParent() {
		return parent;
	}

	public void setParent(final Parent parent) {
		this.parent = parent;
	}

	public IAxis<IParent> ancestors() {
		return new Axis<IParent>(this) {
			@Override
			protected Iterator<IParent> createRootIterator(final ContentRange contentRange, final boolean includeText) {
				return NodesInContentRangeIterator.iterator(new Iterable<IParent>() {
					public Iterator<IParent> iterator() {
						return new AncestorsIterator(Node.this);
					}
				}, contentRange);
			}
		};
	}

	public void associate(final IContent content, final ContentRange range) {
		if (isAssociated()) {
			dissociate();
		}

		this.content = content;
		startPosition = content.createPosition(range.getStartOffset());
		endPosition = content.createPosition(range.getEndOffset());
	}

	public void dissociate() {
		Assert.isTrue(isAssociated(), "This node must be associated to a ContentRange before it can be dissociated.");

		content.removePosition(startPosition);
		content.removePosition(endPosition);
		startPosition = IPosition.NULL;
		endPosition = IPosition.NULL;
		content = null;
	}

	public boolean isAssociated() {
		return content != null;
	}

	public IContent getContent() {
		return content;
	}

	public int getStartOffset() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have a start offset.");
		}
		return startPosition.getOffset();
	}

	public int getEndOffset() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have a start offset.");
		}
		return endPosition.getOffset();
	}

	public ContentRange getRange() {
		if (!isAssociated()) {
			return ContentRange.NULL;
		}
		return new ContentRange(getStartOffset(), getEndOffset());
	}

	public boolean isEmpty() {
		if (!isAssociated()) {
			return false;
		}
		return getEndOffset() - getStartOffset() == 1;
	}

	public boolean containsOffset(final int offset) {
		if (!isAssociated()) {
			return false;
		}
		return getRange().contains(offset);
	}

	public boolean isInRange(final ContentRange range) {
		if (!isAssociated()) {
			return false;
		}
		return range.contains(getRange());
	}

	public String getText() {
		return getText(getRange());
	}

	public String getText(final ContentRange range) {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a Content region to have textual content.");
		}
		return content.getText(range.intersection(getRange()));
	}

	public Document getDocument() {
		if (this instanceof Document) {
			return (Document) this;
		}
		for (final INode ancestor : ancestors()) {
			if (ancestor instanceof Document) {
				return (Document) ancestor;
			}
		}
		return null;
	}

	public String getBaseURI() {
		if (getParent() != null) {
			return getParent().getBaseURI();
		}
		if (getDocument() != null) {
			return getDocument().getBaseURI();
		}
		return null;
	}

	public static List<QualifiedName> getNodeNames(final Iterable<? extends INode> nodes) {
		final List<QualifiedName> names = new ArrayList<QualifiedName>();

		for (final INode node : nodes) {
			node.accept(new BaseNodeVisitor() {
				@Override
				public void visit(final IText text) {
					names.add(IValidator.PCDATA);
				}

				@Override
				public void visit(final IElement element) {
					names.add(element.getQualifiedName());
				}
			});
		}

		return names;
	}

}
