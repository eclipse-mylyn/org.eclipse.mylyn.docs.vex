/*******************************************************************************
 * Copyright (c) 2004, 2016 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - use a visitor to create a new table row
 *     Florian Thienel - introduce IDocumentEditor
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.layout.ElementOrPositionRangeCallback;
import org.eclipse.vex.core.internal.layout.ElementOrRangeCallback;
import org.eclipse.vex.core.internal.layout.LayoutUtils;
import org.eclipse.vex.core.internal.layout.LayoutUtils.ElementOrRange;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.ui.internal.editor.DocumentContextSourceProvider;
import org.eclipse.vex.ui.internal.editor.VexEditor;

/**
 * Static helper methods used across handlers.
 */
public final class VexHandlerUtil {

	public static IDocumentEditor getDocumentEditor(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		IDocumentEditor documentEditor = null;
		if (activeEditor instanceof VexEditor) {
			documentEditor = ((VexEditor) activeEditor).getVexWidget();
		}
		assertNotNull(documentEditor, "Can not compute document editor.");
		return documentEditor;
	}

	public static IDocumentEditor getDocumentEditor(final IWorkbenchWindow window) {
		final VexEditor editor = getActiveVexEditor(window);
		if (editor == null) {
			return null;
		}
		return editor.getVexWidget();
	}

	public static VexEditor getActiveVexEditor(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);

