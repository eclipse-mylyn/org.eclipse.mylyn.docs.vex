/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.List;

/**
 * A block box that is not associated with a particular element.
 */
public class AnonymousBlockBox extends AbstractBlockBox {

	public AnonymousBlockBox(final LayoutContext context, final BlockBox parent, final int startOffset, final int endOffset) {

		super(context, parent, startOffset, endOffset);
	}

	@Override
	protected List<Box> createChildren(final LayoutContext context) {
		return createBlockBoxes(context, getStartOffset(), getEndOffset(), getWidth(), null, null);
	}

}
