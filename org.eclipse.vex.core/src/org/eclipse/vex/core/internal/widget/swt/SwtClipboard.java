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

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.vex.core.internal.io.XMLFragment;
import org.eclipse.vex.core.internal.widget.IClipboard;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;

public class SwtClipboard implements IClipboard {

	private final Clipboard clipboard;

	public SwtClipboard(final Display display) {
		clipboard = new Clipboard(display);
	}

	@Override
	public void dispose() {
		clipboard.dispose();
	}

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

		final String text = editor.getSelectedText();
		if (text.isEmpty()) {
			// Some elements (like XInclude) may not contain textual content.
			final Object[] data = { editor.getSelectedFragment() };
			final Transfer[] transfers = { DocumentFragmentTransfer.getInstance() };
			clipboard.setContents(data, transfers);
		} else {
			final Object[] data = { editor.getSelectedFragment(), editor.getSelectedText() };
			final Transfer[] transfers = { DocumentFragmentTransfer.getInstance(), TextTransfer.getInstance() };
			clipboard.setContents(data, transfers);
		}
	}

	@Override
	public boolean hasContent() {
		final TransferData[] availableTypes = clipboard.getAvailableTypes();
		for (final TransferData availableType : availableTypes) {
			if (DocumentFragmentTransfer.getInstance().isSupportedType(availableType)) {
				return true;
			}
			if (TextTransfer.getInstance().isSupportedType(availableType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void paste(final IDocumentEditor editor) throws DocumentValidationException {
		final IDocumentFragment fragment = (IDocumentFragment) clipboard.getContents(DocumentFragmentTransfer.getInstance());
		if (fragment != null) {
			editor.insertXML(new XMLFragment(fragment).getXML());
		} else {
			pasteText(editor);
		}
	}

	@Override
	public boolean hasTextContent() {
		final TransferData[] availableTypes = clipboard.getAvailableTypes();
		for (final TransferData availableType : availableTypes) {
			if (TextTransfer.getInstance().isSupportedType(availableType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void pasteText(final IDocumentEditor editor) throws DocumentValidationException {
		final String text = (String) clipboard.getContents(TextTransfer.getInstance());
		if (text != null) {
			editor.insertText(text);
		}
	}

}
