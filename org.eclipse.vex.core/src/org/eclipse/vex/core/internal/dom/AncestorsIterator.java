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
package org.eclipse.vex.core.internal.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.dom.IParent;

/**
 * @author Florian Thienel
 */
public class AncestorsIterator implements Iterator<IParent> {

	private INode current;

	public AncestorsIterator(final INode startNode) {
		current = startNode;
	}

	public boolean hasNext() {
		return current.getParent() != null;
	}

	public IParent next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		current = current.getParent();
		return (IParent) current;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}