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
 * Represents a logical location in a document. As the document is modified, existing <code>Position</code> objects are
 * updated to reflect the appropriate character offset in the document.
 * 
 * Positions can be invalid if they were removed from their associated Content instance. Invalid positions do not get
 * updated on content modifications. They must not be used for anything anymore.
 */
public interface Position {

	static Position NULL = new Position() {
		public int getOffset() {
			return -1;
		}

		public boolean isValid() {
			return false;
		};
	};

	/**
	 * @return the character offset corresponding to the position.
	 */
	int getOffset();

	/**
	 * @return true if this position is still valid and actively maintained by its creator
	 */
	boolean isValid();

}
