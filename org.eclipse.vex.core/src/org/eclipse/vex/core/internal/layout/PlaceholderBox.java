/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A zero-width box that represents a single offset in the document.
 */
public class PlaceholderBox extends AbstractInlineBox {

	private final INode node;
	private final int relOffset;
	private final int textTop;
	private final int baseline;

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext in effect.
	 * @param node
	 *            Node containing this placeholder. The node is used both to determine the size of the box and its
	 *            caret, but also as a base point for relOffset.
	 * @param relOffset
	 *            Offset of the placeholder, relative to the start of the element.
	 */
	public PlaceholderBox(final LayoutContext context, final INode node, final int relOffset) {

		this.node = node;
		this.relOffset = relOffset;

		setWidth(0);

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		final int height = fm.getAscent() + fm.getDescent();

		final int lineHeight = styles.getLineHeight();
		textTop = (lineHeight - height) / 2;

		baseline = textTop + fm.getAscent();
		setHeight(lineHeight);
		g.setFont(oldFont);
		font.dispose();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	public int getBaseline() {
		return baseline;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#split(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, boolean)
	 */
	public Pair split(final LayoutContext context, final int maxWidth, final boolean force) {
		return new Pair(null, this);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {
		return new TextCaret(0, 0, getHeight());
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getNode()
	 */
	@Override
	public INode getNode() {
		return node;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return node.getStartOffset() + relOffset;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		return node.getStartOffset() + relOffset;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return true;
	}

	public boolean isEOL() {
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[placeholder(" + getStartOffset() + ")]";
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#viewToModel(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {
		return getStartOffset();
	}

}
