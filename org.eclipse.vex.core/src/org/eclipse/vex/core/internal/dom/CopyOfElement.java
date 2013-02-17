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

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IElement;

/**
 * This visitor copies the properties of a source element into the visited elements:
 * <ul>
 * <li>attributes</li>
 * <li>namespace declarations</li>
 * </ul>
 * 
 * @author Florian Thienel
 */
public class CopyOfElement extends BaseNodeVisitor {

	private final IElement source;

	/**
	 * @param source
	 *            the source element
	 */
	public CopyOfElement(final IElement source) {
		this.source = source;
	}

	@Override
	public void visit(final IElement element) {
		for (final IAttribute attribute : source.getAttributes()) {
			element.setAttribute(attribute.getQualifiedName(), attribute.getValue());
		}

		element.declareDefaultNamespace(source.getDeclaredDefaultNamespaceURI());

		for (final String prefix : source.getNamespacePrefixes()) {
			element.declareNamespace(prefix, source.getNamespaceURI(prefix));
		}
	}
}
