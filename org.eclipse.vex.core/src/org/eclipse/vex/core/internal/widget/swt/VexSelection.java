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
package org.eclipse.vex.core.internal.widget.swt;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class VexSelection implements IVexSelection {

	private final int caretOffset;
	private final ContentRange selectedRange;
	private final IDocumentFragment selectedFragment;
	private final INode currentNode;

	public VexSelection(final IDocumentEditor editor) {
		caretOffset = editor.getCaretPosition().getOffset();
		selectedRange = editor.getSelectedRange();
		selectedFragment = editor.getSelectedFragment();
		currentNode = editor.getCurrentNode();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public int getCaretOffset() {
		return caretOffset;
	}

	@Override
	public ContentRange getSelectedRange() {
		return selectedRange;
	}

	public INode getFirstElement() {
		if (selectedFragment == null) {
			return currentNode;
		}
		return selectedFragment.children().first();
	}

	public Iterator<INode> iterator() {
		if (selectedFragment == null) {
			return Collections.singletonList(currentNode).iterator();
		}
		return selectedFragment.children().iterator();
	}

	public int size() {
		if (selectedFragment == null) {
			return 1;
		}
		return selectedFragment.children().count();
	}

	public Object[] toArray() {
		return toList().toArray();
	}

	public List<INode> toList() {
		if (selectedFragment == null) {
			return Collections.singletonList(currentNode);
		}
		return selectedFragment.children().asList();
	}

}
