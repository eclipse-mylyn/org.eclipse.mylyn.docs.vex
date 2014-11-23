/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout.endtoend;

import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * @author Florian Thienel
 */
public class Tracer {

	private final PrintStream out;

	public Tracer(final PrintStream out) {
		this.out = out;
	}

	public void trace(final String message, final Object... arguments) {
		out.println(MessageFormat.format(message, arguments));
	}
}
