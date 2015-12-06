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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class CascadingPropertiesTest {

	@Test
	public void pushAndPeekAPropertiesValue() throws Exception {
		final CascadingProperties properties = new CascadingProperties();
		final Object value = new Object();

		properties.push("property", value);

		assertEquals(value, properties.peek("property"));
	}

	@Test
	public void pushAndPeekValuesForTwoProperties() throws Exception {
		final CascadingProperties properties = new CascadingProperties();
		final Object value1 = new Object();
		final Object value2 = new Object();

		properties.push("property1", value1);
		properties.push("property2", value2);

		assertEquals(value1, properties.peek("property1"));
		assertEquals(value2, properties.peek("property2"));
	}

	@Test
	public void popValueRevealsOldValue() throws Exception {
		final CascadingProperties properties = new CascadingProperties();
		final Object value1 = new Object();
		final Object value2 = new Object();

		properties.push("property", value1);
		properties.push("property", value2);
		properties.pop("property");

		assertEquals(value1, properties.peek("property"));
	}
}
