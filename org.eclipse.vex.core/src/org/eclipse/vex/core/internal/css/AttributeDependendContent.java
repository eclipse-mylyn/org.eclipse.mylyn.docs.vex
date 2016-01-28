/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.IElement;

public class AttributeDependendContent implements IPropertyContent {

	public final IElement element;
	public final QualifiedName attributeName;

	public AttributeDependendContent(final IElement element, final QualifiedName attributeName) {
		this.element = element;
		this.attributeName = attributeName;
	}

	@Override
	public <T> T accept(final IPropertyContentVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String toString() {
		final String attributeValue = element.getAttributeValue(attributeName);
		if (attributeValue != null) {
			return attributeValue;
		}
		return "";
	}

}
