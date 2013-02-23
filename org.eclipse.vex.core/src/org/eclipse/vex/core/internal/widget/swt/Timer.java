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
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.swt.widgets.Display;

/**
 * Periodic timer, built using the Display.timerExec method.
 */
public class Timer {

	/**
	 * Class constructor. The timer must be explicitly started using the start() method.
	 * 
	 * @param periodMs
	 *            Milliseconds between each invocation.
	 * @param runnable
	 *            Runnable to execute when the period expires.
	 */
	public Timer(final int periodMs, final Runnable runnable) {
		this.periodMs = periodMs;
		this.runnable = runnable;
	}

	/**
	 * Reset the timer so that it waits another period before firing.
	 */
	public void reset() {
		if (started) {
			stop();
			start();
		}
	}

	/**
	 * Start the timer.
	 */
	public void start() {
		if (!started) {
			innerRunnable = new InnerRunnable();
			Display.getCurrent().timerExec(periodMs, innerRunnable);
			started = true;
		}
	}

	/**
	 * Stop the timer.
	 */
	public void stop() {
		if (started) {
			innerRunnable.discarded = true;
			innerRunnable = null;
			started = false;
		}
	}

	// ==================================================== PRIVATE

	private final Runnable runnable;
	private final int periodMs;
	private boolean started = false;
	private InnerRunnable innerRunnable;

	private class InnerRunnable implements Runnable {
		public boolean discarded = false;

		public void run() {
			if (!discarded) {
				runnable.run();
				// Display display = Display.getCurrent();
				Display.getCurrent().timerExec(periodMs, this);
			}
		}
	}
}
