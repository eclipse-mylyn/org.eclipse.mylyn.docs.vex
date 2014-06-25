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

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IHostComponent;

/**
 * @author Florian Thienel
 */
public class TracingHostComponent implements IHostComponent {

	private final Tracer tracer;
	private Rectangle viewPort = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public TracingHostComponent(final PrintStream out) {
		tracer = new Tracer(out);
	}

	@Override
	public Graphics createDefaultGraphics() {
		tracer.trace("HostComponent.createDefaultGraphics()");
		return new TracingGraphics(tracer);
	}

	@Override
	public Rectangle getViewport() {
		tracer.trace("HostComponent.getViewport()");
		return viewPort;
	}

	public void setViewport(final Rectangle viewPort) {
		tracer.trace("HostComponent.setViewport({0})", viewPort);
		this.viewPort = viewPort;
	}

	@Override
	public void fireSelectionChanged() {
		tracer.trace("HostComponent.fireSelectionChanged()");
	}

	@Override
	public void invokeLater(final Runnable runnable) {
		tracer.trace("HostComponent.invokeLater({0})", runnable.getClass().getName());
	}

	@Override
	public void repaint() {
		tracer.trace("HostComponent.repaint()");
	}

	@Override
	public void repaint(final int x, final int y, final int width, final int height) {
		tracer.trace("HostComponent.repaint({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x, y, width, height);
	}

	@Override
	public void scrollTo(final int left, final int top) {
		tracer.trace("HostComponent.scrollTo({0,number,#}, {1,number,#})", left, top);
	}

	@Override
	public void setPreferredSize(final int width, final int height) {
		tracer.trace("HostComponent.setPreferredSize({0,number,#}. {1,number,#})", width, height);
	}

}
