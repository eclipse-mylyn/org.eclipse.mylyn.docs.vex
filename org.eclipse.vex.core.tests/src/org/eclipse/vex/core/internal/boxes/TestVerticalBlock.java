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
public class TestVerticalBlock {

	private VerticalBlock box;

	@Before
	public void setUp() throws Exception {
		box = new VerticalBlock();
	}

	@Test
	public void whenCreatedIsAtOrigin() throws Exception {
		assertEquals(0, box.getTop());
		assertEquals(0, box.getLeft());
	}

	@Test
	public void positionIsMutable() throws Exception {
		box.setPosition(12, 34);
		assertEquals(12, box.getTop());
		assertEquals(34, box.getLeft());
	}

	@Test
	public void whenCreatedHasNoWidth() throws Exception {
		assertEquals(0, box.getWidth());
	}

	@Test
	public void widthIsMutable() throws Exception {
		box.setWidth(1234);
		assertEquals(1234, box.getWidth());
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
