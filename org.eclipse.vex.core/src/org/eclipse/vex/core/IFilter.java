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
package org.eclipse.vex.core;

/**
 * A simple filtering facility.
 *
 * @author Florian Thienel
 */
public interface IFilter<T> {

	/**
	 * @param t
	 *            the object to evaluate
	 * @return true if the given object matches this filter
	 */
	boolean matches(T t);
}
