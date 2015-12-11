package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

public class InlineFrame extends BaseBox implements IInlineBox, IDecoratorBox<IInlineBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private Margin margin = Margin.NULL;
	private Border border = Border.NULL;
	private Padding padding = Padding.NULL;

	private IInlineBox component;

	@Override
	public void setParent(final IBox parent) {
		this.parent = parent;
	}

	@Override
	public IBox getParent() {
		return parent;
	}

	@Override
	public int getAbsoluteTop() {
		if (parent == null) {
			return top;
		}
		return parent.getAbsoluteTop() + top;
	}

	@Override
	public int getAbsoluteLeft() {
		if (parent == null) {
			return left;
		}
		return parent.getAbsoluteLeft() + left;
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getBaseline() {
		if (component == null) {
			return 0;
		}
		return component.getTop() + component.getBaseline();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, height);
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public Margin getMargin() {
		return margin;
	}

	public void setMargin(final Margin margin) {
		this.margin = margin;
	}

	public Border getBorder() {
		return border;
	}

	public void setBorder(final Border border) {
		this.border = border;
	}

	public Padding getPadding() {
		return padding;
	}

	public void setPadding(final Padding padding) {
		this.padding = padding;
	}

	@Override
	public void setComponent(final IInlineBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IInlineBox getComponent() {
		return component;
	}

	@Override
	public void layout(final Graphics graphics) {
		layoutComponent(graphics);

		calculateBounds();
	}

	private void calculateBounds() {
		if (component == null || component.getWidth() == 0 || component.getHeight() == 0) {
			height = 0;
			width = 0;
		} else {
			height = topFrame() + component.getHeight() + bottomFrame();
			width = leftFrame() + component.getWidth() + rightFrame();
		}
	}

	private void layoutComponent(final Graphics graphics) {
		if (component == null) {
			return;
		}
		component.setPosition(topFrame(), leftFrame());
		component.layout(graphics);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		final int oldWidth = width;

		calculateBounds();

		return oldHeight != height || oldWidth != width;
	}

	private int rightFrame() {
		return margin.right + border.right + padding.right;
	}

	private int leftFrame() {
		return margin.left + border.left + padding.left;
	}

	private int bottomFrame() {
		return margin.bottom + border.bottom + padding.bottom;
	}

	private int topFrame() {
		return margin.top + border.top + padding.top;
	}

	@Override
	public void paint(final Graphics graphics) {
		drawBorder(graphics);
		paintComponent(graphics);
	}

	private void drawBorder(final Graphics graphics) {
		final ColorResource colorResource = graphics.getColor(Color.BLACK); // TODO store border color
		graphics.setColor(colorResource);

		drawBorderLine(graphics, border.top, margin.top, margin.left - border.left / 2, margin.top, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.left, margin.top - border.top / 2, margin.left, height - margin.bottom + border.bottom / 2, margin.left);
		drawBorderLine(graphics, border.bottom, height - margin.bottom, margin.left - border.left / 2, height - margin.bottom, width - margin.right + border.right / 2);
		drawBorderLine(graphics, border.right, margin.top - border.top / 2, width - margin.right, height - margin.bottom + border.bottom / 2, width - margin.right);
	}

	private void drawBorderLine(final Graphics graphics, final int lineWidth, final int top, final int left, final int bottom, final int right) {
		if (lineWidth <= 0) {
			return;
		}
		graphics.setLineWidth(lineWidth);
		graphics.drawLine(left, top, right, bottom);
	}

	private void paintComponent(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}

	@Override
	public boolean canJoin(final IInlineBox other) {
		if (!(other instanceof InlineFrame)) {
			return false;
		}
		final InlineFrame otherFrame = (InlineFrame) other;

		if (!margin.equals(otherFrame.margin) || !border.equals(otherFrame.border) || !padding.equals(otherFrame.padding)) {
			return false;
		}
		if (!component.canJoin(otherFrame.component)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean join(final IInlineBox other) {
		if (!canJoin(other)) {
			return false;
		}
		final InlineFrame otherFrame = (InlineFrame) other;

		component.join(otherFrame.component);

		calculateBounds();

		return true;
	}

	@Override
	public boolean canSplit() {
		if (component == null) {
			return false;
		}
		return component.canSplit();
	}

	@Override
	public IInlineBox splitTail(final Graphics graphics, final int headWidth, final boolean force) {
		final IInlineBox tailComponent;
		final int tailHeadWidth = headWidth - leftFrame();
		if (tailHeadWidth < 0) {
			tailComponent = component;
			component = null;
		} else {
			tailComponent = component.splitTail(graphics, tailHeadWidth, force);
		}

		final InlineFrame tail = new InlineFrame();
		tail.setComponent(tailComponent);
		tail.setParent(parent);
		tail.setMargin(margin);
		tail.setBorder(border);
		tail.setPadding(padding);
		tail.layout(graphics);

		layout(graphics);

		return tail;
	}

	@Override
	public String toString() {
		return "InlineFrame [top=" + top + ", left=" + left + ", width=" + width + ", height=" + height + ", margin=" + margin + ", border=" + border + ", padding=" + padding + "]";
	}

}
