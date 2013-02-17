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

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.IFilter;

/**
 * This class provides several useful node filters which can for example be used on axes.
 * 
 * @author Florian Thienel
 * @see IFilter
 * @see IAxis#matching(IFilter)
 */
public final class Filters {

	/**
	 * This filter matches only element nodes.
	 * 
	 * @see IElement
	 */
	public static IFilter<INode> elements() {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return node instanceof IElement;
			}
		};
	}

	/**
	 * This filter matches only element nodes with the given name.
	 * 
	 * @param localName
	 *            the local name to match
	 */
	public static IFilter<INode> elementsNamed(final String localName) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return node.accept(new BaseNodeVisitorWithResult<Boolean>(false) {
					@Override
					public Boolean visit(final IElement element) {
						return element.getLocalName().equals(localName);
					}
				});
			}
		};
	}

	/**
	 * This filter matches only element nodes with the given name.
	 * 
	 * @param name
	 *            the qualified name to match
	 */
	public static IFilter<INode> elementsNamed(final QualifiedName name) {
		return new IFilter<INode>() {
			public boolean matches(final INode node) {
				return node.accept(new BaseNodeVisitorWithResult<Boolean>(false) {
					@Override
					public Boolean visit(final IElement element) {
						return element.getQualifiedName().equals(name);
					}
				});
			}
		};
	}

	private Filters() {
	}
}
