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
package org.eclipse.vex.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.vex.core.internal.VEXCorePlugin;

public class ValidationResult implements IValidationResult {

	private final String message;
	private final int severity;

	private static final IStatus[] emptyStatusArray = new IStatus[0];

	public static final IValidationResult VALIDATE_OK = new ValidationResult(OK, "ok");

	/**
	 * Create a new error result.
	 * 
	 * @param message
	 *            The validation message.
	 * @return A new ValidationResult with the given message and severity set to {@link IStatus#ERROR}.
	 */
	public static IValidationResult error(final String message) {
		return new ValidationResult(ERROR, message);
	}

	public ValidationResult(final int severity, final String message) {
		this.severity = severity;
		this.message = message;
	}

	@Override
	public IStatus[] getChildren() {
		return emptyStatusArray;
	}

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public Throwable getException() {
		return null;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getPlugin() {
		return VEXCorePlugin.ID;
	}

	@Override
	public int getSeverity() {
		return severity;
	}

	@Override
	public boolean isMultiStatus() {
		return false;
	}

	@Override
	public boolean isOK() {
		return severity == OK;
	}

	@Override
	public boolean matches(final int severityMask) {
		return (severity & severityMask) != 0;
	}

}
