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

/**
 * A representation of textual content of an XML document within the DOM. Text objects are not used in the internal
 * document structure; they are dynamically created as needed by the <code>IElement.children()</code> axis.
 *
 * @see IElement#children()
 */
public interface IText extends INode {

}