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

import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.IParentBox;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.INode;

public class NodeVisualization<T extends IBox> extends BaseNodeVisitorWithResult<T> implements Comparable<NodeVisualization<?>> {
	private final int priority;
	private VisualizationChain chain;

	public NodeVisualization() {
		this(0);
	}

	public NodeVisualization(final int priority) {
		this.priority = priority;
	}

	public final T visualize(final INode node) {
		return node.accept(this);
	}

	@Override
	public final int compareTo(final NodeVisualization<?> other) {
		return other.priority - priority;
	}

	public final void setChain(final VisualizationChain chain) {
		this.chain = chain;
	}

	protected final <P extends IParentBox<IChildBox>> P visualizeChildrenStructure(final Iterable<INode> children, final P parentBox) {
		for (final INode child : children) {
			final IChildBox childBox = chain.visualizeStructure(child);
			if (childBox != null) {
				parentBox.appendChild(childBox);
			}
		}
		return parentBox;
	}

	protected final <P extends IParentBox<IInlineBox>> P visualizeChildrenInline(final Iterable<INode> children, final P parentBox) {
		for (final INode child : children) {
			final IInlineBox childBox = chain.visualizeInline(child);
			if (childBox != null) {
				parentBox.appendChild(childBox);
			}
		}
		return parentBox;
	}
}
