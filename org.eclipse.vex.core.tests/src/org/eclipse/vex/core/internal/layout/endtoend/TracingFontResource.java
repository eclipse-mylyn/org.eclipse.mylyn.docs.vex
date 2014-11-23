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

import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;

/**
 * @author Florian Thienel
 */
public class TracingFontResource implements FontResource {

	private final Tracer tracer;
	private final FontSpec fontSpec;

	public TracingFontResource(final Tracer tracer, final FontSpec fontSpec) {
		this.tracer = tracer;
		this.fontSpec = fontSpec;
	}

	@Override
	public void dispose() {
		tracer.trace("FontResource[{0}].dispose()", fontSpec);
	}

	@Override
	public String toString() {
		return "TracingFontResource [fontSpec=" + fontSpec + "]";
	}

}
