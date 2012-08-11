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
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.CommentElement;
import org.eclipse.vex.core.internal.dom.Element;

/**
 * Implementation of the BoxFactory interface that returns boxes that represent CSS semantics.
 */
public class CssBoxFactory implements BoxFactory {

	private static final long serialVersionUID = -6882526795866485074L;

	public Box createBox(final LayoutContext context, final Element element, final BlockBox parent, final int containerWidth) {
		final Styles styles = context.getStyleSheet().getStyles(element);
		if (element instanceof CommentElement) {
			return new CommentBlockElementBox(context, parent, element);
		} else if (styles.getDisplay().equals(CSS.TABLE)) {
			return new TableBox(context, parent, element);
		} else if (styles.isBlock()) {
			return new BlockElementBox(context, parent, element);
		} else {
			throw new RuntimeException("Unexpected display property: " + styles.getDisplay());
		}
	}

}
