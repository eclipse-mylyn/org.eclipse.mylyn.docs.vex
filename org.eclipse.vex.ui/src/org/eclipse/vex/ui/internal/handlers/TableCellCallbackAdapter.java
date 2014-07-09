/*******************************************************************************
 * Copyright (c) 2009 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

/**
 * This adapter class provides empty default implementations for the methods of {@link ITableCellCallback}. Callback
 * implementations can extend this class and override only the methods which they are interested in.
 */
public abstract class TableCellCallbackAdapter implements ITableCellCallback {

	@Override
	public void endRow(final Object row, final int rowIndex) {
		// NOP (adapter pattern)
	}

	@Override
	public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {
		// NOP (adapter pattern)
	}

	@Override
	public void startRow(final Object row, final int rowIndex) {
		// NOP (adapter pattern)
	}

}
