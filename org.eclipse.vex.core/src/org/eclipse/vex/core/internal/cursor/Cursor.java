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
import org.eclipse.vex.core.internal.boxes.DepthFirstBoxTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.Image;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.NodeTag;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.Square;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.NodeGraphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class Cursor implements ICursor {

	public static final int CARET_BUFFER = 30;
	private static final Color CARET_FOREGROUND_COLOR = new Color(255, 255, 255);
	private static final Color CARET_BACKGROUND_COLOR = new Color(0, 0, 0);
	private static final Color SELECTION_FOREGROUND_COLOR = new Color(255, 255, 255);
	private static final Color SELECTION_BACKGROUND_COLOR = new Color(0, 0, 255);

	private final ContentTopology contentTopology = new ContentTopology();
	private final IContentSelector selector;
	private final IViewPort viewPort;

	private final LinkedList<MoveWithSelection> moves = new LinkedList<MoveWithSelection>();

	private int offset;
	private Caret caret;
	private IContentBox box;
	private int preferredX;
	private boolean preferX;

	private final LinkedList<ICursorPositionListener> cursorPositionListeners = new LinkedList<ICursorPositionListener>();

	public Cursor(final IContentSelector selector, final IViewPort viewPort) {
		this.selector = selector;
		this.viewPort = viewPort;
	}

	public void setRootBox(final RootBox rootBox) {
		contentTopology.setRootBox(rootBox);
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public boolean hasSelection() {
		return selector.isActive();
	}

	@Override
	public ContentRange getSelectedRange() {
		if (!hasSelection()) {
			return new ContentRange(offset, offset);
		}
		return selector.getRange();
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

	public Rectangle getCaretArea() {
		if (caret == null) {
			return Rectangle.NULL;
		}
		return caret.getHotArea();
	}

	@Override
	public void addPositionListener(final ICursorPositionListener listener) {
		cursorPositionListeners.add(listener);
	}

	@Override
	public void removePositionListener(final ICursorPositionListener listener) {
		cursorPositionListeners.remove(listener);
	}

	private void firePositionChanged(final int offset) {
		for (final ICursorPositionListener listener : cursorPositionListeners) {
			try {
				listener.positionChanged(offset);
			} catch (final Throwable t) {
				t.printStackTrace();
				// TODO remove listener?
			}
		}
	}

	private void firePositionAboutToChange() {
		for (final ICursorPositionListener listener : cursorPositionListeners) {
			try {
				listener.positionAboutToChange();
			} catch (final Throwable t) {
				t.printStackTrace();
				// TODO remove listener?
			}
		}
	}

	@Override
	public void move(final ICursorMove move) {
		moves.add(new MoveWithSelection(move, false));
		firePositionAboutToChange();
	}

	@Override
	public void select(final ICursorMove move) {
		moves.add(new MoveWithSelection(move, true));
		firePositionAboutToChange();
	}

	public void reconcile(final Graphics graphics) {
		preferX = true;
		applyCaretForPosition(graphics, offset);
	}

	public void applyMoves(final Graphics graphics) {
		for (MoveWithSelection move = moves.poll(); move != null; move = moves.poll()) {
			final int oldOffset = offset;
			offset = move.move.calculateNewOffset(graphics, viewPort, contentTopology, offset, box, getHotArea(), preferredX);
			if (move.select) {
				if (move.move.isAbsolute()) {
					selector.setEndAbsoluteTo(offset);
				} else {
					selector.moveEndTo(offset);
				}
				offset = selector.getCaretOffset();
			} else {
				selector.setMark(offset);
			}
			preferX = move.move.preferX();
			applyCaretForPosition(graphics, offset);

			if (oldOffset != offset) {
				firePositionChanged(offset);
			}
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
		if (caret == null) {
			return;
		}

		paintSelection(graphics);
		caret.paint(graphics);
	}

	private void applyCaretForPosition(final Graphics graphics, final int offset) {
		box = contentTopology.findBoxForPosition(offset);
		if (box == null) {
			return;
		}

		caret = getCaretForBox(graphics, box, offset);
		if (preferX) {
			preferredX = caret.getHotArea().getX();
		}
	}

	private void paintSelection(final Graphics graphics) {
		if (!hasSelection()) {
			return;
		}
		final ContentRange selectedRange = selector.getRange();
		final IBox selectionRootBox = contentTopology.findBoxForRange(selectedRange);
		selectionRootBox.accept(new DepthFirstBoxTraversal<Object>() {
			@Override
			public Object visit(final StructuralNodeReference box) {
				if (selectedRange.contains(box.getRange())) {
					box.highlight(graphics, SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
					return null;
				}
				return super.visit(box);
			}

			@Override
			public Object visit(final InlineNodeReference box) {
				if (selectedRange.contains(box.getNode().getRange())) {
					box.highlight(graphics, SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
					return null;
				}
				return super.visit(box);
			}

			@Override
			public Object visit(final TextContent box) {
				if (selectedRange.intersects(box.getRange())) {
					box.highlight(graphics, selectedRange.getStartOffset(), selectedRange.getEndOffset(), SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
				}
				return null;
			}

			@Override
			public Object visit(final NodeEndOffsetPlaceholder box) {
				if (selectedRange.contains(box.getRange())) {
					box.highlight(graphics, SELECTION_FOREGROUND_COLOR, SELECTION_BACKGROUND_COLOR);
					return null;
				}
				return null;
			}
		});
	}

	private Caret getCaretForBox(final Graphics graphics, final IContentBox box, final int offset) {
		return box.accept(new BaseBoxVisitorWithResult<Caret>() {
			@Override
			public Caret visit(final StructuralNodeReference box) {
				return getCaretForStructuralNode(graphics, box, offset);
			}

			@Override
			public Caret visit(final InlineNodeReference box) {
				return getCaretForInlineNode(graphics, box, offset);
			}

			@Override
			public Caret visit(final TextContent box) {
				return getCaretForText(graphics, box, offset);
			}

			@Override
			public Caret visit(final NodeEndOffsetPlaceholder box) {
				return getCaretForEndOffsetPlaceholder(graphics, box, offset);
			}
		});
	}

	private Caret getCaretForStructuralNode(final Graphics graphics, final StructuralNodeReference box, final int offset) {
		final Rectangle area = getAbsolutePositionArea(graphics, box, offset);
		if (box.isAtStart(offset)) {
			return new InsertBeforeStructuralNodeCaret(area, box.getNode());
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
			public Rectangle visit(final StructuralNodeReference box) {
				if (box.isAtStart(offset)) {
					return makeAbsolute(box.getPositionArea(graphics, offset), box);
				} else if (box.isAtEnd(offset) && box.canContainText() && !box.isEmpty()) {
					final int lastOffset = offset - 1;
					final IContentBox lastBox = contentTopology.findBoxForPosition(lastOffset, box);
					return makeAbsolute(lastBox.getPositionArea(graphics, lastOffset), lastBox);
				} else if (box.isAtEnd(offset) && box.canContainText() && box.isEmpty()) {
					final IBox lastLowestChild = findDeepestLastInlineChildBox(box);
					if (lastLowestChild != null) {
						return new Rectangle(lastLowestChild.getAbsoluteLeft(), lastLowestChild.getAbsoluteTop(), lastLowestChild.getWidth(), lastLowestChild.getHeight());
					} else {
						return makeAbsolute(box.getPositionArea(graphics, offset), box);
					}
				} else if (box.isAtEnd(offset)) {
					return makeAbsolute(box.getPositionArea(graphics, offset), box);
				} else {
					return Rectangle.NULL;
				}
			}

			@Override
			public Rectangle visit(final InlineNodeReference box) {
				if (box.isAtStart(offset)) {
					return makeAbsolute(box.getPositionArea(graphics, offset), box);
				} else if (box.isAtEnd(offset) && box.canContainText() && !box.isEmpty()) {
					final int lastOffset = offset - 1;
					final IContentBox lastBox = contentTopology.findBoxForPosition(lastOffset, box);
					return getAbsolutePositionArea(graphics, lastBox, lastOffset);
				} else if (box.isAtEnd(offset) && box.canContainText() && box.isEmpty()) {
					final IBox lastLowestChild = findDeepestLastInlineChildBox(box);
					if (lastLowestChild != null) {
						return makeAbsolute(lastLowestChild.getBounds(), lastLowestChild);
					} else {
						return makeAbsolute(box.getPositionArea(graphics, offset), box);
					}
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

			@Override
			public Rectangle visit(final NodeEndOffsetPlaceholder box) {
				return makeAbsolute(box.getPositionArea(graphics, offset), box);
			}
		});
	}

	private static Rectangle makeAbsolute(final Rectangle rectangle, final IBox box) {
		return new Rectangle(rectangle.getX() + box.getAbsoluteLeft(), rectangle.getY() + box.getAbsoluteTop(), rectangle.getWidth(), rectangle.getHeight());
	}

	private static IInlineBox findDeepestLastInlineChildBox(final IBox startBox) {
		final IInlineBox[] deepestLastChildBox = new IInlineBox[1];
		startBox.accept(new DepthFirstBoxTraversal<IBox>() {
			@Override
			public IBox visit(final Square box) {
				deepestLastChildBox[0] = box;
				return null;
			}

			@Override
			public IBox visit(final StaticText box) {
				deepestLastChildBox[0] = box;
				return null;
			}

			@Override
			public IBox visit(final Image box) {
				deepestLastChildBox[0] = box;
				return null;
			}

			@Override
			public IBox visit(final TextContent box) {
				deepestLastChildBox[0] = box;
				return null;
			}

			@Override
			public IBox visit(final NodeEndOffsetPlaceholder box) {
				deepestLastChildBox[0] = box;
				return null;
			}

			@Override
			public IBox visit(final NodeTag box) {
				deepestLastChildBox[0] = box;
				return null;
			}
		});
		return deepestLastChildBox[0];
	}

	private Caret getCaretForInlineNode(final Graphics graphics, final InlineNodeReference box, final int offset) {
		final Rectangle area = getAbsolutePositionArea(graphics, box, offset);
		if (box.getNode().getStartOffset() == offset) {
			return new InsertBeforeInlineNodeCaret(area, box.getNode());
		} else if (box.getNode().getEndOffset() == offset) {
			return new AppendNodeWithTextCaret(area, box.getNode(), box.isEmpty() && box.canContainText());
		} else {
			return new IntermediateInlineCaret(area, box.isAtStart(offset));
		}
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

	private Caret getCaretForEndOffsetPlaceholder(final Graphics graphics, final NodeEndOffsetPlaceholder box, final int offset) {
		final Rectangle area = getAbsolutePositionArea(graphics, box, offset);
		return new AppendNodeWithTextCaret(area, box.getNode(), box.isEmpty());
	}

	private static interface Caret {
		Rectangle getHotArea();

		Rectangle getVisibleArea();

		void paint(Graphics graphics);
	}

	private static class InsertBeforeStructuralNodeCaret implements Caret {
		private final Rectangle area;
		private final INode node;

		public InsertBeforeStructuralNodeCaret(final Rectangle area, final INode node) {
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

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			final int x = area.getX();
			final int y = area.getY();

			graphics.swapColors();
			graphics.fillRect(x - 2, y - 2, area.getWidth(), 6);
			graphics.fillRect(x - 2, y - 2, 6, area.getHeight());
			graphics.swapColors();

			graphics.fillRect(x, y, area.getWidth(), 2);
			graphics.fillRect(x, y, 2, area.getHeight());

			NodeGraphics.drawStartTag(graphics, node, x + 5, y + 5, false, false);
		}

	}

	private static class InsertBeforeInlineNodeCaret implements Caret {
		private final Rectangle area;
		private final INode node;

		public InsertBeforeInlineNodeCaret(final Rectangle area, final INode node) {
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

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			final int x = area.getX();
			final int y = area.getY();

			graphics.fillRect(x, y, 2, area.getHeight());

			NodeGraphics.drawStartTag(graphics, node, x + 5, y + area.getHeight() / 2, true, false);
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

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			final int x = getX();
			final int y = area.getY();

			graphics.fillRect(x, y, 2, area.getHeight());

			NodeGraphics.drawEndTag(graphics, node, x + 5, y + area.getHeight() / 2, true, false);
		}

		private int getX() {
			return nodeIsEmpty ? area.getX() : area.getX() + area.getWidth();
		}
	}

	private static class IntermediateInlineCaret implements Caret {
		private final Rectangle area;
		private final boolean isAtBoxStart;

		public IntermediateInlineCaret(final Rectangle area, final boolean isAtBoxStart) {
			this.area = area;
			this.isAtBoxStart = isAtBoxStart;
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

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			final int x = getX();
			final int y = area.getY();

			graphics.fillRect(x, y, 2, area.getHeight());
		}

		private int getX() {
			return isAtBoxStart ? area.getX() : area.getX() + area.getWidth();
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

			graphics.setForeground(graphics.getColor(CARET_FOREGROUND_COLOR));
			graphics.setBackground(graphics.getColor(CARET_BACKGROUND_COLOR));

			final int x = area.getX();
			final int y = area.getY() + area.getHeight();

			graphics.swapColors();
			graphics.fillRect(x - 2, y - 2, area.getWidth(), 6);
			graphics.fillRect(x - 2, y - 2, 6, area.getHeight());
			graphics.swapColors();

			graphics.fillRect(x, y, area.getWidth(), 2);
			graphics.fillRect(x + area.getWidth() - 2, y, 2, -area.getHeight());

			NodeGraphics.drawEndTag(graphics, node, x + 5, y + 5, false, false);
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
