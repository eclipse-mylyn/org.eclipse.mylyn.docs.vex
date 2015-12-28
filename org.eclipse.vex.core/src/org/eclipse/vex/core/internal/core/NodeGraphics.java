/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.IIncludeNode;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

/**
 * @author Florian Thienel
 */
public class NodeGraphics {

	private static final FontSpec FONT = new FontSpec("Arial", FontSpec.BOLD, 10.0f);

	public static void drawStartTag(final Graphics graphics, final INode node, final int x, final int y, final boolean verticallyCentered) {
		drawSimpleTag(graphics, getNodeStartMarker(node), x, y, false, verticallyCentered);
	}

	public static void drawTag(final Graphics graphics, final INode node, final int x, final int y, final boolean horizontallyCentered, final boolean verticallyCentered, final boolean transparent) {
		drawTag(graphics, getNodeName(node), x, y, horizontallyCentered, verticallyCentered, transparent);
	}

	public static Point getTagSize(final Graphics graphics, final INode node) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textPadding = 3;
		final int margin = 2;
		final int width = graphics.stringWidth(getNodeName(node)) + (textPadding + margin) * 2 + 1;
		final int height = graphics.getFontMetrics().getHeight() + (textPadding + margin) * 2 + 1;
		return new Point(width, height);
	}

	public static int getTagBaseline(final Graphics graphics) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textPadding = 3;
		final int margin = 2;
		final FontMetrics fontMetrics = graphics.getFontMetrics();
		return fontMetrics.getAscent() + fontMetrics.getLeading() + textPadding + margin;
	}

	public static void drawEndTag(final Graphics graphics, final INode node, final int x, final int y, final boolean verticallyCentered) {
		drawSimpleTag(graphics, getNodeEndMarker(node), x, y, false, verticallyCentered);
	}

	private static void drawSimpleTag(final Graphics graphics, final String text, final int x, final int y, final boolean horizontallyCentered, final boolean verticallyCentered) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textPadding = 3;
		final int textWidth = graphics.stringWidth(text) + textPadding * 2;
		final int textHeight = graphics.getFontMetrics().getHeight() + textPadding * 2;

		final int effectiveX;
		if (horizontallyCentered) {
			effectiveX = x - textWidth / 2;
		} else {
			effectiveX = x;
		}

		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - textHeight / 2;
		} else {
			effectiveY = y;
		}

		graphics.setLineWidth(1);
		graphics.fillRect(effectiveX, effectiveY, textWidth, textHeight);
		graphics.drawRect(effectiveX, effectiveY, textWidth, textHeight);
		graphics.drawString(text, effectiveX + textPadding, effectiveY + textPadding);
	}

	private static void drawTag(final Graphics graphics, final String text, final int x, final int y, final boolean horizontallyCentered, final boolean verticallyCentered, final boolean transparent) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textPadding = 3;
		final int textWidth = graphics.stringWidth(text) + textPadding * 2;
		final int textHeight = graphics.getFontMetrics().getHeight() + textPadding * 2;
		final int arc = textHeight / 3;
		final int margin = 2;

		final int effectiveX;
		if (horizontallyCentered) {
			effectiveX = x - textWidth / 2;
		} else {
			effectiveX = x;
		}

		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - textHeight / 2;
		} else {
			effectiveY = y;
		}

		if (!transparent) {
			graphics.fillRoundRect(effectiveX, effectiveY, textWidth + margin * 2 + 1, textHeight + margin * 2 + 1, arc, arc);
		}
		graphics.setLineWidth(1);
		graphics.drawRoundRect(effectiveX + margin, effectiveY + margin, textWidth, textHeight, arc, arc);
		graphics.drawString(text, effectiveX + textPadding + margin, effectiveY + textPadding + margin);
	}

	private static String getNodeName(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<String>() {
			@Override
			public String visit(final IDocument document) {
				return "DOCUMENT";
			}

			@Override
			public String visit(final IElement element) {
				return element.getPrefixedName();
			}

			@Override
			public String visit(final IComment comment) {
				return "COMMENT";
			}

			@Override
			public String visit(final IProcessingInstruction pi) {
				return pi.getTarget();
			}

			@Override
			public String visit(final IIncludeNode include) {
				return getNodeName(include.getReference());
			}
		});
	}

	private static String getNodeStartMarker(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<String>() {
			@Override
			public String visit(final IDocument document) {
				return getNodeName(document);
			}

			@Override
			public String visit(final IElement element) {
				return "<" + getNodeName(element) + "...";
			}

			@Override
			public String visit(final IComment comment) {
				return "<!--";
			}

			@Override
			public String visit(final IProcessingInstruction pi) {
				return "<?" + getNodeName(pi) + "...";
			}

			@Override
			public String visit(final IIncludeNode include) {
				return getNodeStartMarker(include.getReference());
			}
		});
	}

	private static String getNodeEndMarker(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<String>() {
			@Override
			public String visit(final IDocument document) {
				return getNodeName(document);
			}

			@Override
			public String visit(final IElement element) {
				return "</" + getNodeName(element) + ">";
			}

			@Override
			public String visit(final IComment comment) {
				return "--!>";
			}

			@Override
			public String visit(final IProcessingInstruction pi) {
				return "?>";
			}

			@Override
			public String visit(final IIncludeNode include) {
				return getNodeEndMarker(include.getReference());
			}
		});
	}

}
