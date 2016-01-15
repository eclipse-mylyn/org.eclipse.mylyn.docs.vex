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

import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class InMemoryClipboard implements IClipboard {

	private IDocumentFragment content;

	@Override
	public void cutSelection(final IDocumentEditor editor) {
		copySelection(editor);
		editor.deleteSelection();
	}

	@Override
	public void copySelection(final IDocumentEditor editor) {
		if (!editor.hasSelection()) {
			return;
		}

		content = editor.getSelectedFragment();
	}

	@Override
	public boolean hasContent() {
		if (content == null) {
			return false;
		}
		return true;
	}

	@Override
	public void paste(final IDocumentEditor editor) throws DocumentValidationException {
		if (!hasContent()) {
			return;
		}

		editor.insertFragment(content);
	}

	@Override
	public boolean hasTextContent() {
		if (content == null) {
			return false;
		}
		final String text = content.getText();
		if (text == null || "".equals(text)) {
			return false;
		}
		return true;
	}

	@Override
	public void pasteText(final IDocumentEditor editor) throws DocumentValidationException {
		if (!hasTextContent()) {
			return;
		}

		editor.insertText(content.getText());
	}

}
