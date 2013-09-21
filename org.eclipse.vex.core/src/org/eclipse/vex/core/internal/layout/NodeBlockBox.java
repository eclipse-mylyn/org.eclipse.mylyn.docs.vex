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
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * This class displays special block elements like ProcessingInstructions or comments.
 */
public class NodeBlockBox extends BlockElementBox {

	public NodeBlockBox(final LayoutContext context, final BlockBox parent, final INode node) {
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

		final StyleSheet styleSheet = context.getStyleSheet();
		final List<Box> result = new ArrayList<Box>();
		final List<InlineBox> pendingInlines = new ArrayList<InlineBox>();

		// :before content - includes the target
		final IElement before = styleSheet.getPseudoElement(node, CSS.PSEUDO_BEFORE, true);
		if (before != null) {
			pendingInlines.addAll(LayoutUtils.createGeneratedInlines(context, before, StaticTextBox.START_MARKER));
		}

		// textual content
		if (node.getEndOffset() - node.getStartOffset() > 1) {
			pendingInlines.add(new DocumentTextBox(context, node, node.getStartOffset() + 1, node.getEndOffset() - 1));
		}

		// placeholder
		pendingInlines.add(new PlaceholderBox(context, node, node.getEndOffset() - node.getStartOffset()));

		// :after content
		final IElement after = styleSheet.getPseudoElement(node, CSS.PSEUDO_AFTER, true);
		if (after != null) {
			pendingInlines.addAll(LayoutUtils.createGeneratedInlines(context, after, StaticTextBox.END_MARKER));
		}

		result.add(ParagraphBox.create(context, node, pendingInlines, width));

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 10) {
				System.out.println("ProcessingInstructionBlockBox.layout for " + getNode() + " took " + (end - start) + "ms");
			}
		}

		return result;
	}
}
