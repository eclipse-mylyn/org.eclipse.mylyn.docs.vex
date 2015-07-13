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
public abstract class DepthFirstTraversal<T> extends BaseBoxVisitorWithResult<T> {

	public DepthFirstTraversal() {
		this(null);
	}

	public DepthFirstTraversal(final T defaultValue) {
		super(defaultValue);
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
	public T visit(final Frame box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final NodeReference box) {
		return box.getComponent().accept(this);
	}

	@Override
	public T visit(final Paragraph box) {
		return traverseChildren(box);
	}

	@Override
	public T visit(final InlineContainer box) {
		return traverseChildren(box);
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
