/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - added remainingWidth to Pair
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

/**
 * Represents an inline box. Inline boxes are the children of line boxes.
 */
public interface InlineBox extends Box {

	/**
	 * Represents a pair of inline boxes as returned by the <code>split</code> method.
	 */
	public class Pair {
		private final InlineBox left;
		private final InlineBox right;
		private final int remainingWidth;

		/**
		 * Class constructor.
		 * 
		 * @param left
		 *            box to the left of the split
		 * @param right
		 *            box to the right of the split
		 * @param remaining
		 *            the remaining layout width after this split
		 */
		public Pair(final InlineBox left, final InlineBox right, final int remaining) {
			this.left = left;
			this.right = right;
			remainingWidth = remaining;
		}

		/**
		 * Returns the box to the left of the split.
		 */
		public InlineBox getLeft() {
			return left;
		}

		/**
		 * Returns the box to the right of the split.
		 */
		public InlineBox getRight() {
			return right;
		}

		/**
		 * Return the remaining with after the split
		 */
		public int getRemaining() {
			return remainingWidth;
		}
	}

	/**
	 * Returns the distance from the top of the inline box to the baseline.
	 */
	public int getBaseline();

	/**
	 * Aligns this inline box on the given baseline.
	 */
	public void alignOnBaseline(int baseline);

	/**
	 * Returns true if this inline box must be the last box on the current line.
	 */
	public boolean isEOL();

	/**
	 * @return <code>true</code> if this box can be splitted.
	 */
	public boolean isSplitable();

	/**
	 * Splits this inline box into two. If <code>force</code> is false, this method should find a natural split point
	 * (e.g. after a space) and return two boxes representing a split at that point. The width of the last box must not
	 * exceed <code>maxWidth</code>. If no such natural split exists, null should be returned as the left box and
	 * <code>this</code> returned as the right box.
	 * 
	 * <p>
	 * If <code>force</code> is true, it means we are adding the first inline box to a line, therefore we must return
	 * something as the left box. In some cases, we may find a suboptimal split (e.g. between characters) that satisfies
	 * this. In other cases, <code>this</code> should be returned as the left box even though it exceeds maxWidth.
	 * </p>
	 * 
	 * <p>
	 * If the entire box fits within <code>maxWidth</code>, it should only be returned as the left box if it can end a
	 * line; otherwise, it should be returned as the right box. Most implementations <i>cannot</i> end a line (one
	 * notable exception being a text box ending in whitespace) and should therefore return themselves as the right box.
	 * </p>
	 * 
	 * @param context
	 *            the layout context to be used.
	 * @param maxWidth
	 *            Maximum width of the left part of the box.
	 * @param force
	 *            if true, force a suboptimal split
	 * @return A new {@link Pair} of inline boxes, or null if this box is not splitable
	 */
	public Pair split(LayoutContext context, int maxWidth, boolean force);
}
