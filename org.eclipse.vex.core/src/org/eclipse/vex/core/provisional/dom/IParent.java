/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;


/**
 * A parent node is a node which can contain other nodes as children. This interface defines the tree-like structure of
 * the DOM. It handles the merging of the child nodes and the textual content of one node within the structure of the
 * document.
 * 
 * @author Florian Thienel
 */
public interface IParent extends INode {

	/**
	 * Indicates whether this parent node has child nodes, including text nodes.
	 * 
	 * @return true if this parent node has any child nodes
	 */
	public abstract boolean hasChildren();

	/**
	 * @return the children axis of this parent.
	 * @see IAxis
	 */
	public abstract IAxis<INode> children();

	/**
	 * Returns the child node which contains the given offset, or this node, if no child contains the offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the node at the given offset
	 */
	public abstract INode getChildAt(int offset);

	/**
	 * Returns the child node which contains the given position, or this node, if no child contains the position.
	 * 
	 * @param position
	 *            the position
	 * @return the node at the given position
	 */
	public abstract INode getChildAt(ContentPosition position);

}