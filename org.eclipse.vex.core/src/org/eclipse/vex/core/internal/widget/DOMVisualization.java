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

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitor;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.boxes.InlineFrame;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.cursor.ContentTopology;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.visualization.IBoxModelBuilder;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * @author Florian Thienel
 */
public class DOMVisualization {

	private final ContentTopology contentTopology = new ContentTopology();
	private final Cursor cursor;
	private final BoxView view;

	private IBoxModelBuilder boxModelBuilder;
	private IDocument document;

	public DOMVisualization(final Cursor cursor, final BoxView view) {
		this.cursor = cursor;
		this.view = view;
	}

	public void setDocument(final IDocument document) {
		this.document = document;
		buildAll();
	}

	public void setBoxModelBuilder(final IBoxModelBuilder boxModelBuilder) {
		Assert.isNotNull(boxModelBuilder);
		this.boxModelBuilder = boxModelBuilder;
		buildAll();
	}

	public void buildAll() {
		if (boxModelBuilder == null) {
			return;
		}

		final RootBox rootBox = boxModelBuilder.visualizeRoot(document);

		contentTopology.setRootBox(rootBox);
		cursor.setRootBox(rootBox);
		view.setRootBox(rootBox);
	}

	public void rebuildStructure(final INode node) {
		final Collection<IContentBox> modifiedBoxes = contentTopology.findBoxesForNode(node);
		if (modifiedBoxes.isEmpty()) {
			return;
		}
		final IBox parent = getCommonParent(modifiedBoxes);

		replaceModifiedBoxesWithRebuiltVisualization(parent, modifiedBoxes, node);

		view.invalidateLayout(parent);
	}

	private void replaceModifiedBoxesWithRebuiltVisualization(final IBox parent, final Collection<IContentBox> modifiedBoxes, final INode node) {
		parent.accept(new BaseBoxVisitor() {
			@Override
			public void visit(final RootBox box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final VerticalBlock box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final StructuralFrame box) {
				box.setComponent(boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final StructuralNodeReference box) {
				box.setComponent(boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final Paragraph box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeInline(node));
			}

			@Override
			public void visit(final InlineNodeReference box) {
				box.setComponent(boxModelBuilder.visualizeInline(node));
			}

			@Override
			public void visit(final InlineContainer box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeInline(node));
			}

			@Override
			public void visit(final InlineFrame box) {
				box.setComponent(boxModelBuilder.visualizeInline(node));
			}
		});
	}

	private IBox getCommonParent(final Collection<IContentBox> boxes) {
		IBox parent = null;
		for (final IContentBox box : boxes) {
			if (parent == null) {
				parent = box.getParent();
			} else {
				Assert.isTrue(parent == box.getParent(), "The modified boxes do not have a common parent box.");
			}
		}
		return parent;
	}

	public void rebuildContentRange(final INode node, final ContentRange modifiedRange) {
		final IContentBox modifiedBox = contentTopology.findBoxForRange(modifiedRange);
		Assert.isNotNull(modifiedBox, "No box found for range " + modifiedRange);

		modifiedBox.accept(new BaseBoxVisitor() {
			@Override
			public void visit(final StructuralNodeReference box) {
				rebuildStructure(node);
			}

			@Override
			public void visit(final InlineNodeReference box) {
				rebuildStructure(node);
			}

			@Override
			public void visit(final TextContent box) {
				view.invalidateLayout(modifiedBox);
			}
		});
	}
}
