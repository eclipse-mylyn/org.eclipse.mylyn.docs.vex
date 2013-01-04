/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

/**
 * A representation of textual content of an XML document within the DOM. Text objects are not used in the internal
 * document structure; they are dynamically created as needed by the <code>Element.getChildNodes()</code> method.
 * 
 * @see Element#getChildNodes()
 */
public class Text extends Node {

	/**
	 * Create a new Text node for the given range in the given content. This constructor automatically associates the
	 * Text node with the given content and sets its parent.
	 * 
	 * @param parent
	 *            The parent node containing the text
	 * @param content
	 *            Content object containing the text
	 * @param startOffset
	 *            character offset of the start of the run
	 * @param endOffset
	 *            character offset of the end of the run
	 */
	public Text(final Parent parent, final Content content, final ContentRange range) {
		setParent(parent);
		associate(content, range);
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isKindOf(final Node node) {
		return false;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		sb.append("Text (");
		sb.append(getStartOffset());
		sb.append(",");
		sb.append(getEndOffset());
		sb.append(")");

		return sb.toString();
	}
}
