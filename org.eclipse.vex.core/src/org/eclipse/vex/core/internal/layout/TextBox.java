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
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * An inline box containing text. The <code>getText</code> and <code>splitAt</code> methods are abstract and must be
 * implemented by subclasses.
 */
public abstract class TextBox extends AbstractInlineBox implements InlineBox {

	private final INode node;
	private int baseline;

	public static final char NEWLINE_CHAR = 0xa;
	public static final String NEWLINE_STRING = "\n";

	/**
	 * Class constructor.
	 * 
	 * @param node
	 *            Node containing the text. This is used for styling information.
	 */
	public TextBox(final INode node) {
		this.node = node;
	}

	/**
	 * Causes the box to recalculate it size. Subclasses should call this from their constructors after they are
	 * initialized.
	 * 
	 * @param context
	 *            LayoutContext used to calculate size.
	 */
	protected void calculateSize(final LayoutContext context) {
		String s = getText();
		if (s.endsWith(NEWLINE_STRING)) {
			s = s.substring(0, s.length() - 1);
		}

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		setWidth(g.stringWidth(s));
		setHeight(styles.getLineHeight());
		final int halfLeading = (getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
		baseline = halfLeading + fm.getAscent();
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
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {
		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);
		final FontResource oldFont = g.getFont();
		final FontResource font = g.createFont(styles.getFont());
		g.setFont(font);
		final char[] chars = getText().toCharArray();
		final int x = g.charsWidth(chars, 0, offset - getStartOffset());
		g.setFont(oldFont);
		font.dispose();
		return new TextCaret(x, 0, getHeight());
	}

	/**
	 * Returns the node that controls the styling for this text box.
	 */
	@Override
	public INode getNode() {
		return node;
	}

	/**
	 * Return the text that comprises this text box. The actual text can come from the document content or from a static
	 * string.
	 */
	public abstract String getText();

	/**
	 * Returns true if the given character is one where a linebreak should occur, e.g. a space.
	 * 
	 * @param c
	 *            the character to test
	 */
	public static boolean isSplitChar(final char c) {
		return Character.isWhitespace(c);
	}

	public boolean isEOL() {
		final String s = getText();
		return s.length() > 0 && s.charAt(s.length() - 1) == NEWLINE_CHAR;
	}

	/**
	 * Paints a string as selected text.
	 * 
	 * @param context
	 *            LayoutContext to be used. It is assumed that the contained Graphics object is set up with the proper
	 *            font.
	 * @param s
	 *            String to draw
	 * @param x
	 *            x-coordinate at which to draw the text
	 * @param y
	 *            y-coordinate at which to draw the text
	 */
	protected void paintSelectedText(final LayoutContext context, final String s, final int x, final int y) {
		final Graphics g = context.getGraphics();

		boolean inSelectedBlock = false;
		INode node = getNode();
		while (node != null) {
			if (context.getWhitespacePolicy().isBlock(node)) {
				if (context.isNodeSelected(node)) {
					inSelectedBlock = true;
				}
				break;
			}
			node = node.getParent();
		}

		if (inSelectedBlock) {
			g.setColor(g.getSystemColor(ColorResource.SELECTION_BACKGROUND));
			g.drawString(s, x, y);
		} else {
			final int width = g.stringWidth(s);
			g.setColor(g.getSystemColor(ColorResource.SELECTION_BACKGROUND));
			g.fillRect(x, y, width, getHeight());
			g.setColor(g.getSystemColor(ColorResource.SELECTION_FOREGROUND));
			g.drawString(s, x, y);
		}
	}

	protected void paintTextDecoration(final LayoutContext context, final Styles styles, final String s, final int x, final int y) {
		final int fontStyle = styles.getFont().getStyle();
		final Graphics g = context.getGraphics();
		final FontMetrics fm = g.getFontMetrics();

		if ((fontStyle & FontSpec.UNDERLINE) != 0) {
			final int lineWidth = fm.getAscent() / 12;
			final int ypos = y + fm.getAscent() + lineWidth;
			paintBaseLine(g, s, x, ypos);
		}
		if ((fontStyle & FontSpec.OVERLINE) != 0) {
			final int lineWidth = fm.getAscent() / 12;
			final int ypos = y + lineWidth / 2;
			paintBaseLine(g, s, x, ypos);
		}
		if ((fontStyle & FontSpec.LINE_THROUGH) != 0) {
			final int ypos = y + fm.getHeight() / 2;
			paintBaseLine(g, s, x, ypos);
		}
	}

	/**
	 * Paint a line along the baseline of the text, for showing underline, overline and strike-through formatting.
	 * 
	 * @param context
	 *            LayoutContext to be used. It is assumed that the contained Graphics object is set up with the proper
	 *            font.
	 * @param x
	 *            x-coordinate at which to start drawing baseline
	 * @param y
	 *            x-coordinate at which to start drawing baseline (adjusted to produce the desired under/over/though
	 *            effect)
	 */
	protected void paintBaseLine(final Graphics g, final String s, final int x, final int y) {
		final FontMetrics fm = g.getFontMetrics();
		final int width = g.stringWidth(s);
		final int lineWidth = fm.getAscent() / 12;
		g.setLineStyle(Graphics.LINE_SOLID);
		g.setLineWidth(lineWidth);
		g.drawLine(x, y, x + width, y);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#split(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, boolean)
	 */
	public Pair split(final LayoutContext context, final int maxWidth, final boolean force) {

		final char[] chars = getText().toCharArray();

		if (chars.length == 0) {
			throw new IllegalStateException();
		}

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);

		int split = 0;
		int next = 1;
		boolean eol = false; // end of line found
		while (next < chars.length) {
			if (isSplitChar(chars[next - 1])) {
				if (g.charsWidth(chars, 0, next) <= maxWidth) {
					split = next;
					if (chars[next - 1] == NEWLINE_CHAR) {
						eol = true;
						break;
					}
				} else {
					break;
				}
			}
			next++;
		}

		if (force && split == 0) {
			// find some kind of split
			split = 1;
			while (split < chars.length) {
				if (g.charsWidth(chars, 0, split + 1) > maxWidth) {
					break;
				}
				split++;
			}

		}

		// include any trailing spaces in the split
		// this also grabs any leading spaces when split==0
		if (!eol) {
			while (split < chars.length - 1 && chars[split] == ' ') {
				split++;
			}
		}

		g.setFont(oldFont);
		font.dispose();

		return splitAt(context, split);
	}

	/**
	 * Return a pair of boxes representing a split at the given offset. If split is zero, then the returned left box
	 * should be null. If the split is equal to the length of the text, then the right box should be null.
	 * 
	 * @param context
	 *            LayoutContext used to calculate the sizes of the resulting boxes.
	 * @param offset
	 *            location of the split, relative to the start of the text box.
	 * @return
	 */
	public abstract Pair splitAt(LayoutContext context, int offset);

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getText();
	}

}
