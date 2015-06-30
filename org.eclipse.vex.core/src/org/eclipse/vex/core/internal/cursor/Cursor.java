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
package org.eclipse.vex.core.internal.cursor;

import java.util.LinkedList;

import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentRange;
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

	public static final int CARET_BUFFER = 30;
	private static final Color CARET_FOREGROUND_COLOR = new Color(255, 255, 255);
	private static final Color CARET_BACKGROUND_COLOR = new Color(0, 0, 0);
	private static final Color SELECTION_FOREGROUND_COLOR = new Color(255, 255, 255);
	private static final Color SELECTION_BACKGROUND_COLOR = new Color(0, 0, 255);
	private static final FontSpec FONT = new FontSpec("Arial", FontSpec.BOLD, 10.0f);

	private final ContentMap contentMap = new ContentMap();
	private final IContentSelector selector;
	private final LinkedList<MoveWithSelection> moves = new LinkedList<MoveWithSelection>();

	private int offset;
	private Caret caret;
	private IContentBox box;
	private int preferredX;
	private boolean preferX;

	public Cursor(final IContentSelector selector) {
		this.selector = selector;
	}

	public void setRootBox(final RootBox rootBox) {
		contentMap.setRootBox(rootBox);
	}

	public int getOffset() {
		return offset;
	}

	public int getDeltaIntoVisibleArea(final int top, final int height) {
		final Rectangle caretArea = getVisibleArea();
		if (caretArea.getY() + caretArea.getHeight() > top + height) {
			return caretArea.getY() + caretArea.getHeight() - top - height;
		}
		if (caretArea.getY() < top) {
			return caretArea.getY() - top;
		}
		return 0;
	}

	private Rectangle getVisibleArea() {
		if (caret == null) {
			return Rectangle.NULL;
		}
		return caret.getVisibleArea();
	}

	public void move(final ICursorMove move) {
		moves.add(new MoveWithSelection(move, false));
	}

	public void select(final ICursorMove move) {
		moves.add(new MoveWithSelection(move, true));
	}

	public void reconcile(final Graphics graphics) {
		preferX = true;
		applyCaretForPosition(graphics, offset);
	}

	public void applyMoves(final Graphics graphics) {
		for (MoveWithSelection move = moves.poll(); move != null; move = moves.poll()) {
			offset = move.move.calculateNewOffset(graphics, contentMap, offset, box, getHotArea(), preferredX);
			if (move.select) {
				if (move.move.isAbsolute()) {
					selector.endAt(offset);
				} else {
					selector.moveTo(offset);
				}
				offset = selector.getCaretOffset();
			} else {
				selector.setMark(offset);
			}
			preferX = move.move.preferX();
			applyCaretForPosition(graphics, offset);
		}
	}

	private Rectangle getHotArea() {
		if (caret == null) {
			return Rectangle.NULL;
		}
		return caret.getHotArea();
	}

	public void paint(final Graphics graphics) {
		applyCaretForPosition(graphics, offset);
		paintSelection(graphics);
		caret.paint(graphics);
	}

	private void applyCaretForPosition(final Graphics graphics, final int offset) {
		box = contentMap.findBoxForPosition(offset);
		if (box == null) {
			return;
		}
		caret = getCaretForBox(graphics, box, offset);
		if (preferX) {
			preferredX = caret.getHotArea().getX();
		}
	}

	private void paintSelection(final Graphics graphics) {
		if (!selector.isActive()) {
			return;
		}
		final ContentRange selectedRange = selector.getRange();
		final IBox selectionRootBox = contentMap.findBoxForRange(selectedRange);
		selectionRootBox.accept(new DepthFirstTraversal<Object>() {
			@Override
			public Object visit(final NodeReference box) {
				super.visit(box);
				if (selectedRange.contains(box.getRange())) {
					box.highlight(graphics, SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
				}
				return null;
			}

			@Override
			public Object visit(final TextContent box) {
				if (selectedRange.intersects(box.getRange())) {
					box.highlight(graphics, selectedRange.getStartOffset(), selectedRange.getEndOffset(), SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
				}
				return null;
			}
		});
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
		Rectangle getHotArea();

		Rectangle getVisibleArea();

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
		public Rectangle getHotArea() {
			return new Rectangle(area.getX(), area.getY(), 1, 1);
		}

		@Override
		public Rectangle getVisibleArea() {
			return new Rectangle(area.getX(), area.getY(), area.getWidth(), CARET_BUFFER);
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(CARET_FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(CARET_BACKGROUND_COLOR);
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
		public Rectangle getHotArea() {
			return new Rectangle(getX(), area.getY(), 1, area.getHeight());
		}

		@Override
		public Rectangle getVisibleArea() {
			return area;
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(CARET_FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(CARET_BACKGROUND_COLOR);
			graphics.setForeground(foregroundColor);
			graphics.setBackground(backgroundColor);

			final int x = getX();
			final int y = area.getY();

			graphics.fillRect(x, y, 2, area.getHeight());

			graphics.setCurrentFont(graphics.getFont(FONT));
			final String nodeName = getNodeEndMarker(node);
			drawTag(graphics, nodeName, x + 5, y + area.getHeight() / 2, true);
		}

		private int getX() {
			return nodeIsEmpty ? area.getX() : area.getX() + area.getWidth();
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
		public Rectangle getHotArea() {
			return new Rectangle(area.getX() + area.getWidth() - 1, area.getY() + area.getHeight() - 1, 1, 1);
		}

		@Override
		public Rectangle getVisibleArea() {
			return new Rectangle(area.getX(), area.getY() + area.getHeight(), area.getWidth(), CARET_BUFFER);
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			final ColorResource foregroundColor = graphics.getColor(CARET_FOREGROUND_COLOR);
			final ColorResource backgroundColor = graphics.getColor(CARET_BACKGROUND_COLOR);
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
		public Rectangle getHotArea() {
			return area;
		}

		@Override
		public Rectangle getVisibleArea() {
			return new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight() + 2);
		}

		@Override
		public void paint(final Graphics graphics) {
			if (area == Rectangle.NULL) {
				return;
			}

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			if (overwrite) {
				graphics.fillRect(area.getX(), area.getY(), area.getWidth(), area.getHeight());
				graphics.setCurrentFont(graphics.getFont(font));
				graphics.drawString(character, area.getX(), area.getY());
			} else {
				graphics.fillRect(area.getX() - 1, area.getY(), 2, area.getHeight());
			}
		}
	}

	private static class MoveWithSelection {
		public final ICursorMove move;
		public final boolean select;

		public MoveWithSelection(final ICursorMove move, final boolean select) {
			this.move = move;
			this.select = select;
		}
	}
}
