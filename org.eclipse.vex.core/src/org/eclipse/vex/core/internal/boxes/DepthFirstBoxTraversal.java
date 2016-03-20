/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

/**
 * @author Florian Thienel
 */
public abstract class DepthFirstBoxTraversal<T> extends BaseBoxVisitorWithResult<T> {

	public DepthFirstBoxTraversal() {
		super(null);
	}

	@Override
	public T visit(final RootBox box) {
		return traverseChildren(box);
	}

	@Override
	public T visit(final VerticalBlock box) {
		return traverseChildren(box);
	}

	@Override
	public T visit(final StructuralFrame box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final StructuralNodeReference box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final List box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final ListItem box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final Paragraph box) {
		return traverseChildren(box);
	}

	@Override
	public T visit(final InlineNodeReference box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final InlineContainer box) {
		return traverseChildren(box);
	}

	@Override
	public T visit(final InlineFrame box) {
		return box.getComponent().accept(this);
	}

	protected final <C extends IBox> T traverseChildren(final IParentBox<C> box) {
		for (final C child : box.getChildren()) {
			final T childResult = child.accept(this);

			if (childResult != null) {
				return childResult;
			}
		}
		return null;
	}
}
