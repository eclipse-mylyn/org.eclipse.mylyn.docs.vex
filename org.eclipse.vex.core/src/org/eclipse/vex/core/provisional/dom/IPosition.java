/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - NULL object
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * Represents a logical location in a document. As the document is modified, existing <code>Position</code> objects are
 * updated to reflect the appropriate character offset in the document.
 * <p>
 * Positions can be invalid if they were removed from their associated Content instance. Invalid positions do not get
 * updated on content modifications. They must not be used for anything anymore.
 */
public interface IPosition extends Comparable<IPosition> {

	static IPosition NULL = new IPosition() {
		@Override
		public int getOffset() {
			return -1;
		}

		@Override
		public boolean isValid() {
			return false;
		};

		@Override
		public String toString() {
			return "NULL";
		}

		@Override
		public int compareTo(final IPosition other) {
			return -1;
		}
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
