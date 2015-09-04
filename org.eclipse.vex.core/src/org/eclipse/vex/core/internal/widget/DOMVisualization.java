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

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitor;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.cursor.ContentMap;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class DOMVisualization {

	private final ContentMap contentMap = new ContentMap();
	private final Cursor cursor;
	private final BoxView view;

	private VisualizationChain visualizationChain;
	private IDocument document;

	public DOMVisualization(final Cursor cursor, final BoxView view) {
		this.cursor = cursor;
		this.view = view;
	}

	public void setDocument(final IDocument document) {
		this.document = document;
		buildAll();
	}

	public void setVisualizationChain(final VisualizationChain visualizationChain) {
		Assert.isNotNull(visualizationChain);
		this.visualizationChain = visualizationChain;
		buildAll();
	}

	public void buildAll() {
		if (visualizationChain == null) {
			return;
		}

		RootBox rootBox = visualizationChain.visualizeRoot(document);
		if (rootBox == null) {
			rootBox = new RootBox();
		}

		contentMap.setRootBox(rootBox);
		cursor.setRootBox(rootBox);
		view.setRootBox(rootBox);
	}

	public void rebuildStructure(final INode node) {
		final IContentBox modifiedBox = contentMap.findBoxForRange(node.getRange());
		final IStructuralBox newBox = visualizationChain.visualizeStructure(node);
		final IStructuralBox newChildBox = newBox.accept(new BaseBoxVisitorWithResult<IStructuralBox>(newBox) {
			@Override
			public IStructuralBox visit(final StructuralNodeReference box) {
				return box.getComponent();
			}
		});
		modifiedBox.accept(new BaseBoxVisitor() {
			@Override
			public void visit(final StructuralNodeReference box) {
				box.setComponent(newChildBox);
			}
		});

		view.invalidateLayout(modifiedBox);
	}

	public void rebuildContentRange(final ContentRange range) {
		final IContentBox modifiedBox = contentMap.findBoxForRange(range);
		if (modifiedBox == null) {
			return;
		}
		includeGapsInTextContent(modifiedBox);

		view.invalidateLayout(modifiedBox);
	}

	private void includeGapsInTextContent(final IContentBox box) {
		box.accept(new DepthFirstTraversal<Object>() {
			private int lastEndOffset = box.getStartOffset();
			private TextContent lastTextContentBox;

			@Override
			public Object visit(final StructuralNodeReference box) {
				lastEndOffset = box.getStartOffset();
				box.getComponent().accept(this);

				if (lastTextContentBox != null && lastTextContentBox.getEndOffset() < box.getEndOffset() - 1) {
					lastTextContentBox.setEndOffset(box.getEndOffset() - 1);
				}

				lastEndOffset = box.getEndOffset();
				lastTextContentBox = null;
				return super.visit(box);
			}

			@Override
			public Object visit(final TextContent box) {
				if (box.getStartOffset() > lastEndOffset + 1) {
					box.setStartOffset(lastEndOffset + 1);
				}

				lastEndOffset = box.getEndOffset();
				lastTextContentBox = box;
				return super.visit(box);
			}
		});
	}

}
