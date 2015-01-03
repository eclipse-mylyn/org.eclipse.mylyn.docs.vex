/*******************************************************************************
 * Copyright (c) 2014, 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
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
public class Cursor {

	private static final Color FOREGROUND_COLOR = new Color(255, 255, 255);
	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
	private static final FontSpec FONT = new FontSpec("Arial", FontSpec.BOLD, 10.0f);

	private int offset;
	private final ContentMap contentMap;
	private Caret caret;
	private IContentBox box;

	public Cursor(final ContentMap contentMap) {
		this.contentMap = contentMap;
	}

	public void setPosition(final Graphics graphics, final int offset) {
		this.offset = offset;

		box = contentMap.findBoxForPosition(offset);
		if (box == null) {
			return;
		}
		caret = getCaretForBox(graphics, box, offset);
	}

	public int getPosition() {
		return offset;
	}

	public Rectangle getCaretArea() {
		if (caret == null) {
			return Rectangle.NULL;
		}
		return caret.getArea();
	}

	public IContentBox getCurrentBox() {
		return box;
	}

	public void paint(final Graphics graphics) {
		if (caret == null) {
			return;
		}
		caret.paint(graphics);
	}

	private Caret getCaretForBox(final Graphics graphics, final IContentBox box, final int offset) {
		return box.accept(new BaseBoxVisitorWithResult<Caret>() {
			@Override
			public Caret visit(final NodeReference box) {
				return getCaretForNode(graphics, box, offset);
			}

			@Override
			public Caret visit(final TextContent box) {
				return getCaretForText(graphics, box, offset);
			}
		});
	}

	private Caret getCaretForNode(final Graphics graphics, final NodeReference box, final int offset) {
		final Rectangle area = getAbsolutePositionArea(graphics, box, offset);
		if (box.isAtStart(offset)) {
			return new InsertBeforeNodeCaret(area, box.getNode());
		} else if (box.isAtEnd(offset) && box.canContainText()) {
			return new AppendNodeWithTextCaret(area, box.getNode(), box.isEmpty());
		} else if (box.isAtEnd(offset) && !box.canContainText()) {
			return new AppendStructuralNodeCaret(area, box.getNode());
		} else {
			return null;
		}
	}

	private Rectangle getAbsolutePositionArea(final Graphics graphics, final IContentBox box, final int offset) {
		if (box == null) {
			return Rectangle.NULL;
		}
		return box.accept(new BaseBoxVisitorWithResult<Rectangle>() {
			@Override
			public Rectangle visit(final NodeReference box) {
				if (box.isAtStart(offset)) {
					return makeAbsolute(box.getPositionArea(graphics, offset), box);
				} else if (box.isAtEnd(offset) && box.canContainText() && !box.isEmpty()) {
					final int lastOffset = offset - 1;
					final IContentBox lastBox = contentMap.findBoxForPosition(lastOffset);
					return getAbsolutePositionArea(graphics, lastBox, lastOffset);
				} else if (box.isAtEnd(offset)) {
					return makeAbsolute(box.getPositionArea(graphics, offset), box);
				} else {
					return Rectangle.NULL;
				}
			}

			@Override
			public Rectangle visit(final TextContent box) {
				return makeAbsolute(box.getPositionArea(graphics, offset), box);
			}
		});
	}

	private static Rectangle makeAbsolute(final Rectangle rectangle, final IBox box) {
		return new Rectangle(rectangle.getX() + box.getAbsoluteLeft(), rectangle.getY() + box.getAbsoluteTop(), rectangle.getWidth(), rectangle.getHeight());
	}

	private Caret getCaretForText(final Graphics graphics, final TextContent box, final int offset) {
		if (box.getStartOffset() > offset || box.getEndOffset() < offset) {
			return null;
		}
		final Rectangle relativeArea = box.getPositionArea(graphics, offset);
		final Rectangle area = new Rectangle(relativeArea.getX() + box.getAbsoluteLeft(), relativeArea.getY() + box.getAbsoluteTop(), relativeArea.getWidth(), relativeArea.getHeight());
		final FontSpec font = box.getFont();
		final String character = box.getText().substring(offset - box.getStartOffset(), offset - box.getStartOffset() + 1);
		return new TextCaret(area, font, character, false);
	}

	private static String getNodeStartMarker(final INode node) {
		return node.accept(new BaseNodeVisitorWithResult<String>() {
			@Override
			public String visit(final IDocument document) {
				return "DOCUMENT";
			}

			@Override
			public String visit(final IElement element) {
				return "<" + element.getPrefixedName() + "...";
			}

			@Override
			public String visit(final IComment comment) {
				return "<!--";
			}

			@Override
			public String visit(final IProcessingInstruction pi) {
				return "<?" + pi.getTarget() + "...";
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
				return "DOCUMENT";
			}

			@Override
			public String visit(final IElement element) {
				return "</" + element.getPrefixedName() + ">";
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

	private static void drawTag(final Graphics graphics, final String text, final int x, final int y, final boolean verticallyCentered) {
		final int textWidth = graphics.stringWidth(text);
		final int textHeight = graphics.getFontMetrics().getHeight();
		final int textPadding = 3;
		final int effectiveY;
		if (verticallyCentered) {
			effectiveY = y - (textHeight + textPadding * 2) / 2;
		} else {
			effectiveY = y;
		}
		graphics.fillRect(x, effectiveY, textWidth + textPadding * 2, textHeight + textPadding * 2);
		graphics.drawString(text, x + textPadding, effectiveY + textPadding);
	}

	private static interface Caret {
		Rectangle getArea();

		void paint(Graphics graphics);
	}

	private static class InsertBeforeNodeCaret implements Caret {
		private final Rectangle area;
		private final INode node;

		public InsertBeforeNodeCaret(final Rectangle area, final INode node) {
			this.area = area;
			this.node = node;
		}

		@Override
		public Rectangle getArea() {
			return area;
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(BACKGROUND_COLOR);
			graphics.setForeground(foregroundColor);
			graphics.setBackground(backgroundColor);

			final int x = area.getX();
			final int y = area.getY();

			graphics.fillRect(x, y, area.getWidth(), 2);
			graphics.fillRect(x, y, 2, area.getHeight());

			graphics.setCurrentFont(graphics.getFont(FONT));
			final String nodeName = getNodeStartMarker(node);
			drawTag(graphics, nodeName, x + 5, y + 5, false);
		}

	}

	private static class AppendNodeWithTextCaret implements Caret {
		private final Rectangle area;
		private final INode node;
		private final boolean nodeIsEmpty;

		public AppendNodeWithTextCaret(final Rectangle area, final INode node, final boolean nodeIsEmpty) {
			this.area = area;
			this.node = node;
			this.nodeIsEmpty = nodeIsEmpty;
		}

		@Override
		public Rectangle getArea() {
			return area;
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(BACKGROUND_COLOR);
			graphics.setForeground(foregroundColor);
			graphics.setBackground(backgroundColor);

			final int x = nodeIsEmpty ? area.getX() : area.getX() + area.getWidth();
			final int y = area.getY();

			graphics.fillRect(x, y, 2, area.getHeight());

			graphics.setCurrentFont(graphics.getFont(FONT));
			final String nodeName = getNodeEndMarker(node);
			drawTag(graphics, nodeName, x + 5, y + area.getHeight() / 2, true);
		}

	}

	private static class AppendStructuralNodeCaret implements Caret {
		private final Rectangle area;
		private final INode node;

		public AppendStructuralNodeCaret(final Rectangle area, final INode node) {
			this.area = area;
			this.node = node;
		}

		@Override
		public Rectangle getArea() {
			return area;
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(BACKGROUND_COLOR);
			graphics.setForeground(foregroundColor);
			graphics.setBackground(backgroundColor);

			final int x = area.getX();
			final int y = area.getY() + area.getHeight();

			graphics.fillRect(x, y, area.getWidth(), 2);
			graphics.fillRect(x + area.getWidth() - 2, y, 2, -area.getHeight());

			graphics.setCurrentFont(graphics.getFont(FONT));
			final String nodeName = getNodeEndMarker(node);
			drawTag(graphics, nodeName, x + 5, y + 5, false);
		}
	}

	private static class TextCaret implements Caret {
		private final Rectangle area;
		private final FontSpec font;
		private final String character;
		private final boolean overwrite;

		public TextCaret(final Rectangle area, final FontSpec font, final String character, final boolean overwrite) {
			this.area = area;
			this.font = font;
			this.character = character;
			this.overwrite = overwrite;
		}

		@Override
		public Rectangle getArea() {
			return area;
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			graphics.setForeground(graphics.getColor(FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(BACKGROUND_COLOR));

			if (overwrite) {
				graphics.fillRect(area.getX(), area.getY(), area.getWidth(), area.getHeight());
				graphics.setCurrentFont(graphics.getFont(font));
				graphics.drawString(character, area.getX(), area.getY());
			} else {
				graphics.fillRect(area.getX() - 1, area.getY(), 2, area.getHeight());
			}
		}
	}
}
