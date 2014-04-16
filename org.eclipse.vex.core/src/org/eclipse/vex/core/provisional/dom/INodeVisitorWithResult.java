/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hieserich - added processing instruction and include
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * An incarantion of the <a href="http://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a> which handles the
 * nodes of the structural part of the DOM and is able to return a value of a certain type.
 *
 * @author Florian Thienel
 */
public interface INodeVisitorWithResult<T> {

	T visit(IDocument document);

	T visit(IDocumentFragment fragment);

	T visit(IElement element);

	T visit(IText text);

	T visit(IComment comment);

	T visit(IProcessingInstruction pi);

	T visit(IIncludeNode include);
}
