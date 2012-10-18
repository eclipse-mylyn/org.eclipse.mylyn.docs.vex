/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

/**
 * <code>Text</code> represents a run of text in a document. Text objects are not used in the internal document
 * structure; they are only returned as needed by the <code>Element.getContent</code> method.
 */
public class Text extends Node {

	/**
	 * Class constructor.
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
	public Text(final Parent parent, final Content content, final int startOffset, final int endOffset) {
		setParent(parent);
		associate(content, startOffset, endOffset);
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getBaseURI() {
		return null;
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
