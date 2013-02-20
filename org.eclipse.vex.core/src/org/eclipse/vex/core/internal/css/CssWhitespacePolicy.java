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

import org.eclipse.vex.core.internal.io.IWhitespacePolicy;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * Implementation of IWhitespacePolicy using a CSS stylesheet.
 * 
 * @see IWhitespacePolicy
 */
public class CssWhitespacePolicy implements IWhitespacePolicy {

	private final StyleSheet styleSheet;

	/**
	 * Create a whitespace policy based on the given stylesheet.
	 * 
	 * @param styleSheet
	 *            the stylesheet used for the policy
	 */
	public CssWhitespacePolicy(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public boolean isBlock(final INode node) {
		return styleSheet.getStyles(node).isBlock();
	}

	public boolean isPre(final INode node) {
		return CSS.PRE.equals(styleSheet.getStyles(node).getWhiteSpace());
	}

}
