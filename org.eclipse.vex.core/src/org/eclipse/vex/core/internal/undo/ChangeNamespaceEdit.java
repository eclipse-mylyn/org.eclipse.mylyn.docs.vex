/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		John Krasnay - initial API and implementation
 *		Carsten Hiesserich - Refactored to use AbstractUndoableEdit
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IElement;

public class ChangeNamespaceEdit extends AbstractUndoableEdit {

	private final IElement element;
	private final String prefix;
	private final String oldUri;
	private final String newUri;

	public ChangeNamespaceEdit(final IElement element, final String prefix, final String oldUri, final String newUri) {
		super();
		this.element = element;
		this.prefix = prefix;
		this.oldUri = oldUri;
		this.newUri = newUri;
	}

	@Override
	protected void performUndo() throws CannotUndoException {
		try {
			if (oldUri == null) {
				element.removeNamespace(prefix);
			} else {
				element.declareNamespace(prefix, oldUri);
			}
		} catch (final DocumentValidationException ex) {
			throw new CannotUndoException();
		}
	}

	@Override
	protected void performRedo() throws CannotRedoException {
		try {
			if (newUri == null) {
				element.removeNamespace(prefix);
			} else {
				element.declareNamespace(prefix, newUri);
			}
		} catch (final DocumentValidationException ex) {
			throw new CannotRedoException();
		}
	}
}
