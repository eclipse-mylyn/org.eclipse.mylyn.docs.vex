/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A box that wraps inline content into a paragraph.
 */
public class ParagraphBox extends AbstractBox implements BlockBox {

	private final INode node;
	private final LineBox[] children;
	private LineBox firstContentLine;
	private LineBox lastContentLine;

	/**
	 * Class constructor.
	 * 
	 * @param children
	 *            Line boxes that comprise the paragraph.
	 * @param node
	 *            The node that this paragraph is associated to
	 */
	private ParagraphBox(final LineBox[] children, final INode node) {
		this.node = node;
		this.children = children;
		for (final LineBox element : children) {
			if (element.hasContent()) {
				if (firstContentLine == null) {
					firstContentLine = element;
				}
				lastContentLine = element;
			}
		}
	}

	/**
	 * Create a paragraph by word-wrapping a list of inline boxes.
	 * 
	 * @param context
	 *            LayoutContext used for this layout.
	 * @param node
	 *            Element that controls the styling for this paragraph.
	 * @param inlines
	 *            List of InlineBox objects to be wrapped
	 * @param width
	 *            width to which the paragraph is to be wrapped
	 */
	public static ParagraphBox create(final LayoutContext context, final INode node, final List<InlineBox> inlines, final int width) {
		final InlineBox[] array = inlines.toArray(new InlineBox[inlines.size()]);
		return create(context, node, array, width);
	}

	/**
	 * Create a paragraph by word-wrapping a list of inline boxes.
	 * 
	 * @param context
	 *            LayoutContext used for this layout
	 * @param node
	 *            Node that controls the styling of this paragraph, in particular text alignment.
	 * @param inlines
	 *            Array of InlineBox objects to be wrapped.
	 * @param width
	 *            width to which the paragraph is to be wrapped.
	 */
	public static ParagraphBox create(final LayoutContext context, final INode node, final InlineBox[] inlines, final int width) {

		// lines is the list of LineBoxes we are creating
		final List<Box> lines = new ArrayList<Box>();

		InlineBox right = new LineBox(context, node, inlines);

		while (right != null) {
			final InlineBox.Pair pair = right.split(context, width, true);
			lines.add(pair.getLeft());
			right = pair.getRight();
		}

		final Styles styles = context.getStyleSheet().getStyles(node);
		final String textAlign = styles.getTextAlign();
		final boolean alignRight = textAlign.equals(CSS.RIGHT);
		final boolean alignCenter = textAlign.equals(CSS.CENTER);

		// y-offset of the next line
		int y = 0;

		int actualWidth = 0;

		for (final Box lineBox : lines) {
			int x;
			if (alignRight) {
				x = width - lineBox.getWidth();
			} else if (alignCenter) {
				x = (width - lineBox.getWidth()) / 2;
			} else {
				x = 0;
			}

			lineBox.setX(x);
			lineBox.setY(y);

			y += lineBox.getHeight();
			actualWidth = Math.max(actualWidth, lineBox.getWidth());
		}

		final ParagraphBox para = new ParagraphBox(lines.toArray(new LineBox[lines.size()]), node);
		para.setWidth(actualWidth);
		para.setHeight(y);

		return para;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final ContentPosition position) {

		final LineBox line = getLineAt(position);
		final Caret caret = line.getCaret(context, position);
		caret.translate(line.getX(), line.getY());
		return caret;

	}

	@Override
	public Box[] getChildren() {
		return children;
	}

	@Override
	public INode getNode() {
		return node;
	}

	@Override
	public int getEndOffset() {
		return lastContentLine.getEndOffset();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getFirstLine()
	 */
	public LineBox getFirstLine() {
		if (children.length == 0) {
			return null;
		} else {
			return children[0];
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getLastLine()
	 */
	public LineBox getLastLine() {
		if (children.length == 0) {
			return null;
		} else {
			return children[children.length - 1];
		}
	}

	/**
	 * Returns the LineBox at the given offset.
	 * 
	 * @param offset
	 *            the offset to check.
	 */
	public LineBox getLineAt(final ContentPosition linePosition) {
		final LineBox[] children = this.children;
		for (final LineBox element : children) {
			// When the position is at the end of a line (isLineEnd() == true), we return the line that ends BEFORE the given position
			final int endOffset = element.getEndOffset();
			if (element.hasContent() && linePosition.getOffset() <= endOffset) {
				return element;
			}
		}
		return lastContentLine;
	}

	public ContentPosition getLineEndPosition(final ContentPosition linePosition) {
		return getLineAt(linePosition).getEndPosition();
	}

	public ContentPosition getLineStartPosition(final ContentPosition linePosition) {
		return getLineAt(linePosition).getStartPosition();
	}

	public int getMarginBottom() {
		return 0;
	}

	public int getMarginTop() {
		return 0;
	}

	public ContentPosition getNextLinePosition(final LayoutContext context, final ContentPosition linePosition, final int x) {
		LineBox nextLine = null;
		final LineBox[] children = this.children;
		for (final LineBox element : children) {
			if (element.hasContent() && element.getStartOffset() > linePosition.getOffset()) {
				nextLine = element;
				break;
			}
		}

		if (nextLine != null) {
			return nextLine.viewToModel(context, x - nextLine.getX(), 0);
		}

		// No next line - let the parent handle this
		return null;
	}

	public BlockBox getParent() {
		throw new IllegalStateException("ParagraphBox does not currently track parent");
	}

	public ContentPosition getPreviousLinePosition(final LayoutContext context, final ContentPosition linePosition, final int x) {
		LineBox prevLine = null;
		final LineBox[] children = this.children;
		for (int i = children.length - 1; i >= 0; i--) {
			if (children[i].hasContent() && children[i].getEndOffset() < linePosition.getOffset()) {
				prevLine = children[i];
				break;
			}
		}

		if (prevLine != null) {
			return prevLine.viewToModel(context, x - prevLine.getX(), 0);
		}

		// No next line - let the parent handle this
		return null;
	}

	@Override
	public int getStartOffset() {
		return firstContentLine.getStartOffset();
	}

	@Override
	public boolean hasContent() {
		return firstContentLine != null && firstContentLine.hasContent();
	}

	public VerticalRange layout(final LayoutContext context, final int top, final int bottom) {
		return null;
	}

	public void invalidate(final boolean direct) {
		throw new IllegalStateException("invalidate called on a non-element BlockBox");
	}

	@Override
	public boolean isAnonymous() {
		// This box stores the node only to return it in an ContentPosition
		return true;
	}

	public void setInitialSize(final LayoutContext context) {
		// NOP - size calculated in factory method
	}

	@Override
	public String toString() {
		return "ParagraphBox";
	}

	@Override
	public ContentPosition viewToModel(final LayoutContext context, final int x, final int y) {

		final LineBox[] children = this.children;
		for (final LineBox child : children) {
			if (child.hasContent() && y <= child.getY() + child.getHeight()) {
				return child.viewToModel(context, x - child.getX(), y - child.getY());
			}
		}
		throw new RuntimeException("No line at (" + x + ", " + y + ")");
	}

	// ===================================================== PRIVATE

}
