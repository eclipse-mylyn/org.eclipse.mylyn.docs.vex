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
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.DummyValidator;

/**
 * A dummy validator tzo be used with layout tests
 */
public class LayoutTestValidator extends DummyValidator {

	final static List<String> nodesForInsertion = new ArrayList<String>();

	static {
		nodesForInsertion.add("p");
	};

	@Override
	public boolean isValidSequence(final QualifiedName element, final List<QualifiedName> sequence1, final List<QualifiedName> sequence2, final List<QualifiedName> sequence3, final boolean partial) {
		// Allow insertions in <p> element
		if (nodesForInsertion.contains(element.getLocalName())) {
			return true;
		}

		return false;
	}
}
