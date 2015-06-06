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
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.cursor.IContentSelector;
import org.eclipse.vex.core.provisional.dom.ContentRange;

/**
 * @author Florian Thienel
 */
public abstract class BaseSelector implements IContentSelector {

	private int mark;
	private int startOffset;
	private int endOffset;
	private int caretOffset;

	protected final int getMark() {
		return mark;
	}

	public final void setMark(final int offset) {
		mark = offset;
		startOffset = mark;
		endOffset = mark;
		caretOffset = mark;
	}

	public final boolean isActive() {
		return mark != caretOffset;
	}

	public final int getStartOffset() {
		return startOffset;
	}

	protected final void setStartOffset(final int startOffset) {
		this.startOffset = startOffset;
	}

	public final int getEndOffset() {
		return endOffset;
	}

	protected final void setEndOffset(final int endOffset) {
		this.endOffset = endOffset;
	}

	public final ContentRange getRange() {
		return new ContentRange(startOffset, endOffset);
	}

	public final int getCaretOffset() {
		return caretOffset;
	}

	protected final void setCaretOffset(final int caretOffset) {
		this.caretOffset = caretOffset;
	}
}
