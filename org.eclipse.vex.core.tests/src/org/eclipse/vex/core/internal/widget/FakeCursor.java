/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
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
	private final BalancingSelector selector;
	private final IViewPort viewPort = new FakeViewPort();

	private IDocument document;

	public FakeCursor(final IDocument document) {
		this.document = document;
		contentTopology = new ContentTopology() {
			@Override
			public int getLastOffset() {
				return FakeCursor.this.document.getEndOffset();
			}
		};
		selector = new BalancingSelector();
		selector.setDocument(document);
	}

	public void setDocument(final IDocument document) {
		this.document = document;
		selector.setDocument(document);
	}

	@Override
	public int getOffset() {
		return selector.getCaretOffset();
	}

	@Override
	public boolean hasSelection() {
		return selector.isActive();
	}

	@Override
	public ContentRange getSelectedRange() {
		if (selector.isActive()) {
			return selector.getRange();
		} else {
			return new ContentRange(selector.getCaretOffset(), selector.getCaretOffset());
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
		selector.setMark(move.calculateNewOffset(graphics, viewPort, contentTopology, selector.getCaretOffset(), null, null, 0));
		firePositionChanged(selector.getCaretOffset());
	}

	@Override
	public void select(final ICursorMove move) {
		firePositionAboutToChange();
		final int newOffset = move.calculateNewOffset(graphics, viewPort, contentTopology, selector.getCaretOffset(), null, null, 0);
		if (move.isAbsolute()) {
			selector.setEndAbsoluteTo(newOffset);
		} else {
			selector.moveEndTo(newOffset);
		}
		firePositionChanged(selector.getCaretOffset());
	}

}
