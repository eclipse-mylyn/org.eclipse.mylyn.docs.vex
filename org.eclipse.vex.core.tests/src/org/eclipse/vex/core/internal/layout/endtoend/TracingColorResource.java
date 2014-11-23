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

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;

/**
 * @author Florian Thienel
 */
public class TracingColorResource implements ColorResource {

	private final Tracer tracer;
	private final Color color;
	private final int id;

	public TracingColorResource(final Tracer tracer, final Color color) {
		this.tracer = tracer;
		this.color = color;
		id = -1;
	}

	public TracingColorResource(final Tracer tracer, final int id) {
		this.tracer = tracer;
		color = null;
		this.id = id;
	}

	@Override
	public void dispose() {
		tracer.trace("ColorResource[{0}, {1}].dispose()", color, id);
	}

	@Override
	public String toString() {
		return "TracingColorResource [color=" + color + ", id=" + id + "]";
	}

}
