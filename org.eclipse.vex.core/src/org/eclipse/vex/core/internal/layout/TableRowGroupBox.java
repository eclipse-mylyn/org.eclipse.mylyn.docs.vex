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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.dom.IParent;
import org.eclipse.vex.core.internal.core.Insets;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;

/**
 * Container for TableRowBox objects. May correspond to an element with display:table-row-group,
 * display:table-head-group, display:table-foot-group, or may be anonymous.
 */
public class TableRowGroupBox extends AbstractBlockBox {

	/**
	 * Class constructor for non-anonymous table row groups.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param parent
	 *            Parent of this box.
	 * @param node
	 *            Node that generated this box.
	 */
	public TableRowGroupBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	/**
	 * Class constructor for anonymous table row groups.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param parent
	 *            Parent of this box.
	 * @param startOffset
	 *            Start of the range encompassing the table.
	 * @param endOffset
	 *            End of the range encompassing the table.
	 */
	public TableRowGroupBox(final LayoutContext context, final BlockBox parent, final int startOffset, final int endOffset) {
		super(context, parent, startOffset, endOffset);

	}

	@Override
	protected List<Box> createChildren(final LayoutContext context) {
		// TODO Auto-generated method stub

		// Walk children in range
		// - table-row children get non-anonymous TableRowBox
		// - runs of others get anonymous TableRowBox

		final List<Box> children = new ArrayList<Box>();

		iterateChildrenByDisplayStyle(context.getStyleSheet(), childDisplayStyles, new ElementOrRangeCallback() {
			public void onElement(final IElement child, final String displayStyle) {
				children.add(new TableRowBox(context, TableRowGroupBox.this, child));
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				children.add(new TableRowBox(context, TableRowGroupBox.this, startOffset, endOffset));
			}
		});

		return children;
	}

	@Override
	public Insets getInsets(final LayoutContext context, final int containerWidth) {
		return Insets.ZERO_INSETS;
	}

	@Override
	public int getMarginBottom() {
		return 0;
	}

	@Override
	public int getMarginTop() {
		return 0;
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		if (skipPaint(context, x, y)) {
			return;
		}

		paintChildren(context, x, y);

		paintSelectionFrame(context, x, y, true);
	}

	@Override
	protected int positionChildren(final LayoutContext context) {

		final Styles styles = context.getStyleSheet().getStyles(findContainingParent());
		final int spacing = styles.getBorderSpacing().getVertical();

		int childY = spacing;
		for (int i = 0; i < getChildren().length; i++) {

			final TableRowBox child = (TableRowBox) getChildren()[i];
			// TODO must force table row margins to be zero
			final Insets insets = child.getInsets(context, getWidth());

			childY += insets.getTop();

			child.setX(insets.getLeft());
			child.setY(childY);

			childY += child.getHeight() + insets.getBottom() + spacing;
		}
		setHeight(childY);

		return -1; // TODO revisit
	}

	// ====================================================== PRIVATE

	private static Set<String> childDisplayStyles = new HashSet<String>();

	static {
		childDisplayStyles.add(CSS.TABLE_ROW);
	}

}
