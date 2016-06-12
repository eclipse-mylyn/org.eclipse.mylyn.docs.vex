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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class TableCell extends BaseBox implements IStructuralBox, IParentBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private final ArrayList<IStructuralBox> children = new ArrayList<IStructuralBox>();

	private int startColumnIndex;
	private int endColumnIndex;
	private String columnName;
	private String startColumnName;
	private String endColumnName;
	private int verticalSpan = 1;

	private GridArea gridArea;

	private int naturalHeight;

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

	public void setHeight(final int height) {
		this.height = height;
	}

	public int getNaturalHeight() {
		return naturalHeight;
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

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void prependChild(final IStructuralBox child) {
		if (child == null) {
			return;
		}
		child.setParent(this);
		children.add(0, child);
	}

	public void appendChild(final IStructuralBox child) {
		if (child == null) {
			return;
		}
		child.setParent(this);
		children.add(child);
	}

	@Override
	public void replaceChildren(final Collection<? extends IBox> oldChildren, final IStructuralBox newChild) {
		boolean newChildInserted = false;

		for (final ListIterator<IStructuralBox> iter = children.listIterator(); iter.hasNext();) {
			final IStructuralBox child = iter.next();
			if (oldChildren.contains(child)) {
				iter.remove();
				if (!newChildInserted) {
					iter.add(newChild);
					newChild.setParent(this);
					newChildInserted = true;
				}
			}
		}
	}

	public Iterable<IStructuralBox> getChildren() {
		return children;
	}

	public int getStartColumnIndex() {
		return startColumnIndex;
	}

	public void setStartColumnIndex(final int startColumnIndex) {
		this.startColumnIndex = startColumnIndex;
	}

	public int getEndColumnIndex() {
		return endColumnIndex;
	}

	public void setEndColumnIndex(final int endColumnIndex) {
		this.endColumnIndex = endColumnIndex;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(final String columnName) {
		this.columnName = columnName;
	}

	public String getStartColumnName() {
		return startColumnName;
	}

	public void setStartColumnName(final String startColumnName) {
		this.startColumnName = startColumnName;
	}

	public String getEndColumnName() {
		return endColumnName;
	}

	public void setEndColumnName(final String endColumnName) {
		this.endColumnName = endColumnName;
	}

	public int getVerticalSpan() {
		return verticalSpan;
	}

	public void setVerticalSpan(final int verticalSpan) {
		this.verticalSpan = verticalSpan;
	}

	public GridArea getGridArea() {
		return gridArea;
	}

	public void setGridArea(final GridArea gridArea) {
		this.gridArea = gridArea;
	}

	public int calculateNaturalHeight(final Graphics graphics, final int width) {
		naturalHeight = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			child.setWidth(width);
			child.layout(graphics);
			naturalHeight += child.getHeight();
		}
		height = naturalHeight;

		return naturalHeight;
	}

	public void layout(final Graphics graphics) {
		if (naturalHeight == 0) {
			naturalHeight = calculateNaturalHeight(graphics, width);
		}

		positionChildren();
	}

	private void positionChildren() {
		int childTop = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			child.setPosition(childTop, 0);
			childTop += child.getHeight();
		}
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = naturalHeight;
		naturalHeight = calculateNaturalHeight(graphics, width);

		if (oldHeight == naturalHeight) {
			return false;
		}

		positionChildren();
		return true;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(children, graphics);
	}
}
