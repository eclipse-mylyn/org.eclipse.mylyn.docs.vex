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
package org.eclipse.vex.core.provisional.dom;

import java.util.List;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Represents a wellformed fragment of an XML document.
 * 
 * @author Florian Thienel
 */
public interface IDocumentFragment extends IParent {

	/**
	 * @return the length of the textual content of this fragment plus 1 for each opening or closing XML tag (element
	 *         tags, comment tags, PI tags and entity references)
	 */
	int getLength();

	/**
	 * @return a list with the qualified names off all nodes on the root level of this fragment
	 */
	List<QualifiedName> getNodeNames();

	/**
	 * @return all nodes on the root level of this fragment
	 */
	List<? extends INode> getNodes();

}