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

/**
 * @author Florian Thienel
 */
public class TableColumnDefinitions {

	private final TableColumnDefinitions parent;
	private int width;

	private int lastIndex;
	private final HashMap<String, Span> spanByName = new HashMap<String, Span>();

	public TableColumnDefinitions() {
		this(null);
	}

	public TableColumnDefinitions(final TableColumnDefinitions parent) {
		this.parent = parent;
	}

	public int addColumn(final int index, final String name, final String width) {
		final int columnIndex;
		if (index <= lastIndex) {
			columnIndex = lastIndex += 1;
		} else {
			columnIndex = lastIndex = index;
		}

		if (name != null) {
			spanByName.put(name, new Span(columnIndex));
		}

		return columnIndex;
	}

	public void addSpan(final int startIndex, final int endIndex, final String name) {
		spanByName.put(name, new Span(startIndex, endIndex));
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public boolean isEmpty() {
		return lastIndex == 0;
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
		if (spanByName.containsKey(name)) {
			return spanByName.get(name);
		}
		if (parent != null) {
			return parent.getSpan(name);
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
