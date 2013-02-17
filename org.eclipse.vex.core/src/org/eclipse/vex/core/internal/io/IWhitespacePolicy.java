/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - a NULL object
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import org.eclipse.vex.core.provisional.dom.INode;


/**
 * Determines whitespace policy for document elements. For example, a CSS stylesheet implements a whitespace policy via
 * its display and white-space properties.
 */
public interface IWhitespacePolicy {

	/**
	 * A NULL object of this type. No blocks and no pre elements.
	 */
	IWhitespacePolicy NULL = new IWhitespacePolicy() {
		public boolean isBlock(final INode node) {
			return false;
		}

		public boolean isPre(final INode node) {
			return false;
		}
	};

	/**
	 * Returns true if the given element is normally block-formatted.
	 * 
	 * @param element
	 *            Element to test.
	 */
	boolean isBlock(INode node);

	/**
	 * Returns true if the given element is pre-formatted, that is, all of its contained whitespace should be preserved.
	 * 
	 * @param element
	 *            Element to test.
	 */
	boolean isPre(INode node);
}
