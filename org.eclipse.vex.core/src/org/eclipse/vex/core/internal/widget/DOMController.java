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

import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;

import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;

/**
 * @author Florian Thienel
 */
public class DOMController {

	private IDocument document;
	private final Cursor cursor;
	private final BoxView view;
	private final DOMVisualization visualization;

	private final IDocumentListener documentListener = new IDocumentListener() {
		@Override
		public void attributeChanged(final AttributeChangeEvent event) {
		}

		@Override
		public void namespaceChanged(final NamespaceDeclarationChangeEvent event) {
		}

		@Override
		public void beforeContentDeleted(final ContentChangeEvent event) {
		}

		@Override
		public void beforeContentInserted(final ContentChangeEvent event) {
		}

		@Override
		public void contentDeleted(final ContentChangeEvent event) {
		}

		@Override
		public void contentInserted(final ContentChangeEvent event) {
			if (event.isStructuralChange()) {
				visualization.rebuildStructure(event.getParent());
			} else {
				visualization.rebuildContentRange(event.getRange());
			}
		}
	};

	public DOMController(final Cursor cursor, final BoxView view) {
		this.cursor = cursor;
		this.view = view;
		visualization = new DOMVisualization(cursor, view);
	}

	public void setDocument(final IDocument document) {
		disconnectDocumentListener();
		this.document = document;
		connectDocumentListener();

		visualization.setDocument(document);
	}

	private void connectDocumentListener() {
		if (document != null) {
			document.addDocumentListener(documentListener);
		}
	}

	private void disconnectDocumentListener() {
		if (document != null) {
			document.removeDocumentListener(documentListener);
		}
	}

	public void setVisualizationChain(final VisualizationChain visualizationChain) {
		visualization.setVisualizationChain(visualizationChain);
	}

	public void moveCursor(final ICursorMove move) {
		cursor.move(move);
		view.invalidateCursor();
	}

	public void enterChar(final char c) {
		document.insertText(cursor.getOffset(), Character.toString(c));
		moveCursor(toOffset(cursor.getOffset() + 1));
	}

}
