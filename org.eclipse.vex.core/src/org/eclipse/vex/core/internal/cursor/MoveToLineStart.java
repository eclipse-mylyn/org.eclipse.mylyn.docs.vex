/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.cursor;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.ParentTraversal;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;

public class MoveToLineStart implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final IViewPort viewPort, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		return currentBox.accept(new BaseBoxVisitorWithResult<Integer>(currentOffset) {
			@Override
			public Integer visit(final StructuralNodeReference box) {
				final Paragraph containedParagraph = getContainedParagraph(box);
				if (containedParagraph != null) {
					if (currentOffset == box.getEndOffset()) {
						return getFirstOffsetInLastLine(containedParagraph, currentOffset);
					}
				}
				return super.visit(box);
			}

			@Override
			public Integer visit(final InlineNodeReference box) {
				return getFirstOffsetInLine(box, currentOffset);
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
				return getFirstOffsetInLine(box, currentOffset);
			}

			@Override
			public Integer visit(final TextContent box) {
				return getFirstOffsetInLine(box, currentOffset);
			}
		});
	}

	private static Paragraph getContainedParagraph(final StructuralNodeReference nodeReference) {
		return nodeReference.accept(new DepthFirstBoxTraversal<Paragraph>() {
			@Override
			public Paragraph visit(final StructuralNodeReference box) {
				if (box == nodeReference) {
					return super.visit(box);
				}
				return null;
			}

			@Override
			public Paragraph visit(final Paragraph box) {
				return box;
			}
		});
	}

	private static int getFirstOffsetInLastLine(final Paragraph box, final int defaultValue) {
		int currentBaseline = 0;
		IContentBox firstChildOnLine = null;
		for (final IInlineBox child : box.getChildren()) {
			final int baseline = getAbsoluteBaseline(child);
			if (baseline > currentBaseline && child instanceof IContentBox) {
				currentBaseline = baseline;
				firstChildOnLine = (IContentBox) child;
			}
		}
		if (firstChildOnLine == null) {
			return defaultValue;
		}
		return firstChildOnLine.getStartOffset();
	}

	private static int getFirstOffsetInLine(final IInlineBox box, final int defaultValue) {
		final int targetBaseline = getAbsoluteBaseline(box);
		final IParentBox<IInlineBox> structuralParent = getStructuralParent(box);
		final IContentBox firstBoxInLine = findFirstContentBoxOnTargetBaseline(structuralParent, targetBaseline);
		if (firstBoxInLine == null) {
			return defaultValue;
		}

		return firstBoxInLine.getStartOffset();
	}

	private static int getAbsoluteBaseline(final IInlineBox box) {
		return box.getAbsoluteTop() + box.getBaseline();
	}

	private static IContentBox findFirstContentBoxOnTargetBaseline(final IParentBox<IInlineBox> structuralParent, final int targetBaseline) {
		for (final IInlineBox child : structuralParent.getChildren()) {
			if (targetBaseline == getAbsoluteBaseline(child)) {
				final IContentBox contentBox = findFirstContentBox(child);
				if (contentBox != null) {
					return contentBox;
				}
			}
		}
		return null;
	}

	private static IContentBox findFirstContentBox(final IInlineBox box) {
		return box.accept(new DepthFirstBoxTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final InlineNodeReference box) {
				return box;
			}

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				return box;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				return box;
			}
		});
	}

	private static IParentBox<IInlineBox> getStructuralParent(final IBox box) {
		return box.accept(new ParentTraversal<IParentBox<IInlineBox>>(null) {
			@Override
			public IParentBox<IInlineBox> visit(final Paragraph box) {
				return box;
			}
		});
	}

	@Override
	public boolean preferX() {
		return true;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

}
