/*******************************************************************************
 * Copyright (c) 2014 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * A representation of an XML element that includes content.
 */
public interface IIncludeNode extends INode {
	/**
	 * @return The Element that defines this include in the source document.
	 */
	public IElement getReference();

	INode getResolved();

	INode getFallback();
}
