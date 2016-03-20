/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.core.TextAlign;

public class ListItem extends BaseBox implements IStructuralBox, IDecoratorBox<IStructuralBox> {

	public static final int BULLET_SPACING = 5;

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private IInlineBox bullet;
	private int bulletWidth;
	private TextAlign bulletAlign = TextAlign.RIGHT;

	private Paragraph bulletContainer;
	private IStructuralBox component;

	private IVisualDecorator<IStructuralBox> visualDecorator;

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

	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public void setPosition(final int top, final int left) {
		this.top = top;
		this.left = left;
	}

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = Math.max(0, width);
	}

	@Override
	public int getHeight() {
		return height;
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

	public void setBulletWidth(final int bulletWidth) {
		this.bulletWidth = bulletWidth;
	}

	public int getBulletWidth() {
		return bulletWidth;
	}

	public void setBulletAlign(final TextAlign bulletAlign) {
		this.bulletAlign = bulletAlign;
	}

	public TextAlign getBulletAlign() {
		return bulletAlign;
	}

	public void setBullet(final IInlineBox bullet) {
		this.bullet = bullet;
		bulletContainer = null;
	}

	public IInlineBox getBullet() {
		return bullet;
	}

	public void setComponent(final IStructuralBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IStructuralBox getComponent() {
		return component;
	}

	@Override
	public void layout(final Graphics graphics) {
		layoutWithOutsideBullet(graphics);

		height = Math.max(getBulletHeight(), getComponentHeight());
	}

	private void layoutWithOutsideBullet(final Graphics graphics) {
		if (bullet != null && bulletContainer == null) {
			bulletContainer = new Paragraph();
			bulletContainer.setParent(this);
			bulletContainer.setTextAlign(bulletAlign);
			bulletContainer.appendChild(bullet);
		}

		if (bulletContainer != null) {
			bulletContainer.setWidth(bulletWidth);
			bulletContainer.layout(graphics);
		}

		if (component != null) {
			component.setWidth(getWidthForComponent());
			component.layout(graphics);
		}

		final int bulletBaseline = findTopBaselineRelativeToParent(bulletContainer);
		final int componentBaseline = findTopBaselineRelativeToParent(component);

		final int baselineDelta = componentBaseline - bulletBaseline;
		final int bulletTop;
		final int componentTop;
		if (baselineDelta > 0) {
			bulletTop = baselineDelta;
			componentTop = 0;
		} else {
			bulletTop = 0;
			componentTop = -baselineDelta;
		}

		if (bulletContainer != null) {
			bulletContainer.setPosition(bulletTop, 0);
		}
		if (component != null) {
			component.setPosition(componentTop, width - component.getWidth());
		}
	}

	private static int findTopBaselineRelativeToParent(final IStructuralBox parent) {
		if (parent == null) {
			return 0;
		}

		final Integer result = parent.accept(new DepthFirstBoxTraversal<Integer>() {
			private int getBaselineRelativeToParent(final IInlineBox box) {
				return box.getBaseline() + box.getAbsoluteTop() - parent.getAbsoluteTop();
			}

			@Override
			public Integer visit(final InlineContainer box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final Image box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final InlineFrame box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final InlineNodeReference box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final NodeEndOffsetPlaceholder box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final NodeTag box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final Square box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final StaticText box) {
				return getBaselineRelativeToParent(box);
			}

			@Override
			public Integer visit(final TextContent box) {
				return getBaselineRelativeToParent(box);
			}
		});

		if (result == null) {
			return 0;
		}
		return result.intValue();
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;

		height = Math.max(getBulletHeight(), getComponentHeight());

		return oldHeight != height;
	}

	private int getBulletHeight() {
		if (bulletContainer == null) {
			return 0;
		}
		return bulletContainer.getHeight();
	}

	private int getComponentHeight() {
		if (component == null) {
			return 0;
		}
		return component.getHeight();
	}

	private int getWidthForComponent() {
		if (bulletContainer == null) {
			return width;
		}
		return width - bulletWidth - BULLET_SPACING;
	}

	@Override
	public void paint(final Graphics graphics) {
		if (bulletContainer != null) {
			ChildBoxPainter.paint(bulletContainer, graphics);
		}
		ChildBoxPainter.paint(component, graphics);
	}

	@Override
	public void setVisualDecorator(final IVisualDecorator<IStructuralBox> visualDecorator) {
		this.visualDecorator = visualDecorator;

	}

	@Override
	public void resetVisualDecorator() {
		visualDecorator = null;
	}

	@Override
	public void applyVisualDecorator() {
		if (visualDecorator != null) {
			visualDecorator.decorate(this);
		}
	}
}
