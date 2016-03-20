package org.eclipse.vex.core.internal.boxes;

import java.util.ArrayList;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.BulletStyle;

public class List extends BaseBox implements IStructuralBox, IDecoratorBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;

	private BulletStyle bulletStyle;
	private IBulletFactory bulletFactory;

	private IStructuralBox component;

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

	public void setBulletStyle(final BulletStyle bulletStyle) {
		this.bulletStyle = bulletStyle;
	}

	public BulletStyle getBulletStyle() {
		return bulletStyle;
	}

	public void setBulletFactory(final IBulletFactory bulletFactory) {
		this.bulletFactory = bulletFactory;
	}

	public IBulletFactory getBulletFactory() {
		return bulletFactory;
	}

	@Override
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
		if (component == null) {
			return;
		}

		layoutBullets(graphics, collectListItems(this));
		layoutComponent(graphics);

		height = component.getHeight();
	}

	private void layoutBullets(final Graphics graphics, final java.util.List<ListItem> listItems) {
		int bulletWidth = 0;
		for (int i = 0; i < listItems.size(); i += 1) {
			final IInlineBox bullet;
			if (bulletFactory == null) {
				bullet = null;
			} else {
				bullet = bulletFactory.createBullet(bulletStyle, i, listItems.size());
				bullet.layout(graphics);
				bulletWidth = Math.max(bulletWidth, bullet.getWidth());
			}

			listItems.get(i).setBullet(bullet);
		}

		for (final ListItem listItem : listItems) {
			listItem.setBulletWidth(bulletWidth);
		}
	}

	private static java.util.List<ListItem> collectListItems(final List list) {
		final ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		list.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final List box) {
				if (box == list) {
					return super.visit(box);
				}
				return null;
			}

			@Override
			public Object visit(final ListItem box) {
				listItems.add(box);
				return null;
			}
		});
		return listItems;
	}

	private void layoutComponent(final Graphics graphics) {
		component.setPosition(0, 0);
		component.setWidth(width);
		component.layout(graphics);
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		height = component.getHeight();
		if (oldHeight != height) {
			layout(graphics);
			return true;
		}
		return false;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}

	@Override
	public void setVisualDecorator(final IVisualDecorator<IStructuralBox> visualDecorator) {
		// ignore, will be removed anyway
	}

	@Override
	public void resetVisualDecorator() {
		// ignore, will be removed anyway
	}

	@Override
	public void applyVisualDecorator() {
		// ignore, will be removed anyway
	}
}
