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
package org.eclipse.vex.core.internal.visualization;

import java.util.TreeSet;

import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.provisional.dom.INode;

public final class VisualizationChain implements IBoxModelBuilder {
	private final TreeSet<NodeVisualization<RootBox>> rootChain = new TreeSet<NodeVisualization<RootBox>>();
	private final TreeSet<NodeVisualization<IStructuralBox>> structureChain = new TreeSet<NodeVisualization<IStructuralBox>>();
	private final TreeSet<NodeVisualization<IInlineBox>> inlineChain = new TreeSet<NodeVisualization<IInlineBox>>();

	@Override
	public RootBox visualizeRoot(final INode node) {
		final RootBox rootBox = visualize(node, rootChain);
		if (rootBox == null) {
			return new RootBox();
		}
		return rootBox;
	}

	@Override
	public IStructuralBox visualizeStructure(final INode node) {
		return visualize(node, structureChain);
	}

	@Override
	public IInlineBox visualizeInline(final INode node) {
		return visualize(node, inlineChain);
	}

	private static <T extends IBox> T visualize(final INode node, final TreeSet<NodeVisualization<T>> chain) {
		for (final NodeVisualization<T> visualization : chain) {
			final T box = visualization.visualize(node);
			if (box != null) {
				return box;
			}
		}
		return null;
	}

	public void addForRoot(final NodeVisualization<RootBox> visualization) {
		add(visualization, rootChain);
	}

	public void addForStructure(final NodeVisualization<IStructuralBox> visualization) {
		add(visualization, structureChain);
	}

	public void addForInline(final NodeVisualization<IInlineBox> visualization) {
		add(visualization, inlineChain);
	}

	private <T extends IBox> void add(final NodeVisualization<T> visualization, final TreeSet<NodeVisualization<T>> chain) {
		chain.add(visualization);
		visualization.setChain(this);
	}
}
