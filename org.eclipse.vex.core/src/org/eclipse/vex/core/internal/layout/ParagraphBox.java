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
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A box that wraps inline content into a paragraph.
 */
public class ParagraphBox extends AbstractBox implements BlockBox {

	private final LineBox[] children;
	private LineBox firstContentLine;
	private LineBox lastContentLine;

	/**
	 * Class constructor.
	 * 
	 * @param children
	 *            Line boxes that comprise the paragraph.
	 */
	private ParagraphBox(final LineBox[] children) {
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
			//FIXME icampist this indicates some design problem, since later on, LineBoxes are expected
			//either we cast here to LineBox or we make an if later on.
			lines.add(pair.getLeft());
			right = pair.getRight();
		}

		final Styles styles = context.getStyleSheet().getStyles(node);
		final String textAlign = styles.getTextAlign();

		// y-offset of the next line
		int y = 0;

		int actualWidth = 0;

		//children for the ParagraphBox constructor that accepts only LineBoxes
		final List<LineBox> lineBoxesChildren = new ArrayList<LineBox>();

		for (final Box lineBox : lines) {
			int x;
			if (textAlign.equals(CSS.RIGHT)) {
				x = width - lineBox.getWidth();
			} else if (textAlign.equals(CSS.CENTER)) {
				x = (width - lineBox.getWidth()) / 2;
			} else {
				x = 0;
			}

			lineBox.setX(x);
			lineBox.setY(y);

			y += lineBox.getHeight();
			actualWidth = Math.max(actualWidth, lineBox.getWidth());

			//strange, but we need to check the case because it's not explicit anywhere
			if (lineBox instanceof LineBox) {
				lineBoxesChildren.add((LineBox) lineBox);
			}
		}

		final ParagraphBox para = new ParagraphBox(lineBoxesChildren.toArray(new LineBox[lineBoxesChildren.size()]));
		para.setWidth(actualWidth);
		para.setHeight(y);

		// BlockElementBox uses a scaling factor to estimate box height based
		// on font size, layout width, and character count, as follows.
		//
		// estHeight = factor * fontSize * fontSize * charCount / width
		//
		// This bit reports the actual factor that would correctly estimate
		// the height of a BlockElementBox containing only this paragraph.
		//
		// factor = estHeight * width / (fontSize * fontSize * charCount)
		//
		/*
		 * Box firstContentBox = null; for (int i = 0; i < inlines.length; i++) { Box box = inlines[i]; if
		 * (box.hasContent()) { firstContentBox = box; break; } }
		 * 
		 * if (firstContentBox != null) { float fontSize = styles.getFontSize(); int charCount =
		 * lastContentBox.getEndOffset() - firstContentBox.getStartOffset(); float factor = para.getHeight()
		 * para.getWidth() / (fontSize fontSize charCount); System.out.println("Actual factor is " + factor); }
		 */

		return para;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {

		final LineBox line = getLineAt(offset);
		final Caret caret = line.getCaret(context, offset);
		caret.translate(line.getX(), line.getY());
		return caret;

	}

	@Override
	public Box[] getChildren() {
		return children;
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
	public LineBox getLineAt(final int offset) {
		final LineBox[] children = this.children;
		for (final LineBox element : children) {
			if (element.hasContent() && offset <= element.getEndOffset()) {
				return element;
			}
		}
		return lastContentLine;
	}

	public int getLineEndOffset(final int offset) {
		return getLineAt(offset).getEndOffset();
	}

	public int getLineStartOffset(final int offset) {
		return getLineAt(offset).getStartOffset();
	}

	public int getMarginBottom() {
		return 0;
	}

	public int getMarginTop() {
		return 0;
	}

	public int getNextLineOffset(final LayoutContext context, final int offset, final int x) {
		LineBox nextLine = null;
		final LineBox[] children = this.children;
		for (final LineBox element : children) {
			if (element.hasContent() && element.getStartOffset() > offset) {
				nextLine = element;
				break;
			}
		}
		if (nextLine == null) {
			// return this.getEndOffset() + 1;
			return -1;
		} else {
			return nextLine.viewToModel(context, x - nextLine.getX(), 0);
		}
	}

	public BlockBox getParent() {
		throw new IllegalStateException("ParagraphBox does not currently track parent");
	}

	public int getPreviousLineOffset(final LayoutContext context, final int offset, final int x) {
		LineBox prevLine = null;
		final LineBox[] children = this.children;
		for (int i = children.length - 1; i >= 0; i--) {
			if (children[i].hasContent() && children[i].getEndOffset() < offset) {
				prevLine = children[i];
				break;
			}
		}
		if (prevLine == null) {
			// return this.getStartOffset() - 1;
			return -1;
		} else {
			return prevLine.viewToModel(context, x - prevLine.getX(), 0);
		}
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

	public void setInitialSize(final LayoutContext context) {
		// NOP - size calculated in factory method
	}

	@Override
	public String toString() {
		return "ParagraphBox";
	}

	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {

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
