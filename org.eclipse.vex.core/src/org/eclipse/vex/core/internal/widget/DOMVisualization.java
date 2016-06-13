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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitor;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.boxes.InlineFrame;
import org.eclipse.vex.core.internal.boxes.InlineNodeReference;
import org.eclipse.vex.core.internal.boxes.List;
import org.eclipse.vex.core.internal.boxes.ListItem;
import org.eclipse.vex.core.internal.boxes.NodeEndOffsetPlaceholder;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.StructuralFrame;
import org.eclipse.vex.core.internal.boxes.StructuralNodeReference;
import org.eclipse.vex.core.internal.boxes.Table;
import org.eclipse.vex.core.internal.boxes.TableCell;
import org.eclipse.vex.core.internal.boxes.TableColumnSpec;
import org.eclipse.vex.core.internal.boxes.TableRow;
import org.eclipse.vex.core.internal.boxes.TableRowGroup;
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
		if (document == null) {
			return;
		}

		final RootBox rootBox = boxModelBuilder.visualizeRoot(document);

		contentTopology.setRootBox(rootBox);
		cursor.setRootBox(rootBox);
		view.setRootBox(rootBox);
	}

	public void rebuildStructure(final INode node) {
		final Collection<IContentBox> boxesToReplace = contentTopology.findBoxesForNode(node);
		final Collection<IBox> affectedParents = parents(boxesToReplace);
		if (affectedParents.size() > 1) {
			rebuildStructure(node.getParent());
			return;
		}
		final IBox parentBox = affectedParents.iterator().next();

		replaceModifiedBoxesWithRebuiltVisualization(parentBox, boxesToReplace, node);
		view.invalidateLayout(parentBox);
	}

	private static Set<IBox> parents(final Collection<IContentBox> boxes) {
		final Set<IBox> parents = new HashSet<IBox>();
		for (final IContentBox box : boxes) {
			final IBox parent = box.getParent();
			if (parent != null) {
				parents.add(parent);
			}
		}
		return parents;
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
			public void visit(final ListItem box) {
				box.setComponent(boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final List box) {
				box.setComponent(boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final Table box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final TableRowGroup box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final TableColumnSpec box) {
				box.setComponent(boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final TableRow box) {
				box.replaceChildren(modifiedBoxes, boxModelBuilder.visualizeStructure(node));
			}

			@Override
			public void visit(final TableCell box) {
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
				if (modifiedRange.getStartOffset() == modifiedBox.getStartOffset() || modifiedRange.getEndOffset() == modifiedBox.getEndOffset()) {
					rebuildStructure(node);
				} else {
					view.invalidateLayout(modifiedBox);
				}
			}

			@Override
			public void visit(final NodeEndOffsetPlaceholder box) {
				view.invalidateLayout(modifiedBox);
			}
		});
	}
}
