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

import java.util.List;

import org.eclipse.vex.core.internal.core.IntRange;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Element;

/**
 * Implements a Block
 */
public class BlockPseudoElementBox extends AbstractBox implements BlockBox {

	private final Element pseudoElement;
	private final BlockBox parent;
	private final ParagraphBox para;

	private final int marginTop;
	private final int marginBottom;

	public BlockPseudoElementBox(final LayoutContext context, final Element pseudoElement, final BlockBox parent, final int width) {

		this.pseudoElement = pseudoElement;
		this.parent = parent;

		final Styles styles = context.getStyleSheet().getStyles(pseudoElement);

		marginTop = styles.getMarginTop().get(width);
		marginBottom = styles.getMarginBottom().get(width);

		final int leftInset = styles.getMarginLeft().get(width) + styles.getBorderLeftWidth() + styles.getPaddingLeft().get(width);
		final int rightInset = styles.getMarginRight().get(width) + styles.getBorderRightWidth() + styles.getPaddingRight().get(width);

		final int childWidth = width - leftInset - rightInset;
		final List<InlineBox> inlines = LayoutUtils.createGeneratedInlines(context, pseudoElement);
		para = ParagraphBox.create(context, pseudoElement, inlines, childWidth);

		para.setX(0);
		para.setY(0);
		setWidth(width - leftInset - rightInset);
		setHeight(para.getHeight());
	}

	/**
	 * Provide children for {@link AbstractBox#paint}.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#getChildren()
	 */
	@Override
	public Box[] getChildren() {
		return new Box[] { para };
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getElement()
	 */
	@Override
	public Element getElement() {
		return pseudoElement;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getFirstLine()
	 */
	public LineBox getFirstLine() {
		throw new IllegalStateException();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getLastLine()
	 */
	public LineBox getLastLine() {
		throw new IllegalStateException();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getLineEndOffset(int)
	 */
	public int getLineEndOffset(final int offset) {
		throw new IllegalStateException();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getLineStartOffset(int)
	 */
	public int getLineStartOffset(final int offset) {
		throw new IllegalStateException();
	}

	public int getMarginBottom() {
		return marginBottom;
	}

	public int getMarginTop() {
		return marginTop;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getNextLineOffset(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	public int getNextLineOffset(final LayoutContext context, final int offset, final int x) {
		throw new IllegalStateException();
	}

	/**
	 * Returns this box's parent.
	 */
	public BlockBox getParent() {
		return parent;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.BlockBox#getPreviousLineOffset(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	public int getPreviousLineOffset(final LayoutContext context, final int offset, final int x) {
		throw new IllegalStateException();
	}

	public IntRange layout(final LayoutContext context, final int top, final int bottom) {
		return null;
	}

	public void invalidate(final boolean direct) {
		throw new IllegalStateException("invalidate called on a non-element BlockBox");
	}

	/**
	 * Draw boxes before painting our child.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.Box#paint(org.eclipse.vex.core.internal.layout.LayoutContext, int, int)
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		this.drawBox(context, x, y, getParent().getWidth(), true);
		super.paint(context, x, y);
	}

	public void setInitialSize(final LayoutContext context) {
		// NOP - size calculated in the ctor
	}

}
