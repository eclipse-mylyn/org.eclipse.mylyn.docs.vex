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
import org.eclipse.vex.core.internal.dom.Element;

/**
 * @author Florian Thienel
 */
public class CommentBlockElementBox extends BlockElementBox {

	private static final String AFTER_TEXT = "-->";
	private static final String BEFORE_TEXT = "<!--";

	public CommentBlockElementBox(final LayoutContext context, final BlockBox parent, final Element element) {
		super(context, parent, element);
	}

	@Override
	public List<Box> createChildren(final LayoutContext context) {
		long start = 0;
		if (VEXCorePlugin.getInstance().isDebugging()) {
			start = System.currentTimeMillis();
		}

		final Element element = getElement();
		final int width = getWidth();

		final List<Box> childList = new ArrayList<Box>();

		// :before content
		final List<InlineBox> beforeInlines = new ArrayList<InlineBox>();
		beforeInlines.add(new StaticTextBox(context, element, BEFORE_TEXT));

		// :after content
		final List<InlineBox> afterInlines = new ArrayList<InlineBox>();
		afterInlines.add(new StaticTextBox(context, element, AFTER_TEXT));

		final int startOffset = element.getStartOffset() + 1;
		final int endOffset = element.getEndOffset();
		final List<Box> blockBoxes = createBlockBoxes(context, startOffset, endOffset, width, beforeInlines, afterInlines);
		childList.addAll(blockBoxes);

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 10) {
				System.out.println("CommentBlockElementBox.layout for " + getElement().getPrefixedName() + " took " + (end - start) + "ms");
			}
		}

		return childList;
	}
}
