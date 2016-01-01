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
	private static final int TEXT_PADDING = 3;
	private static final int MARGIN = 2;

	public static void drawTag(final Graphics graphics, final INode node, final int x, final int y, final boolean horizontallyCentered, final boolean verticallyCentered, final boolean transparent) {
		drawTag(graphics, getNodeName(node), x, y, horizontallyCentered, verticallyCentered, transparent);
	}

	public static void drawTag(final Graphics graphics, final String text, final int x, final int y, final boolean horizontallyCentered, final boolean verticallyCentered, final boolean transparent) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textWidth = graphics.stringWidth(text) + TEXT_PADDING * 2;
		final int textHeight = graphics.getFontMetrics().getHeight() + TEXT_PADDING * 2;
		final int arc = textHeight / 3;

		final int effectiveX;
		if (horizontallyCentered) {
			effectiveX = x - textWidth / 2 - MARGIN;
		} else {
			effectiveX = x;
		}

		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - textHeight / 2 - MARGIN;
		} else {
			effectiveY = y;
		}

		if (!transparent) {
			graphics.fillRoundRect(effectiveX, effectiveY, textWidth + MARGIN * 2 - 1, textHeight + MARGIN * 2 - 1, arc, arc);
		}
		graphics.setLineWidth(1);
		graphics.setLineStyle(LineStyle.SOLID);
		graphics.drawRoundRect(effectiveX + MARGIN - 1, effectiveY + MARGIN - 1, textWidth, textHeight, arc, arc);
		graphics.drawString(text, effectiveX + TEXT_PADDING + MARGIN - 1, effectiveY + TEXT_PADDING + MARGIN - 1);
	}

	public static Point getTagSize(final Graphics graphics, final INode node) {
		return getTagSize(graphics, getNodeName(node));
	}

	public static Point getTagSize(final Graphics graphics, final String text) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int width = graphics.stringWidth(text) + (TEXT_PADDING + MARGIN) * 2 + 1;
		final int height = graphics.getFontMetrics().getHeight() + (TEXT_PADDING + MARGIN) * 2 + 1;
		return new Point(width, height);
	}

	public static int getTagBaseline(final Graphics graphics) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final FontMetrics fontMetrics = graphics.getFontMetrics();
		return fontMetrics.getAscent() + fontMetrics.getLeading() + TEXT_PADDING + MARGIN;
	}

	public static void drawStartTag(final Graphics graphics, final INode node, final int x, final int y, final boolean verticallyCentered, final boolean transparent) {
		drawStartTag(graphics, getNodeName(node), x, y, verticallyCentered, false);
	}

	public static void drawStartTag(final Graphics graphics, final String text, final int x, final int y, final boolean verticallyCentered, final boolean transparent) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textWidth = graphics.stringWidth(text) + TEXT_PADDING;
		final int textHeight = graphics.getFontMetrics().getHeight() + TEXT_PADDING * 2;

		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - textHeight / 2 - MARGIN;
		} else {
			effectiveY = y;
		}

		if (!transparent) {
			graphics.fillPolygon(arrowRight(x, effectiveY, textWidth + 2 * MARGIN, textHeight + MARGIN * 2 - 1));
		}

		graphics.setLineWidth(1);
		graphics.setLineStyle(LineStyle.SOLID);
		graphics.drawPolygon(arrowRight(x + MARGIN, effectiveY + MARGIN - 1, textWidth, textHeight));

		graphics.drawString(text, x + TEXT_PADDING + MARGIN, effectiveY + TEXT_PADDING + MARGIN - 1);
	}

	private static int[] arrowRight(final int x, final int y, final int width, final int height) {
		final int arrow = height / 2;
		return new int[] {
				x, y,
				x + width, y,
				x + width + arrow, y + height / 2,
				x + width, y + height,
				x, y + height
		};
	}

	public static Point getStartTagSize(final Graphics graphics, final INode node) {
		return getStartTagSize(graphics, getNodeName(node));
	}

	public static Point getStartTagSize(final Graphics graphics, final String text) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int height = graphics.getFontMetrics().getHeight() + (TEXT_PADDING + MARGIN) * 2;
		final int width = graphics.stringWidth(text) + TEXT_PADDING + MARGIN * 2 + height / 2;
		return new Point(width, height);
	}

	public static void drawEndTag(final Graphics graphics, final INode node, final int x, final int y, final boolean verticallyCentered, final boolean transparent) {
		drawEndTag(graphics, getNodeName(node), x, y, verticallyCentered, false);
	}

	public static void drawEndTag(final Graphics graphics, final String text, final int x, final int y, final boolean verticallyCentered, final boolean transparent) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int textWidth = graphics.stringWidth(text) + TEXT_PADDING;
		final int textHeight = graphics.getFontMetrics().getHeight() + TEXT_PADDING * 2;
		final int arrow = textHeight / 2 + MARGIN;

		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - textHeight / 2 - MARGIN;
		} else {
			effectiveY = y;
		}

		if (!transparent) {
			graphics.fillPolygon(arrowLeft(x, effectiveY, textWidth + MARGIN + 1, textHeight + MARGIN * 2 - 1));
		}

		graphics.setLineWidth(1);
		graphics.setLineStyle(LineStyle.SOLID);
		graphics.drawPolygon(arrowLeft(x + MARGIN, effectiveY + MARGIN - 1, textWidth, textHeight));

		graphics.drawString(text, x + arrow, effectiveY + TEXT_PADDING + MARGIN - 1);
	}

	private static int[] arrowLeft(final int x, final int y, final int width, final int height) {
		final int arrow = height / 2;
		return new int[] {
				x, y + height / 2,
				x + arrow, y,
				x + arrow + width, y,
				x + arrow + width, y + height,
				x + arrow, y + height
		};
	}

	public static Point getEndTagSize(final Graphics graphics, final INode node) {
		return getStartTagSize(graphics, getNodeName(node));
	}

	public static Point getEndTagSize(final Graphics graphics, final String text) {
		graphics.setCurrentFont(graphics.getFont(FONT));
		final int height = graphics.getFontMetrics().getHeight() + (TEXT_PADDING + MARGIN) * 2;
		final int width = graphics.stringWidth(text) + TEXT_PADDING + MARGIN * 2 + height / 2;
		return new Point(width, height);
	}

	public static String getNodeName(final INode node) {
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
				return "?" + pi.getTarget();
			}

			@Override
			public String visit(final IIncludeNode include) {
				return getNodeName(include.getReference());
			}
		});
	}

}
