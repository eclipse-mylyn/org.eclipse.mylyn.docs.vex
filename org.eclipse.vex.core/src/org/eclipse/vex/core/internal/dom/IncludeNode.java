/*******************************************************************************
 * Copyright (c) 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;

/**
 * The IncludeNode wraps an XML include element.
 */
public class IncludeNode extends Node implements IIncludeNode {

	private Element reference;

	/**
	 * Create a new include node that wraps the given Element. The Include node bypasses the element hierarchy, so the
	 * parent of the given Element has to be set when calling this constructor.
	 *
	 * @param element
	 */
	public IncludeNode(final Element element) {
		setReference(element);
	}

	@Override
	public boolean isKindOf(final INode node) {
		return false;
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isAssociated() {
		return reference != null && reference.isAssociated();
	}

	@Override
	public void associate(final IContent content, final ContentRange range) {
		reference.associate(content, range);
	}

	@Override
	public void dissociate() {
		if (isAssociated()) {
			reference.dissociate();
		}
	}

	@Override
	public void setParent(final Parent parent) {
		super.setParent(parent);
		reference.setParent(parent);
	}

	@Override
	public IContent getContent() {
		return reference.getContent();
	}

	@Override
	public ContentPosition getStartPosition() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have a start position.");
		}
		return new ContentPosition(this, getStartOffset());
		//return reference.getStartPosition();
	}

	@Override
	public int getStartOffset() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have a start offset.");
		}
		return reference.getStartOffset();
	}

	@Override
	public ContentPosition getEndPosition() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have a end position.");
		}
		return new ContentPosition(this, getEndOffset());
		//return reference.getEndPosition();
	}

	@Override
	public int getEndOffset() {
		if (!isAssociated()) {
			throw new AssertionFailedException("Node must be associated to a ContentRange to have an end offset.");
		}
		return reference.getEndOffset();
	}

	public void setReference(final Element element) {
		reference = element;
	}

	@Override
	public Element getReference() {
		return reference;
	}

	@Override
	public INode getResolved() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INode getFallback() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {

		final StringBuffer sb = new StringBuffer();

		sb.append("IncludeNode (");
		if (reference == null) {
			sb.append("<Reference element not set>");
		} else {
			sb.append("<Reference:");
			sb.append(reference.toString());
		}

		return sb.toString();
	}

}
