/*******************************************************************************
 * Copyright (c) 2010, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - added isSplitable() method
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

/**
 * @author Florian Thienel
 */
public abstract class AbstractInlineBox extends AbstractBox implements InlineBox {

	@Override
	public void alignOnBaseline(final int baseline) {
		setY(baseline - getBaseline());
	}

	@Override
	public boolean isSplitable() {
		return false;
	}

	@Override
	public Pair split(final LayoutContext context, final int maxWidth, final boolean force) {
		throw new UnsupportedOperationException("split method not supported");
	}

}
