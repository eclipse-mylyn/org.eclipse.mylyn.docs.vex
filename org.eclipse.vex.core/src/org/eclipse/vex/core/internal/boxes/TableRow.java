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
public class TableRow extends BaseBox implements IStructuralBox, IParentBox<IStructuralBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;
	private int height;
	private final ArrayList<IStructuralBox> children = new ArrayList<IStructuralBox>();

	private int rowIndex;

	private TableLayoutGrid layoutGrid = new TableLayoutGrid();

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

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(final int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public TableLayoutGrid getLayoutGrid() {
		return layoutGrid;
	}

	public void setLayoutGrid(final TableLayoutGrid layoutGrid) {
		if (layoutGrid == null) {
			this.layoutGrid = new TableLayoutGrid();
		} else {
			this.layoutGrid = layoutGrid;
		}
	}

	public void layout(final Graphics graphics) {
		TableLayoutGrid.setupLayoutGrid(graphics, this, layoutGrid);
		int cellHeight = 0;

		for (int column = 1; column <= layoutGrid.getColumns(); column += 1) {
			final GridPosition position = new GridPosition(rowIndex, column);
			final TableCell cell = layoutGrid.getCell(position);
			final IStructuralBox child = layoutGrid.getRowChild(position);
			if (cell != null) {
				final GridArea area = cell.getGridArea();

				final int childLeft = getColumnWidth(1, area.startColumn - 1);
				final int columnWidth = getColumnWidth(area.startColumn, area.endColumn);

				if (column == area.startColumn) {
					if (rowIndex == area.startRow) {
						cell.calculateNaturalHeight(graphics, columnWidth);
					}
					if (rowIndex == area.endRow) {
						cellHeight = Math.max(cellHeight, cell.getNaturalHeight() - cell.getHeight());
					}
				}

				if (child != null && child.getParent() == this) {
					child.setPosition(0, childLeft);
					child.setWidth(columnWidth);
				}
			}
		}

		height = 0;
		for (int column = 1; column <= layoutGrid.getColumns(); column += 1) {
			final GridPosition position = new GridPosition(rowIndex, column);
			final TableCell cell = layoutGrid.getCell(position);
			final IStructuralBox child = layoutGrid.getRowChild(position);
			if (cell != null && child != null) {
				final GridArea area = cell.getGridArea();
				if (column == area.startColumn) {
					cell.useHeight(cellHeight);

					if (rowIndex == area.endRow) {
						child.layout(graphics);
						if (rowIndex == area.startRow) {
							height = Math.max(height, child.getHeight());
						}
					}
				}
			}
		}
	}

	private int getColumnWidth(final int startIndex, final int endIndex) {
		int columnWidth = 0;
		for (int i = startIndex; i <= endIndex; i += 1) {
			columnWidth += getColumnWidth(i);
		}
		return columnWidth;
	}

	private int getColumnWidth(final int index) {
		if (index < 1 || index > layoutGrid.getColumns()) {
			return 0;
		}
		return Math.round(width / layoutGrid.getColumns());
	}

	@Override
	public boolean reconcileLayout(final Graphics graphics) {
		final int oldHeight = height;
		layout(graphics);
		return oldHeight != height;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(children, graphics);
	}
}
