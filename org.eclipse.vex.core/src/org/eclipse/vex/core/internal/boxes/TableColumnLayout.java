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

import java.util.HashMap;

import org.eclipse.vex.core.internal.core.Graphics;

/**
 * @author Florian Thienel
 */
public class TableColumnLayout {

	private final TableColumnLayout parentLayout;
	private int width;

	private int lastIndex;
	private final HashMap<String, Span> indexByName = new HashMap<String, Span>();

	public static void addColumnLayoutInformationForChildren(final Graphics graphics, final IStructuralBox parent, final TableColumnLayout parentColumnLayout) {
		parent.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final Table box) {
				if (box == parent) {
					traverseChildren(box);
				} else {
					box.setColumnLayout(new TableColumnLayout(parentColumnLayout));
				}
				return null;
			}

			@Override
			public Object visit(final TableRowGroup box) {
				if (box == parent) {
					traverseChildren(box);
				} else {
					box.setColumnLayout(new TableColumnLayout(parentColumnLayout));
				}
				return null;
			}

			@Override
			public Object visit(final TableColumnSpec box) {
				if (box.getStartName() != null) {
					box.setStartIndex(parentColumnLayout.getIndex(box.getStartName()));
				}
				if (box.getEndName() != null) {
					box.setEndIndex(parentColumnLayout.getIndex(box.getEndName()));
				}
				if (box.getStartIndex() == box.getEndIndex()) {
					parentColumnLayout.addColumn(box.getStartIndex(), box.getName(), box.getWidthExpression());
				} else {
					parentColumnLayout.addSpan(box.getStartIndex(), box.getEndIndex(), box.getName());
				}
				return null;
			}

			@Override
			public Object visit(final TableRow box) {
				if (box == parent) {
					traverseChildren(box);
				} else {
					box.setColumnLayout(parentColumnLayout);
				}
				return null;
			}

			@Override
			public Object visit(final TableCell box) {
				if (box.getColumnName() != null) {
					box.setStartColumnIndex(parentColumnLayout.getStartIndex(box.getColumnName()));
					box.setEndColumnIndex(parentColumnLayout.getEndIndex(box.getColumnName()));
				} else if (box.getStartColumnName() != null && box.getEndColumnName() != null) {
					box.setStartColumnIndex(parentColumnLayout.getIndex(box.getStartColumnName()));
					box.setEndColumnIndex(parentColumnLayout.getIndex(box.getEndColumnName()));
				}
				return null;
			}
		});
	}

	public TableColumnLayout() {
		this(null);
	}

	public TableColumnLayout(final TableColumnLayout parentLayout) {
		this.parentLayout = parentLayout;
	}

	public TableColumnLayout getParentLayout() {
		return parentLayout;
	}

	public int addColumn(final int index, final String name, final String width) {
		final int columnIndex;
		if (index <= lastIndex) {
			columnIndex = lastIndex += 1;
		} else {
			columnIndex = lastIndex = index;
		}

		if (name != null) {
			indexByName.put(name, new Span(columnIndex));
		}

		return columnIndex;
	}

	public void addSpan(final int startIndex, final int endIndex, final String name) {
		indexByName.put(name, new Span(startIndex, endIndex));
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getLastIndex() {
		if (parentLayout != null) {
			return Math.max(lastIndex, parentLayout.getLastIndex());
		}
		return lastIndex;
	}

	public int getIndex(final String name) {
		return getStartIndex(name);
	}

	public int getStartIndex(final String name) {
		return getSpan(name).start;
	}

	public int getEndIndex(final String name) {
		return getSpan(name).end;
	}

	private Span getSpan(final String name) {
		if (indexByName.containsKey(name)) {
			return indexByName.get(name);
		}
		if (parentLayout != null) {
			return parentLayout.getSpan(name);
		}
		return Span.NULL;
	}

	public int getWidth(final int startIndex, final int endIndex) {
		return 0;
	}

	public int getWidth(final int index) {
		return 0;
	}

	private static class Span {
		public static final Span NULL = new Span(0, 0);

		public final int start;
		public final int end;

		public Span(final int index) {
			this(index, index);
		}

		public Span(final int start, final int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + end;
			result = prime * result + start;
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
			final Span other = (Span) obj;
			if (end != other.end) {
				return false;
			}
			if (start != other.start) {
				return false;
			}
			return true;
		}
	}
}
