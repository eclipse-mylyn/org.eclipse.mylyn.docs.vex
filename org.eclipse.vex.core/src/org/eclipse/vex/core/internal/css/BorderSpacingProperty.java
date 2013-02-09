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

import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.w3c.css.sac.LexicalUnit;

/**
 * The CSS 'border-spacing' property.
 */
public class BorderSpacingProperty extends AbstractProperty {

	/**
	 * Represents the computed value of border-spacing, which is a pair of values representing vertical and horizontal
	 * spacing.
	 */
	public static class Value {

		private final int horizontal;
		private final int vertical;

		public static final Value ZERO = new Value(0, 0);

		public Value(final int horizontal, final int vertical) {
			this.horizontal = horizontal;
			this.vertical = vertical;
		}

		/**
		 * Returns the horizontal spacing, in pixels.
		 */
		public int getHorizontal() {
			return horizontal;
		}

		/**
		 * Returns the vertical spacing, in pixels.
		 */
		public int getVertical() {
			return vertical;
		}
	}

	/**
	 * Class constructor.
	 */
	public BorderSpacingProperty() {
		super(CSS.BORDER_SPACING);
	}

	public Object calculate(LexicalUnit lu, final Styles parentStyles, final Styles styles, final INode node) {

		int horizontal = 0;
		int vertical = 0;

		final DisplayDevice device = DisplayDevice.getCurrent();

		if (isLength(lu)) {
			horizontal = getIntLength(lu, styles.getFontSize(), device.getHorizontalPPI());
			lu = lu.getNextLexicalUnit();
			if (isLength(lu)) {
				vertical = getIntLength(lu, styles.getFontSize(), device.getVerticalPPI());
			} else {
				vertical = horizontal;
			}
			return new Value(horizontal, vertical);
		} else {
			// 'inherit' or an invalid value
			if (parentStyles == null) {
				return Value.ZERO;
			} else {
				return parentStyles.getBorderSpacing();
			}
		}
	}

}
