package org.eclipse.vex.core.internal.widget;

import java.util.LinkedList;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.cursor.ContentTopology;
import org.eclipse.vex.core.internal.cursor.ICursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.cursor.ICursorPositionListener;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;

public class FakeCursor implements ICursor {

	private final LinkedList<ICursorPositionListener> cursorPositionListeners = new LinkedList<ICursorPositionListener>();
	private final Graphics graphics = new FakeGraphics();
	private final ContentTopology contentTopology;

	private int offset;
	private boolean hasSelection;
	private int selectionStartOffset;
	private int selectionEndOffset;

	public FakeCursor(final IDocument document) {
		contentTopology = new ContentTopology() {
			@Override
			public int getLastOffset() {
				return document.getEndOffset();
			}
		};
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public boolean hasSelection() {
		return hasSelection;
	}

	@Override
	public ContentRange getSelectedRange() {
		if (hasSelection) {
			return new ContentRange(selectionStartOffset, selectionEndOffset);
		} else {
			return new ContentRange(offset, offset);
		}
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
			listener.positionChanged(offset);
		}
	}

	private void firePositionAboutToChange() {
		for (final ICursorPositionListener listener : cursorPositionListeners) {
			listener.positionAboutToChange();
		}
	}

	@Override
	public void move(final ICursorMove move) {
		firePositionAboutToChange();
		offset = move.calculateNewOffset(graphics, contentTopology, offset, null, null, 0);
		hasSelection = false;
		firePositionChanged(offset);
	}

	@Override
	public void select(final ICursorMove move) {
		firePositionAboutToChange();
		if (!hasSelection) {
			selectionStartOffset = offset;
		}
		offset = move.calculateNewOffset(graphics, contentTopology, offset, null, null, 0);
		hasSelection = true;
		selectionEndOffset = offset;
		firePositionChanged(offset);
	}

}
