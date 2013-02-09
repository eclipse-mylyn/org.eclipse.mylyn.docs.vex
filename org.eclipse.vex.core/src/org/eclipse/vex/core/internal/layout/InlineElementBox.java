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

import org.eclipse.vex.core.dom.BaseNodeVisitor;
import org.eclipse.vex.core.dom.ContentRange;
import org.eclipse.vex.core.dom.IComment;
import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.dom.IText;
import org.eclipse.vex.core.internal.core.Drawable;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.PseudoElement;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * An inline box that represents an inline element. This box is responsible for creating and laying out its child boxes.
 */
public class InlineElementBox extends CompositeInlineBox {

	private static final String COMMENT_AFTER_TEXT = "-->";
	private static final String COMMENT_BEFORE_TEXT = "<!--";
	private final INode node;
	private final InlineBox[] children;
	private InlineBox firstContentChild = null;
	private InlineBox lastContentChild = null;
	private int baseline;
	private int halfLeading;

	/**
	 * Class constructor, called by the createInlineBoxes static factory method.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param node
	 *            Element that generated this box
	 * @param startOffset
	 *            Start offset of the range being rendered, which may be arbitrarily before or inside the element.
	 * @param endOffset
	 *            End offset of the range being rendered, which may be arbitrarily after or inside the element.
	 */
	private InlineElementBox(final LayoutContext context, final INode node, final int startOffset, final int endOffset) {

		this.node = node;

		final List<InlineBox> childList = new ArrayList<InlineBox>();

		final Styles styles = context.getStyleSheet().getStyles(node);

		if (startOffset <= node.getStartOffset()) {
			// space for the left margin/border/padding
			final int space = styles.getMarginLeft().get(0) + styles.getBorderLeftWidth() + styles.getPaddingLeft().get(0);

			if (space > 0) {
				childList.add(new SpaceBox(space, 1));
			}

			// :before content
			final PseudoElement beforeElement = context.getStyleSheet().getBeforeElement((IElement) node);
			if (beforeElement != null) {
				childList.addAll(LayoutUtils.createGeneratedInlines(context, beforeElement));
			}
			// left marker
			childList.add(createLeftMarker(node, styles));
		}

		// background image
		if (styles.hasBackgroundImage() && !styles.getDisplay().equalsIgnoreCase(CSS.NONE)) {
			final ImageBox imageBox = ImageBox.createWithHeight(getNode(), context, styles.getLineHeight());
			if (imageBox != null) {
				childList.add(imageBox);
			}
		}

		final InlineBoxes inlines = createInlineBoxes(context, node, new ContentRange(startOffset, endOffset));
		childList.addAll(inlines.boxes);
		firstContentChild = inlines.firstContentBox;
		lastContentChild = inlines.lastContentBox;

		if (endOffset > node.getEndOffset()) {
			childList.add(new PlaceholderBox(context, node, node.getEndOffset() - node.getStartOffset()));

			// trailing marker
			childList.add(createRightMarker(node, styles));

			// :after content
			final PseudoElement afterElement = context.getStyleSheet().getAfterElement((IElement) node);
			if (afterElement != null) {
				childList.addAll(LayoutUtils.createGeneratedInlines(context, afterElement));
			}

			// space for the right margin/border/padding
			final int space = styles.getMarginRight().get(0) + styles.getBorderRightWidth() + styles.getPaddingRight().get(0);

			if (space > 0) {
				childList.add(new SpaceBox(space, 1));
			}
		}

		children = childList.toArray(new InlineBox[childList.size()]);
		layout(context);
	}

