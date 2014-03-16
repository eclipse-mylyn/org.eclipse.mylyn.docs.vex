/*******************************************************************************
 * Copyright (c) 2013, 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;

/**
 * The PseudoElement is used to pass CSS pseudo elements to layout functions.<br />
 * To get an instance of this class use {@link StyleSheet#getPseudoElementBefore(INode)} or
 * {@link StyleSheet#getPseudoElementAfter(INode)}.
 * 
 * @author Carsten Hiesserich
 */
public class PseudoElement extends Element {

	private final INode parentNode;
	private final String pseudoElementName;

	public PseudoElement(final INode parentNode, final String name) {
		super(name);
		this.parentNode = parentNode;
		pseudoElementName = name;
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
	public int getStartOffset() {
		return parentNode.getStartOffset();
	}

	@Override
	public int getEndOffset() {
		return parentNode.getEndOffset();
	}

	public INode getParentNode() {
		return parentNode;
	}

	public String getName() {
		return pseudoElementName;
	}

}
