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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * @author Florian Thienel
 */
public class TableCell extends BaseBox implements IStructuralBox, IDecoratorBox<IHeightAdjustableBox> {

	private IBox parent;
	private int top;
	private int left;
	private int width;

	private IHeightAdjustableBox component;

	private String columnName;
	private String startColumnName;
	private String endColumnName;
	private int horizontalSpan = 1;
	private int verticalSpan = 1;

	private GridArea gridArea;
	private TableLayoutGrid layoutGrid;

	private int naturalHeight;
	private int usedHeight;

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
		return usedHeight;
	}

	public void useHeight(final int height) {
		usedHeight += height;
	}

	public int getNaturalHeight() {
		return naturalHeight;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(left, top, width, usedHeight);
	}

	@Override
	public void accept(final IBoxVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final IBoxVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public void setComponent(final IHeightAdjustableBox component) {
		this.component = component;
		component.setParent(this);
	}

	@Override
	public IHeightAdjustableBox getComponent() {
		return component;
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

	public int getHorizontalSpan() {
		return horizontalSpan;
	}

	public void setHorizontalSpan(final int horizontalSpan) {
		this.horizontalSpan = horizontalSpan;
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

	public TableLayoutGrid getLayoutGrid() {
		return layoutGrid;
	}

	public void setLayoutGrid(final TableLayoutGrid layoutGrid) {
		this.layoutGrid = layoutGrid;
	}

	public int calculateNaturalHeight(final Graphics graphics, final int width) {
		naturalHeight = 0;
		if (component == null) {
			return 0;
		}

		component.setWidth(width);
		component.layout(graphics);
		naturalHeight = component.getHeight();
		usedHeight = 0;

		return naturalHeight;
	}

	public void layout(final Graphics graphics) {
		if (naturalHeight == 0) {
			naturalHeight = calculateNaturalHeight(graphics, width);
		}

		component.setPosition(0, 0);
		adjustComponentHeight();
	}

	private void adjustComponentHeight() {
		component.setHeight(usedHeight);
	}

	@Override
	public Collection<IBox> reconcileLayout(final Graphics graphics) {
		final int oldHeight = naturalHeight;
		naturalHeight = calculateNaturalHeight(graphics, width);

		if (oldHeight == naturalHeight) {
			return NOTHING_INVALIDATED;
		}

		component.setPosition(0, 0);
		adjustComponentHeight();

		final ArrayList<IBox> invalidatedRows = new ArrayList<IBox>();
		for (int row = gridArea.startRow; row <= gridArea.endRow; row += 1) {
			invalidatedRows.add(layoutGrid.getRow(row));
		}

		return invalidatedRows;
	}

	@Override
	public void paint(final Graphics graphics) {
		ChildBoxPainter.paint(component, graphics);
	}
}
