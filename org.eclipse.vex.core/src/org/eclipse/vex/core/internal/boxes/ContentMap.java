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
public class ContentMap {

	private RootBox rootBox;

	public void setRootBox(final RootBox rootBox) {
		this.rootBox = rootBox;
	}

	public IContentBox findBoxForPosition(final int offset) {
		final IBoxVisitorWithResult<IContentBox> search = new BaseBoxVisitorWithResult<IContentBox>() {

			@Override
			public IContentBox visit(final RootBox box) {
				return traverseChildren(box);
			}

			@Override
			public IContentBox visit(final VerticalBlock box) {
				return traverseChildren(box);
			}

			@Override
			public IContentBox visit(final Frame box) {
				return box.getComponent().accept(this);
			}

			@Override
			public IContentBox visit(final NodeReference box) {
				if (box.getStartOffset() == offset || box.getEndOffset() == offset) {
					return box;
				}
				if (box.getStartOffset() < offset && box.getEndOffset() > offset) {
					return box.getComponent().accept(this);
				}
				return null;
			}

			@Override
			public IContentBox visit(final Paragraph box) {
				return traverseChildren(box);
			}

			@Override
			public IContentBox visit(final TextContent box) {
				if (box.getStartOffset() <= offset && box.getEndOffset() >= offset) {
					return box;
				}
				return null;
			}

			private <T extends IBox> IContentBox traverseChildren(final IParentBox<T> box) {
				for (final T child : box.getChildren()) {
					final IContentBox childResult = child.accept(this);

					if (childResult != null) {
						return childResult;
					}
				}
				return null;
			}
		};

		return rootBox.accept(search);
	}
}
