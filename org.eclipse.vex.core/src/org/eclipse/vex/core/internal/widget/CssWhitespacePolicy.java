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
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Node;

/**
 * Implementation of WhitespacePolicy using a CSS stylesheet.
 */
public class CssWhitespacePolicy implements IWhitespacePolicy {

	/**
	 * Class constructor.
	 * 
	 * @param styleSheet
	 *            The stylesheet used for the policy.
	 */
	public CssWhitespacePolicy(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public boolean isBlock(final Node node) {
		return styleSheet.getStyles(node).isBlock();
	}

	public boolean isPre(final Node node) {
		return CSS.PRE.equals(styleSheet.getStyles(node).getWhiteSpace());
	}

	// ===================================================== PRIVATE

	private final StyleSheet styleSheet;
}
