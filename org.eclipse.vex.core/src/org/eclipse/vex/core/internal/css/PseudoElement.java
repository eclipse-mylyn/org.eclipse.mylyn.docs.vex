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

import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.internal.dom.Element;

/**
 * Represents a :before or :after pseudo-element.
 * 
 * XXX REMOVE THIS HACK!!!
 */
public class PseudoElement extends Element {

	public static final String AFTER = "after";
	public static final String BEFORE = "before";

	/**
	 * Class constructor.
	 * 
	 * @param parent
	 *            Parent element to this pseudo-element.
	 * @param name
	 *            Name of this pseudo-element, e.g. PseudoElement.BEFORE.
	 */
	public PseudoElement(final IElement parent, final String name) {
		super(name);
		setParent((Element) parent);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		final PseudoElement other = (PseudoElement) o;
		return getParent() == other.getParent() && getQualifiedName().equals(other.getQualifiedName());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getParent().hashCode() + getQualifiedName().hashCode();
	}
}
