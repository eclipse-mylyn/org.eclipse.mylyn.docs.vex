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

	private TableColumnLayout columnLayout = new TableColumnLayout();
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

	public TableColumnLayout getColumnLayout() {
		return columnLayout;
	}

	public void setColumnLayout(final TableColumnLayout columnLayout) {
		if (columnLayout == null) {
			this.columnLayout = new TableColumnLayout();
		} else {
			this.columnLayout = columnLayout;
		}
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
		TableColumnLayout.addColumnLayoutInformationForChildren(graphics, this, columnLayout);
		TableLayoutGrid.setupLayoutGrid(graphics, this, layoutGrid);
		int cellHeight = 0;
		int columnIndex = 1;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			final TableCell cell = getContainedTableCell(child);
			if (cell != null) {
				final int startColumn = getStartColumn(cell, columnIndex);
				final int endColumn = getEndColumn(cell, startColumn);

				final int childLeft = getColumnWidth(1, startColumn - 1);
				final int columnWidth = getColumnWidth(startColumn, endColumn);

				child.setWidth(columnWidth);
				child.setPosition(0, childLeft);

				cellHeight = Math.max(cellHeight, cell.calculateNaturalHeight(graphics, columnWidth));
				columnIndex = endColumn + 1;
			}
		}

		height = 0;
		for (int i = 0; i < children.size(); i += 1) {
			final IStructuralBox child = children.get(i);
			final TableCell cell = getContainedTableCell(child);
			if (cell != null) {
				cell.setHeight(cellHeight);
				child.layout(graphics);
				height = Math.max(height, child.getHeight());
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
		if (index < 1 || index > columnLayout.getLastIndex()) {
			return 0;
		}
		return Math.round(width / columnLayout.getLastIndex());
	}

	private static TableCell getContainedTableCell(final IStructuralBox parent) {
		return parent.accept(new TableCellVisitor<TableCell>() {
			@Override
			public TableCell visit(final TableCell box) {
				return box;
			}
		});
	}

	private static int getStartColumn(final TableCell cell, final int defaultColumn) {
		if (cell == null || cell.getStartColumnIndex() <= defaultColumn) {
			return defaultColumn;
		}
		return cell.getStartColumnIndex();
	}

	private static int getEndColumn(final TableCell cell, final int defaultColumn) {
		if (cell == null || cell.getEndColumnIndex() <= defaultColumn) {
			return defaultColumn;
		}
		return cell.getEndColumnIndex();
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

	private static class TableCellVisitor<T> extends DepthFirstBoxTraversal<T> {
		@Override
		public final T visit(final Table box) {
			return null;
		}

		@Override
		public final T visit(final TableRowGroup box) {
			return null;
		}

		@Override
		public final T visit(final TableRow box) {
			return null;
		}
	};
}
