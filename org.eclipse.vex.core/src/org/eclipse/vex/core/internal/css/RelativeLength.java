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
package org.eclipse.vex.core.internal.css;

/**
 * A length that may be expressed as an absolute or relative value.
 */
public class RelativeLength {

	private final float percentage;
	private final int absolute;
	boolean isAbsolute;

	private static RelativeLength ZERO = new RelativeLength(0, 0, true);

	/**
	 * Create a relative length representing an absolute value.
	 * 
	 * @return the new RelativeLength value.
	 */
	public static RelativeLength createAbsolute(final int value) {
		if (value == 0) {
			return ZERO;
		} else {
			return new RelativeLength(0, value, true);
		}
	}

	/**
	 * Create a relative length representing a relative value.
	 * 
	 * @return the new RelativeLength value.
	 */
	public static RelativeLength createRelative(final float percentage) {
		return new RelativeLength(percentage, 0, false);
	}

	/**
	 * Return the value of the length given a reference value. If this object represents an absolute value, that value
	 * is simply returned. Otherwise, returns the given reference length multiplied by the given percentage and rounded
	 * to the nearest integer.
	 * 
	 * @param referenceLength
	 *            reference length by which percentage lengths will by multiplied.
	 * @return the actual value
	 */
	public int get(final int referenceLength) {
		if (isAbsolute) {
			return absolute;
		} else {
			return Math.round(percentage * referenceLength);
		}
	}

	// ==================================================== PRIVATE

	private RelativeLength(final float percentage, final int absolute, final boolean isAbsolute) {
		this.percentage = percentage;
		this.absolute = absolute;
		this.isAbsolute = isAbsolute;
	}
}
