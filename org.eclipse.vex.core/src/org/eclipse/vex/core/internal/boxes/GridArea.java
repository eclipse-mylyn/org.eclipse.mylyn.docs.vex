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

/**
 * @author Florian Thienel
 */
public class GridArea {

	public final int startRow;
	public final int startColumn;
	public final int endRow;
	public final int endColumn;

	public GridArea(final GridPosition position) {
		this(position.row, position.column, position.row, position.column);
	}

	public GridArea(final GridPosition startPosition, final GridPosition endPosition) {
		this(startPosition.row, startPosition.column, endPosition.row, endPosition.column);
	}

	public GridArea(final int row, final int column) {
		this(row, column, row, column);
	}

	public GridArea(final int startRow, final int startColumn, final int endRow, final int endColumn) {
		this.startRow = Math.min(startRow, endRow);
		this.startColumn = Math.min(startColumn, endColumn);
		this.endRow = Math.max(startRow, endRow);
		this.endColumn = Math.max(startColumn, endColumn);
	}

	public boolean contains(final int row, final int column) {
		return row >= startRow && row <= endRow && column >= startColumn && column <= endColumn;
	}

	public Collection<GridPosition> positions() {
		final ArrayList<GridPosition> positions = new ArrayList<GridPosition>();
		for (int row = startRow; row <= endRow; row += 1) {
			for (int column = startColumn; column <= endColumn; column += 1) {
				positions.add(new GridPosition(row, column));
			}
		}
		return positions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endColumn;
		result = prime * result + endRow;
		result = prime * result + startColumn;
		result = prime * result + startRow;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GridArea other = (GridArea) obj;
		if (endColumn != other.endColumn) {
			return false;
		}
		if (endRow != other.endRow) {
			return false;
		}
		if (startColumn != other.startColumn) {
			return false;
		}
		if (startRow != other.startRow) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "GridArea [startRow=" + startRow + ", startColumn=" + startColumn + ", endRow=" + endRow + ", endColumn=" + endColumn + "]";
	}
}
