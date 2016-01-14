/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Carsten Hiesserich - remove selection listener on dispose (bug 413878)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.FindNextAction;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorActionBarContributor;

/**
 * Contribute actions on behalf of the VexEditor.
 */
public class VexActionBarContributor extends XMLMultiPageEditorActionBarContributor {

	@Override
	public void dispose() {
		activeEditor = null;
		getPage().removeSelectionListener(selectionListener);
	}

	public VexEditor getVexEditor() {
		return (VexEditor) activeEditor;
	}

	public IDocumentEditor getDocumentEditor() {
		if (activeEditor != null) {
			return ((VexEditor) activeEditor).getVexWidget();
		} else {
			return null;
		}
	}

	@Override
	public void init(final IActionBars bars, final IWorkbenchPage page) {
		super.init(bars, page);
		page.addSelectionListener(selectionListener);
	}

	@Override
	public void setActiveEditor(final IEditorPart activeEditor) {

		// This can occur if we have an error loading the editor,
		// in which case Eclipse provides its own part
		if (!(activeEditor instanceof VexEditor)) {
			this.activeEditor = null;
			return;
		}

		this.activeEditor = activeEditor;
		setId(copyAction, ActionFactory.COPY.getId());
		setId(cutAction, ActionFactory.CUT.getId());
		setId(deleteAction, ActionFactory.DELETE.getId());
		setId(pasteAction, ActionFactory.PASTE.getId());
		setId(redoAction, ActionFactory.REDO.getId());
		setId(selectAllAction, ActionFactory.SELECT_ALL.getId());
		setId(undoAction, ActionFactory.UNDO.getId());

		final String findActionMessagesBundleId = "org.eclipse.ui.texteditor.ConstructedEditorMessages";
		final ResourceBundle resourceBundle = ResourceBundle.getBundle(findActionMessagesBundleId);

		// Find/Replace
		final IAction findAction = new FindReplaceAction(resourceBundle, "Editor.FindReplace.", this.activeEditor);
		setId(findAction, ActionFactory.FIND.getId());

		// Find Next
		final IAction findNextAction = new FindNextAction(resourceBundle, "Editor.FindNext.", this.activeEditor, true);
		setIds(findNextAction, ITextEditorActionConstants.FIND_NEXT, IWorkbenchActionDefinitionIds.FIND_NEXT);

		// Find Previous
		final IAction findPreviousAction = new FindNextAction(resourceBundle, "Editor.FindPrevious.", this.activeEditor, false);
		setIds(findPreviousAction, ITextEditorActionConstants.FIND_PREVIOUS, IWorkbenchActionDefinitionIds.FIND_PREVIOUS);

		enableActions();
	}

	private void setIds(final IAction action, final String actionId, final String commandId) {
		action.setActionDefinitionId(commandId);
		setId(action, actionId);
	}

	private void setId(final IAction action, final String actionId) {
		getActionBars().setGlobalActionHandler(actionId, action);
	}

	private IEditorPart activeEditor;

	private final IAction copyAction = new CopyAction();
	private final IAction cutAction = new CutAction();
	private final IAction deleteAction = new DeleteAction();
	private final IAction pasteAction = new PasteAction();
	private final IAction redoAction = new RedoAction();
	private final IAction selectAllAction = new SelectAllAction();
	private final IAction undoAction = new UndoAction();

	private void enableActions() {
		final IDocumentEditor editor = getDocumentEditor();
		copyAction.setEnabled(editor != null && editor.hasSelection());
		cutAction.setEnabled(editor != null && editor.hasSelection());
		deleteAction.setEnabled(editor != null && editor.hasSelection());
		redoAction.setEnabled(editor != null && editor.canRedo());
		undoAction.setEnabled(editor != null && editor.canUndo());
	}

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
			enableActions();
		}
	};

	private class CopyAction extends Action {
		@Override
		public void run() {
			getDocumentEditor().copySelection();
		}
	};

	private class CutAction extends Action {
		@Override
		public void run() {
			getDocumentEditor().cutSelection();
		}
	}

	private class DeleteAction extends Action {
		@Override
		public void run() {
			getDocumentEditor().deleteSelection();
		}
	};

	private class PasteAction extends Action {
		@Override
		public void run() {
			try {
				getDocumentEditor().paste();
			} catch (final DocumentValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private class SelectAllAction extends Action {
		@Override
		public void run() {
			getDocumentEditor().selectAll();
		}
	};

	private class RedoAction extends Action {
		@Override
		public void run() {
			if (getDocumentEditor().canRedo()) {
				getDocumentEditor().redo();
			}
		}
	};

	private class UndoAction extends Action {
		@Override
		public void run() {
			if (getDocumentEditor().canUndo()) {
				getDocumentEditor().undo();
			}
		}
	}

}