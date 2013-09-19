/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.IValidationResult;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

public class EditProcessingInstructionEdit extends AbstractUndoableEdit {

	private final IDocument document;
	private final int offset;
	private final String target;
	private String oldTarget;
	private final String data;
	private String oldData;

	public EditProcessingInstructionEdit(final IDocument document, final int offset, final String target, final String data) {
		super();
		this.document = document;
		this.offset = offset;
		this.target = target;
		this.data = data;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			final INode node = document.getNodeForInsertionAt(offset);
			if (!(node instanceof IProcessingInstruction)) {
				throw new CannotUndoException("Current Node is not a processing instruction.");
			}

			final IProcessingInstruction pi = (IProcessingInstruction) node;

			document.setProcessingInstructionTarget(offset, oldTarget);
			document.delete(pi.getRange().resizeBy(1, -1));
			document.insertText(pi.getEndOffset(), oldData);

		} catch (final DocumentValidationException e) {
			throw new CannotRedoException(e);
		}
	}

	@Override
	protected void performRedo() throws CannotRedoException {
		try {
			final INode node = document.getNodeForInsertionAt(offset);
			if (!(node instanceof IProcessingInstruction)) {
				throw new CannotRedoException("Current Node is not a processing instruction.");
			}

			final IProcessingInstruction pi = (IProcessingInstruction) node;

			oldTarget = pi.getTarget();
			oldData = pi.getText();

			// Validate both Strings first to prevent a partial Redo
			if (target != null) {
				final IValidationResult resultTarget = XML.validateProcessingInstructionTarget(target);
				if (!resultTarget.isOK()) {
					throw new CannotRedoException(resultTarget.getMessage());
				}
			}

			if (data != null) {
				final IValidationResult resultData = XML.validateProcessingInstructionData(data);
				if (!resultData.isOK()) {
					throw new CannotRedoException(resultData.getMessage());
				}
			}

			if (target != null) {
				document.setProcessingInstructionTarget(offset, target);
			}

			if (data != null) {
				document.delete(pi.getRange().resizeBy(1, -1));
				document.insertText(pi.getEndOffset(), data);
			}

		} catch (final DocumentValidationException e) {
			throw new CannotRedoException(e);
		}
	}
}