		if (activeEditor instanceof VexEditor) {
			return (VexEditor) activeEditor;
		}
		return null;
	}

	public static VexEditor getActiveVexEditor(final IWorkbenchWindow window) {
		final IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		if (activeEditor == null) {
			return null;
		}

		if (activeEditor instanceof VexEditor) {
			return (VexEditor) activeEditor;
		}
		return null;
	}

	public static Rectangle getCaretArea(final ExecutionEvent event) throws ExecutionException {
		return getVariable(event, DocumentContextSourceProvider.CARET_AREA);
	}

	public static INode getCurrentNode(final ExecutionEvent event) throws ExecutionException {
		return getVariable(event, DocumentContextSourceProvider.CURRENT_NODE);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getVariable(final ExecutionEvent event, final String name) {
		return (T) HandlerUtil.getVariable(event, DocumentContextSourceProvider.CARET_AREA);
	}

	private static void assertNotNull(final Object object, final String message) throws ExecutionException {
		if (object == null) {
			throw new ExecutionException(message);
		}
	}

	public static class RowColumnInfo {
		public Object row;
		public Object cell;
		public int rowIndex;
		public int cellIndex;
		public int rowCount;
		public int columnCount;
		public int maxColumnCount;
	}

	/**
	 * Duplicate the given table row, inserting a new empty one aove or below it. The new row contains empty children
	 * corresponding to the given row's children.
	 *
	 * @param editor
	 *            IVexWidget with which we're working
	 * @param tableRow
	 *            TableRowBox to be duplicated.
	 * @param addAbove
	 *            <code>true</code> to add the new row above the current one
	 */
	public static void duplicateTableRow(final IDocumentEditor editor, final IElement tableRow, final boolean addAbove) {
		final StyleSheet styleSheet = editor.getTableModel().getStyleSheet();
		if (!styleSheet.getStyles(tableRow).getDisplay().equals(CSS.TABLE_ROW)) {
			return;
		}

		if (addAbove) {
			editor.moveTo(tableRow.getStartPosition());
		} else {
			editor.moveTo(tableRow.getEndPosition().moveBy(1));
		}

		// Create a new table row
		final IElement newRow = editor.insertElement(tableRow.getQualifiedName());

		// Iterate all direct children and add them to the new row
		final Iterator<? extends INode> childIterator = tableRow.children().withoutText().iterator();
		while (childIterator.hasNext()) {
			childIterator.next().accept(new BaseNodeVisitor() {
				@Override
				public void visit(final IElement element) {
					final IElement newElement = editor.insertElement(element.getQualifiedName());
					for (final IAttribute attr : element.getAttributes()) {
						newElement.setAttribute(attr.getQualifiedName(), attr.getValue());
					}
					editor.moveBy(1);
				}

				@Override
				public void visit(final IComment comment) {
					// Comments are copied with content
					editor.insertComment();
					editor.insertText(comment.getText());
					editor.moveBy(1);
				}

				@Override
				public void visit(final IProcessingInstruction pi) {
					// Processing instructions are copied with target and  content
					editor.insertProcessingInstruction(pi.getTarget());
					editor.insertText(pi.getText());
					editor.moveBy(1);
				}
			});
		}
		try {
			final INode firstTextChild = newRow.childElements().first();
			editor.moveTo(firstTextChild.getStartPosition());
		} catch (final NoSuchElementException ex) {
			editor.moveTo(newRow.getStartPosition().moveBy(1));
		}
	}

	/**
	 * Returns true if the given element or range is at least partially selected.
	 *
	 * @param editor
	 *            IVexWidget being tested.
	 * @param elementOrRange
	 *            Element or IntRange being tested.
	 */
	public static boolean elementOrRangeIsPartiallySelected(final IDocumentEditor editor, final Object elementOrRange) {
		final ContentRange elementContentRange = getInnerRange(elementOrRange);
		final ContentRange selectedRange = editor.getSelectedRange();
		return elementContentRange.intersects(selectedRange);
	}

	/**
	 * Returns the zero-based index of the table column containing the current offset. Returns -1 if we are not inside a
	 * table.
	 */
	public static int getCurrentColumnIndex(final IDocumentEditor editor) {

		final IElement row = getCurrentTableRow(editor);

		if (row == null) {
			return -1;
		}

		final ContentPosition offset = editor.getCaretPosition();
		final int[] column = new int[] { -1 };
		LayoutUtils.iterateTableCells(editor.getTableModel().getStyleSheet(), row, new ElementOrRangeCallback() {
			private int i = 0;

			@Override
			public void onElement(final IElement child, final String displayStyle) {
				if (offset.isAfter(child.getStartPosition()) && offset.isBeforeOrEquals(child.getEndPosition())) {
					column[0] = i;
				}
				i++;
			}

			@Override
			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				i++;
			}
		});

		return column[0];
	}

	/**
	 * Returns the innermost Element with style table-row containing the caret, or null if no such element exists.
	 *
	 * @param editor
	 *            IVexWidget to use.
	 */
	public static IElement getCurrentTableRow(final IDocumentEditor editor) {
		final StyleSheet styleSheet = editor.getTableModel().getStyleSheet();
		IElement element = editor.getCurrentElement();

		while (element != null) {
			if (styleSheet.getStyles(element).getDisplay().equals(CSS.TABLE_ROW)) {
				return element;
			}
			element = element.getParentElement();
		}

		return null;
	}

	/**
	 * Returns the currently selected table rows, or the current row if ther is no selection. If no row can be found,
	 * returns an empty array.
	 *
	 * @param editor
	 *            IVexWidget to use.
	 */
	public static SelectedRows getSelectedTableRows(final IDocumentEditor editor) {
		final SelectedRows selected = new SelectedRows();

		VexHandlerUtil.iterateTableCells(editor, new TableCellCallbackAdapter() {
			@Override
			public void startRow(final Object row, final int rowIndex) {
				if (VexHandlerUtil.elementOrRangeIsPartiallySelected(editor, row)) {
					if (selected.rows == null) {
						selected.rows = new ArrayList<Object>();
					}
					selected.rows.add(row);
				} else {
					if (selected.rows == null) {
						selected.rowBefore = row;
					} else {
						if (selected.rowAfter == null) {
							selected.rowAfter = row;
						}
					}
				}
			}
		});

		return selected;
	}

	public static void iterateTableCells(final IDocumentEditor editor, final ITableCellCallback callback) {

		final StyleSheet ss = editor.getTableModel().getStyleSheet();

		iterateTableRows(editor, new ElementOrPositionRangeCallback() {

			final private int[] rowIndex = { 0 };

			@Override
			public void onElement(final IElement row, final String displayStyle) {

				callback.startRow(row, rowIndex[0]);

				LayoutUtils.iterateTableCells(ss, row, new ElementOrPositionRangeCallback() {
					private int cellIndex = 0;

					@Override
					public void onElement(final IElement cell, final String displayStyle) {
						callback.onCell(new ElementOrRange(row), new ElementOrRange(cell), rowIndex[0], cellIndex);
						cellIndex++;
					}

					@Override
					public void onRange(final IParent parent, final ContentPosition startPosition, final ContentPosition endPosition) {
						final ContentPositionRange range = new ContentPositionRange(startPosition, endPosition);
						callback.onCell(new ElementOrRange(row), new ElementOrRange(range), rowIndex[0], cellIndex);
						cellIndex++;
					}
				});

				callback.endRow(row, rowIndex[0]);

				rowIndex[0]++;
			}

			@Override
			public void onRange(final IParent parent, final ContentPosition startPosition, final ContentPosition endPosition) {

				final ContentPositionRange row = new ContentPositionRange(startPosition, endPosition);
				callback.startRow(row, rowIndex[0]);

				LayoutUtils.iterateTableCells(ss, parent, startPosition, endPosition, new ElementOrPositionRangeCallback() {
					private int cellIndex = 0;

					@Override
					public void onElement(final IElement cell, final String displayStyle) {
						callback.onCell(new ElementOrRange(row), new ElementOrRange(cell), rowIndex[0], cellIndex);
						cellIndex++;
					}

					@Override
					public void onRange(final IParent parent, final ContentPosition startPosition, final ContentPosition endPosition) {
						final ContentPositionRange range = new ContentPositionRange(startPosition, endPosition);
						callback.onCell(new ElementOrRange(row), new ElementOrRange(range), rowIndex[0], cellIndex);
						cellIndex++;
					}
				});

				callback.endRow(row, rowIndex[0]);

				rowIndex[0]++;
			}
		});
	}

	/**
	 * Returns a RowColumnInfo structure containing information about the table containing the caret. Returns null if
	 * the caret is not currently inside a table.
	 *
	 * @param editor
	 *            IVexWidget to inspect.
	 */
	public static RowColumnInfo getRowColumnInfo(final IDocumentEditor editor) {

		final boolean[] found = new boolean[1];
		final RowColumnInfo[] rcInfo = new RowColumnInfo[] { new RowColumnInfo() };
		final ContentPosition position = editor.getCaretPosition();

		rcInfo[0].cellIndex = -1;
		rcInfo[0].rowIndex = -1;

		iterateTableCells(editor, new ITableCellCallback() {

			private int rowColumnCount;

			@Override
			public void startRow(final Object row, final int rowIndex) {
				rowColumnCount = 0;
			}

			@Override
			public void onCell(final ElementOrRange row, final ElementOrRange cell, final int rowIndex, final int cellIndex) {
				found[0] = true;
				if (row.contains(position)) {
					rcInfo[0].row = row;
					rcInfo[0].rowIndex = rowIndex;
					rcInfo[0].columnCount++;

					if (cell.contains(position)) {
						rcInfo[0].cell = cell;
						rcInfo[0].cellIndex = cellIndex;
					}
				}

				rowColumnCount++;
			}

			@Override
			public void endRow(final Object row, final int rowIndex) {
				rcInfo[0].rowCount++;
				rcInfo[0].maxColumnCount = Math.max(rcInfo[0].maxColumnCount, rowColumnCount);
			}
		});

		if (found[0]) {
			return rcInfo[0];
		} else {
			return null;
		}
	}

	/**
	 * Iterate over all rows in the table containing the caret.
	 *
	 * @param editor
	 *            IVexWidget to iterate over.
	 * @param callback
	 *            Caller-provided callback that this method calls for each row in the current table.
	 */
	public static void iterateTableRows(final IDocumentEditor editor, final ElementOrPositionRangeCallback callback) {

		final StyleSheet ss = editor.getTableModel().getStyleSheet();
		final IDocument doc = editor.getDocument();
		final ContentPosition position = editor.getCaretPosition();

		// This may or may not be a table
		// In any case, it's the element that contains the top-level table
		// children
		IElement table = doc.getElementForInsertionAt(position.getOffset());

		while (table != null && !LayoutUtils.isTableChild(ss, table)) {
			table = table.getParentElement();
		}

		while (table != null && LayoutUtils.isTableChild(ss, table)) {
			table = table.getParentElement();
		}

		if (table == null || table.getParentElement() == null) {
			return;
		}

		final List<IElement> tableChildren = new ArrayList<IElement>();
		final boolean[] found = new boolean[] { false };
		LayoutUtils.iterateChildrenByDisplayStyle(ss, LayoutUtils.TABLE_CHILD_STYLES, table, new ElementOrRangeCallback() {
			@Override
			public void onElement(final IElement child, final String displayStyle) {
				if (position.isAfterOrEquals(child.getStartPosition()) && position.isBeforeOrEquals(child.getEndPosition())) {
					found[0] = true;
				}
				tableChildren.add(child);
			}

			@Override
			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				if (!found[0]) {
					tableChildren.clear();
				}
			}
		});

		if (!found[0]) {
			return;
		}

		final ContentPosition startOffset = tableChildren.get(0).getStartPosition();
		final ContentPosition endOffset = tableChildren.get(tableChildren.size() - 1).getEndPosition().moveBy(1);
		LayoutUtils.iterateTableRows(ss, table, startOffset, endOffset, callback);
	}

	/**
	 * Returns an IntRange representing the offsets inside the given Element or IntRange. If an Element is passed,
	 * returns the offsets inside the sentinels. If an IntRange is passed it is returned directly.
	 *
	 * @param elementOrRange
	 *            Element or IntRange to be inspected.
	 */
	public static ContentRange getInnerRange(final Object elementOrRange) {
		if (elementOrRange instanceof IElement) {
			final IElement element = (IElement) elementOrRange;
			return new ContentRange(element.getStartOffset() + 1, element.getEndOffset());
		} else {
			return (ContentRange) elementOrRange;
		}
	}

	/**
	 * Returns an ContentPositionRange representing the offsets outside the given Element or IntRange. If an Element is
	 * passed, returns the offsets outside the sentinels. If an PositionRange is passed it is returned directly.
	 *
	 * @param elementOrRange
	 *            Element or PositionRange to be inspected.
	 */
	public static ContentPositionRange getOuterRange(final Object elementOrRange) {
		if (elementOrRange instanceof IElement) {
			final IElement element = (IElement) elementOrRange;
			return new ContentPositionRange(element.getStartPosition(), element.getEndPosition().moveBy(1));
		} else {
			return (ContentPositionRange) elementOrRange;
		}
	}

	public static class SelectedRows {

		private SelectedRows() {
		}

		public List<Object> getRows() {
			return rows;
		}

		public Object getRowBefore() {
			return rowBefore;
		}

		public Object getRowAfter() {
			return rowAfter;
		}

		private List<Object> rows;
		private Object rowBefore;
		private Object rowAfter;
	}

}
