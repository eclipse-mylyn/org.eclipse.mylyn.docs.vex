/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

/**
 * @author Florian Thienel
 */
public class ReadOnlyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReadOnlyException() {
		super();
	}

	public ReadOnlyException(final String message) {
		super(message);
	}

	public ReadOnlyException(final Throwable cause) {
		super(cause);
	}

	public ReadOnlyException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
