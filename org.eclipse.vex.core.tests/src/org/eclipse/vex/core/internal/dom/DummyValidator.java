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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.dom.IAttribute;
import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.IValidator;
import org.eclipse.vex.core.internal.validator.AttributeDefinition;

/**
 * @author Florian Thienel
 */
public class DummyValidator implements IValidator {

	public AttributeDefinition getAttributeDefinition(final IAttribute attribute) {
		return null;
	}

	public List<AttributeDefinition> getAttributeDefinitions(final IElement element) {
		return Collections.emptyList();
	}

	public Set<QualifiedName> getValidRootElements() {
		return Collections.emptySet();
	}

	public Set<QualifiedName> getValidItems(final IElement element) {
		return Collections.emptySet();
	}

	public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> nodes, final boolean partial) {
		return false;
	}

	public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3, final boolean partial) {
		return false;
	}

	public Set<String> getRequiredNamespaces() {
		return Collections.emptySet();
	}

}
