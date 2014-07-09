package org.eclipse.vex.ui.internal.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;

public class TextInputListener implements ITextInputListener {

	@Override
	public void inputDocumentAboutToBeChanged(final IDocument oldInput, final IDocument newInput) {
		// do nothing
	}

	@Override
	public void inputDocumentChanged(final IDocument oldInput, final IDocument newInput) {
		// TODO  Implement document update and switching.
		//		if ((fDesignViewer != null) && (newInput != null)) {
		//			fDesignViewer.setDocument(newInput);
		//		}
	}
}