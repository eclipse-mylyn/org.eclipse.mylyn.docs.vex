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
package org.eclipse.vex.ui.internal.swt;

import org.eclipse.swt.events.KeyEvent;

/**
 * Represents a keystroke and a certain set of modifiers.
 */
public class KeyStroke {

	private final char character;
	private final int keyCode;
	private final int stateMask;

	/**
	 * Class constructor.
	 * 
	 * @param character
	 *            the key character
	 * @param keyCode
	 *            the key code
	 * @param stateMask
	 *            the set of modifiers
	 */
	public KeyStroke(final char character, final int keyCode, final int stateMask) {
		this.character = character;
		this.keyCode = keyCode;
		this.stateMask = stateMask;
	}

	/**
	 * Class constructor.
	 * 
	 * @param e
	 *            a KeyEvent representing the key stroke
	 */
	public KeyStroke(final KeyEvent e) {
		character = e.character;
		keyCode = e.keyCode;
		stateMask = e.stateMask;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof KeyStroke)) {
			return false;
		}
		final KeyStroke other = (KeyStroke) o;
		return character == other.character && keyCode == other.keyCode && stateMask == other.stateMask;
	}

	@Override
	public int hashCode() {
		return character + keyCode + stateMask;
	}

}
