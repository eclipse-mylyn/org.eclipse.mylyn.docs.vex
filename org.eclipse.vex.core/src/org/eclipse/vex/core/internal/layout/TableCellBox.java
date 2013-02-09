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

import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * Represents an element with display:table-cell, or a generated, anonymous table cell.
 */
public class TableCellBox extends AbstractBlockBox {

	/**
	 * Class constructor for non-anonymous table cells.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param parent
	 *            Parent box.
	 * @param element
	 *            Element with which this box is associated.
	 */
	public TableCellBox(final LayoutContext context, final BlockBox parent, final IElement element, final int width) {
		super(context, parent, element);
		final Styles styles = context.getStyleSheet().getStyles(element);
		setWidth(width - styles.getBorderLeftWidth() - styles.getPaddingLeft().get(parent.getWidth()) - styles.getPaddingRight().get(parent.getWidth()) - styles.getBorderRightWidth());
	}

	public TableCellBox(final LayoutContext context, final BlockBox parent, final int startOffset, final int endOffset, final int width) {
		super(context, parent, startOffset, endOffset);
		setWidth(width);
	}

	@Override
	protected List<Box> createChildren(final LayoutContext context) {
		return createBlockBoxes(context, getStartOffset(), getEndOffset(), getWidth(), null, null);
	}

	@Override
	public void setInitialSize(final LayoutContext context) {
		// we've already set width in the ctor
		// override to avoid setting width again
		setHeight(getEstimatedHeight(context));
	}

	// ======================================================= PRIVATE

}
