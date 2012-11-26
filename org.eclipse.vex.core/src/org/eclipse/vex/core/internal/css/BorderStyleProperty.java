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

import org.eclipse.vex.core.internal.dom.Node;
import org.w3c.css.sac.LexicalUnit;

/**
 * The border-XXX-style CSS property.
 */
public class BorderStyleProperty extends AbstractProperty {

	/**
	 * Class constructor.
	 * 
	 * @param name
	 *            Name of the property.
	 */
	public BorderStyleProperty(final String name) {
		super(name);
	}

	/**
	 * Returns true if the given lexical unit represents a border style.
	 * 
	 * @param lu
	 *            LexicalUnit to check.
	 */
	public static boolean isBorderStyle(final LexicalUnit lu) {
		if (lu == null) {
			return false;
		} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			final String s = lu.getStringValue();
			return s.equals(CSS.NONE) || s.equals(CSS.HIDDEN) || s.equals(CSS.DOTTED) || s.equals(CSS.DASHED) || s.equals(CSS.SOLID) || s.equals(CSS.DOUBLE) || s.equals(CSS.GROOVE)
					|| s.equals(CSS.RIDGE) || s.equals(CSS.INSET) || s.equals(CSS.OUTSET);
		}

		return false;
	}

	public Object calculate(final LexicalUnit lu, final Styles parentStyles, final Styles styles, final Node node) {
		if (isBorderStyle(lu)) {
			return lu.getStringValue();
		} else if (isInherit(lu) && parentStyles != null) {
			return parentStyles.get(getName());
		} else {
			return CSS.NONE;
		}
	}

}
