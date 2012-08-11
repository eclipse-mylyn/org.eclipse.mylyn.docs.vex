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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * InlineBox consisting of several children. This is the parent class of InlineElementBox and LineBox, and implements
 * the split method.
 */
public abstract class CompositeInlineBox extends AbstractInlineBox {

	/**
	 * Returns true if any of the children have content.
	 */
	@Override
	public boolean hasContent() {
		final Box[] children = getChildren();
		for (final Box element : children) {
			if (element.hasContent()) {
				return true;
			}
		}
		return false;
	}

	public boolean isEOL() {
		final Box[] children = getChildren();
		return children.length > 0 && ((InlineBox) children[children.length - 1]).isEOL();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getCaret(org.eclipse.vex.core.internal.layout.LayoutContext, int)
	 */
	@Override
	public Caret getCaret(final LayoutContext context, final int offset) {

		int x = 0;
		final Box[] children = getChildren();

		// we want the caret to be to the right of any leading static boxes...
		int start = 0;
		while (start < children.length && !children[start].hasContent()) {
			x += children[start].getWidth();
			start++;
		}

		// ...and to the left of any trailing static boxes
		int end = children.length;
		while (end < 0 && !children[end - 1].hasContent()) {
			end--;
		}

		for (int i = start; i < end; i++) {
			final Box child = children[i];
			if (child.hasContent()) {
				if (offset < child.getStartOffset()) {
					break;
				} else if (offset <= child.getEndOffset()) {
					final Caret caret = child.getCaret(context, offset);
					caret.translate(child.getX(), child.getY());
					return caret;
				}
			}
			x += child.getWidth();
		}

		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(getElement());

		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		final int height = fm.getAscent() + fm.getDescent();
		g.setFont(oldFont);
		font.dispose();

		final int lineHeight = styles.getLineHeight();
		final int y = (lineHeight - height) / 2;
		return new TextCaret(x, y, height);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#split(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, boolean)
	 */
	public Pair split(final LayoutContext context, final int maxWidth, final boolean force) {

		// list of children that have yet to be added to the left side
		final LinkedList<Box> rights = new LinkedList<Box>(Arrays.asList(getChildren()));

		// pending is a list of inlines we are trying to add to the left side
		// but which cannot end at a split
		final List<InlineBox> pending = new ArrayList<InlineBox>();

		// list of inlines that make up the left side
		final List<InlineBox> lefts = new ArrayList<InlineBox>();

		int remaining = maxWidth;
		boolean eol = false;

		while (!rights.isEmpty() && remaining >= 0) {
			final InlineBox inline = (InlineBox) rights.removeFirst();
			final InlineBox.Pair pair = inline.split(context, remaining, force && lefts.isEmpty());

			if (pair.getLeft() != null) {
				lefts.addAll(pending);
				pending.clear();
				lefts.add(pair.getLeft());
				remaining -= pair.getLeft().getWidth();
			}

			if (pair.getRight() != null) {
				pending.add(pair.getRight());
				remaining -= pair.getRight().getWidth();
			}

			if (pair.getLeft() != null && pair.getLeft().isEOL()) {
				eol = true;
				break;
			}

		}

		if ((force && lefts.isEmpty() || remaining >= 0) && !eol) {
			lefts.addAll(pending);
		} else {
			rights.addAll(0, pending);
		}

		final InlineBox[] leftKids = lefts.toArray(new InlineBox[lefts.size()]);
		final InlineBox[] rightKids = rights.toArray(new InlineBox[rights.size()]);
		return this.split(context, leftKids, rightKids);
	}

	/**
	 * Creates a Pair of InlineBoxes, each with its own set of children.
	 * 
	 * @param context
	 *            LayoutContext used for this layout.
	 * @param lefts
	 *            Child boxes to be given to the left box.
	 * @param rights
	 *            Child boxes to be given to the right box.
	 * @return
	 */
	protected abstract Pair split(LayoutContext context, InlineBox[] lefts, InlineBox[] rights);

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#viewToModel(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public int viewToModel(final LayoutContext context, final int x, final int y) {

		if (!hasContent()) {
			throw new RuntimeException("Oops. Calling viewToModel on a line with no content");
		}

		Box closestContentChild = null;
		int delta = Integer.MAX_VALUE;
		final Box[] children = getChildren();
		for (final Box child : children) {
			if (child.hasContent()) {
				int newDelta = 0;
				if (x < child.getX()) {
					newDelta = child.getX() - x;
				} else if (x > child.getX() + child.getWidth()) {
					newDelta = x - (child.getX() + child.getWidth());
				}
				if (newDelta < delta) {
					delta = newDelta;
					closestContentChild = child;
				}
			}
		}

		return closestContentChild.viewToModel(context, x - closestContentChild.getX(), y - closestContentChild.getY());
	}

}