	/**
	 * Class constructor. This constructor is called by the split method.
	 * 
	 * @param context
	 *            LayoutContext used for the layout.
	 * @param node
	 *            Node to which this box applies.
	 * @param children
	 *            Child boxes.
	 */
	private InlineElementBox(final LayoutContext context, final INode node, final InlineBox[] children) {
		this.node = node;
		this.children = children;
		layout(context);
		for (final InlineBox child : children) {
			if (child.hasContent()) {
				if (firstContentChild == null) {
					firstContentChild = child;
				}
				lastContentChild = child;
			}
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	public int getBaseline() {
		return baseline;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getChildren()
	 */
	@Override
	public Box[] getChildren() {
		return children;
	}

	/**
	 * Returns the element associated with this box.
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
		if (lastContentChild == null) {
			return getNode().getEndOffset();
		} else {
			return lastContentChild.getEndOffset();
		}
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		if (firstContentChild == null) {
			return getNode().getStartOffset();
		} else {
			return firstContentChild.getStartOffset();
		}
	}

	/**
	 * Override to paint background and borders.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.AbstractBox#paint(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		this.drawBox(context, x, y, 0, true); // TODO CSS violation
		super.paint(context, x, y);
	}

	@Override
	public Pair split(final LayoutContext context, final InlineBox[] lefts, final InlineBox[] rights) {

		InlineElementBox left = null;
		InlineElementBox right = null;

		if (lefts.length > 0 || rights.length == 0) {
			left = new InlineElementBox(context, getNode(), lefts);
		}

		if (rights.length > 0) {
			right = new InlineElementBox(context, getNode(), rights);
		}

		return new Pair(left, right);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if (getStartOffset() == getNode().getStartOffset() + 1) {
			sb.append("<");
			sb.append(getNode());
			sb.append(">");
		}
		final Box[] children = getChildren();
		for (final Box element2 : children) {
			sb.append(element2);
		}
		if (getEndOffset() == getNode().getEndOffset()) {
			sb.append("</");
			sb.append(getNode());
			sb.append(">");
		}
		return sb.toString();
	}

	/**
	 * Holds the results of the createInlineBoxes method.
	 */
	static class InlineBoxes {

		/** List of generated boxes */
		public List<InlineBox> boxes = new ArrayList<InlineBox>();

		/** First generated box that has content */
		public InlineBox firstContentBox;

		/** Last generated box that has content */
		public InlineBox lastContentBox;
	}

	/**
	 * Creates a list of inline boxes given a range of offsets. This method is used when creating both ParagraphBoxes
	 * and InlineElementBoxes.
	 * 
	 * @param context
	 *            LayoutContext to be used.
	 * @param node
	 *            Parent containing both offsets
	 * @param startOffset
	 *            The start of the range to convert to inline boxes.
	 * @param endOffset
	 *            The end of the range to convert to inline boxes.
	 * @return
	 */
	static InlineBoxes createInlineBoxes(final LayoutContext context, final INode node, final ContentRange range) {
		final InlineBoxes result = new InlineBoxes();

		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final IElement element) {
				for (final INode childNode : element.children().in(range)) {
					childNode.accept(new BaseNodeVisitor() {
						@Override
						public void visit(final IElement element) {
							final InlineBox placeholder = new PlaceholderBox(context, node, element.getStartOffset() - node.getStartOffset());
							result.boxes.add(placeholder);
							if (result.firstContentBox == null) {
								result.firstContentBox = placeholder;
							}
							final InlineBox child = new InlineElementBox(context, element, range.getStartOffset(), range.getEndOffset());
							addChildInlineBox(result, child);
						}

						@Override
						public void visit(final IComment comment) {
							final PlaceholderBox placeholder = new PlaceholderBox(context, node, comment.getStartOffset() - node.getStartOffset());
							result.boxes.add(placeholder);
							if (result.firstContentBox == null) {
								result.firstContentBox = placeholder;
							}
							final List<Box> commentBoxes = new ArrayList<Box>();
							commentBoxes.add(new StaticTextBox(context, comment, COMMENT_BEFORE_TEXT));
							if (comment.getEndOffset() - comment.getStartOffset() > 1) {
								commentBoxes.add(new DocumentTextBox(context, comment, comment.getStartOffset() + 1, comment.getEndOffset() - 1));
							}
							commentBoxes.add(new PlaceholderBox(context, comment, comment.getEndOffset() - comment.getStartOffset()));
							commentBoxes.add(new StaticTextBox(context, comment, COMMENT_AFTER_TEXT));
							final InlineBox child = new InlineElementBox(context, comment, commentBoxes.toArray(new InlineBox[commentBoxes.size()]));
							addChildInlineBox(result, child);
						};

						@Override
						public void visit(final IText text) {
							final ContentRange boxRange = range.intersection(text.getRange());
							final InlineBox child = new DocumentTextBox(context, node, boxRange.getStartOffset(), boxRange.getEndOffset());
							addChildInlineBox(result, child);
						}
					});
				}
			}
		});

		return result;
	}

	private static void addChildInlineBox(final InlineBoxes result, final InlineBox child) {
		if (result.firstContentBox == null) {
			result.firstContentBox = child;
		}

		result.lastContentBox = child;

		result.boxes.add(child);
	}

	// ========================================================== PRIVATE

	private static InlineBox createLeftMarker(final INode node, final Styles styles) {
		final int size = Math.round(0.5f * styles.getFontSize());
		final int lift = Math.round(0.1f * styles.getFontSize());
		final Drawable drawable = new Drawable() {
			public void draw(final Graphics g, final int x, int y) {
				g.setLineStyle(Graphics.LINE_SOLID);
				g.setLineWidth(1);
				y -= lift;
				g.drawLine(x, y - size, x, y);
				g.drawLine(x, y, x + size - 1, y - size / 2);
				g.drawLine(x + size - 1, y - size / 2, x, y - size);
			}

			public Rectangle getBounds() {
				return new Rectangle(0, -size, size, size);
			}
		};
		return new DrawableBox(drawable, node, DrawableBox.START_MARKER);
	}

	private static InlineBox createRightMarker(final INode node, final Styles styles) {
		final int size = Math.round(0.5f * styles.getFontSize());
		final int lift = Math.round(0.1f * styles.getFontSize());
		final Drawable drawable = new Drawable() {
			public void draw(final Graphics g, final int x, int y) {
				g.setLineStyle(Graphics.LINE_SOLID);
				g.setLineWidth(1);
				y -= lift;
				g.drawLine(x + size - 1, y - size, x + size - 1, y);
				g.drawLine(x + size - 1, y, x, y - size / 2);
				g.drawLine(x, y - size / 2, x + size - 1, y - size);
			}

			public Rectangle getBounds() {
				return new Rectangle(0, -size, size, size);
			}
		};
		return new DrawableBox(drawable, node, DrawableBox.END_MARKER);
	}

	private void layout(final LayoutContext context) {
		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		setHeight(styles.getLineHeight());
		halfLeading = (styles.getLineHeight() - fm.getAscent() - fm.getDescent()) / 2;
		baseline = halfLeading + fm.getAscent();
		g.setFont(oldFont);
		font.dispose();

		int x = 0;
		for (final InlineBox child : children) {
			// TODO: honour the child's vertical-align property
			child.setX(x);
			child.alignOnBaseline(baseline);
			x += child.getWidth();
		}

		setWidth(x);
	}

}
