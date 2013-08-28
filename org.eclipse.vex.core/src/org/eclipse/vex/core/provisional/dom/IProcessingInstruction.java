/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
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
 * A representation of an XML processing insctruction in the DOM. PI's have textual content, a start and an end tag.
 * 
 * @author Carsten Hiesserich
 */
public interface IProcessingInstruction extends INode {

	/**
	 * @return the target of this processing instruction .
	 */
	public String getTarget();

	/**
	 * Set the target of this processing instruction.
	 * 
	 * @param target
	 *            The new target.
	 * @throws DocumentValidationException
	 *             If the given String is no valid target.
	 */
	public void setTarget(String target) throws DocumentValidationException;
}