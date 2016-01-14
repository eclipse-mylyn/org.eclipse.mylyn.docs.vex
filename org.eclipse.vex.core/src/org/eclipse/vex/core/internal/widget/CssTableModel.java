/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.css.StyleSheet;

/**
 * @author Florian Thienel
 */
public class CssTableModel implements ITableModel {

	private final StyleSheet styleSheet;

	public CssTableModel(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

}
