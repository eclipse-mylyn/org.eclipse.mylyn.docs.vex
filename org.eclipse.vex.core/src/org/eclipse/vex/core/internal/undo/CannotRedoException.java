/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.undo;

/**
 * Thrown when an IUndoableEdit cannot be undone.
 */
public class CannotRedoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Class constructor.
	 */
	public CannotRedoException() {
	}

	/**
	 * Class constructor.
	 * 
	 * @param message
	 *            Message indicating the reason for the failure.
	 */
	public CannotRedoException(final String message) {
		super(message);
	}

	/**
	 * Class constructor.
	 * 
	 * @param cause
	 *            Root cause of the failure.
	 */
	public CannotRedoException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Class constructor.
	 * 
	 * @param message
	 *            Message indicating the reason for the failure.
	 * @param cause
	 *            Root cause of the failure.
	 */
	public CannotRedoException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
