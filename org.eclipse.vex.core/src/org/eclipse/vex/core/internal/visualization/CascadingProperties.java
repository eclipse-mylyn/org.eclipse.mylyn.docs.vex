/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.visualization;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Florian Thienel
 */
public class CascadingProperties {

	private final Map<String, Deque<Object>> propertyToStack = new HashMap<String, Deque<Object>>();

	public void push(final String property, final Object value) {
		getStackForProperty(property).push(value);
	}

	@SuppressWarnings("unchecked")
	public <T> T peek(final String property) {
		return (T) getStackForProperty(property).peek();
	}

	public void pop(final String property) {
		getStackForProperty(property).pop();
	}

	private Deque<Object> getStackForProperty(final String property) {
		if (propertyToStack.containsKey(property)) {
			return propertyToStack.get(property);
		}

		final Deque<Object> stack = new LinkedList<Object>();
		propertyToStack.put(property, stack);
		return stack;
	}

}
