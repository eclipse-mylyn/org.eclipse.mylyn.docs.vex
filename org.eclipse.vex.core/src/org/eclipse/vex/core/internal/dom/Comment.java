/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

/**
 * @author Florian Thienel
 */
public class Comment extends Node {

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		sb.append("Comment (");
		if (isAssociated()) {
			sb.append(getStartOffset());
			sb.append(",");
			sb.append(getEndOffset());
		} else {
			sb.append("n/a");
		}
		sb.append(")");

		return sb.toString();
	}

}
