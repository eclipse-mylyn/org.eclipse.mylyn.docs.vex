/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
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
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * Implementation of the BoxFactory interface that returns boxes that represent CSS semantics.
 */
public class CssBoxFactory implements BoxFactory {

	private static final long serialVersionUID = -6882526795866485074L;

	@Override
	public Box createBox(final LayoutContext context, final INode node, final BlockBox parentBox, final int containerWidth) {
		final Styles styles = context.getStyleSheet().getStyles(node);
		return node.accept(new BaseNodeVisitorWithResult<Box>() {
			@Override
			public Box visit(final IDocument document) {
				throw new IllegalStateException("The box factory must not be called for Document nodes.");
			}

			@Override
			public Box visit(final IDocumentFragment fragment) {
				throw new IllegalStateException("The box factory must not be called for DocumentFragment nodes.");
			}

			@Override
			public Box visit(final IElement element) {
				final String displayStyle = styles.getDisplay();
				if (displayStyle.equals(CSS.TABLE)) {
					return new TableBox(context, parentBox, element);
				} else if (displayStyle.equals(CSS.INCLUDE)) {
					return new IncludeBlockBox(context, parentBox, element);
				} else if (displayStyle.equals(CSS.LIST_ITEM)) {
					return new ListItemBox(context, parentBox, node);
				} else if (context.getWhitespacePolicy().isBlock(element)) {
					return new BlockElementBox(context, parentBox, node);
				} else {
					throw new RuntimeException("Unexpected display property: " + styles.getDisplay());
				}
			}

			@Override
			public Box visit(final IComment comment) {
				return new NodeBlockBox(context, parentBox, comment);
			}

			@Override
			public Box visit(final IProcessingInstruction pi) {
				return new NodeBlockBox(context, parentBox, pi);
			}

			@Override
			public Box visit(final IText text) {
				throw new IllegalStateException("The box factory must not be called for Text nodes.");
			}
		});
	}
}
