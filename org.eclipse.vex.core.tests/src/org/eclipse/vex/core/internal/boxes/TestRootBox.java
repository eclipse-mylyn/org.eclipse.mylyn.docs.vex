/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestRootBox {

	private RootBox box;

	@Before
	public void setUp() throws Exception {
		box = new RootBox();
	}

	@Test
	public void whenCreated_hasNoWidth() throws Exception {
		assertEquals(0, box.getWidth());
	}

	@Test
	public void widthIsMutable() throws Exception {
		box.setWidth(100);
		assertEquals(100, box.getWidth());
	}

	@Test
	public void whenCreatedHasNoChildren() throws Exception {
		assertFalse(box.hasChildren());
	}

	@Test
	public void canAppendChild() throws Exception {
		final VerticalBlock child = new VerticalBlock();
		box.appendChild(child);
		assertTrue(box.hasChildren());
	}

}
