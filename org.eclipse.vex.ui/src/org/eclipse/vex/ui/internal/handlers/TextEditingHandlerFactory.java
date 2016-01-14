/*******************************************************************************
 * Copyright (c) 2013 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * Factory for following command handlers:
 * <ul>
 * <li><em>Go to</em> and <em>Select</em>:
 * <ul>
 * <li>Previous/Next Column (<em>column</em> in terms of <em>character</em>)</li>
 * <li>Line Up/Down</li>
 * <li>Previous/Next Word</li>
 * <li>Line Start/End</li>
 * <li>Page Up/Down</li>
 * <li>Text Start/End (<em>text</em> in terms of <em>document</em>)</li>
 * </ul>
 * </li>
 * <li><em>Delete</em>:
 * <ul>
 * <li>Previous/Next (Character)</li>
 * <li>Previous/Next Word</li>
 * <li>Line</li>
 * <li>To Beginning/End of Line</li>
 * </ul>
 * </li>
 * <li><em>Cut</em>:
 * <ul>
 * <li>Line</li>
 * <li>Delete to Beginning/End of Line</li>
 * </ul>
 * </li>
 * </ul>
 */
public class TextEditingHandlerFactory implements IExecutableExtensionFactory, IExecutableExtension {

	private String id;

	@Override
	public Object create() throws CoreException {

		// go to handlers
		if (PreviousColumn.class.getSimpleName().equals(id)) {
			return new PreviousColumn();
		}
		if (NextColumn.class.getSimpleName().equals(id)) {
			return new NextColumn();
		}
		if (LineUp.class.getSimpleName().equals(id)) {
			return new LineUp();
		}
		if (LineDown.class.getSimpleName().equals(id)) {
			return new LineDown();
		}
		if (PreviousWord.class.getSimpleName().equals(id)) {
			return new PreviousWord();
		}
		if (NextWord.class.getSimpleName().equals(id)) {
			return new NextWord();
		}
		if (LineStart.class.getSimpleName().equals(id)) {
			return new LineStart();
		}
		if (LineEnd.class.getSimpleName().equals(id)) {
			return new LineEnd();
		}
		if (PageUp.class.getSimpleName().equals(id)) {
			return new PageUp();
		}
		if (PageDown.class.getSimpleName().equals(id)) {
			return new PageDown();
		}
		if (TextStart.class.getSimpleName().equals(id)) {
			return new TextStart();
		}
		if (TextEnd.class.getSimpleName().equals(id)) {
			return new TextEnd();
		}

		// select handlers
		if (SelectPreviousColumn.class.getSimpleName().equals(id)) {
			return new SelectPreviousColumn();
		}
		if (SelectNextColumn.class.getSimpleName().equals(id)) {
			return new SelectNextColumn();
		}
		if (SelectLineUp.class.getSimpleName().equals(id)) {
			return new SelectLineUp();
		}
		if (SelectLineDown.class.getSimpleName().equals(id)) {
			return new SelectLineDown();
		}
		if (SelectPreviousWord.class.getSimpleName().equals(id)) {
			return new SelectPreviousWord();
		}
		if (SelectNextWord.class.getSimpleName().equals(id)) {
			return new SelectNextWord();
		}
		if (SelectLineStart.class.getSimpleName().equals(id)) {
			return new SelectLineStart();
		}
		if (SelectLineEnd.class.getSimpleName().equals(id)) {
			return new SelectLineEnd();
		}
		if (SelectPageUp.class.getSimpleName().equals(id)) {
			return new SelectPageUp();
		}
		if (SelectPageDown.class.getSimpleName().equals(id)) {
			return new SelectPageDown();
		}
		if (SelectTextStart.class.getSimpleName().equals(id)) {
			return new SelectTextStart();
		}
		if (SelectTextEnd.class.getSimpleName().equals(id)) {
			return new SelectTextEnd();
		}

		// delete handlers
		if (DeletePrevious.class.getSimpleName().equals(id)) {
			return new DeletePrevious();
		}
		if (DeleteNext.class.getSimpleName().equals(id)) {
			return new DeleteNext();
		}
		if (DeletePreviousWord.class.getSimpleName().equals(id)) {
			return new DeletePreviousWord();
		}
		if (DeleteNextWord.class.getSimpleName().equals(id)) {
			return new DeleteNextWord();
		}
		if (DeleteLine.class.getSimpleName().equals(id)) {
			return new DeleteLine();
		}
		if (DeleteToBeginningOfLine.class.getSimpleName().equals(id)) {
			return new DeleteToBeginningOfLine();
		}
		if (DeleteToEndOfLine.class.getSimpleName().equals(id)) {
			return new DeleteToEndOfLine();
		}

		// cut handlers
		if (CutLine.class.getSimpleName().equals(id)) {
			return new CutLine();
		}
		if (CutLineToBeginning.class.getSimpleName().equals(id)) {
			return new CutLineToBeginning();
		}
		if (CutLineToEnd.class.getSimpleName().equals(id)) {
			return new CutLineToEnd();
		}

		// not available
		throw new CoreException(new Status(IStatus.ERROR, VexPlugin.ID, 0, "Unknown id in data argument for " + getClass(), null)); //$NON-NLS-1$
	}

	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		if (data instanceof String) {
			id = (String) data;
			return;
		}
		throw new CoreException(new Status(IStatus.ERROR, VexPlugin.ID, 0, "Data argument must be a String for " + getClass(), null)); //$NON-NLS-1$
	}

	private static class PreviousColumn extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveBy(-1);
		}

	}

	private static class NextColumn extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveBy(1);
		}

	}

	private static class LineUp extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousLine(false);
		}

	}

	private static class LineDown extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextLine(false);
		}

	}

	private static class PreviousWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousWord(false);
		}

	}

	private static class NextWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextWord(false);
		}

	}

	private static class LineStart extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToLineStart(false);
		}

	}

	private static class LineEnd extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToLineEnd(false);
		}

	}

	private static class PageUp extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousPage(false);
		}

	}

	private static class PageDown extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextPage(false);
		}

	}

	private static class TextStart extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveTo(editor.getDocument().getStartPosition());
		}

	}

	private static class TextEnd extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1));
		}

	}

	private static class SelectPreviousColumn extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveBy(-1, true);
		}

	}

	private static class SelectNextColumn extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveBy(1, true);
		}

	}

	private static class SelectLineUp extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousLine(true);
		}

	}

	private static class SelectLineDown extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextLine(true);
		}

	}

	private static class SelectPreviousWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousWord(true);
		}

	}

	private static class SelectNextWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextWord(true);
		}

	}

	private static class SelectLineStart extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToLineStart(true);
		}

	}

	private static class SelectLineEnd extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToLineEnd(true);
		}

	}

	private static class SelectPageUp extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousPage(true);
		}

	}

	private static class SelectPageDown extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextPage(true);
		}

	}

	private static class SelectTextStart extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveTo(editor.getDocument().getStartPosition(), true);
		}

	}

	private static class SelectTextEnd extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveTo(editor.getDocument().getEndPosition().moveBy(-1), true);
		}

	}

	private static class DeletePrevious extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.deleteBackward();
		}
	}

	private static class DeleteNext extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.deleteForward();
		}

	}

	private static class DeletePreviousWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToPreviousWord(true);
			editor.deleteSelection();
		}

	}

	private static class DeleteNextWord extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			editor.moveToNextWord(true);
			editor.deleteSelection();
		}

	}

	private static class DeleteLine extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			selectWholeLines(editor);
			editor.deleteSelection();

		}

	}

	private static class DeleteToBeginningOfLine extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			final int selectionLength = editor.getSelectedRange().length();
			if (selectionLength > 1) {
				editor.moveTo(editor.getSelectedPositionRange().getStartPosition());
			}
			editor.moveToLineStart(true);
			editor.deleteSelection();
			if (selectionLength > 1) {
				editor.moveBy(selectionLength, true);
			}
		}

	}

	private static class DeleteToEndOfLine extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			if (editor.hasSelection()) {
				editor.moveTo(editor.getSelectedPositionRange().getStartPosition());
			}
			editor.moveToLineEnd(true);
			editor.deleteSelection();
		}

	}

	private static class CutLine extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			selectWholeLines(editor);
			editor.cutSelection();
		}

	}

	private static class CutLineToBeginning extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			final int selectionLength = editor.getSelectedRange().length();
			if (selectionLength > 1) {
				editor.moveTo(editor.getSelectedPositionRange().getStartPosition());
			}
			editor.moveToLineStart(true);
			editor.cutSelection();
			if (selectionLength > 1) {
				editor.moveBy(selectionLength, true);
			}
		}

	}

	private static class CutLineToEnd extends AbstractVexWidgetHandler {

		@Override
		public void execute(ExecutionEvent event, final IDocumentEditor editor) throws ExecutionException {
			if (editor.hasSelection()) {
				editor.moveTo(editor.getSelectedPositionRange().getStartPosition());
			}
			editor.moveToLineEnd(true);
			editor.cutSelection();
		}

	}

	private static void selectWholeLines(final IDocumentEditor editor) {

		// no selection?
		if (!editor.hasSelection()) {
			editor.moveToLineStart(false);
			editor.moveToLineEnd(true);
			return;
		}

		// remember start
		final ContentPosition start = editor.getSelectedPositionRange().getStartPosition();

		// calculate end of deletion
		ContentPosition end = editor.getSelectedPositionRange().getEndPosition();
		editor.moveTo(end);
		editor.moveToLineEnd(false);
		end = editor.getCaretPosition();

		// go to start of deletion
		editor.moveTo(start);
		editor.moveToLineStart(false);

		// select and delete
		editor.moveTo(end, true);

	}

}
