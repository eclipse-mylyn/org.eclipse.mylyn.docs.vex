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

import java.text.MessageFormat;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A TextBox that gets its text from the document. Represents text which is editable within the VexWidget.
 */
public class DocumentTextBox extends TextBox {

	private final int startRelative;
	private final int endRelative;

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext used to calculate the box's size.
	 * @param node
	 *            Node that directly contains the text.
	 * @param startOffset
	 *            start offset of the text
	 * @param endOffset
	 *            end offset of the text
	 */
	public DocumentTextBox(final LayoutContext context, final INode node, final int startOffset, final int endOffset) {
		super(node);
		if (startOffset > endOffset) {
			// Do not use Assert.isTrue. This Contructor is called very often and the use of Assert.isTrue would evaluate the Message.format every time.
			throw new AssertionFailedException(MessageFormat.format("assertion failed: DocumentTextBox for {2}: startOffset {0} > endOffset {1}", startOffset, endOffset, node)); //$NON-NLS-1$
		}

		final int nodeStart = node.getStartOffset();
		startRelative = startOffset - nodeStart;
		endRelative = endOffset - nodeStart;
		calculateSize(context);

		if (getText().length() < endOffset - startOffset) {
			// Do not use Assert.isTrue. This Contructor is called very often and the use of Assert.isTrue would evaluate the Message.format every time.
			throw new AssertionFailedException(MessageFormat.format("DocumentTextBox for {2}: text shorter than range: {0} < {1}", getText().length(), endOffset - startOffset, node)); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		if (endRelative == -1) {
			return -1;
		} else {
			return getNode().getStartOffset() + endRelative;
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		if (startRelative == -1) {
			return -1;
		} else {
			return getNode().getStartOffset() + startRelative;
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.TextBox#getText()
	 */
	@Override
	public String getText() {
		return getNode().getText(new ContentRange(getStartOffset(), getEndOffset()));
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return getNode().isAssociated();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#paint(org.eclipse.vex.core.internal.layout.LayoutContext, int, int)
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final Graphics g = context.getGraphics();

		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final ColorResource foreground = g.createColor(styles.getColor());
		final ColorResource oldForeground = g.setColor(foreground);
		// ColorResource background =
		// g.createColor(styles.getBackgroundColor());
		// ColorResource oldBackground = g.setBackgroundColor(background);

		final char[] chars = getText().toCharArray();

		if (chars.length < getEndOffset() - getStartOffset()) {
			throw new IllegalStateException();
		}

		if (chars.length == 0) {
			throw new IllegalStateException();
		}

		final int start = 0;
		int end = chars.length;
		if (chars[end - 1] == NEWLINE_CHAR) {
			end--;
		}
		int selStart = context.getSelectionStart() - getStartOffset();
		selStart = Math.min(Math.max(selStart, start), end);
		int selEnd = context.getSelectionEnd() - getStartOffset();
		selEnd = Math.min(Math.max(selEnd, start), end);

		// text before selection
		if (start < selStart) {
			g.drawChars(chars, start, selStart - start, x, y);
			final String s = new String(chars, start, selStart - start);
			paintTextDecoration(context, styles, s, x, y);
		}

		// text after selection
		if (selEnd < end) {
			final int x1 = x + g.charsWidth(chars, 0, selEnd);
			g.drawChars(chars, selEnd, end - selEnd, x1, y);
			final String s = new String(chars, selEnd, end - selEnd);
			paintTextDecoration(context, styles, s, x1, y);
		}

		// text within selection
		if (selStart < selEnd) {
			final String s = new String(chars, selStart, selEnd - selStart);
			final int x1 = x + g.charsWidth(chars, 0, selStart);
			paintSelectedText(context, s, x1, y);
			paintTextDecoration(context, styles, s, x1, y);
		}

		g.setFont(oldFont);
		g.setColor(oldForeground);
		// g.setBackgroundColor(oldBackground);
		font.dispose();
		foreground.dispose();
		// background.dispose();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.TextBox#splitAt(int)
	 */
	@Override
	public Pair splitAt(final LayoutContext context, final int offset) {

		if (offset < 0 || offset > endRelative - startRelative + 1) {
			throw new IllegalStateException();
		}

		final int split = getStartOffset() + offset;

		DocumentTextBox left;
		if (offset == 0) {
			left = null;
		} else {
			left = new DocumentTextBox(context, getNode(), getStartOffset(), split - 1);
		}

		InlineBox right;
		if (split > getEndOffset()) {
			right = null;
		} else {
			right = new DocumentTextBox(context, getNode(), split, getEndOffset());
		}
		return new Pair(left, right);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#viewToModel(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final char[] chars = getText().toCharArray();

		if (getWidth() <= 0) {
			return getStartOffset();
		}

		// first, get an estimate based on x / width
		int offset = x / getWidth() * chars.length;
		offset = Math.max(0, offset);
		offset = Math.min(chars.length, offset);

		int delta = Math.abs(x - g.charsWidth(chars, 0, offset));

		// Search backwards
		while (offset > 0) {
			final int newDelta = Math.abs(x - g.charsWidth(chars, 0, offset - 1));
			if (newDelta > delta) {
				break;
			}
			delta = newDelta;
			offset--;
		}

		// Search forwards
		while (offset < chars.length - 1) {
			final int newDelta = Math.abs(x - g.charsWidth(chars, 0, offset + 1));
			if (newDelta > delta) {
				break;
			}
			delta = newDelta;
			offset++;
		}

		g.setFont(oldFont);
		font.dispose();
		return getStartOffset() + offset;
	}

}
