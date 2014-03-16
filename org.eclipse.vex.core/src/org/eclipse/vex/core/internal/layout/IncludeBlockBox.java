/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * This class displays include elements like XInclude.
 */
public class IncludeBlockBox extends BlockElementBox {
	private static final int H_CARET_LENGTH = 20;

	public IncludeBlockBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	@Override
	public Caret getCaret(final LayoutContext context, final ContentPosition position) {
		return new HCaret(0, getHeight() - 2, H_CARET_LENGTH);
	}

	@Override
	public List<Box> createChildren(final LayoutContext context) {
		final INode node = getNode();
		final int width = getWidth();

		final StyleSheet styleSheet = context.getStyleSheet();
		final List<Box> result = new ArrayList<Box>();
		final List<InlineBox> pendingInlines = new ArrayList<InlineBox>();

		// :before content - includes the target
		final IElement before = styleSheet.getPseudoElementBefore(node);
		if (before != null) {
			pendingInlines.addAll(LayoutUtils.createGeneratedInlines(context, before, StaticTextBox.START_MARKER));
		}

		// content
		final String text = LayoutUtils.getGeneratedContent(context, styleSheet.getStyles(node), node);
		if (text.length() > 0) {
			pendingInlines.add(new StaticTextBox(context, node, text));
		}

		// :after content
		final IElement after = styleSheet.getPseudoElementAfter(node);
		if (after != null) {
			pendingInlines.addAll(LayoutUtils.createGeneratedInlines(context, after, StaticTextBox.END_MARKER));
		}

		if (pendingInlines.isEmpty()) {
			// No pseudo element and no content - return something
			pendingInlines.add(new StaticTextBox(context, node, CSS.INCLUDE, StaticTextBox.START_MARKER));
		}
		result.add(ParagraphBox.create(context, node, pendingInlines, width));

		return result;
	}

	@Override
	public ContentPosition viewToModel(final LayoutContext context, final int x, final int y) {
		return getNode().getStartPosition().moveBy(1);
	}

	@Override
	public String toString() {
		return "IncludeBlockBox: <" + getNode() + ">" + "[x=" + getX() + ",y=" + getY() + ",width=" + getWidth() + ",height=" + getHeight() + "]";
	}
}
