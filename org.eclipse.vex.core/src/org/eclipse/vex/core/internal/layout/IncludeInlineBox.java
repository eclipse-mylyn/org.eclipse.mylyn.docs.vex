package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;

public class IncludeInlineBox extends CompositeInlineBox {
	private static final int H_CARET_LENGTH = 20;

	private final INode node;
	private InlineBox[] children;
	private InlineBox firstContentChild = null;
	private int baseline;

	/**
	 * Class constructor, called by the {@link InlineElementBox#createInlineBoxes} static factory method. The Box
	 * created here is only temporary, it will be replaced by the split method.
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
	public IncludeInlineBox(final LayoutContext context, final INode node, final int startOffset, final int endOffset) {
		this.node = node;
		final Styles styles = context.getStyleSheet().getStyles(node);
		final List<InlineBox> childList = new ArrayList<InlineBox>();

		if (startOffset <= node.getStartOffset()) {
			// space for the left margin/border/padding
			final int space = styles.getMarginLeft().get(0) + styles.getBorderLeftWidth() + styles.getPaddingLeft().get(0);

			if (space > 0) {
				childList.add(new SpaceBox(space, 1));
			}

			// :before content
			final IElement beforeElement = context.getStyleSheet().getPseudoElement(node, CSS.PSEUDO_BEFORE, true);
			if (beforeElement != null) {
				childList.addAll(LayoutUtils.createGeneratedInlines(context, beforeElement, StaticTextBox.START_MARKER));
			}

		}

		if (endOffset > node.getEndOffset()) {
			// :after content
			final IElement afterElement = context.getStyleSheet().getPseudoElement(node, CSS.PSEUDO_AFTER, true);
			if (afterElement != null) {
				childList.addAll(LayoutUtils.createGeneratedInlines(context, afterElement));
			}

			// space for the right margin/border/padding
			final int space = styles.getMarginRight().get(0) + styles.getBorderRightWidth() + styles.getPaddingRight().get(0);

			if (space > 0) {
				childList.add(new SpaceBox(space, 1));
			}
		}

		if (childList.isEmpty()) {
			// No pseudo element and no content - return something
			childList.add(new StaticTextBox(context, node, CSS.INCLUDE, StaticTextBox.START_MARKER));
		}

		children = childList.toArray(new InlineBox[childList.size()]);
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
	private IncludeInlineBox(final LayoutContext context, final INode node, final InlineBox[] children) {
		this.node = node;
		this.children = children;
		layout(context);
		for (final InlineBox child : children) {
			if (child.hasContent() && firstContentChild == null) {
				firstContentChild = child;
			}
		}
	}

	private void layout(final LayoutContext context) {
		final Graphics g = context.getGraphics();
		final Styles styles = context.getStyleSheet().getStyles(node);
		final FontResource font = g.createFont(styles.getFont());
		final FontResource oldFont = g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		setHeight(styles.getLineHeight());
		final int halfLeading = (styles.getLineHeight() - fm.getAscent() - fm.getDescent()) / 2;
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

	/**
	 * Override to paint background and borders.
	 * 
	 * @see org.eclipse.vex.core.internal.layout.AbstractBox#paint(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		this.drawBox(context, x, y, 0, true);
		super.paint(context, x, y);
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
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		return getNode().getStartOffset();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return getNode().getEndOffset();
	}

	@Override
	public boolean hasContent() {
		return node.isAssociated();
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public Caret getCaret(final LayoutContext context, final ContentPosition position) {
		return new HCaret(0, getBaseline() + 1, H_CARET_LENGTH);
	}

	@Override
	public int getBaseline() {
		return baseline;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#viewToModel(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      int, int)
	 */
	@Override
	public ContentPosition viewToModel(final LayoutContext context, final int x, final int y) {
		return getNode().getStartPosition().moveBy(1);
	}

	@Override
	public Pair split(final LayoutContext context, final InlineBox[] lefts, final InlineBox[] rights, final int remaining) {

		IncludeInlineBox left = null;
		IncludeInlineBox right = null;

		if (lefts.length > 0 || rights.length == 0) {
			left = new IncludeInlineBox(context, getNode(), lefts);
		}

		if (rights.length > 0) {
			// We reuse this element instead of creating a new one
			children = rights;
			for (final InlineBox child : children) {
				if (child.hasContent()) {
					// The lastContentChild is already set in this instance an did not change
					firstContentChild = child;
					break;
				}
			}
			if (left == null && remaining >= 0) {
				// There is no left box, and the right box fits without further splitting, so we have to calculate the size here
				layout(context);
			}
			right = this;
		}

		return new Pair(left, right, remaining);
	}
}
