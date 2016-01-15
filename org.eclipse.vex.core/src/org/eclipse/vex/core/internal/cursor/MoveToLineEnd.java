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
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;

public class MoveToLineEnd implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, final IViewPort viewPort, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		return currentBox.accept(new BaseBoxVisitorWithResult<Integer>(currentOffset) {
			@Override
			public Integer visit(final InlineNodeReference box) {
				return getLastOffsetInLine(box, currentOffset);
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
				return getLastOffsetInLine(box, currentOffset);
			}

			@Override
			public Integer visit(final TextContent box) {
				return getLastOffsetInLine(box, currentOffset);
			}
		});
	}

	private int getLastOffsetInLine(final IInlineBox box, final int defaultValue) {
		final int targetBaseline = getAbsoluteBaseline(box);
		final IParentBox<IInlineBox> structuralParent = getStructuralParent(box);
		final IContentBox lastBoxInLine = findLastContentBoxOnTargetBaseline(structuralParent, targetBaseline);
		if (lastBoxInLine == null) {
			return defaultValue;
		}

		return lastBoxInLine.getEndOffset();
	}

	private static IContentBox findLastContentBoxOnTargetBaseline(final IParentBox<IInlineBox> structuralParent, final int targetBaseline) {
		IContentBox lastBox = null;
		for (final IInlineBox child : structuralParent.getChildren()) {
			if (targetBaseline < getAbsoluteBaseline(child)) {
				return lastBox;
			}

			final IContentBox contentBox = findLastContentBox(child);
			if (contentBox != null) {
				lastBox = contentBox;
			}
		}
		return ContentTopology.getParentContentBox(structuralParent);
	}

	private static IContentBox findLastContentBox(final IInlineBox box) {
		final IContentBox[] contentBox = new IContentBox[1];
		box.accept(new DepthFirstBoxTraversal<IContentBox>() {
			@Override
			public IContentBox visit(final InlineNodeReference box) {
				contentBox[0] = box;
				return null;
			}

			@Override
			public IContentBox visit(final NodeEndOffsetPlaceholder box) {
				contentBox[0] = box;
				return null;
			}

			@Override
			public IContentBox visit(final TextContent box) {
				contentBox[0] = box;
				return null;
			}
		});
		return contentBox[0];
	}

	private static int getAbsoluteBaseline(final IInlineBox box) {
		return box.getAbsoluteTop() + box.getBaseline();
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
