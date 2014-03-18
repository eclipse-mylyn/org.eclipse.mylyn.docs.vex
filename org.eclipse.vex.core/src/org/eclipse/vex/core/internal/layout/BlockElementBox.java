/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *     Carsten Hiesserich - changed pseudo element handling
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * A block box corresponding to a DOM Element. Block boxes lay their children out stacked top to bottom. Block boxes
 * correspond to the <code>display: block;</code> CSS property.
 */
public class BlockElementBox extends AbstractBlockBox {

	/** number of boxes created since VM startup, for profiling */
	private static int boxCount;

	/**
	 * Class constructor. This box's children are not created here but in the first call to layout. Instead, we estimate
	 * the box's height here based on the given width.
	 * 
	 * @param context
	 *            LayoutContext used for this layout.
	 * @param parent
	 *            This box's parent box.
	 * @param node
	 *            Node to which this box corresponds.
	 */
	public BlockElementBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	/**
	 * @return the number of boxes created since VM startup. Used for profiling.
	 */
	public static int getBoxCount() {
		return boxCount;
	}

	@Override
	public int getEndOffset() {
		return getNode().getEndOffset();
	}

	@Override
	public int getStartOffset() {
		return getNode().getStartOffset() + 1;
	}

	@Override
	public String toString() {
		return "BlockElementBox: <" + getNode() + ">" + "[x=" + getX() + ",y=" + getY() + ",width=" + getWidth() + ",height=" + getHeight() + "]";
	}

	@Override
	public List<Box> createChildren(final LayoutContext context) {
		long start = 0;
		if (VEXCorePlugin.getInstance().isDebugging()) {
			start = System.currentTimeMillis();
		}

		final INode node = getNode();
		final int width = getWidth();

		final List<Box> childList = new ArrayList<Box>();

		final StyleSheet styleSheet = context.getStyleSheet();
		final Styles styles = styleSheet.getStyles(node);

		// :before content
		List<InlineBox> beforeInlines = null;
		final IElement before = styleSheet.getPseudoElementBefore(node);
		if (before != null) {
			final Styles beforeStyles = styleSheet.getStyles(before);
			if (beforeStyles.getDisplay().equals(CSS.INLINE)) {
				beforeInlines = new ArrayList<InlineBox>();
				beforeInlines.addAll(LayoutUtils.createGeneratedInlines(context, before));
			} else {
				childList.add(new BlockPseudoElementBox(context, before, this, width));
			}
		}

		// background image
		if (styles.hasBackgroundImage() && !styles.getDisplay().equalsIgnoreCase(CSS.NONE)) {
			final InlineBox imageBox = ImageBox.create(node, context, getWidth());
			if (imageBox != null) {
				if (beforeInlines == null) {
					beforeInlines = new ArrayList<InlineBox>();
				}
				beforeInlines.add(imageBox);
			}
		}

		// :after content
		Box afterBlock = null;
		List<InlineBox> afterInlines = null;

		final IElement after = styleSheet.getPseudoElementAfter(node);
		if (after != null) {
			final Styles afterStyles = styleSheet.getStyles(after);
			if (afterStyles.getDisplay().equals(CSS.INLINE)) {
				afterInlines = new ArrayList<InlineBox>();
				afterInlines.addAll(LayoutUtils.createGeneratedInlines(context, after));
			} else {
				afterBlock = new BlockPseudoElementBox(context, after, this, width);
			}
		}

		final int startOffset = node.getStartOffset() + 1;
		final int endOffset = node.getEndOffset();
		final List<Box> blockBoxes = createBlockBoxes(context, startOffset, endOffset, width, beforeInlines, afterInlines);
		childList.addAll(blockBoxes);

		if (afterBlock != null) {
			childList.add(afterBlock);
		}

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 10) {
				System.out.println("BEB.layout for " + getNode() + " took " + (end - start) + "ms");
			}
		}

		return childList;
	}

	protected int getFirstLineTop(final LayoutContext context) {
		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final int top = styles.getBorderTopWidth() + styles.getPaddingTop().get(0);
		final Box[] children = getChildren();
		if (children != null && children.length > 0 && children[0] instanceof BlockElementBox) {
			return top + ((BlockElementBox) children[0]).getFirstLineTop(context);
		} else {
			return top;
		}
	}

}
