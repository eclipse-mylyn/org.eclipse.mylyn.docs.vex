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

import org.eclipse.vex.core.dom.ContentRange;
import org.eclipse.vex.core.dom.IContent;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.dom.INodeVisitor;
import org.eclipse.vex.core.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.dom.IText;

public class Text extends Node implements IText {

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
	public Text(final Parent parent, final IContent content, final ContentRange range) {
		setParent(parent);
		associate(content, range);
	}

	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	public boolean isKindOf(final INode node) {
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
