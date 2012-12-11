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

import org.eclipse.vex.core.internal.dom.Element;
import org.w3c.css.sac.LexicalUnit;

/**
 * Represents a CSS property.
 */
public interface IProperty {

	public static enum Axis {
		HORIZONTAL, VERTICAL
	};

	/**
	 * Returns the name of the property.
	 */
	public String getName();

	/**
	 * Calculates the value of a property given a LexicalUnit.
	 * 
	 * @param lu
	 *            LexicalUnit to interpret.
	 * @param parentStyles
	 *            Styles of the parent element. These are used when the property inherits a value.
	 * @param styles
	 *            Styles currently in effect. Often, the calculated value depends on previously calculated styles such
	 *            as font size and color.
	 * @param element
	 *            The current element for which this property is calculated. May be null.
	 */
	public Object calculate(LexicalUnit lu, Styles parentStyles, Styles styles, Element element);

}
