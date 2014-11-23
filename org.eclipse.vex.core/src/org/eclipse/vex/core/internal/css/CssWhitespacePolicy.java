/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.provisional.dom.IValidator;

/**
 * Implementation of IWhitespacePolicy using a CSS stylesheet.
 *
 * @see IWhitespacePolicy
 */
public class CssWhitespacePolicy implements IWhitespacePolicy {

	public static final IWhitespacePolicyFactory FACTORY = new IWhitespacePolicyFactory() {
		@Override
		public IWhitespacePolicy createPolicy(final IValidator validator, final DocumentContentModel documentContentModel, final StyleSheet styleSheet) {
			return new CssWhitespacePolicy(styleSheet);
		}
	};

	private final StyleSheet styleSheet;

	/**
	 * Create a whitespace policy based on the given stylesheet.
	 *
	 * @param styleSheet
	 *            the stylesheet used for the policy
	 */
	public CssWhitespacePolicy(final StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public boolean isBlock(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<Boolean>(true) {
			@Override
			public Boolean visit(final IElement element) {
				if (isDisplay(element, CSS.BLOCK)) {
					return true;
				}

				if (isDisplay(element, CSS.LIST_ITEM)) {
					return true;
				}

				if (isDisplay(element, CSS.INCLUDE)) {
					// When this method is called by the DocumentBuilder, the note is not yet associated
					if (!element.isAssociated() || element.getDocument() == null) {
						return false;
					}

					if (element.getDocument().canInsertText(element.getStartOffset())) {
						return false;
					}

					return isBlock(element.getParent());
				}

				if (isDisplay(element, CSS.TABLE)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_CAPTION, CSS.TABLE)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_CELL, CSS.TABLE_ROW)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_COLUMN, CSS.TABLE_COLUMN_GROUP)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_COLUMN_GROUP, CSS.TABLE)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_FOOTER_GROUP, CSS.TABLE, CSS.TABLE_ROW_GROUP)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_HEADER_GROUP, CSS.TABLE, CSS.TABLE_ROW_GROUP)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_ROW_GROUP, CSS.TABLE, CSS.TABLE_ROW_GROUP)) {
					return true;
				}

				if (isDisplay(element, CSS.TABLE_ROW, CSS.TABLE, CSS.TABLE_ROW_GROUP, CSS.TABLE_HEADER_GROUP, CSS.TABLE_FOOTER_GROUP)) {
					return true;
				}

				return false;
			}

			@Override
			public Boolean visit(final IComment comment) {
				final boolean parentIsInline = !isBlock(comment.getParent());
				final boolean isInline = CSS.INLINE.equals(getDisplay(comment));
				return !(parentIsInline && isInline);
			}

			@Override
			public Boolean visit(final IProcessingInstruction pi) {
				final boolean parentIsInline = !isBlock(pi.getParent());
				final boolean isInline = CSS.INLINE.equals(getDisplay(pi));
				return !(parentIsInline && isInline);
			}

			@Override
			public Boolean visit(final IIncludeNode include) {
				// TODO Evaluate the included content instead of the element
				return visit(include.getReference());
			}

			@Override
			public Boolean visit(final IText text) {
				return false;
			}
		});
	}

	private boolean isDisplay(final INode node, final String display, final String... allowedParents) {
		final String nodeDisplay = getDisplay(node);
		final String parentDisplay = getDisplay(node.getParent());

		if (!display.equals(nodeDisplay)) {
			return false;
		}

		if (allowedParents.length == 0) {
			return true;
		}

		for (final String parent : allowedParents) {
			if (parent.equals(parentDisplay)) {
				return true;
			}
		}
		return false;
	}

	private String getDisplay(final INode node) {
		if (node == null) {
			return DisplayProperty.DEFAULT;
		}

		final Styles styles = styleSheet.getStyles(node);
		if (styles == null) {
			return DisplayProperty.DEFAULT;
		}

		return styles.getDisplay();
	}

	@Override
	public boolean isPre(final INode node) {
		return CSS.PRE.equals(styleSheet.getStyles(node).getWhiteSpace());
	}

}
