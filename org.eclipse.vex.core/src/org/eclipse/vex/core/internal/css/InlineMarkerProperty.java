/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.provisional.dom.INode;
import org.w3c.css.sac.LexicalUnit;

/**
 * The -vex-inline-marker CSS property. This property decides the type of inline markers<br />
 * The value <code>none</code> will hide the inline markers around the element.<br />
 * The defualt is <code>normal</code> to display the default markers.
 */
public class InlineMarkerProperty extends AbstractProperty {

	/**
	 * Class constructor.
	 */
	public InlineMarkerProperty() {
		super(CSS.INLINE_MARKER);
	}

	@Override
	public Object calculate(final LexicalUnit lu, final Styles parentStyles, final Styles styles, final INode node) {
		if (isInlineMarker(lu)) {
			return lu.getStringValue();
		}

		return CSS.NORMAL;
	}

	/**
	 * Returns true if the given lexical unit represents a valid _vex-inline-marker.
	 *
	 * @param lu
	 *            LexicalUnit to check.
	 */
	public static boolean isInlineMarker(final LexicalUnit lu) {
		if (lu == null) {
			return false;
		} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			final String s = lu.getStringValue();
			return s.equals(CSS.NORMAL) || s.equals(CSS.NONE);
		} else {
			return false;
		}
	}
}
