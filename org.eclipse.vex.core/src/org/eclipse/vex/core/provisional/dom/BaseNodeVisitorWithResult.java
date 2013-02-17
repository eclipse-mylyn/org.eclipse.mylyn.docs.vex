/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
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
 * This class provides default implementations for the methods defined by the <code>INodeVisitorWithResult</code>
 * interface. An overloaded variant of the constructor allows to define a default value which is returned by the
 * unimplemented visit methods.
 * 
 * @see INodeVisitorWithResult
 * @author Florian Thienel
 */
public class BaseNodeVisitorWithResult<T> implements INodeVisitorWithResult<T> {

	private final T defaultValue;

	public BaseNodeVisitorWithResult() {
		this(null);
	}

	public BaseNodeVisitorWithResult(final T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public T visit(final IDocument document) {
		return defaultValue;
	}

	public T visit(final IDocumentFragment fragment) {
		return defaultValue;
	}

	public T visit(final IElement element) {
		return defaultValue;
	}

	public T visit(final IText text) {
		return defaultValue;
	}

	public T visit(final IComment comment) {
		return defaultValue;
	}
}
