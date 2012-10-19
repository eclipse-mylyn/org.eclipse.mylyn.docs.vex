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
 * An incarantion of the <a href="http://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a> which handles the
 * nodes of the structural part of the DOM.
 * 
 * @author Florian Thienel
 * 
 */
public interface INodeVisitor {

	void visit(Document document);

	void visit(DocumentFragment fragment);

	void visit(Element element);

	void visit(Text text);

}
