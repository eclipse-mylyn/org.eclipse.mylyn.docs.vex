/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class CommentBlockBox extends BlockElementBox {

	private static final String AFTER_TEXT = "-->";
	private static final String BEFORE_TEXT = "<!--";

	public CommentBlockBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	@Override
	public VerticalRange layout(final LayoutContext context, final int top, final int bottom) {
		return super.layout(context, top, bottom);
	}

	@Override
	public List<Box> createChildren(final LayoutContext context) {
		long start = 0;
		if (VEXCorePlugin.getInstance().isDebugging()) {
			start = System.currentTimeMillis();
		}

		final INode node = getNode();
		final int width = getWidth();

		final List<Box> result = new ArrayList<Box>();

		final List<InlineBox> pendingInlines = new ArrayList<InlineBox>();

		// :before content
		pendingInlines.add(new StaticTextBox(context, node, BEFORE_TEXT));

		// textual content
		if (node.getEndOffset() - node.getStartOffset() > 1) {
			pendingInlines.add(new DocumentTextBox(context, node, node.getStartOffset() + 1, node.getEndOffset() - 1));
		}

		// placeholder
		pendingInlines.add(new PlaceholderBox(context, node, node.getEndOffset() - node.getStartOffset()));

		// :after content
		pendingInlines.add(new StaticTextBox(context, node, AFTER_TEXT));

		result.add(ParagraphBox.create(context, node, pendingInlines, width));

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 10) {
				System.out.println("CommentBlockElementBox.layout for " + getNode() + " took " + (end - start) + "ms");
			}
		}

		return result;
	}
}
